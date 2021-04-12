# navigation-compose-screen

Screen object support for navigation-compose

## Requirements

- Android Gradle Plugin 7.0.0-alpha14 or later
- Jetpack Compose 1.0.0-beta04
- navigation-compose 1.0.0-alpha10
- Kotlin 1.4.30
- minimum SDK version 23 (Android 6.0)

## What can I do?

Provide type safe navigation to navigation-compose

```kotlin
// screenBaseClass is optional
@AutoScreenId(
    "ExampleScreen",
    screenBaseClass = MyScreen::class,
)
enum class ExampleScreenId {
    // Deep Link support
    @Route(
        "/",
        deepLinks = ["https://takuji31.jp/compose-navigation/"],
    )
    Home,

    // arguments support
    @Route(
        "/blog/{blogId}",
        stringArguments = [StringArgument("blogId")],
    )
    Blog,

    // auto argument type is String
    @Route("/blog/{blogId}/entry/{entryId}")
    Entry,

    // enum type arguments supported
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
```

You can use generated screen code in your code!

```kotlin
@Composable
fun Main(navController: ScreenNavController) { // val navController = rememberScreenNavController()
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
            // ...
        }
    }
}

```

## Setup

[![Release](https://jitpack.io/v/takuji31/navigation-compose-screen.svg)](https://jitpack.io/#takuji31/navigation-compose-screen)

Add JitPack to your repositories

```groovy
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' } // or maven("https://jitpack.io") in Kotlin DSL
    }
}
```

Apply kapt plugin

```groovy
plugins {
    id("com.android.library")
    id("kotlin-kapt") // or kotlin("kapt") in Kotlin DSL
}
```

Add dependencies

```groovy
dependencies {
    implementation("com.github.takuji31.navigation-compose-screen:navigation-compose-screen:+")
    kapt("com.github.takuji31.navigation-compose-screen:compiler:+")
}
```

