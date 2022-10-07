package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.*
import jp.takuji31.compose.navigation.compiler.NavOptionsBuilder
import jp.takuji31.compose.navigation.compiler.PopUpToBuilder

data class NavOptionsBuilderExtensions(
    val idClass: ClassName,
) {
    val spec: FunSpec by lazy {
        val funSpec = FunSpec.builder("popUpTo")
            .receiver(NavOptionsBuilder)
            .addParameter("id", idClass)
            .addParameter("builder", LambdaTypeName.get(PopUpToBuilder, returnType = UNIT))
            .addStatement(
                "return popUpTo(id.%M, builder)",
                MemberName(idClass.packageName, "route"),
            )

        funSpec.build()
    }
}
