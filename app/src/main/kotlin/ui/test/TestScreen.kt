package jp.takuji31.compose.navigation.example.ui.test

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.compose.NavHost
import jp.takuji31.compose.navigation.ScreenNavController
import jp.takuji31.compose.navigation.example.navigation.MyScreen
import jp.takuji31.compose.screengenerator.annotation.AutoScreenId
import jp.takuji31.compose.screengenerator.annotation.Route

@AutoScreenId("TestingScreen", screenBaseClass = MyScreen::class)
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
    NavHost(
        navController = navController.navController,
        startDestination = TestingScreenId.Home.route,
    ) {
        val onHomeButtonClick = { navController.navigate(TestingScreen.Subscreen) }
        val onSubScreenButton1234Click =
            { navController.navigate(TestingScreen.Parameterized("1234")) }
        val onSubScreenButton5678Click =
            { navController.navigate(TestingScreen.Parameterized("1234")) }
        testingscreenComposable {
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
            subscreen {
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
