package jp.takuji31.compose.navigation.compiler

import com.squareup.kotlinpoet.asTypeName
import jp.takuji31.compose.screengenerator.annotation.NavArgumentType
import jp.takuji31.compose.screengenerator.annotation.Route
import javax.lang.model.type.MirroredTypeException

data class ScreenEnumValue(
    val name: String,
    val annotation: Route,
) {
    val bestTypeName: String by lazy {
        name.toCamelCase()
    }

    val bestFunctionName: String by lazy {
        name.toLowerCamelCase()
    }

    val hasArgs: Boolean by lazy {
        annotation.route.contains(argPattern)
    }

    val hasDeepLinks: Boolean by lazy {
        annotation.deepLinks.isNotEmpty()
    }

    val args: List<Arg> by lazy {
        argPattern.findAll(annotation.route).map { result ->
            val key = result.groupValues[1]
            val navArgument = annotation.arguments.firstOrNull { it.name == key }?.let {
                val enumClass = try {
                    it.enumClass.asTypeName()
                } catch (e: MirroredTypeException) {
                    @Suppress("DEPRECATION")
                    e.typeMirror.asTypeName()
                }
                Arg(it.name, it.type, enumClass)
            } ?: Arg(key, NavArgumentType.String)
            navArgument
        }.toList()
    }

    companion object {
        private val argPattern = """\{([^/]+)}""".toRegex()
    }
}
