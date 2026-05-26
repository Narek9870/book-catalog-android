package com.example.bookcatalog.domain.usecase

import com.example.bookcatalog.domain.model.Book
import com.example.bookcatalog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteBookUseCaseTest {

    class FakeBookRepository : BookRepository {
        var deletedBookId: Int? = null

        override fun getBooks(): Flow<List<Book>> = emptyFlow()
        override suspend fun syncBooks(): Result<Unit> = Result.success(Unit)
        override suspend fun addBook(title: String, author: String, genre: String?, rating: Int, review: String?): Result<Unit> = Result.success(Unit)
        override suspend fun editBook(id: Int, title: String, author: String, genre: String?, rating: Int, review: String?): Result<Unit> = Result.success(Unit)

        override suspend fun deleteBook(id: Int): Result<Unit> {
            deletedBookId = id
            return Result.success(Unit)
        }
    }

    @Test
    fun `DeleteBookUseCase - успешно вызывает удаление с правильным ID`() = runBlocking {
        val fakeRepo = FakeBookRepository()
        val useCase = DeleteBookUseCase(fakeRepo)

        val bookIdToDelete = 42
        val result = useCase(bookIdToDelete)

        // Проверяем, что запрос прошел успешно и передался правильный ID книги (42)
        assertTrue(result.isSuccess)
        assertTrue(fakeRepo.deletedBookId == 42)
    }
}