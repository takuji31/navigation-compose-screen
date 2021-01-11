package jp.takuji31.compose.navigation.example.repository

import jp.takuji31.compose.navigation.example.model.Blog
import kotlinx.coroutines.delay
import javax.inject.Inject

interface BlogRepository {
    suspend fun getAllBlogs(): List<Blog>
}

class BlogRepositoryImpl @Inject constructor() : BlogRepository {
    override suspend fun getAllBlogs(): List<Blog> {
        // Simulate network loading
        delay(3000L)
        return Blog.blogs
    }
}
