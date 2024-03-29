package jp.takuji31.compose.navigation.example.ui.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import jp.takuji31.compose.navigation.screen.ScreenNavController
import jp.takuji31.compose.navigation.screen.ScreenNavHost
import jp.takuji31.compose.navigation.screen.annotation.Route
import jp.takuji31.compose.navigation.screen.annotation.ScreenId

@ScreenId("TestingScreen")
enum class TestingScreenId {
    @Route("/")
    Home,

    @Route("/subscreen")
    SubScreen,

    @Route("/parameterized/{id}")
    Parameterized
}

@Composable
fun TestingComposable(navController: ScreenNavController) {
    ScreenNavHost(
        navController = navController,
        startScreen = TestingScreen.Home,
    ) {
        val onHomeButtonClick = { navController.navigate(TestingScreen.SubScreen) }
        val onSubScreenButton1234Click =
            { navController.navigate(TestingScreen.Parameterized("1234")) }
        val onSubScreenButton5678Click =
            { navController.navigate(TestingScreen.Parameterized("1234")) }
        testingScreenComposable {
            home {
                Column {
                    Text(
                        text = "This is home",
                        Modifier.semantics { contentDescription = "Home Label" },
                    )
                    Button(
                        onClick = onHomeButtonClick,
                        Modifier.semantics { contentDescription = "Home Button" },
                    ) {
                        Text(text = "Goto SubScreen")
                    }
                }
            }
            subScreen {
                Column {
                    Text(
                        text = "This is SubScreen",
                        Modifier.semantics { contentDescription = "SubScreen Label" },
                    )
                    Button(
                        onClick = onSubScreenButton1234Click,
                        Modifier.semantics { contentDescription = "SubScreen Button 1234" },
                    ) {
                        Text(text = "Goto Parameterized Button 1234")
                    }
                    Button(
                        onClick = onSubScreenButton5678Click,
                        Modifier.semantics { contentDescription = "SubScreen Button 5678" },
                    ) {
                        Text(text = "Goto Parameterized Button 5678")
                    }
                }
            }
            parameterized {
                Column {
                    Text(
                        text = "This is Parameterized",
                        Modifier.semantics { contentDescription = "SubScreen Label" },
                    )
                    Text(
                        text = it.id,
                        Modifier.semantics { contentDescription = "ID Label" },
                    )
                }
            }
        }
    }
}
