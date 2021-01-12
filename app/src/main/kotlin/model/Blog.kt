package jp.takuji31.compose.navigation.example.model

data class Blog(
    val id: String,
    val title: String,
    val entries: List<Entry>,
) {
    companion object {
        val blogs: List<Blog> by lazy {
            (1..4).map { num ->
                val id = (num..num + 4).joinToString("")
                Blog(
                    id,
                    "takuji3${num}'s Blog",
                    listOf("first", "second", "latest")
                        .mapIndexed { index, keyword ->
                            Entry(
                                "${id}_${index + 1}",
                                "takuji3${num}'s Entry1",
                                "This is takuji3${num}'s $keyword Entry",
                            )
                        }
                        .reversed(),
                )
            }
        }
    }
}
