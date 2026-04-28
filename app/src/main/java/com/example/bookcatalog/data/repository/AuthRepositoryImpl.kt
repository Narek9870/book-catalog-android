package com.example.bookcatalog.data.repository

import com.example.bookcatalog.data.local.TokenManager
import com.example.bookcatalog.data.remote.AuthResponse
import com.example.bookcatalog.data.remote.UserCredentials
import com.example.bookcatalog.domain.repository.AuthRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = httpClient.post("login") {
                setBody(UserCredentials(email, password))
            }
            if (response.status.isSuccess()) {
                val authResponse: AuthResponse = response.body()
                tokenManager.saveToken(authResponse.token)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Неверный логин или пароль"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.localizedMessage}"))
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            val response = httpClient.post("register") {
                setBody(UserCredentials(email, password))
            }
            if (response.status.isSuccess()) {
                val authResponse: AuthResponse = response.body()
                tokenManager.saveToken(authResponse.token)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Пользователь уже существует или ошибка сервера"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.localizedMessage}"))
        }
    }

    override fun logout() {
        tokenManager.clearToken()
    }

    override fun isUserLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }
}