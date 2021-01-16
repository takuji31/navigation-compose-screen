package jp.takuji31.compose.navigation.screen

interface Screen<out ID : Enum<*>> {
    val screenId: ID
    val route: String
    val parameterizedRoute: String
        get() = route
}
