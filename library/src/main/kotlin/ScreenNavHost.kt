package jp.takuji31.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost

@Composable
fun ScreenNavHost(
    navController: ScreenNavController,
    startScreen: Screen<*>,
    builder: NavGraphBuilder.() -> Unit,
) {
    LaunchedEffect(navController, startScreen, builder) {
        navController.setFirstScreen(startScreen)
    }
    NavHost(
        navController = navController.navController,
        startDestination = startScreen.parameterizedRoute,
        builder = builder,
    )
}
