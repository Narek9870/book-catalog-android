package com.example.bookcatalog.di

import android.content.Context
import com.example.bookcatalog.data.local.TokenManager
import com.example.bookcatalog.data.repository.AuthRepositoryImpl
import com.example.bookcatalog.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        bookDao: com.example.bookcatalog.data.local.dao.BookDao,
        @ApplicationContext context: Context
    ): com.example.bookcatalog.domain.repository.BookRepository {
        return com.example.bookcatalog.data.repository.BookRepositoryImpl(bookDao, context)
    }
}