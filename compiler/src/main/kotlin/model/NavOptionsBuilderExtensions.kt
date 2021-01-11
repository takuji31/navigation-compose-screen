package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.UNIT
import jp.takuji31.compose.navigation.compiler.NavOptionsBuilder
import jp.takuji31.compose.navigation.compiler.PopUpToBuilder
import jp.takuji31.compose.navigation.compiler.popUpTo

data class NavOptionsBuilderExtensions(
    val idClass: ClassName,
) {
    val spec: FunSpec by lazy {
        val funSpec = FunSpec.builder("popUpTo")
            .receiver(NavOptionsBuilder)
            .addParameter("id", idClass)
            .addParameter("builder", LambdaTypeName.get(PopUpToBuilder, returnType = UNIT))
            .addStatement(
                "return %M(id.%M, builder)",
                popUpTo,
                MemberName(idClass.packageName, "route"),
            )

        funSpec.build()
    }
}
