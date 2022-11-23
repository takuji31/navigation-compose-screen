package jp.takuji31.compose.navigation.compiler.visitor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import jp.takuji31.compose.navigation.compiler.model.ComposeDestinationBuilder
import jp.takuji31.compose.navigation.screen.annotation.ScreenId
import jp.takuji31.compose.navigation.screen.annotation.Route as RouteAnnotation

class ComposeDestinationBuilderVisitor(
    private val logger: KSPLogger,
) : KSDefaultVisitor<TypeSpec.Builder, Unit>() {
    @OptIn(KspExperimental::class)
    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: TypeSpec.Builder,
    ) {
        require(classDeclaration.classKind == ClassKind.ENUM_CLASS)
        val screenId =
            classDeclaration.getAnnotationsByType(ScreenId::class).first()
        val baseClassName =
            ClassName(classDeclaration.packageName.asString(), screenId.screenClassName)
        val routes = classDeclaration
            .declarations
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ENUM_ENTRY }
            .map {
                val route = it.getAnnotationsByType(RouteAnnotation::class).first()
                ComposeDestinationBuilder.Route(
                    it.simpleName.asString(),
                    baseClassName.nestedClass(it.simpleName.asString()),
                    route.route,
                    route.type,
                )
            }
            .toList()
        val composeDestinationBuilder = ComposeDestinationBuilder(
            classDeclaration.toClassName(),
            baseClassName,
            screenId.dynamicDeepLinkPrefix,
            routes,
        )
        data.addType(composeDestinationBuilder.typeSpec)
    }

    override fun defaultHandler(node: KSNode, data: TypeSpec.Builder) {}
}
