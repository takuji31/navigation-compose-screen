package jp.takuji31.compose.navigation.example.navigation

import jp.takuji31.compose.navigation.screen.Screen
import jp.takuji31.compose.navigation.screen.annotation.AutoScreenId
import jp.takuji31.compose.navigation.screen.annotation.BooleanArgument
import jp.takuji31.compose.navigation.screen.annotation.EnumArgument
import jp.takuji31.compose.navigation.screen.annotation.Route
import jp.takuji31.compose.navigation.screen.annotation.StringArgument

abstract class MyScreen<S : Enum<*>> : Screen<S>

@AutoScreenId(
    "ExampleScreen",
    screenBaseClass = MyScreen::class,
    dynamicDeepLinkPrefix = true,
)
enum class ExampleScreenId {
    @Route(
        "/",
        deepLinks = ["/?fromDeepLink={fromDeepLink}&deepLinkOnlyArg={deepLinkOnlyArg}"],
        booleanArguments = [
            BooleanArgument(
                "fromDeepLink",
                isNullable = false,
                hasDefaultValue = true,
                defaultValue = false,
            ),
        ],
    )
    Home,

    @Route(
        "/blog/{blogId}",
        stringArguments = [StringArgument("blogId")],
        deepLinks = ["/blog/{blogId}"],
    )
    Blog,

    @Route("/blog/{blogId}/entry/{entryId}", deepLinks = ["/blog/{blogId}/entry/{entryId}"])
    Entry,

    @Route(
        "/ranking/?rankingType={rankingType}",
        enumArguments = [
            EnumArgument(
                "rankingType",
                RankingType::class,
                hasDefaultValue = true,
                "daily",
            ),
        ],
    )
    Ranking,

    @Route("/settings")
    Settings,
}

@Suppress("EnumEntryName")
enum class RankingType {
    daily, monthly, total
}

