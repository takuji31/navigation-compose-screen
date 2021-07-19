package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import jp.takuji31.compose.navigation.compiler.NamedNavArgument
import jp.takuji31.compose.navigation.compiler.NavDeepLink
import jp.takuji31.compose.navigation.compiler.navDeepLink

data class ScreenIdExtensions(
    val idClassName: ClassName,
    val dynamicDeepLinkPrefix: Boolean,
    val routes: List<ScreenRoute>,
) {
    val propertySpecs by lazy {
        listOf(
            routeExtensionSpec,
            navArgsExtensionSpec,
        )
    }
    private val routeExtensionSpec: PropertySpec by lazy {
        createEnumExtensionPropertySpec("route", STRING) { value, member ->
            addStatement("%M -> %S", member, value.annotation.route)
        }
    }

    private val navArgsExtensionSpec: PropertySpec by lazy {
        createEnumExtensionPropertySpec(
            "navArgs",
            LIST.parameterizedBy(NamedNavArgument),
        ) { value, member ->
            if (value.hasArgs) {
                addStatement("%M -> listOf(", member)

                val codeBlock = CodeBlock.builder().indent()
                value.args.forEach {
                    codeBlock.add(it.navArgsExtensionStatement)
                }
                add(codeBlock.unindent().build())
                addStatement(")")
            } else {
                addStatement("%M -> emptyList()", member)
            }
        }
    }

    val deepLinksExtensionSpec: FunSpec by lazy {
        val spec = FunSpec.builder("deepLinks")
            .receiver(idClassName)
            .returns(LIST.parameterizedBy(NavDeepLink))

        if (dynamicDeepLinkPrefix) {
            spec.addParameter("prefix", STRING)
        }

        val codeBlock = CodeBlock.builder()
            .beginControlFlow("return when(this)")

        routes.forEach { screenRoute ->
            if (screenRoute.hasDeepLinks) {
                codeBlock.addStatement("%M -> listOf(", MemberName(idClassName, screenRoute.name))

                val builderBlock = CodeBlock.builder().indent()
                screenRoute.annotation.deepLinks.forEach {
                    if (dynamicDeepLinkPrefix) {
                        builderBlock.addStatement(
                            "%M { uriPattern = prefix + %S },",
                            navDeepLink,
                            it,
                        )
                    } else {
                        builderBlock.addStatement(
                            "%M { uriPattern = %S },",
                            navDeepLink, it,
                        )
                    }
                }

                codeBlock.add(builderBlock.unindent().build())
                codeBlock.addStatement(")")
            } else {
                codeBlock.addStatement(
                    "%M -> emptyList()",
                    MemberName(idClassName, screenRoute.name),
                )
            }
        }
        codeBlock.endControlFlow()

        spec.addCode(codeBlock.build())
        spec.build()
    }

    private fun createEnumExtensionPropertySpec(
        propertyName: String,
        type: TypeName,
        statementBuilder: CodeBlock.Builder.(screenRoute: ScreenRoute, memberName: MemberName) -> Unit,
    ): PropertySpec {
        val spec = PropertySpec.builder(propertyName, type)
            .receiver(idClassName)
        val funSpec = FunSpec.getterBuilder()

        val codeBlock = CodeBlock.builder()
            .beginControlFlow("return when(this)")
        routes.forEach {
            codeBlock.statementBuilder(it, MemberName(idClassName, it.name))
        }
        codeBlock.endControlFlow()
        funSpec.addCode(codeBlock.build())
        return spec.getter(funSpec.build()).build()
    }
}
