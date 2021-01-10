package jp.takuji31.compose.navigation

interface Screen<ID : Any> {
    val screenId: ID
    val route: String
}
