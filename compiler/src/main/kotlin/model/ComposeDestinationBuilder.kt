package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import jp.takuji31.compose.navigation.compiler.ComposableAnnotation
import jp.takuji31.compose.navigation.compiler.NavGraphBuilder
import jp.takuji31.compose.navigation.compiler.ScreenFactoryRegistry
import jp.takuji31.compose.navigation.compiler.composable
import jp.takuji31.compose.navigation.compiler.dialog
import jp.takuji31.compose.navigation.compiler.toLowerCamelCase
import jp.takuji31.compose.navigation.screen.annotation.RouteType

// TODO: convert to Visitor
data class ComposeDestinationBuilder(
    private val enumClassName: ClassName,
    private val baseClassName: ClassName,
    private val dynamicDeepLinkPrefix: Boolean,
    private val routes: List<Route>,
) {
    val typeSpec: TypeSpec by lazy {
        val className = baseClassName.nestedClass("ComposeDestinationBuilder")
        val spec = TypeSpec.classBuilder(className)

        val navGraphBuilderName = "navGraphBuilder"
        val navGraphBuilder =
            PropertySpec.builder(navGraphBuilderName, NavGraphBuilder, KModifier.PRIVATE)
                .initializer(navGraphBuilderName)
                .build()

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
            val routeClassName = screenRoute.routeClassName
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
                            route.routeClassName,
                        )
                            .build(),
                    ),
                ).copy(annotations = listOf(ComposableAnnotation)),
            ).build()

            val codeBlock = CodeBlock.builder()

            if (dynamicDeepLinkPrefix) {
                codeBlock.addStatement(
                    "%1N.%2M(%3L.route, %3L.navArgs, %3L.deepLinks(%4N)) {",
                    navGraphBuilder,
                    when (route.type) {
                        RouteType.Default -> composable
                        RouteType.Dialog -> dialog
                    },
                    enumClassName.member(route.name),
                    deepLinkPrefixName,
                )
            } else {
                codeBlock.addStatement(
                    "%1N.%2M(%3L.route, %3L.navArgs, %3L.deepLinks()) {",
                    navGraphBuilder,
                    composable,
                    enumClassName.member(route.name),
                )
            }

            val lambdaCodeBlock = CodeBlock.builder()
                .indent()
                .addStatement("val screen = %T.fromBundle(it.arguments)", route.routeClassName)
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

    data class Route(
        val name: String,
        val routeClassName: ClassName,
        val route: String,
        val type: RouteType,
    ) {
        val bestFunctionName: String by lazy { name.toLowerCamelCase() }
    }
}
