package jp.takuji31.compose.navigation.compiler.visitor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.UNIT
import jp.takuji31.compose.navigation.compiler.NavGraphBuilder
import jp.takuji31.compose.navigation.compiler.toLowerCamelCase
import jp.takuji31.compose.navigation.screen.annotation.ScreenId

class ComposeDestinationBuilderFunctionVisitor(
    private val logger: KSPLogger,
) : KSDefaultVisitor<FileSpec.Builder, Unit>() {
    @OptIn(KspExperimental::class)
    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: FileSpec.Builder,
    ) {
        val screenId = classDeclaration.getAnnotationsByType(ScreenId::class).first()
        val screenClassName =
            ClassName(classDeclaration.packageName.asString(), screenId.screenClassName)
        val composeBuilderClassName = screenClassName.nestedClass("ComposeDestinationBuilder")

        val spec = FunSpec.builder(screenClassName.simpleName.toLowerCamelCase() + "Composable")
            .receiver(NavGraphBuilder)
        if (screenId.dynamicDeepLinkPrefix) {
            spec.addParameter("deepLinkPrefix", STRING)
            spec.addStatement("return %T(this, deepLinkPrefix).builder()", composeBuilderClassName)
        } else {
            spec.addStatement("return %T(this).builder()", composeBuilderClassName)
        }
        spec.addParameter(
            ParameterSpec.builder(
                "builder",
                LambdaTypeName.get(composeBuilderClassName, returnType = UNIT),
            )
                .build(),
        )

        data.addFunction(spec.build())
    }

    override fun defaultHandler(node: KSNode, data: FileSpec.Builder) {}
}
