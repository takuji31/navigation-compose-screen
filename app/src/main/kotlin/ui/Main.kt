package jp.takuji31.compose.navigation.example.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import jp.takuji31.compose.navigation.example.navigation.ExampleScreen
import jp.takuji31.compose.navigation.example.navigation.ExampleScreenId
import jp.takuji31.compose.navigation.example.navigation.exampleScreenComposable
import jp.takuji31.compose.navigation.example.navigation.popUpTo
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
                popUpTo(ExampleScreenId.Home) { inclusive = screen is ExampleScreen.Home }
            }
        }
        exampleScreenComposable(deepLinkPrefix = "compose-navigation-example://blog.takuji31.jp") {
            home { screen ->
                val context = LocalContext.current
                val navBackStackEntry by navController.navController.currentBackStackEntryAsState()
                val viewModelFactory = remember(context) {
                    HiltViewModelFactory(context, checkNotNull(navBackStackEntry))
                }
                val viewModel = viewModel<HomeViewModel>(factory = viewModelFactory)
                val state by viewModel.state.collectAsState()

                Home(
                    state = state,
                    snackbarHostState = viewModel.snackbarHostState,
                    screen = screen,
                    onBottomSheetItemClicked = onBottomSheetItemClicked,
                    onReloadButtonClick = { viewModel.reload() },
                    onSettingsButtonClick = { navController.navigate(ExampleScreen.Settings) },
                    onItemClick = { navController.navigate(ExampleScreen.Blog(it.id)) },
                )
            }
            blog { screen ->
                val context = LocalContext.current
                val navBackStackEntry by navController.navController.currentBackStackEntryAsState()
                val viewModelFactory = remember(context) {
                    HiltViewModelFactory(context, checkNotNull(navBackStackEntry))
                }
                val viewModel = viewModel<BlogViewModel>(factory = viewModelFactory)
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
                val context = LocalContext.current
                val navBackStackEntry by navController.navController.currentBackStackEntryAsState()
                val viewModelFactory = remember(context) {
                    HiltViewModelFactory(context, checkNotNull(navBackStackEntry))
                }
                val viewModel = viewModel<EntryViewModel>(factory = viewModelFactory)
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
