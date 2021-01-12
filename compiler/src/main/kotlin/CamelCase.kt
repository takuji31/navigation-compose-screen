package jp.takuji31.compose.navigation.compiler

import java.util.*

private val camelCasePattern = """^[A-Z][a-zA-Z]+$""".toRegex()
val String.mayBeCamelCase: Boolean get() = camelCasePattern.matches(this)

fun String.toCamelCase() = if (contains("_")) {
    split("_").joinToString("") {
        it.first().toUpperCase() + it.drop(1).toLowerCase(
            Locale.getDefault(),
        )
    }
} else if (!mayBeCamelCase) {
    first().toUpperCase() + drop(1).toLowerCase(Locale.getDefault())
} else {
    this
}

fun String.toLowerCamelCase() = toCamelCase().let {
    it.first().toLowerCase() + it.drop(1)
}
