package jp.takuji31.compose.navigation.example.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import jp.takuji31.compose.navigation.example.navigation.ExampleScreen
import jp.takuji31.compose.navigation.example.navigation.ExampleScreenId
import jp.takuji31.compose.navigation.example.navigation.exampleScreenComposable
import jp.takuji31.compose.navigation.example.navigation.popUpToScreenId
import jp.takuji31.compose.navigation.screen.ScreenNavController
import jp.takuji31.compose.navigation.screen.ScreenNavHost

@Suppress("EXPERIMENTAL_API_USAGE")
@Composable
fun Main(navController: ScreenNavController) {
    val currentScreen by navController.currentScreen.collectAsState()
    ScreenNavHost(
        navController = navController,
        startScreen = ExampleScreen.Home(),
    ) {
        val onBottomSheetItemClicked: (ExampleScreen) -> Unit = { screen ->
            navController.navigate(screen) {
                popUpToScreenId(ExampleScreenId.Home) { inclusive = screen is ExampleScreen.Home }
            }
        }
        exampleScreenComposable(deepLinkPrefix = "compose-navigation-example://blog.takuji31.jp") {
            home { screen ->
                val viewModel = hiltViewModel<HomeViewModel>()
                val state by viewModel.state.collectAsState()

                Home(
                    state = state,
                    snackbarHostState = viewModel.snackbarHostState,
                    screen = screen,
                    onBottomSheetItemClicked = onBottomSheetItemClicked,
                    onReloadButtonClick = { viewModel.reload() },
                    onSettingsButtonClick = { navController.navigate(ExampleScreen.Settings) },
                    onAboutClick = { navController.navigate(ExampleScreen.About) },
                    onItemClick = { navController.navigate(ExampleScreen.Blog(it.id)) },
                )
            }
            blog { screen ->
                val viewModel = hiltViewModel<BlogViewModel>()
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
                val viewModel = hiltViewModel<EntryViewModel>()
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
            about {
                About {
                    navController.popBackStack()
                }
            }
        }
    }
}
