package jp.takuji31.compose.navigation.compiler.visitor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import jp.takuji31.compose.navigation.screen.annotation.ScreenId

class ScreenFileVisitor constructor(
    private val resolver: Resolver,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : KSVisitorVoid() {
    @OptIn(KspExperimental::class)
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val screenId = classDeclaration.getAnnotationsByType(ScreenId::class).first()

        val fileSpec = FileSpec.builder(
            classDeclaration.packageName.asString(),
            screenId.screenClassName,
        )

        classDeclaration.accept(ScreenIdClassVisitor(resolver, logger), fileSpec)

        fileSpec.build()
            .writeTo(
                codeGenerator,
                Dependencies(aggregating = false, classDeclaration.containingFile!!),
            )
    }
}
