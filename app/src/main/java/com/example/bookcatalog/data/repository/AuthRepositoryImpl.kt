package com.example.bookcatalog.data.repository

import com.example.bookcatalog.data.local.TokenManager
import com.example.bookcatalog.data.remote.AuthResponse
import com.example.bookcatalog.data.remote.UserCredentials
import com.example.bookcatalog.domain.repository.AuthRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = httpClient.post("http://10.0.2.2:8080/login") {
                setBody(UserCredentials(email, password))
            }
            if (response.status.isSuccess()) {
                val authResponse: AuthResponse = response.body()
                tokenManager.saveToken(authResponse.token)
                Result.success(Unit)
            } else {
                val errorText = response.bodyAsText()
                Result.failure(Exception("Ошибка ${response.status.value}: $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Сбой сети: ${e.localizedMessage}"))
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            val response = httpClient.post("http://10.0.2.2:8080/register") {
                setBody(UserCredentials(email, password))
            }
            if (response.status.isSuccess()) {
                val authResponse: AuthResponse = response.body()
                tokenManager.saveToken(authResponse.token)
                Result.success(Unit)
            } else {
                val errorText = response.bodyAsText()
                Result.failure(Exception("Ошибка ${response.status.value}: $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Сбой сети: ${e.localizedMessage}"))
        }
    }

    override fun logout() {
        tokenManager.clearToken()
    }

    override fun isUserLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }
}