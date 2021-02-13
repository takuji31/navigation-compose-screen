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
        annotation.route.contains(argPattern) || annotation.deepLinks.any { it.contains(argPattern) }
    }

    val hasDeepLinks: Boolean by lazy {
        annotation.deepLinks.isNotEmpty()
    }

    private val argMap: Map<String, Arg> by lazy {
        val definedArgs = listOf(
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
                entry.value.map { it.second }.first().toArg()
            }

        val definedKeys = definedArgs.keys
        check(routePathArgNames.none { routeQueryArgNames.contains(it) }) {
            "Argument key used twice in $name.route"
        }
        val generatedArgs = (
            (routePathArgNames - definedKeys).map {
                it to Arg(
                    type = Arg.Type.String,
                    name = it,
                    isNullable = false,
                    hasDefaultValue = false,
                    typeName = STRING,
                )
            } +
                (routeQueryArgNames - definedKeys).map {
                    it to Arg(
                        type = Arg.Type.String,
                        name = it,
                        isNullable = true,
                        hasDefaultValue = true,
                        typeName = STRING,
                        defaultValue = null,
                    )
                }
            ).toMap()

        val deepLinkArgs = (annotation.deepLinks.map { deepLink ->
            deepLink.extractParameters()
        }.flatten() - (definedKeys + generatedArgs.keys)).map {
            it to Arg(
                type = Arg.Type.String,
                name = it,
                isNullable = true,
                hasDefaultValue = true,
                typeName = STRING,
                defaultValue = null,
            )
        }.toMap()

        definedArgs + generatedArgs + deepLinkArgs
    }
    private val routePathAndQuery by lazy { annotation.route.split("?", limit = 2) }
    private val routePath: String by lazy { routePathAndQuery[0] }
    private val routeQuery: String by lazy { routePathAndQuery.getOrNull(1) ?: "" }
    private val routePathArgNames: List<String> by lazy {
        routePath.extractParameters()
    }
    private val routeQueryArgNames: List<String> by lazy {
        routeQuery.extractParameters()
    }

    private fun String.extractParameters() =
        argPattern.findAll(this).map { it.groupValues[1] }.toList()

    val args: List<Arg> by lazy { argMap.values.toList() }

    private fun Annotation.toArg(): Arg = when (this) {
        is StringArgument -> Arg.from(this)
        is IntArgument -> Arg.from(this)
        is LongArgument -> Arg.from(this)
        is BooleanArgument -> Arg.from(this)
        is FloatArgument -> Arg.from(this)
        is EnumArgument -> Arg.from(elements, this)
        else -> error("unknown annotation $this")
    }

    companion object {
        private val argPattern = """\{([^/}]+)}""".toRegex()
    }
}
