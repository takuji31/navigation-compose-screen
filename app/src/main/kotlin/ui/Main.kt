package jp.takuji31.compose.navigation.example.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import jp.takuji31.compose.navigation.ScreenNavController
import jp.takuji31.compose.navigation.ScreenNavHost
import jp.takuji31.compose.navigation.example.navViewModel

@Composable
fun Main(navController: ScreenNavController) {
    val currentScreen by navController.currentScreen.collectAsState()
    ScreenNavHost(
        navController = navController,
        startScreen = ExampleScreen.Home,
    ) {
        val onBottomSheetItemClicked: (ExampleScreen) -> Unit = { screen ->
            navController.navigate(screen) {
                popUpTo(ExampleScreenId.Home) { inclusive = screen == ExampleScreen.Home }
            }
        }
        examplescreenComposable {
            home { screen ->
                val viewModel = navViewModel<HomeViewModel>()
                val state by viewModel.state.collectAsState()
                BlogScaffold(
                    currentScreen = screen,
                    topBar = {
                        TopAppBar(
                            title = { Text(text = screen.title) },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.reload() },
                                    enabled = state !is HomeViewModel.State.Loading,
                                ) {
                                    Icon(Icons.Default.Refresh)
                                }
                            },
                        )
                    },
                    onBottomSheetItemClicked = onBottomSheetItemClicked,
                ) {
                    val blogs = (state as? HomeViewModel.State.Loaded)?.blogs
                    Home(blogs) {
                        navController.navigate(ExampleScreen.Blog(it.id))
                    }
                }
            }
            blog {

            }
            entry {

            }
            settings {
                Text(text = "This is settings")
            }
        }
    }
}
