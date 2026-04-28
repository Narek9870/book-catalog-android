package com.example.bookcatalog.di

import android.content.Context
import com.example.bookcatalog.data.local.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideHttpClient(tokenManager: TokenManager): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
            // Этот блок выполняется перед КАЖДЫМ запросом
            defaultRequest {
                // Адрес твоего локального сервера на компе для эмулятора:
                url("http://10.0.2.2:8080/")
                contentType(ContentType.Application.Json)

                // Достаем токен. Если он есть - цепляем в заголовок!
                val token = tokenManager.getToken()
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
            }
        }
    }
}