package jp.takuji31.compose.navigation.compiler

import java.util.*

fun String.toCamelCase() = if (contains("_")) {
    split("_").joinToString("") {
        it.first().toUpperCase() + it.drop(1).toLowerCase(
            Locale.getDefault())
    }
} else {
    first().toUpperCase() + drop(1).toLowerCase(Locale.getDefault())
}

fun String.toLowerCamelCase() = toCamelCase().let {
    it.first().toLowerCase() + it.drop(1)
}