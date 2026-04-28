package com.example.bookcatalog.domain.usecase

import com.example.bookcatalog.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Поля не могут быть пустыми"))
        }
        return repository.login(email, password)
    }
}

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.length < 5) {
            return Result.failure(Exception("Email не может быть пустым, а пароль должен быть от 5 символов"))
        }
        return repository.register(email, password)
    }
}