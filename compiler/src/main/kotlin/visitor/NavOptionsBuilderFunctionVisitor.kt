package jp.takuji31.compose.navigation.compiler.visitor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toClassName
import jp.takuji31.compose.navigation.compiler.NavOptionsBuilder
import jp.takuji31.compose.navigation.compiler.PopUpToBuilder

class NavOptionsBuilderFunctionVisitor : KSDefaultVisitor<FileSpec.Builder, Unit>() {
    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: FileSpec.Builder,
    ) {
        val idClass = classDeclaration.toClassName()
        val spec = FunSpec.builder("popUpToScreenId")
            .addPopupToBody(idClass)

        val deprecatedSpec = FunSpec.builder("popUpTo")
            .addPopupToBody(idClass)
            .addAnnotation(
                AnnotationSpec.builder(Deprecated::class)
                    .addMember("message = \"The function implemented in navigation-compose 1.8.0 is being called\"")
                    .addMember("replaceWith = ReplaceWith(\"popUpToScreenId(id, builder)\")")
                    .addMember("level = DeprecationLevel.ERROR")
                    .build()
            )

        data.addFunction(spec.build())
        data.addFunction(deprecatedSpec.build())
    }

    private fun FunSpec.Builder.addPopupToBody(idClass: ClassName) = this
        .receiver(NavOptionsBuilder)
        .addParameter("id", idClass)
        .addParameter("builder", LambdaTypeName.get(PopUpToBuilder, returnType = UNIT))
        .addStatement(
            "return popUpTo(id.%M, builder)",
            MemberName(idClass.packageName, "route"),
        )

    override fun defaultHandler(node: KSNode, data: FileSpec.Builder) {}
}
