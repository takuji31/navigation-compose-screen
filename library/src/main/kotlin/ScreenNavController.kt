package jp.takuji31.compose.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.KEY_ROUTE
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScreenNavController(val navController: NavHostController) {
    private val _currentScreen = MutableStateFlow<Screen<Enum<*>>?>(null)
    val currentScreen: StateFlow<Screen<Enum<*>>?>
        get() = _currentScreen

    private var onDestinationChanged: ((route: String) -> Screen<*>?)? = null

    init {
        navController.addOnDestinationChangedListener { _, destination, args ->
            checkNotNull(args)
            val route = args[KEY_ROUTE] as String
            val argsScreen = args[KEY_SCREEN] as? Screen<*>
            val newScreen = onDestinationChanged?.invoke(route)
            if (newScreen != null) {
                // NOTE: Generated screen class always implements Parcelable
                args.putParcelable(KEY_SCREEN, newScreen as Parcelable)
                _currentScreen.value = newScreen
            } else if (argsScreen != null) {
                _currentScreen.value = argsScreen
            }
            onDestinationChanged = null
        }
    }

    fun navigate(screen: Screen<Enum<*>>, builder: NavOptionsBuilder.() -> Unit = {}) {
        onDestinationChanged = { route ->
            screen.takeIf { it.route == route }
        }
        navController.navigate(screen.parameterizedRoute, builder)
    }

    fun popBackStack() {
        navController.popBackStack()
    }

    companion object {
        private const val KEY_SCREEN = "jp.takuji31.compose.navigation.ScreenNavController:screen"
    }
}

@Composable
fun rememberScreenNavController(): ScreenNavController {
    val navController = rememberNavController()
    return remember(navController) {
        ScreenNavController(navController)
    }
}
