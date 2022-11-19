package jp.takuji31.compose.navigation.example.navigation
//
//import android.os.Bundle
//import android.os.Parcelable
//import androidx.compose.runtime.Composable
//import androidx.lifecycle.SavedStateHandle
//import androidx.navigation.NamedNavArgument
//import androidx.navigation.NavDeepLink
//import androidx.navigation.NavGraphBuilder
//import androidx.navigation.NavOptionsBuilder
//import androidx.navigation.NavType
//import androidx.navigation.PopUpToBuilder
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.dialog
//import androidx.navigation.navArgument
//import androidx.navigation.navDeepLink
//import jp.takuji31.compose.navigation.example.navigation.ExampleScreenId.About
//import jp.takuji31.compose.navigation.example.navigation.ExampleScreenId.Blog
//import jp.takuji31.compose.navigation.example.navigation.ExampleScreenId.Entry
//import jp.takuji31.compose.navigation.example.navigation.ExampleScreenId.Home
//import jp.takuji31.compose.navigation.example.navigation.ExampleScreenId.Ranking
//import jp.takuji31.compose.navigation.example.navigation.ExampleScreenId.Settings
//import jp.takuji31.compose.navigation.example.navigation.RankingType.daily
//import jp.takuji31.compose.navigation.screen.Screen
//import jp.takuji31.compose.navigation.screen.ScreenFactory
//import jp.takuji31.compose.navigation.screen.ScreenFactoryRegistry
//import kotlinx.parcelize.Parcelize
//
//public sealed class ExampleScreen(
//    public override val screenId: ExampleScreenId,
//) : Screen<ExampleScreenId>, Parcelable {
//    @Parcelize
//    public data class Home(
//        public val fromDeepLink: Boolean = false,
//        public val deepLinkOnlyArg: String? = null,
//    ) : ExampleScreen(ExampleScreenId.Home) {
//        public override val route: String
//            get() = screenId.route
//
//        public override val parameterizedRoute: String
//            get() = """/"""
//
//        public companion object : ScreenFactory<Home> {
//            public override fun fromBundle(bundle: Bundle?): Home {
//                bundle ?: return Home()
//                val fromDeepLink = bundle["fromDeepLink"] as? Boolean ?: false
//                val deepLinkOnlyArg = bundle["deepLinkOnlyArg"] as? String?
//                return Home(
//                    fromDeepLink = fromDeepLink,
//                    deepLinkOnlyArg = deepLinkOnlyArg,
//                )
//            }
//
//            public override fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): Home {
//                val fromDeepLink = savedStateHandle.get("fromDeepLink") as? Boolean ?: false
//                val deepLinkOnlyArg = savedStateHandle.get("deepLinkOnlyArg") as? String?
//                return Home(
//                    fromDeepLink = fromDeepLink,
//                    deepLinkOnlyArg = deepLinkOnlyArg,
//                )
//            }
//        }
//    }
//
//    @Parcelize
//    public data class Blog(
//        public val blogId: String,
//    ) : ExampleScreen(ExampleScreenId.Blog) {
//        public override val route: String
//            get() = screenId.route
//
//        public override val parameterizedRoute: String
//            get() = """/blog/$blogId"""
//
//        public companion object : ScreenFactory<Blog> {
//            public override fun fromBundle(bundle: Bundle?): Blog {
//                checkNotNull(bundle) {
//                    error("Screen ExampleScreen.Blog has non-optional parameter")
//                }
//                val blogId = bundle["blogId"] as? String
//                    ?: error("Screen ExampleScreen.Blog requires parameter: blogId")
//                return Blog(
//                    blogId = blogId,
//                )
//            }
//
//            public override fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): Blog {
//                val blogId = savedStateHandle.get("blogId") as? String
//                    ?: error("Screen ExampleScreen.Blog requires parameter: blogId")
//                return Blog(
//                    blogId = blogId,
//                )
//            }
//        }
//    }
//
//    @Parcelize
//    public data class Entry(
//        public val blogId: String,
//        public val entryId: String,
//    ) : ExampleScreen(ExampleScreenId.Entry) {
//        public override val route: String
//            get() = screenId.route
//
//        public override val parameterizedRoute: String
//            get() = """/blog/$blogId/entry/$entryId"""
//
//        public companion object : ScreenFactory<Entry> {
//            public override fun fromBundle(bundle: Bundle?): Entry {
//                checkNotNull(bundle) {
//                    error("Screen ExampleScreen.Entry has non-optional parameter")
//                }
//                val blogId = bundle["blogId"] as? String
//                    ?: error("Screen ExampleScreen.Entry requires parameter: blogId")
//                val entryId = bundle["entryId"] as? String
//                    ?: error("Screen ExampleScreen.Entry requires parameter: entryId")
//                return Entry(
//                    blogId = blogId,
//                    entryId = entryId,
//                )
//            }
//
//            public override fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): Entry {
//                val blogId = savedStateHandle.get("blogId") as? String
//                    ?: error("Screen ExampleScreen.Entry requires parameter: blogId")
//                val entryId = savedStateHandle.get("entryId") as? String
//                    ?: error("Screen ExampleScreen.Entry requires parameter: entryId")
//                return Entry(
//                    blogId = blogId,
//                    entryId = entryId,
//                )
//            }
//        }
//    }
//
//    @Parcelize
//    public data class Ranking(
//        public val rankingType: RankingType = daily,
//    ) : ExampleScreen(ExampleScreenId.Ranking) {
//        public override val route: String
//            get() = screenId.route
//
//        public override val parameterizedRoute: String
//            get() = """/ranking/?rankingType=$rankingType"""
//
//        public companion object : ScreenFactory<Ranking> {
//            public override fun fromBundle(bundle: Bundle?): Ranking {
//                bundle ?: return Ranking()
//                val rankingType = bundle["rankingType"] as? RankingType ?: RankingType.daily
//                return Ranking(
//                    rankingType = rankingType,
//                )
//            }
//
//            public override fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): Ranking {
//                val rankingType =
//                    savedStateHandle.get("rankingType") as? RankingType ?: RankingType.daily
//                return Ranking(
//                    rankingType = rankingType,
//                )
//            }
//        }
//    }
//
//    @Parcelize
//    public object Settings : ExampleScreen(ExampleScreenId.Settings), ScreenFactory<Settings> {
//        public override val route: String
//            get() = screenId.route
//
//        public override fun fromBundle(bundle: Bundle?): Settings = this
//
//        public override fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): Settings =
//            this
//    }
//
//    @Parcelize
//    public object About : ExampleScreen(ExampleScreenId.About), ScreenFactory<About> {
//        public override val route: String
//            get() = screenId.route
//
//        public override fun fromBundle(bundle: Bundle?): About = this
//
//        public override fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): About = this
//    }
//
//    public class ComposeDestinationBuilder(
//        private val navGraphBuilder: NavGraphBuilder,
//        public val deepLinkPrefix: String,
//    ) {
//        init {
//            ScreenFactoryRegistry.register("/", Home::class, Home)
//            ScreenFactoryRegistry.register("/blog/{blogId}", Blog::class, Blog)
//            ScreenFactoryRegistry.register("/blog/{blogId}/entry/{entryId}", Entry::class, Entry)
//            ScreenFactoryRegistry.register(
//                "/ranking/?rankingType={rankingType}",
//                Ranking::class,
//                Ranking,
//            )
//            ScreenFactoryRegistry.register("/settings", Settings::class, Settings)
//            ScreenFactoryRegistry.register("/about", About::class, About)
//        }
//
//        public fun home(content: @Composable (screen: Home) -> Unit): Unit {
//            navGraphBuilder.composable(
//                ExampleScreenId.Home.route, ExampleScreenId.Home.navArgs,
//                ExampleScreenId.Home.deepLinks(deepLinkPrefix),
//            ) {
//                val screen = Home.fromBundle(it.arguments)
//                content(screen)
//            }
//        }
//
//        public fun blog(content: @Composable (screen: Blog) -> Unit): Unit {
//            navGraphBuilder.composable(
//                ExampleScreenId.Blog.route, ExampleScreenId.Blog.navArgs,
//                ExampleScreenId.Blog.deepLinks(deepLinkPrefix),
//            ) {
//                val screen = Blog.fromBundle(it.arguments)
//                content(screen)
//            }
//        }
//
//        public fun entry(content: @Composable (screen: Entry) -> Unit): Unit {
//            navGraphBuilder.composable(
//                ExampleScreenId.Entry.route, ExampleScreenId.Entry.navArgs,
//                ExampleScreenId.Entry.deepLinks(deepLinkPrefix),
//            ) {
//                val screen = Entry.fromBundle(it.arguments)
//                content(screen)
//            }
//        }
//
//        public fun ranking(content: @Composable (screen: Ranking) -> Unit): Unit {
//            navGraphBuilder.composable(
//                ExampleScreenId.Ranking.route, ExampleScreenId.Ranking.navArgs,
//                ExampleScreenId.Ranking.deepLinks(deepLinkPrefix),
//            ) {
//                val screen = Ranking.fromBundle(it.arguments)
//                content(screen)
//            }
//        }
//
//        public fun settings(content: @Composable (screen: Settings) -> Unit): Unit {
//            navGraphBuilder.composable(
//                ExampleScreenId.Settings.route, ExampleScreenId.Settings.navArgs,
//                ExampleScreenId.Settings.deepLinks(deepLinkPrefix),
//            ) {
//                val screen = Settings.fromBundle(it.arguments)
//                content(screen)
//            }
//        }
//
//        public fun about(content: @Composable (screen: About) -> Unit): Unit {
//            navGraphBuilder.dialog(
//                ExampleScreenId.About.route, ExampleScreenId.About.navArgs,
//                ExampleScreenId.About.deepLinks(deepLinkPrefix),
//            ) {
//                val screen = About.fromBundle(it.arguments)
//                content(screen)
//            }
//        }
//    }
//}
//
//public val ExampleScreenId.route: String
//    get() = when (this) {
//        Home -> "/"
//        Blog -> "/blog/{blogId}"
//        Entry -> "/blog/{blogId}/entry/{entryId}"
//        Ranking -> "/ranking/?rankingType={rankingType}"
//        Settings -> "/settings"
//        About -> "/about"
//    }
//
//public val ExampleScreenId.navArgs: List<NamedNavArgument>
//    get() = when (this) {
//        Home -> listOf(
//            navArgument("fromDeepLink") {
//                type = NavType.BoolType
//                defaultValue = false
//                nullable = false
//            },
//            navArgument("deepLinkOnlyArg") {
//                type = NavType.StringType
//                defaultValue = null
//                nullable = true
//            },
//        )
//        Blog -> listOf(
//            navArgument("blogId") {
//                type = NavType.StringType
//                nullable = false
//            },
//        )
//        Entry -> listOf(
//            navArgument("blogId") {
//                type = NavType.StringType
//                nullable = false
//            },
//            navArgument("entryId") {
//                type = NavType.StringType
//                nullable = false
//            },
//        )
//        Ranking -> listOf(
//            navArgument("rankingType") {
//                type = NavType.EnumType(RankingType::class.java)
//                defaultValue = RankingType.daily
//                nullable = false
//            },
//        )
//        Settings -> emptyList()
//        About -> emptyList()
//    }
//
//public fun ExampleScreenId.deepLinks(prefix: String): List<NavDeepLink> = when (this) {
//    Home -> listOf(
//        navDeepLink {
//            uriPattern = prefix +
//                "/?fromDeepLink={fromDeepLink}&deepLinkOnlyArg={deepLinkOnlyArg}"
//        },
//    )
//    Blog -> listOf(
//        navDeepLink { uriPattern = prefix + "/blog/{blogId}" },
//    )
//    Entry -> listOf(
//        navDeepLink { uriPattern = prefix + "/blog/{blogId}/entry/{entryId}" },
//    )
//    Ranking -> emptyList()
//    Settings -> emptyList()
//    About -> emptyList()
//}
//
//public fun NavGraphBuilder.exampleScreenComposable(
//    deepLinkPrefix: String,
//    builder: ExampleScreen.ComposeDestinationBuilder.() -> Unit,
//) =
//    ExampleScreen.ComposeDestinationBuilder(this, deepLinkPrefix).builder()
//
//public fun NavOptionsBuilder.popUpTo(id: ExampleScreenId, builder: PopUpToBuilder.() -> Unit) =
//    popUpTo(id.route, builder)
