package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import jp.takuji31.compose.navigation.compiler.Bundle
import jp.takuji31.compose.navigation.compiler.ComposableAnnotation
import jp.takuji31.compose.navigation.compiler.NavGraphBuilder
import jp.takuji31.compose.navigation.compiler.Parcelable
import jp.takuji31.compose.navigation.compiler.Parcelize
import jp.takuji31.compose.navigation.compiler.SavedStateHandle
import jp.takuji31.compose.navigation.compiler.ScreenFactory
import jp.takuji31.compose.navigation.compiler.ScreenFactoryRegistry
import jp.takuji31.compose.navigation.compiler.composable
import jp.takuji31.compose.navigation.compiler.dialog
import jp.takuji31.compose.navigation.compiler.model.ScreenRoute.RouteType.Default
import jp.takuji31.compose.navigation.compiler.model.ScreenRoute.RouteType.Dialog

data class ScreenClass(
    val className: ClassName,
    val enumClassName: ClassName,
    val screenBaseClassName: ClassName,
    val screenBaseClassIsInterface: Boolean,
    val composeBuilderClassName: ClassName,
    val dynamicDeepLinkPrefix: Boolean,
    val routes: List<ScreenRoute>,
) {

    val typeSpec: TypeSpec by lazy {
        val screenIdParameter =
            ParameterSpec.builder("screenId", enumClassName).build()
        val spec = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.SEALED)
            .apply {
                val baseTypeName = screenBaseClassName.parameterizedBy(enumClassName)
                if (screenBaseClassIsInterface) {
                    addSuperinterface(baseTypeName)
                } else {
                    superclass(baseTypeName)
                }
            }
            .addSuperinterface(Parcelable)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        screenIdParameter,
                    )
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("screenId", enumClassName, KModifier.OVERRIDE)
                    .initializer("%N", screenIdParameter).build(),
            )
        val screenChildren = routes.map { screenRoute ->
            val routeClassName = screenRoute.nestedTypeName
            val builder = if (screenRoute.hasArgs) {
                TypeSpec.classBuilder(routeClassName)
            } else {
                TypeSpec.objectBuilder(routeClassName)
            }
            builder.superclass(className)
                .addAnnotation(Parcelize)
                .addSuperclassConstructorParameter(
                    "%N.%N",
                    enumClassName.simpleName,
                    enumClassName.member(screenRoute.name),
                )

            builder
                .addProperty(
                    PropertySpec
                        .builder("route", STRING, KModifier.OVERRIDE)
                        .getter(
                            FunSpec.getterBuilder().addCode("return screenId.route").build(),
                        )
                        .build(),
                )

            val fromBundleBuilder = FunSpec
                .builder("fromBundle")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("bundle", Bundle.copy(nullable = true))
                .returns(routeClassName)
            val fromSavedStateHandleBuilder = FunSpec
                .builder("fromSavedStateHandle")
                .addParameter("savedStateHandle", SavedStateHandle)
                .addModifiers(KModifier.OVERRIDE)
                .returns(routeClassName)

            if (screenRoute.hasArgs) {
                val parameterizedRouteProperty =
                    PropertySpec.builder("parameterizedRoute", STRING, KModifier.OVERRIDE)
                val constructor = FunSpec.constructorBuilder()
                var route = screenRoute.route

                val constructorParameters =
                    screenRoute.args.map {
                        ParameterSpec.builder(
                            it.name,
                            it.typeNameWithNullability,
                        ).apply {
                            if (it.hasDefaultValue) {
                                defaultValue("%L", it.defaultValue)
                            }
                        }.build()
                    }
                constructor.addParameters(constructorParameters)

                val properties = screenRoute.args.map { navArgument ->
                    PropertySpec.builder(navArgument.name, navArgument.typeNameWithNullability)
                        .initializer(navArgument.name).build()
                }

                screenRoute.args.forEach { route = route.replace("{${it.name}}", "\$${it.name}") }

                builder.primaryConstructor(constructor.build())
                builder.addProperties(properties)

                parameterizedRouteProperty.getter(
                    FunSpec.getterBuilder().addCode("return %P", route)
                        .build(),
                )
                builder.addProperty(parameterizedRouteProperty.build())
                val nestedClassSimpleName = routeClassName.simpleNames.joinToString(".")

                if (screenRoute.isAllArgsOptional) {
                    fromBundleBuilder
                        .addStatement("bundle ?: return %T()", routeClassName)
                } else {
                    fromBundleBuilder
                        .beginControlFlow("checkNotNull(bundle)")
                        .addStatement(
                            "error(%S)",
                            "Screen $nestedClassSimpleName has non-optional parameter",
                        )
                        .endControlFlow()
                }

                val initializerBlockBuilder = CodeBlock
                    .builder()
                    .add("return %T(\n", routeClassName)
                    .indent()
                screenRoute.args.forEach { arg ->
                    when {
                        !arg.isNullable && !arg.hasDefaultValue -> {
                            // always not null
                            fromBundleBuilder.addStatement(
                                "val %1N = bundle[%1S] as? %2T ?: error(%3S)",
                                arg.name,
                                arg.typeNameWithNullability,
                                "Screen $nestedClassSimpleName requires parameter: ${arg.name}",
                            )
                            fromSavedStateHandleBuilder.addStatement(
                                "val %1N = savedStateHandle.get(%1S) as? %2T ?: error(%3S)",
                                arg.name,
                                arg.typeNameWithNullability,
                                "Screen $nestedClassSimpleName requires parameter: ${arg.name}",
                            )
                        }
                        arg.hasDefaultValue && arg.defaultValue != null -> {
                            // with default value
                            fromBundleBuilder.addStatement(
                                "val %1N = bundle[%1S] as? %2T ?: %3L",
                                arg.name,
                                arg.typeNameWithNullability,
                                arg.defaultValueLiteral,
                            )
                            fromSavedStateHandleBuilder.addStatement(
                                "val %1N = savedStateHandle.get(%1S) as? %2T ?: %3L",
                                arg.name,
                                arg.typeNameWithNullability,
                                arg.defaultValueLiteral,
                            )
                        }
                        else -> {
                            // with default value
                            fromBundleBuilder.addStatement(
                                "val %1N = bundle[%1S] as? %2T",
                                arg.name,
                                arg.typeNameWithNullability,
                            )
                            fromSavedStateHandleBuilder.addStatement(
                                "val %1N = savedStateHandle.get(%1S) as? %2T",
                                arg.name,
                                arg.typeNameWithNullability,
                            )
                        }
                    }
                    initializerBlockBuilder.add("%N = %N,\n", arg.name, arg.name)
                }

                val initializerBlock = initializerBlockBuilder.unindent().add(")").build()
                fromBundleBuilder.addCode(initializerBlock)
                fromSavedStateHandleBuilder.addCode(initializerBlock)

                val companionObjectBuilder = TypeSpec.companionObjectBuilder()
                    .addSuperinterface(ScreenFactory.parameterizedBy(routeClassName))
                    .addFunction(fromBundleBuilder.build())
                    .addFunction(fromSavedStateHandleBuilder.build())

                builder.addType(companionObjectBuilder.build())
            } else {
                builder
                    .addSuperinterface(ScreenFactory.parameterizedBy(routeClassName))
                    .addFunction(
                        fromBundleBuilder
                            .addCode("return this")
                            .build(),
                    )
                    .addFunction(
                        fromSavedStateHandleBuilder
                            .addCode("return this")
                            .build(),
                    )
            }
            builder.build()
        }
        spec.addTypes(screenChildren)
        spec.addType(composeBuilderSpec)
        spec.build()
    }

    private val composeBuilderSpec: TypeSpec by lazy {
        val navGraphBuilderName = "navGraphBuilder"
        val navGraphBuilder =
            PropertySpec.builder(navGraphBuilderName, NavGraphBuilder, KModifier.PRIVATE)
                .initializer(navGraphBuilderName)
                .build()

        val spec = TypeSpec.classBuilder(composeBuilderClassName)

        val deepLinkPrefixName = "deepLinkPrefix"
        if (dynamicDeepLinkPrefix) {
            spec.primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(navGraphBuilderName, NavGraphBuilder)
                    .addParameter(deepLinkPrefixName, STRING)
                    .build(),
            )
            spec.addProperty(
                PropertySpec.builder(deepLinkPrefixName, STRING)
                    .initializer(deepLinkPrefixName)
                    .build(),
            )
        } else {
            spec.primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(navGraphBuilderName, NavGraphBuilder)
                    .build(),
            )
        }

        spec.addProperty(navGraphBuilder)

        val factoryInitCodes = CodeBlock.builder()

        routes.forEach { screenRoute ->
            val routeClassName = screenRoute.nestedTypeName
            factoryInitCodes.addStatement(
                "%T.%N(%S, %T::class, %T)",
                ScreenFactoryRegistry,
                ScreenFactoryRegistry.member("register"),
                screenRoute.route,
                routeClassName,
                routeClassName,
            )
        }

        spec.addInitializerBlock(factoryInitCodes.build())

        val functions = routes.map { route ->
            val contentParameter = ParameterSpec.builder(
                "content",
                LambdaTypeName.get(
                    receiver = null,
                    returnType = UNIT,
                    parameters = arrayOf(
                        ParameterSpec.builder(
                            "screen",
                            ClassName.bestGuess(route.bestTypeName),
                        )
                            .build(),
                    ),
                ).copy(annotations = listOf(ComposableAnnotation)),
            ).build()

            val codeBlock = CodeBlock.builder()

            if (dynamicDeepLinkPrefix) {
                codeBlock.addStatement(
                    "%1N.%2M(%3T.%4M.route, %3T.%4M.navArgs, %3T.%4M.deepLinks(%5N)) {",
                    navGraphBuilder,
                    when (route.routeType) {
                        Default -> composable
                        Dialog -> dialog
                    },
                    enumClassName,
                    MemberName(enumClassName, route.name),
                    deepLinkPrefixName,
                )
            } else {
                codeBlock.addStatement(
                    "%1N.%2M(%3T.%4M.route, %3T.%4M.navArgs, %3T.%4M.deepLinks()) {",
                    navGraphBuilder,
                    composable,
                    enumClassName,
                    enumClassName.member(route.name),
                )
            }

            val lambdaCodeBlock = CodeBlock.builder()
                .indent()
                .addStatement("val screen = %T.fromBundle(it.arguments)", route.nestedTypeName)
            lambdaCodeBlock.addStatement("content(screen)")

            codeBlock.add(lambdaCodeBlock.unindent().build())
            codeBlock.addStatement("}")

            val funSpec = FunSpec.builder(route.bestFunctionName)
                .addParameter(contentParameter)
                .addCode(codeBlock.build())
            funSpec.build()
        }
        spec.addFunctions(functions)
        spec.build()
    }

    private val ScreenRoute.nestedTypeName: ClassName
        get() = className.nestedClass(bestTypeName)

}
