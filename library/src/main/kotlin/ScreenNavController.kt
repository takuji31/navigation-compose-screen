package jp.takuji31.compose.navigation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public const val KEY_SCREEN = "jp.takuji31.compose.navigation.ScreenNavController:screen"

class ScreenNavController(val navController: NavHostController) {
    private val _currentScreen = MutableStateFlow<Screen<Enum<*>>?>(null)
    val currentScreen: StateFlow<Screen<Enum<*>>?>
        get() = _currentScreen

    init {
        navController.addOnDestinationChangedListener { _, destination, args ->
            val route = destination.route ?: return@addOnDestinationChangedListener
            _currentScreen.value = ScreenFactoryRegistry.findByRoute<ScreenFactory<*>>(route).fromBundle(args)
        }
    }

    fun navigate(screen: Screen<Enum<*>>, builder: NavOptionsBuilder.() -> Unit = {}) {
        navController.navigate(screen.parameterizedRoute, builder)
    }

    fun popBackStack() {
        navController.popBackStack()
    }
}

@Composable
fun rememberScreenNavController(): ScreenNavController {
    val navController = rememberNavController()
    return remember(navController) {
        ScreenNavController(navController)
    }
}
