package jp.takuji31.compose.navigation.example.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import jp.takuji31.compose.navigation.ScreenNavController
import jp.takuji31.compose.navigation.ScreenNavHost
import jp.takuji31.compose.navigation.example.navViewModel
import jp.takuji31.compose.navigation.example.navigation.ExampleScreen
import jp.takuji31.compose.navigation.example.navigation.ExampleScreenId
import jp.takuji31.compose.navigation.example.navigation.exampleScreenComposable
import jp.takuji31.compose.navigation.example.navigation.popUpTo

@Suppress("EXPERIMENTAL_API_USAGE")
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
        exampleScreenComposable {
            home { screen ->
                val viewModel = navViewModel<HomeViewModel>()
                val state by viewModel.state.collectAsState()
                Home(
                    state = state,
                    screen = screen,
                    onBottomSheetItemClicked = onBottomSheetItemClicked,
                    onReloadButtonClick = { viewModel.reload() },
                    onItemClick = { navController.navigate(ExampleScreen.Blog(it.id)) },
                )
            }
            blog { screen ->
                val viewModel = navViewModel<BlogViewModel>()
                val state by viewModel.state.collectAsState()
                Blog(
                    state = state,
                    screen = screen,
                    onReloadButtonClick = { viewModel.reload() },
                    onItemClick = {
                        navController.navigate(
                            ExampleScreen.Entry(
                                screen.blogId,
                                it.id,
                            ),
                        )
                    },
                )
            }
            entry {
                val viewModel = navViewModel<EntryViewModel>()
                val state by viewModel.state.collectAsState()
                Entry(
                    state = state,
                    screen = it,
                    onReloadButtonClick = { viewModel.reload() },
                )
            }
            settings { screen ->
                BlogScaffold(
                    currentScreen = screen,
                    onBottomSheetItemClicked = { navController.navigate(it) },
                ) {
                    Text(text = "This is settings")
                }
            }
        }
    }
}
