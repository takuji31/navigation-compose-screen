package jp.takuji31.compose.navigation

interface Screen<out ID : Enum<*>> {
    val screenId: ID
    val route: String
    val parameterizedRoute: String
        get() = route
}
