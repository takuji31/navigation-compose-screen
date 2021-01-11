package jp.takuji31.compose.navigation.example.model

data class Blog(
    val id: String,
    val title: String,
    val entries: List<Entry>,
) {
    companion object {
        val blogs = listOf(
            Blog(
                "1234",
                "takuji31's Blog",
                listOf(
                ),
            ),
            Blog(
                "2345",
                "takuji32's Blog",
                listOf(
                ),
            ),
            Blog(
                "3456",
                "takuji33's Blog",
                listOf(
                ),
            ),
            Blog(
                "5678",
                "takuji34's Blog",
                listOf(
                ),
            ),
        )
    }
}
