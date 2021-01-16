package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.STRING
import jp.takuji31.compose.navigation.compiler.toCamelCase
import jp.takuji31.compose.navigation.compiler.toLowerCamelCase
import jp.takuji31.compose.navigation.screen.annotation.BooleanArgument
import jp.takuji31.compose.navigation.screen.annotation.EnumArgument
import jp.takuji31.compose.navigation.screen.annotation.FloatArgument
import jp.takuji31.compose.navigation.screen.annotation.IntArgument
import jp.takuji31.compose.navigation.screen.annotation.LongArgument
import jp.takuji31.compose.navigation.screen.annotation.Route
import jp.takuji31.compose.navigation.screen.annotation.StringArgument
import javax.lang.model.util.Elements

data class ScreenRoute(
    val elements: Elements,
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
        val matchedArgs = argPattern.findAll(annotation.route).map { it.groupValues[1] }
        val argMap =
            listOf(
                annotation.stringArguments.map { it.name to it },
                annotation.intArguments.map { it.name to it },
                annotation.longArguments.map { it.name to it },
                annotation.booleanArguments.map { it.name to it },
                annotation.floatArguments.map { it.name to it },
                annotation.enumArguments.map { it.name to it },
            )
                .flatten()
                .groupBy { it.first }
                .mapValues { entry ->
                    if (entry.value.size > 1) {
                        error("Argument key ${entry.key} is Duplicated in $name")
                    }
                    entry.value.map { it.second }.first()
                }

        matchedArgs.map { name ->
            when (val annotation = argMap[name]) {
                is StringArgument -> Arg.from(annotation)
                is IntArgument -> Arg.from(annotation)
                is LongArgument -> Arg.from(annotation)
                is BooleanArgument -> Arg.from(annotation)
                is FloatArgument -> Arg.from(annotation)
                is EnumArgument -> Arg.from(elements, annotation)
                null -> Arg(
                    Arg.Type.String,
                    name,
                    isNullable = false,
                    hasDefaultValue = false,
                    STRING,
                )
                else -> error("unknown annotation $annotation")
            }
        }.toList()
    }

    companion object {
        private val argPattern = """\{([^/]+)}""".toRegex()
    }
}
