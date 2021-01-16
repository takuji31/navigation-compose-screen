package jp.takuji31.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import jp.takuji31.compose.navigation.screen.Screen
import jp.takuji31.compose.navigation.screen.ScreenNavController

@Deprecated(
    "Obsolete package. Please use new package",
    ReplaceWith("KEY_SCREEN", "jp.takuji31.compose.navigation.screen.KEY_SCREEN"),
)
public const val KEY_SCREEN = jp.takuji31.compose.navigation.screen.KEY_SCREEN

@Deprecated(
    "Obsolete package. Please use new package",
    ReplaceWith("ScreenNavController", "jp.takuji31.compose.navigation.screen.ScreenNavController"),
)
typealias ScreenNavController = ScreenNavController

@Deprecated(
    "Obsolete package. Please use new package",
    ReplaceWith(
        "ScreenNavHost(navController = navController, startScreen = startScreen, builder)",
        "jp.takuji31.compose.navigation.screen.ScreenNavHost",
    ),
)
@Composable
fun ScreenNavHost(
    navController: ScreenNavController,
    startScreen: Screen<*>,
    builder: NavGraphBuilder.() -> Unit,
) = jp.takuji31.compose.navigation.screen.ScreenNavHost(
    navController = navController,
    startScreen = startScreen,
    builder = builder,
)

@Deprecated(
    "Obsolete package. Please use new package",
    ReplaceWith(
        "rememberScreenNavController()",
        "jp.takuji31.compose.navigation.screen.rememberScreenNavController",
    ),
)
@Composable
fun rememberScreenNavController(): ScreenNavController =
    jp.takuji31.compose.navigation.screen.rememberScreenNavController()
