package jp.takuji31.compose.navigation.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import jp.takuji31.compose.screengenerator.annotation.NavArgumentType

data class ScreenEnum(
    val enumClassName: ClassName,
    val screenClassName: ClassName,
    val elements: List<ScreenEnumValue>,
) {
    private val composeBuilderClassName by lazy {
        ClassName(
            screenClassName.packageName,
            screenClassName.simpleName,
            "ComposeDestinationBuilder",
        )
    }

    val screenSpec: TypeSpec by lazy {
        val screenIdParameter =
            ParameterSpec.builder("screenId", enumClassName).build()
        val spec = TypeSpec.classBuilder(screenClassName)
            .addModifiers(KModifier.SEALED)
            .addSuperinterface(Parcelable)
            .addSuperinterface(BaseScreen.parameterizedBy(enumClassName))
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
        spec.addTypes(
            elements.map { value ->
                val valueClassName =
                    ClassName(
                        screenClassName.packageName,
                        screenClassName.simpleName,
                        value.bestTypeName,
                    )
                val builder = if (value.hasArgs) {
                    TypeSpec.classBuilder(valueClassName)
                } else {
                    TypeSpec.objectBuilder(valueClassName)
                }
                builder.superclass(screenClassName)
                    .addAnnotation(Parcelize)
                    .addSuperclassConstructorParameter(
                        "%N.%N",
                        enumClassName.simpleName,
                        enumClassName.member(value.name),
                    )

                val routeProperty = PropertySpec.builder("route", STRING, KModifier.OVERRIDE)
                if (value.hasArgs) {
                    val constructor = FunSpec.constructorBuilder()
                    val properties = mutableListOf<PropertySpec.Builder>()
                    var route = value.annotation.route
                    value.args.forEach { navArgument ->
                        constructor.addParameter(
                            ParameterSpec.builder(
                                navArgument.name,
                                navArgument.typeName,
                            )
                                .build(),
                        )
                        properties += PropertySpec.builder(navArgument.name, navArgument.typeName)
                            .initializer(navArgument.name)

                        route = route.replace("{${navArgument.name}}", "\$${navArgument.name}")
                    }

                    builder.primaryConstructor(constructor.build())
                    builder.addProperties(properties.map { it.build() })
                    routeProperty.getter(
                        FunSpec.getterBuilder().addCode("return %P", route)
                            .build(),
                    )
                } else {
                    routeProperty.getter(
                        FunSpec.getterBuilder().addCode("return screenId.route")
                            .build(),
                    )
                }
                builder.addProperty(routeProperty.build())
                builder.build()
            },
        )
        spec.addType(composeBuilderSpec)
        spec.build()
    }

    val routeExtensionSpec: PropertySpec by lazy {
        createEnumExtensionPropertySpec("route", STRING) { value, member ->
            addStatement(
                "    %M -> %S",
                member,
                value.annotation.route,
            )
        }
    }

    val navArgsExtensionSpec: PropertySpec by lazy {
        createEnumExtensionPropertySpec(
            "navArgs",
            LIST.parameterizedBy(NamedNavArgument),
        ) { value, member ->
            if (value.hasArgs) {
                beginControlFlow("%M -> {", member)
                addStatement("listOf(")
                value.args.forEach { arg ->
                    val type = arg.type
                    if (type == NavArgumentType.Enum) {
                        addStatement(
                            "    %M(%S) { type = %T.%M(%T::class.java) },",
                            navArgument,
                            arg.name,
                            NavType,
                            MemberName(NavType, "EnumType"),
                            arg.typeName,
                        )
                    } else {
                        addStatement(
                            "    %M(%S) { type = %T.%M },",
                            navArgument,
                            arg.name,
                            NavType,
                            when (type) {
                                NavArgumentType.String -> MemberName(NavType, "StringType")
                                NavArgumentType.Int -> MemberName(NavType, "IntType")
                                NavArgumentType.Long -> MemberName(NavType, "LongType")
                                NavArgumentType.Bool -> MemberName(NavType, "BoolType")
                                NavArgumentType.Float -> MemberName(NavType, "FloatType")
                                NavArgumentType.Enum -> TODO()
                            },
                        )
                    }
                }
                addStatement(")")
                endControlFlow()
            } else {
                addStatement("%M -> emptyList()", member)
            }
        }
    }

    val deepLinksExtensionSpec: PropertySpec by lazy {
        createEnumExtensionPropertySpec(
            "deepLinks",
            LIST.parameterizedBy(NavDeepLink),
        ) { value, member ->
            if (value.hasDeepLinks) {
                addStatement("%M -> listOf(", member)
                value.annotation.deepLinks.forEach {
                    addStatement("    %M { uriPattern = %S }", navDeepLink, it)
                }
                addStatement(")")
            } else {
                addStatement("%M -> emptyList()", member)
            }
        }
    }

    private fun createEnumExtensionPropertySpec(
        propertyName: String,
        type: TypeName,
        statementBuilder: FunSpec.Builder.(screenEnumValue: ScreenEnumValue, memberName: MemberName) -> Unit,
    ): PropertySpec {
        val spec = PropertySpec.builder(propertyName, type)
            .receiver(enumClassName)
        val funSpec = FunSpec.getterBuilder()
            .beginControlFlow("return when(this) {")

        elements.forEach {
            funSpec.statementBuilder(it, MemberName(enumClassName, it.name))
        }

        funSpec.endControlFlow()
        return spec.getter(funSpec.build()).build()
    }

    private val composeBuilderSpec: TypeSpec by lazy {
        val navGraphBuilderName = "navGraphBuilder"
        val navGraphBuilder = PropertySpec.builder(
            navGraphBuilderName,
            NavGraphBuilder,
            KModifier.PRIVATE,
        )
            .initializer(navGraphBuilderName)
            .build()
        val spec = TypeSpec.classBuilder(composeBuilderClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(navGraphBuilderName, NavGraphBuilder)
                    .build(),
            )
            .addProperty(navGraphBuilder)
        elements.forEach { value ->
            val funSpec = FunSpec.builder(value.bestFunctionName)
                .addParameter(
                    ParameterSpec.builder(
                        "content",
                        LambdaTypeName.get(
                            receiver = null,
                            returnType = UNIT,
                            parameters = arrayOf(
                                ParameterSpec.builder(
                                    "screen",
                                    ClassName.bestGuess(value.bestTypeName),
                                )
                                    .build(),
                            ),
                        ).copy(annotations = listOf(ComposableAnnotation)),
                    )
                        .build(),
                )
                .addCode(
                    CodeBlock.builder()
                        .apply {
                            val codeBlock = CodeBlock.builder()
                            if (value.hasArgs) {
                                val initializerBlock = CodeBlock.builder()
                                    .addStatement(
                                        "val screen = %T(",
                                        ClassName(
                                            screenClassName.packageName,
                                            screenClassName.simpleName,
                                            value.bestTypeName,
                                        ),
                                    )

                                codeBlock.addStatement("val arguments = checkNotNull(it.arguments)")
                                value.args.forEach { arg ->
                                    codeBlock.addStatement(
                                        "val %N = arguments[%S] as %T",
                                        arg.name,
                                        arg.name,
                                        arg.typeName,
                                    )
                                    initializerBlock.addStatement(
                                        "    %N = %N,",
                                        arg.name,
                                        arg.name,
                                    )
                                }
                                initializerBlock.addStatement(")")
                                codeBlock.add(initializerBlock.build())
                                codeBlock.addStatement("content(screen)")
                            } else {
                                codeBlock.addStatement(
                                    "content(%M)",
                                    MemberName(screenClassName, value.bestTypeName),
                                )
                            }
                            addStatement(
                                "%1N.%2M(%3T.%4M.route, %3T.%4M.navArgs, %3T.%4M.deepLinks) {",
                                navGraphBuilder,
                                composable,
                                enumClassName,
                                MemberName(enumClassName, value.name),
                            )
                            add(codeBlock.build())
                            addStatement("}")
                        }
                        .build(),
                )
            spec.addFunction(funSpec.build())
        }
        spec.build()
    }

    val composeFunctionSpec: FunSpec by lazy {
        val spec = FunSpec.builder(screenClassName.simpleName.toLowerCamelCase() + "Composable")
            .receiver(NavGraphBuilder)
            .addParameter(
                ParameterSpec.builder(
                    "builder",
                    LambdaTypeName.get(composeBuilderClassName, returnType = UNIT),
                )
                    .build(),
            )
        val functionBody = CodeBlock.builder()
            .add("%T(this).apply(builder)", composeBuilderClassName)
        spec.addCode(functionBody.build())
        spec.build()
    }
}
