package jp.takuji31.compose.navigation.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Providers
import androidx.compose.ui.platform.setContent
import dagger.hilt.android.AndroidEntryPoint
import jp.takuji31.compose.navigation.example.ui.Main
import jp.takuji31.compose.navigation.example.ui.theme.NavigationComposeScreenGeneratorTheme
import jp.takuji31.compose.navigation.screen.rememberScreenNavController
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var hiltDependencies: HiltDependencies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavigationComposeScreenGeneratorTheme {
                val navController = rememberScreenNavController()
                Providers(
                    AmbientApplication provides application,
                    AmbientNavController provides navController.navController,
                ) {
                    ProvideHiltViewModelFactoryParams(hiltDependencies) {
                        Surface(color = MaterialTheme.colors.background) {
                            Main(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
