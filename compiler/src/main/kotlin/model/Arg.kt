package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import jp.takuji31.compose.screengenerator.annotation.NavArgumentType

data class Arg constructor(
    val name: String,
    val type: NavArgumentType,
    val enumClass: TypeName? = null,
) {
    val typeName: TypeName by lazy {
        when (type) {
            NavArgumentType.String -> STRING
            NavArgumentType.Int -> INT
            NavArgumentType.Long -> LONG
            NavArgumentType.Bool -> BOOLEAN
            NavArgumentType.Float -> FLOAT
            NavArgumentType.Enum -> {
                checkNotNull(enumClass) {
                    "Enum nav type must set enumClass $name"
                }
            }
        }
    }
}
