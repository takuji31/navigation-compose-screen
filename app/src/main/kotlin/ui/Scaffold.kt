package jp.takuji31.compose.navigation.example.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import jp.takuji31.compose.navigation.example.navigation.ExampleScreen

private val topLevelScreens = setOf(
    ExampleScreen.Home(),
    ExampleScreen.Settings,
)

private val ExampleScreen.icon: ImageVector
    get() = when (this) {
        is ExampleScreen.Home -> Icons.Default.Home
        ExampleScreen.Settings -> Icons.Default.Settings
        else -> TODO("Not supported")
    }

val ExampleScreen.title: String
    get() = when (this) {
        is ExampleScreen.Home -> "Home"
        is ExampleScreen.Blog -> "Blog $blogId"
        is ExampleScreen.Entry -> "Entry $entryId"
        is ExampleScreen.Ranking -> "${rankingType.name} ranking"
        ExampleScreen.Settings -> "Settings"
        ExampleScreen.About -> "About"
    }

@Composable
fun BlogScaffold(
    currentScreen: ExampleScreen,
    title: String = currentScreen.title,
    snackbarHostState: SnackbarHostState = remember(currentScreen) { SnackbarHostState() },
    onBottomSheetItemClicked: (ExampleScreen) -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (paddingValues: PaddingValues) -> Unit,
) {
    val state = rememberScaffoldState(snackbarHostState = snackbarHostState)
    Scaffold(
        scaffoldState = state,
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                actions = actions,
            )
        },
        bottomBar = {
            if (topLevelScreens.contains(currentScreen)) {
                BottomNavigation {
                    for (screen in topLevelScreens) {
                        BottomNavigationItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            selected = screen.screenId == currentScreen.screenId,
                            onClick = {
                                onBottomSheetItemClicked(screen)
                            },
                            label = { Text(text = screen.title) },
                        )
                    }
                }
            }
        },
    ) {
        content(it)
    }
}
