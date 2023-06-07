package jp.takuji31.compose.navigation.compiler.visitor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import jp.takuji31.compose.navigation.compiler.NamedNavArgument
import jp.takuji31.compose.navigation.compiler.NavDeepLink
import jp.takuji31.compose.navigation.compiler.model.ScreenRouteClass
import jp.takuji31.compose.navigation.compiler.model.createClassName
import jp.takuji31.compose.navigation.compiler.navDeepLink
import jp.takuji31.compose.navigation.screen.annotation.Route
import jp.takuji31.compose.navigation.screen.annotation.ScreenId

class ScreenIdExtensionVisitor(
    private val logger: KSPLogger,
) : KSDefaultVisitor<FileSpec.Builder, Unit>() {
    @OptIn(KspExperimental::class)
    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: FileSpec.Builder,
    ) {
        val screenId = classDeclaration.getAnnotationsByType(ScreenId::class).first()
        val screenClassName = screenId.createClassName(classDeclaration)
        data.addProperty(
            createEnumExtensionPropertySpec(
                classDeclaration,
                "route",
                STRING,
            ) { entryClassDeclaration ->
                val route = entryClassDeclaration.getAnnotationsByType(Route::class).first()
                addStatement(
                    "%M -> %S",
                    MemberName(
                        classDeclaration.toClassName(),
                        entryClassDeclaration.simpleName.asString(),
                    ),
                    route.route,
                )
            },
        ).addProperty(
            createEnumExtensionPropertySpec(
                classDeclaration,
                "navArgs",
                LIST.parameterizedBy(NamedNavArgument),
            ) { entryClassDeclaration ->
                val screenRouteClass = ScreenRouteClass.from(
                    entryClassDeclaration,
                    classDeclaration.toClassName(),
                    screenClassName,
                )
                val member = MemberName(
                    classDeclaration.toClassName(),
                    entryClassDeclaration.simpleName.asString(),
                )

                if (screenRouteClass is ScreenRouteClass.Parameterized) {
                    addStatement("%M -> listOf(", member)

                    val codeBlock = CodeBlock.builder().indent()
                    screenRouteClass.compiledArgs.forEach {
                        codeBlock.add(it.navArgsExtensionStatement)
                    }
                    add(codeBlock.unindent().build())
                    addStatement(")")

                } else {
                    addStatement("%M -> emptyList()", member)
                }
            },
        ).addFunction(
            createDeepLinksExtensionSpec(
                classDeclaration.toClassName(),
                screenId.dynamicDeepLinkPrefix,
                classDeclaration
                    .declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .filter { it.classKind == ClassKind.ENUM_ENTRY }
                    .mapNotNull {
                        ScreenRouteClass.from(
                            it,
                            classDeclaration.toClassName(),
                            screenClassName,
                        )
                    }.toList(),
            ),
        )
    }

    override fun defaultHandler(node: KSNode, data: FileSpec.Builder) {}


    private fun createEnumExtensionPropertySpec(
        classDeclaration: KSClassDeclaration,
        propertyName: String,
        type: TypeName,
        statementBuilder: CodeBlock.Builder.(classDeclaration: KSClassDeclaration) -> Unit,
    ): PropertySpec {
        val spec = PropertySpec.builder(propertyName, type)
            .receiver(classDeclaration.toClassName())
        val funSpec = FunSpec.getterBuilder()

        val codeBlock = CodeBlock.builder()
            .beginControlFlow("return when(this)")
        classDeclaration.declarations.filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ENUM_ENTRY }.forEach {
                codeBlock.statementBuilder(it)
            }
        codeBlock.endControlFlow()
        funSpec.addCode(codeBlock.build())
        return spec.getter(funSpec.build()).build()
    }

    fun createDeepLinksExtensionSpec(
        enumClassName: ClassName,
        dynamicDeepLinkPrefix: Boolean,
        routes: List<ScreenRouteClass>,
    ): FunSpec {
        val spec = FunSpec.builder("deepLinks")
            .receiver(enumClassName)
            .returns(LIST.parameterizedBy(NavDeepLink))

        if (dynamicDeepLinkPrefix) {
            spec.addParameter("prefix", STRING)
        }

        val codeBlock = CodeBlock.builder()
            .beginControlFlow("return when(this)")

        routes.forEach { screenRoute ->
            if (screenRoute.deepLinks.isNotEmpty()) {
                codeBlock.addStatement(
                    "%M -> listOf(",
                    MemberName(enumClassName, screenRoute.name),
                )

                val builderBlock = CodeBlock.builder().indent()
                screenRoute.deepLinks.forEach {
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
                    MemberName(enumClassName, screenRoute.name),
                )
            }
        }
        codeBlock.endControlFlow()

        spec.addCode(codeBlock.build())
        return spec.build()
    }

}
