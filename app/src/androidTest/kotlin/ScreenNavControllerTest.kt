package jp.takuji31.compose.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.takuji31.compose.navigation.example.ui.test.TestingComposable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenNavControllerTest {

    @get:Rule
    val testRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun init() {
        testRule.setContent {
            val navController = rememberScreenNavController()
            TestingComposable(navController = navController)
        }

        testRule.onNodeWithContentDescription("Home Label").assertExists()
    }

    @Test
    fun navigateTo() {
        testRule.setContent {
            val navController = rememberScreenNavController()
            TestingComposable(navController = navController)
        }
        testRule.onNodeWithContentDescription("Home Button").performClick()

        testRule.onNodeWithContentDescription("SubScreen Label").assertExists()

        testRule.onNodeWithContentDescription("SubScreen Button 1234").performClick()

        testRule.onNodeWithText("1234").assertExists()
    }
}
