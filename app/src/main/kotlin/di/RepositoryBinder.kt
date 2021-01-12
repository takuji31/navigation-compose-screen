package jp.takuji31.compose.navigation.example.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.takuji31.compose.navigation.example.repository.BlogRepository
import jp.takuji31.compose.navigation.example.repository.BlogRepositoryImpl
import jp.takuji31.compose.navigation.example.repository.EntryRepository
import jp.takuji31.compose.navigation.example.repository.EntryRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBinder {
    @Binds
    abstract fun blogRepository(repository: BlogRepositoryImpl): BlogRepository

    @Binds
    abstract fun entryRepository(repository: EntryRepositoryImpl): EntryRepository
}
