package jp.takuji31.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.KEY_ROUTE
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScreenNavController(val navController: NavHostController) {
    private val _currentScreen = MutableStateFlow<Screen<out Any>?>(null)
    val currentScreen: StateFlow<Screen<out Any>?>
        get() = _currentScreen

    private var onDestinationChanged: ((route: String) -> Unit)? = null

    init {
        navController.addOnDestinationChangedListener { _, destination, args ->
            checkNotNull(args)
            val route = args[KEY_ROUTE] as String
            onDestinationChanged?.invoke(route)
        }
    }

    fun navigate(screen: Screen<Any>, popUpTo: String? = null, inclusive: Boolean = false) {
        onDestinationChanged = {
            if (it == screen.route) {
                _currentScreen.value = screen
            }
        }
        navController.navigate(screen.route) {
            if (popUpTo != null) {
                popUpTo("") {
                    this.inclusive = inclusive
                }
            }
        }
    }
}

@Composable
fun rememberScreenNavController(): ScreenNavController {
    val navController = rememberNavController()
    return remember(navController) {
        ScreenNavController(navController)
    }
}
