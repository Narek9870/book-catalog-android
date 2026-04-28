package com.example.bookcatalog.di

import com.example.bookcatalog.data.local.TokenManager
import com.example.bookcatalog.data.repository.AuthRepositoryImpl
import com.example.bookcatalog.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        httpClient: HttpClient,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepositoryImpl(httpClient, tokenManager)
    }
    @Provides
    @Singleton
    fun provideBookRepository(
        httpClient: HttpClient,
        bookDao: com.example.bookcatalog.data.local.dao.BookDao
    ): com.example.bookcatalog.domain.repository.BookRepository {
        return com.example.bookcatalog.data.repository.BookRepositoryImpl(httpClient, bookDao)
    }
}