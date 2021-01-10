package jp.takuji31.compose.navigation.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigate
import jp.takuji31.compose.navigation.Screen
import jp.takuji31.compose.navigation.rememberScreenNavController
import jp.takuji31.compose.screengenerator.annotation.Argument
import jp.takuji31.compose.screengenerator.annotation.AutoScreenId
import jp.takuji31.compose.screengenerator.annotation.NavArgumentType
import jp.takuji31.compose.screengenerator.annotation.Route

abstract class MyScreen<S : Enum<*>> : Screen<S>

@AutoScreenId("ExampleScreen", screenBaseClass = MyScreen::class)
enum class ExampleScreenId {
    @Route("/", deepLinks = ["https://takuji31.jp/compose-navigation/"])
    Home,

    @Route("/user/{userId}", arguments = [Argument("userId", NavArgumentType.String)])
    User,

    @Route("/user/{userId}/blog/{blogId}")
    Blog,

    @Route("/user/{userId}/blog/{blogId}/entry/{entryId}")
    Entry,

    @Route("/user/{userId}/blog/{blogId}/subscribers")
    Subscribers,

    @Route(
        "/ranking/{rankingType}",
        arguments = [Argument("rankingType", NavArgumentType.Enum, RankingType::class)],
    )
    Ranking,
}

@Suppress("EnumEntryName")
enum class RankingType {
    daily, monthly, total
}

fun NavController.navigateExampleScreen(
    screen: ExampleScreen,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    navigate(screen.route, builder)
}

@Composable
fun Main() {
    val navController = rememberScreenNavController()
    val currentScreen by navController.currentScreen.collectAsState()
    NavHost(
        navController = navController.navController,
        startDestination = ExampleScreenId.Home.route,
    ) {
        examplescreenComposable {
            home {

            }
            blog {

            }
        }
    }
}
