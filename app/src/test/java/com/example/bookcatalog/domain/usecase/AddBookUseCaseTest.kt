package com.example.bookcatalog.domain.usecase

import com.example.bookcatalog.domain.model.Book
import com.example.bookcatalog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddBookUseCaseTest {

    // Создаем "фейковый" репозиторий, чтобы не лезть в настоящий интернет во время тестов
    class FakeBookRepository : BookRepository {
        var isAddBookCalled = false

        override fun getBooks(): Flow<List<Book>> = emptyFlow()
        override suspend fun syncBooks(): Result<Unit> = Result.success(Unit)
        override suspend fun editBook(id: Int, title: String, author: String, genre: String?, rating: Int, review: String?): Result<Unit> = Result.success(Unit)
        override suspend fun deleteBook(id: Int): Result<Unit> = Result.success(Unit)

        override suspend fun addBook(title: String, author: String, genre: String?, rating: Int, review: String?): Result<Unit> {
            isAddBookCalled = true
            return Result.success(Unit)
        }
    }

    @Test
    fun `когда название пустое, должна возвращаться ошибка`() = runBlocking {
        val fakeRepo = FakeBookRepository()
        val useCase = AddBookUseCase(fakeRepo)

        val result = useCase(title = "", author = "Пушкин", genre = "Роман", rating = 5, review = null)

        assertTrue(result.isFailure)
        assertEquals("Название и автор обязательны", result.exceptionOrNull()?.message)
    }

    @Test
    fun `когда оценка больше 5, должна возвращаться ошибка`() = runBlocking {
        val fakeRepo = FakeBookRepository()
        val useCase = AddBookUseCase(fakeRepo)

        val result = useCase(title = "Капитанская дочка", author = "Пушкин", genre = "Роман", rating = 10, review = null)

        assertTrue(result.isFailure)
        assertEquals("Оценка должна быть от 1 до 5", result.exceptionOrNull()?.message)
    }

    @Test
    fun `когда данные правильные, книга должна сохраняться`() = runBlocking {
        val fakeRepo = FakeBookRepository()
        val useCase = AddBookUseCase(fakeRepo)

        val result = useCase(title = "Капитанская дочка", author = "Пушкин", genre = "Роман", rating = 5, review = "Супер")

        assertTrue(result.isSuccess)
        assertTrue(fakeRepo.isAddBookCalled)
    }
}