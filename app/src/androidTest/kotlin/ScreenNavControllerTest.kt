package jp.takuji31.compose.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Expect
import jp.takuji31.compose.navigation.example.ui.test.TestingComposable
import jp.takuji31.compose.navigation.example.ui.test.TestingScreen
import jp.takuji31.compose.navigation.screen.ScreenNavController
import jp.takuji31.compose.navigation.screen.rememberScreenNavController
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenNavControllerTest {

    @get:Rule
    val testRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val expect: Expect = Expect.create()

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

    @Test
    fun currentScreen_startDestination() = runBlocking {
        lateinit var navController: ScreenNavController
        testRule.setContent {
            navController = rememberScreenNavController()
            TestingComposable(navController = navController)
        }
        testRule.awaitIdle()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(TestingScreen.Home)
    }

    @Test
    fun currentScreen_navigate() = runBlocking {
        lateinit var navController: ScreenNavController
        testRule.setContent {
            navController = rememberScreenNavController()
            TestingComposable(navController = navController)
        }
        testRule.awaitIdle()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            navController.navigate(TestingScreen.SubScreen)
        }

        testRule.awaitIdle()

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(TestingScreen.SubScreen)
    }

    @Test
    fun currentScreen_popBackStack_to_startDestination() = runBlocking {
        lateinit var navController: ScreenNavController
        testRule.setContent {
            navController = rememberScreenNavController()
            TestingComposable(navController = navController)
        }
        testRule.awaitIdle()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            navController.navigate(TestingScreen.SubScreen)
        }

        testRule.awaitIdle()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            navController.popBackStack()
        }

        expect
            .that(navController.currentScreen.value)
            .isEqualTo(TestingScreen.Home)
    }
}
