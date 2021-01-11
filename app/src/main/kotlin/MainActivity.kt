package jp.takuji31.compose.navigation.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import dagger.hilt.android.AndroidEntryPoint
import jp.takuji31.compose.navigation.example.ui.Main
import jp.takuji31.compose.navigation.example.ui.theme.NavigationComposeScreenGeneratorTheme
import jp.takuji31.compose.navigation.rememberScreenNavController

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavigationComposeScreenGeneratorTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberScreenNavController()
                    Main(navController = navController)
                }
            }
        }
    }
}
