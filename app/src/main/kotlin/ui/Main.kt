package jp.takuji31.compose.navigation.example.ui

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import jp.takuji31.compose.navigation.ScreenNavController
import jp.takuji31.compose.navigation.ScreenNavHost

private val topLevelScreens = setOf(
    ExampleScreen.Home,
    ExampleScreen.Settings,
)

private val ExampleScreen.title: String
    get() = when (this) {
        ExampleScreen.Home -> "Home"
        is ExampleScreen.User -> "User $userId"
        is ExampleScreen.Blog -> "Blog $blogId"
        is ExampleScreen.Entry -> "Entry $entryId"
        is ExampleScreen.Subscribers -> "Subscriber of blog $blogId"
        is ExampleScreen.Ranking -> "${rankingType.name} ranking"
        ExampleScreen.Settings -> "Settings"
    }

private val ExampleScreen.icon: ImageVector
    get() = when (this) {
        ExampleScreen.Home -> Icons.Default.Home
        ExampleScreen.Settings -> Icons.Default.Settings
        else -> TODO("Not supported")
    }

@Composable
fun Main(navController: ScreenNavController) {
    val currentScreen by navController.currentScreen.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = (currentScreen as? ExampleScreen)?.title ?: "") })
        },
        bottomBar = {
            BottomNavigation {
                for (screen in topLevelScreens) {
                    BottomNavigationItem(
                        icon = { Icon(screen.icon) },
                        selected = screen.screenId == currentScreen?.screenId,
                        onClick = {
                            navController.navigate(screen) {
                                popUpTo(ExampleScreenId.Home) {
                                    inclusive = screen == ExampleScreen.Home
                                }
                            }
                        },
                        label = { Text(text = screen.title) },
                    )
                }
            }
        },
    ) {
        ScreenNavHost(
            navController = navController,
            startScreen = ExampleScreen.Home,
        ) {
            examplescreenComposable {
                home {
                    Text(text = "This is Home")
                }
                user {

                }
                blog {

                }
                entry {

                }
                subscribers {

                }
                settings {
                    Text(text = "This is settings")
                }
            }
        }
    }
}
