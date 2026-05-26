package com.example.bookcatalog.domain.usecase

import com.example.bookcatalog.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {

    // Создаем фейковый репозиторий авторизации, который никуда не отправляет данные, а просто проверяет, правильно ли работает логика внутри приложения.
    class FakeAuthRepository : AuthRepository {
        var isLoginCalled = false
        var isRegisterCalled = false

        override suspend fun login(email: String, password: String): Result<Unit> {
            isLoginCalled = true
            return Result.success(Unit)
        }

        override suspend fun register(email: String, password: String): Result<Unit> {
            isRegisterCalled = true
            return Result.success(Unit)
        }

        override fun logout() {}
        override fun isUserLoggedIn(): Boolean = false
    }

    @Test
    fun `LoginUseCase - когда email пустой, возвращается ошибка`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        val useCase = LoginUseCase(fakeRepo)

        // Пытаемся войти с пустым email
        val result = useCase(email = "", password = "password123")

        assertTrue(result.isFailure)
        assertEquals("Поля не могут быть пустыми", result.exceptionOrNull()?.message)
    }

    @Test
    fun `RegisterUseCase - когда пароль короче 5 символов, возвращается ошибка`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        val useCase = RegisterUseCase(fakeRepo)

        // Пытаемся зарегистрироваться со слишком коротким паролем
        val result = useCase(email = "test@mail.ru", password = "123")

        assertTrue(result.isFailure)
        assertEquals("Email не может быть пустым, а пароль должен быть от 5 символов", result.exceptionOrNull()?.message)
    }

    @Test
    fun `LoginUseCase - при правильных данных успешно вызывается репозиторий`() = runBlocking {
        val fakeRepo = FakeAuthRepository()
        val useCase = LoginUseCase(fakeRepo)

        val result = useCase(email = "test@mail.ru", password = "password123")

        assertTrue(result.isSuccess)
        assertTrue(fakeRepo.isLoginCalled)
    }
}