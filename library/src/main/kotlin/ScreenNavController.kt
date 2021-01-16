package jp.takuji31.compose.navigation.screen

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.KEY_ROUTE
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public const val KEY_SCREEN = "jp.takuji31.compose.navigation.ScreenNavController:screen"

class ScreenNavController(val navController: NavHostController) {
    private val _currentScreen = MutableStateFlow<Screen<Enum<*>>?>(null)
    val currentScreen: StateFlow<Screen<Enum<*>>?>
        get() = _currentScreen

    private var onDestinationChanged: ((route: String) -> Screen<*>?)? = null

    init {
        navController.addOnDestinationChangedListener { _, _, args ->
            checkNotNull(args)
            val route = args[KEY_ROUTE] as String
            val argsScreen = args[KEY_SCREEN] as? Screen<*>
            val newScreen = onDestinationChanged?.invoke(route)
            if (newScreen != null) {
                // NOTE: Generated screen class always implements Parcelable
                args.putParcelable(KEY_SCREEN, newScreen as Parcelable)
                _currentScreen.value = newScreen
            } else if (argsScreen != null) {
                // Restore state, BackStack popped etc.
                _currentScreen.value = argsScreen
            }
            onDestinationChanged = null
        }
    }

    internal fun setFirstScreen(firstScreen: Screen<*>) {
        val currentBackStackEntry =
            checkNotNull(navController.currentBackStackEntry) { "ScreenNavController.setFirstScreen should not call before creating graph" }
        val arguments = checkNotNull(currentBackStackEntry.arguments)
        val route = arguments.get(KEY_ROUTE)
        val argumentScreen = arguments.get(KEY_SCREEN)
        if (currentScreen.value == null && argumentScreen == null && route == firstScreen.parameterizedRoute) {
            arguments.putParcelable(KEY_SCREEN, firstScreen as Parcelable)
            _currentScreen.value = firstScreen
        }
        if (currentScreen.value == null) {
            Log.w("ScreenNavController", "Something's wrong! Current screen not found : $route")
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
}

@Composable
fun rememberScreenNavController(): ScreenNavController {
    val navController = rememberNavController()
    return remember(navController) {
        ScreenNavController(navController)
    }
}
