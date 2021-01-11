package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.UNIT
import jp.takuji31.compose.navigation.compiler.NavGraphBuilder
import jp.takuji31.compose.navigation.compiler.toLowerCamelCase

data class ComposeBuilderFunction(
    val screenClassName: ClassName,
    val composeBuilderClassName: ClassName,
) {

    val spec: FunSpec by lazy {
        val spec = FunSpec.builder(screenClassName.simpleName.toLowerCamelCase() + "Composable")
            .receiver(NavGraphBuilder)
            .addParameter(
                ParameterSpec.builder(
                    "builder",
                    LambdaTypeName.get(composeBuilderClassName, returnType = UNIT),
                )
                    .build(),
            )
        spec.addStatement("return %T(this).builder()", composeBuilderClassName)
        spec.build()
    }
}
