package jp.takuji31.compose.navigation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenNavController(val navController: NavHostController) {
    private val _currentScreen = MutableStateFlow<Screen<Enum<*>>?>(null)
    val currentScreen: StateFlow<Screen<Enum<*>>?> = _currentScreen.asStateFlow()

    init {
        navController.addOnDestinationChangedListener { _, destination, args ->
            val route = destination.route ?: return@addOnDestinationChangedListener
            _currentScreen.value =
                ScreenFactoryRegistry.findByRoute<ScreenFactory<*>>(route).fromBundle(args)
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
fun rememberScreenNavController(
    vararg navigators: Navigator<out NavDestination>
): ScreenNavController {
    val navController = rememberNavController(*navigators)
    return remember(navController) {
        ScreenNavController(navController)
    }
}
