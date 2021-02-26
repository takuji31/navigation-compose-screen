package jp.takuji31.compose.navigation.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import dagger.hilt.android.AndroidEntryPoint
import jp.takuji31.compose.navigation.example.ui.Main
import jp.takuji31.compose.navigation.example.ui.theme.NavigationComposeScreenGeneratorTheme
import jp.takuji31.compose.navigation.screen.rememberScreenNavController

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavigationComposeScreenGeneratorTheme {
                val navController = rememberScreenNavController()
                Surface(color = MaterialTheme.colors.background) {
                    Main(navController = navController)
                }
            }
        }
    }
}
