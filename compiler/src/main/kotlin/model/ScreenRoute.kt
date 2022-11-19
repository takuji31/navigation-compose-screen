package jp.takuji31.compose.navigation.compiler.model

import com.squareup.kotlinpoet.STRING
import jp.takuji31.compose.navigation.compiler.toCamelCase
import jp.takuji31.compose.navigation.compiler.toLowerCamelCase
import jp.takuji31.compose.navigation.screen.annotation.BooleanArgument
import jp.takuji31.compose.navigation.screen.annotation.DialogRoute
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
    val routeType: RouteType,
    val route: String,
    val deepLinks: List<String>,
    val stringArguments: List<StringArgument>,
    val intArguments: List<IntArgument>,
    val longArguments: List<LongArgument>,
    val booleanArguments: List<BooleanArgument>,
    val floatArguments: List<FloatArgument>,
    val enumArguments: List<EnumArgument>,
) {
    val bestTypeName: String by lazy {
        name.toCamelCase()
    }

    val bestFunctionName: String by lazy {
        name.toLowerCamelCase()
    }

    val hasArgs: Boolean by lazy {
        route.contains(argPattern) || deepLinks.any { it.contains(argPattern) }
    }

    val hasDeepLinks: Boolean by lazy {
        deepLinks.isNotEmpty()
    }

    private val routePathAndQuery by lazy { route.split("?", limit = 2) }
    private val routePath: String by lazy { routePathAndQuery[0] }
    private val routeQuery: String by lazy { routePathAndQuery.getOrNull(1) ?: "" }
    private val routePathArgNames: List<String> by lazy {
        routePath.extractParameters()
    }
    private val routeQueryArgNames: List<String> by lazy {
        routeQuery.extractParameters()
    }

    val args: Set<Arg> by lazy {
        val definedArgs = listOf(
            stringArguments.map { it.name to it },
            intArguments.map { it.name to it },
            longArguments.map { it.name to it },
            booleanArguments.map { it.name to it },
            floatArguments.map { it.name to it },
            enumArguments.map { it.name to it },
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

        val deepLinkArgNames =
            deepLinks.map { deepLink -> deepLink.extractParameters() }.flatten()
        val deepLinkArgs = (deepLinkArgNames - (definedKeys + generatedArgs.keys)).map {
            it to Arg(
                type = Arg.Type.String,
                name = it,
                isNullable = true,
                hasDefaultValue = true,
                typeName = STRING,
                defaultValue = null,
            )
        }.toMap()

        val navArgs: Map<String, Arg> = definedArgs + generatedArgs + deepLinkArgs

        // order map key by argument order
        (routePathArgNames + routeQueryArgNames + deepLinkArgNames)
            .map { checkNotNull(navArgs[it]) }
            .distinctBy { it.name }
            .toSet()
    }

    val isAllArgsOptional: Boolean by lazy {
        args.all { it.hasDefaultValue }
    }

    private fun String.extractParameters() =
        argPattern.findAll(this).map { it.groupValues[1] }.toList()

    private fun Annotation.toArg(): Arg = when (this) {
        is StringArgument -> Arg.from(this)
        is IntArgument -> Arg.from(this)
        is LongArgument -> Arg.from(this)
        is BooleanArgument -> Arg.from(this)
        is FloatArgument -> Arg.from(this)
        is EnumArgument -> TODO() // Arg.from(elements, this)
        else -> error("unknown annotation $this")
    }

    companion object {
        private val argPattern = """\{([^/}]+)}""".toRegex()

        operator fun invoke(
            elements: Elements,
            name: String,
            route: Route,
        ): ScreenRoute {
            return ScreenRoute(
                elements,
                name,
                RouteType.Default,
                route.route,
                route.deepLinks.toList(),
                route.stringArguments.toList(),
                route.intArguments.toList(),
                route.longArguments.toList(),
                route.booleanArguments.toList(),
                route.floatArguments.toList(),
                route.enumArguments.toList(),
            )
        }

        operator fun invoke(
            elements: Elements,
            name: String,
            route: DialogRoute,
        ): ScreenRoute {
            return ScreenRoute(
                elements,
                name,
                RouteType.Dialog,
                route.route,
                route.deepLinks.toList(),
                route.stringArguments.toList(),
                route.intArguments.toList(),
                route.longArguments.toList(),
                route.booleanArguments.toList(),
                route.floatArguments.toList(),
                route.enumArguments.toList(),
            )
        }
    }

    enum class RouteType {
        Default, Dialog
    }
}
