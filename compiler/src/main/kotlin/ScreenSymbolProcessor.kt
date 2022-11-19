package jp.takuji31.compose.navigation.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import jp.takuji31.compose.navigation.screen.annotation.ScreenId

@OptIn(KspExperimental::class)
class ScreenSymbolProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("jp.takuji31.compose.navigation.screen.annotation.ScreenId")
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ENUM_CLASS }
            .forEach {
                val screenId = it.getAnnotationsByType(ScreenId::class).first()
                val fileSpec = FileSpec.builder(it.packageName.asString(), screenId.screenClassName)

                fileSpec.build()
                    .writeTo(codeGenerator, Dependencies(aggregating = false, it.containingFile!!))
            }
        return emptyList()
    }

    internal class Visitor : KSDefaultVisitor<FileSpec, List<KSAnnotated>>() {
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: FileSpec,
        ): List<KSAnnotated> {
            return super.visitClassDeclaration(classDeclaration, data)
        }

        override fun defaultHandler(node: KSNode, data: FileSpec): List<KSAnnotated> {
            return emptyList()
        }
    }
}

class ScreenSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ScreenSymbolProcessor(environment.logger, environment.codeGenerator)
    }
}
