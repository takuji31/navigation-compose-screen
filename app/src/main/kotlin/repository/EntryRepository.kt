package jp.takuji31.compose.navigation.example.repository

import jp.takuji31.compose.navigation.example.model.Blog
import jp.takuji31.compose.navigation.example.model.Entry
import kotlinx.coroutines.delay
import javax.inject.Inject

interface EntryRepository {
    suspend fun getEntryById(blodId: String, entryId: String): Entry
}

class EntryRepositoryImpl @Inject constructor() :
    EntryRepository {
    override suspend fun getEntryById(blodId: String, entryId: String): Entry {
        delay(1000L)
        return Blog.blogs.first { it.id == blodId }.entries.first { it.id == entryId }
    }
}
