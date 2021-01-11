package jp.takuji31.compose.navigation.example.navigation

import jp.takuji31.compose.navigation.Screen
import jp.takuji31.compose.screengenerator.annotation.Argument
import jp.takuji31.compose.screengenerator.annotation.AutoScreenId
import jp.takuji31.compose.screengenerator.annotation.NavArgumentType
import jp.takuji31.compose.screengenerator.annotation.Route

abstract class MyScreen<S : Enum<*>> : Screen<S>

@AutoScreenId("ExampleScreen", screenBaseClass = MyScreen::class)
enum class ExampleScreenId {
    @Route("/", deepLinks = ["https://takuji31.jp/compose-navigation/"])
    Home,

    @Route("/blog/{blogId}", arguments = [Argument("blogId", NavArgumentType.String)])
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

