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
    val routes: List<ScreenRoute>,
) {
    val propertySpecs by lazy {
        listOf(
            routeExtensionSpec,
            navArgsExtensionSpec,
            deepLinksExtensionSpec,
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

    private val deepLinksExtensionSpec: PropertySpec by lazy {
        createEnumExtensionPropertySpec(
            "deepLinks",
            LIST.parameterizedBy(NavDeepLink),
        ) { value, member ->
            if (value.hasDeepLinks) {
                addStatement("%M -> listOf(", member)

                val codeBlock = CodeBlock.builder().indent()
                value.annotation.deepLinks.forEach {
                    codeBlock.addStatement("%M { uriPattern = %S }", navDeepLink, it)
                }

                add(codeBlock.unindent().build())
                addStatement(")")
            } else {
                addStatement("%M -> emptyList()", member)
            }
        }
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
