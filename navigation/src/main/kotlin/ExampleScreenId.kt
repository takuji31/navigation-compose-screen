package jp.takuji31.compose.navigation.example.navigation

import jp.takuji31.compose.navigation.screen.Screen
import jp.takuji31.compose.navigation.screen.annotation.Argument
import jp.takuji31.compose.navigation.screen.annotation.AutoScreenId
import jp.takuji31.compose.navigation.screen.annotation.NavArgumentType
import jp.takuji31.compose.navigation.screen.annotation.Route

abstract class MyScreen<S : Enum<*>> : Screen<S>

@AutoScreenId(
    "ExampleScreen",
    screenBaseClass = MyScreen::class,
)
enum class ExampleScreenId {
    @Route(
        "/",
        deepLinks = ["https://takuji31.jp/compose-navigation/"],
    )
    Home,

    @Route(
        "/blog/{blogId}",
        arguments = [Argument("blogId", NavArgumentType.String)],
    )
    Blog,

    @Route("/blog/{blogId}/entry/{entryId}")
    Entry,

    @Route(
        "/ranking/{rankingType}",
        arguments = [Argument("rankingType", NavArgumentType.Enum, RankingType::class)],
    )
    Ranking,

    @Route("/settings")
    Settings,
}

@Suppress("EnumEntryName")
enum class RankingType {
    daily, monthly, total
}

