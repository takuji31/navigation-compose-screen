package jp.takuji31.compose.screengenerator.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class AutoScreenId(val screenClassName: String)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Route(
    val route: String,
    val deepLinks: Array<String> = [],
    vararg val arguments: Argument = [],
)

annotation class Argument(
    val name: String,
    val type: NavArgumentType,
    val enumClass: KClass<out Enum<*>> = Enum::class
)

enum class NavArgumentType {
    String, Int, Long, Bool, Float, Enum
}