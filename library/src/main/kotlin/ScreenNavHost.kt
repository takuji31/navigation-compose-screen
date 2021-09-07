package jp.takuji31.compose.navigation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost

@Composable
fun ScreenNavHost(
    navController: ScreenNavController,
    startScreen: Screen<*>,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        navController = navController.navController,
        startDestination = startScreen.parameterizedRoute,
        builder = builder,
        modifier = modifier,
    )
}
