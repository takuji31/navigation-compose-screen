package jp.takuji31.compose.screengenerator.annotation

import jp.takuji31.compose.navigation.Screen
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AutoScreenId(
    val screenClassName: String,
    val screenBaseClass: KClass<*> = Screen::class,
)

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
    val enumClass: KClass<out Enum<*>> = Enum::class,
)

enum class NavArgumentType {
    String, Int, Long, Bool, Float, Enum
}
