package jp.takuji31.compose.navigation.compiler.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import jp.takuji31.compose.navigation.compiler.model.ScreenRouteClass

class ScreenRouteVisitor(
    private val resolver: Resolver,
    private val logger: KSPLogger,
    private val enumClassName: ClassName,
    private val screenBaseClassName: ClassName,
) : KSDefaultVisitor<TypeSpec.Builder, Unit>() {
    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: TypeSpec.Builder,
    ) {

        val routeClass =
            ScreenRouteClass.from(classDeclaration, enumClassName, screenBaseClassName)
        if (routeClass == null) {
            logger.error("Route annotation required", classDeclaration)
            return
        }

        val subTypeSpec = routeClass.typeSpec

        data.addType(subTypeSpec)
    }

    override fun defaultHandler(node: KSNode, data: TypeSpec.Builder) {}
}
