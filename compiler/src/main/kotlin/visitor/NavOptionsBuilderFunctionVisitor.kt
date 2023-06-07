package jp.takuji31.compose.navigation.compiler.visitor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
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

        val spec = FunSpec.builder("popUpTo")
            .receiver(NavOptionsBuilder)
            .addParameter("id", idClass)
            .addParameter("builder", LambdaTypeName.get(PopUpToBuilder, returnType = UNIT))
            .addStatement(
                "return popUpTo(id.%M, builder)",
                MemberName(idClass.packageName, "route"),
            )

        data.addFunction(spec.build())
    }

    override fun defaultHandler(node: KSNode, data: FileSpec.Builder) {}
}
