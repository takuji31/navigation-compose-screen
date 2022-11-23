package jp.takuji31.compose.navigation.compiler.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.squareup.kotlinpoet.FileSpec
import jp.takuji31.compose.navigation.compiler.model.ScreenClass

class ScreenIdClassVisitor(
    private val resolver: Resolver,
    private val logger: KSPLogger,
) : KSDefaultVisitor<FileSpec.Builder, Unit>() {
    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: FileSpec.Builder,
    ) {
        val screenClass = ScreenClass.from(classDeclaration)
        val typeSpecBuilder = screenClass.typeSpecBuilder

        classDeclaration
            .declarations
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ENUM_ENTRY }
            .forEach {
                it.accept(
                    ScreenRouteVisitor(
                        resolver,
                        logger,
                        screenClass.enumClassName,
                        screenClass.className,
                    ),
                    typeSpecBuilder,
                )
            }

        classDeclaration.accept(ComposeDestinationBuilderVisitor(logger), typeSpecBuilder)
        data.addType(typeSpecBuilder.build())

        classDeclaration.accept(ScreenIdExtensionVisitor(logger), data)

        classDeclaration.accept(ComposeDestinationBuilderFunctionVisitor(logger), data)
        classDeclaration.accept(NavOptionsBuilderFunctionVisitor(), data)
    }

    override fun defaultHandler(node: KSNode, data: FileSpec.Builder) {}
}
