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
import jp.takuji31.compose.navigation.compiler.ComposableAnnotation
import jp.takuji31.compose.navigation.compiler.NavGraphBuilder
import jp.takuji31.compose.navigation.compiler.Parcelable
import jp.takuji31.compose.navigation.compiler.Parcelize
import jp.takuji31.compose.navigation.compiler.composable

data class ScreenClass(
    val className: ClassName,
    val enumClassName: ClassName,
    val screenBaseClassName: ClassName,
    val screenBaseClassIsInterface: Boolean,
    val composeBuilderClassName: ClassName,
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
        val screenChildren = routes.map { value ->
            val valueClassName = className.nestedClass(value.bestTypeName)
            val builder = if (value.hasArgs) {
                TypeSpec.classBuilder(valueClassName)
            } else {
                TypeSpec.objectBuilder(valueClassName)
            }
            builder.superclass(className)
                .addAnnotation(Parcelize)
                .addSuperclassConstructorParameter(
                    "%N.%N",
                    enumClassName.simpleName,
                    enumClassName.member(value.name),
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

            if (value.hasArgs) {
                val parameterizedRouteProperty =
                    PropertySpec.builder("parameterizedRoute", STRING, KModifier.OVERRIDE)
                val constructor = FunSpec.constructorBuilder()
                var route = value.annotation.route

                val constructorParameters =
                    value.args.map { ParameterSpec.builder(it.name, it.typeName).build() }
                constructor.addParameters(constructorParameters)

                val properties = value.args.map { navArgument ->
                    PropertySpec.builder(navArgument.name, navArgument.typeName)
                        .initializer(navArgument.name).build()
                }

                value.args.forEach { route = route.replace("{${it.name}}", "\$${it.name}") }

                builder.primaryConstructor(constructor.build())
                builder.addProperties(properties)

                parameterizedRouteProperty.getter(
                    FunSpec.getterBuilder().addCode("return %P", route)
                        .build(),
                )
                builder.addProperty(parameterizedRouteProperty.build())
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
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(navGraphBuilderName, NavGraphBuilder)
                    .build(),
            )
            .addProperty(navGraphBuilder)

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

            val codeBlock = CodeBlock.builder().addStatement(
                "%1N.%2M(%3T.%4M.route, %3T.%4M.navArgs, %3T.%4M.deepLinks) {",
                navGraphBuilder,
                composable,
                enumClassName,
                MemberName(enumClassName, route.name),
            )

            val lambdaCodeBlock = CodeBlock.builder()
                .indent()
            if (route.hasArgs) {
                val argumentsCodeBlock =
                    CodeBlock.builder().addStatement("val arguments = checkNotNull(it.arguments)")
                route.args.forEach { arg ->
                    argumentsCodeBlock.addStatement(
                        "val %N = arguments[%S] as %T",
                        arg.name,
                        arg.name,
                        arg.typeName,
                    )
                }
                lambdaCodeBlock.add(argumentsCodeBlock.build())

                val initializerBlock = CodeBlock.builder()
                    .addStatement("val screen = %T(", className.nestedClass(route.bestTypeName))
                    .indent()
                route.args.forEach { arg ->
                    initializerBlock.addStatement(
                        "%N = %N,",
                        arg.name,
                        arg.name,
                    )
                }
                initializerBlock.unindent().addStatement(")")
                lambdaCodeBlock.add(initializerBlock.build())
                lambdaCodeBlock.addStatement("content(screen)")
            } else {
                lambdaCodeBlock.addStatement(
                    "content(%M)",
                    MemberName(className, route.bestTypeName),
                )
            }

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
}
