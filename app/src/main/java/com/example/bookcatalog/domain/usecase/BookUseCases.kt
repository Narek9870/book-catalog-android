package com.example.bookcatalog.domain.usecase

import com.example.bookcatalog.domain.model.Book
import com.example.bookcatalog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBooksUseCase @Inject constructor(private val repository: BookRepository) {
    operator fun invoke(): Flow<List<Book>> = repository.getBooks()
}

class SyncBooksUseCase @Inject constructor(private val repository: BookRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.syncBooks()
}

class AddBookUseCase @Inject constructor(private val repository: BookRepository) {
    suspend operator fun invoke(title: String, author: String, genre: String?, rating: Int, review: String?): Result<Unit> {
        if (title.isBlank() || author.isBlank()) {
            return Result.failure(Exception("Название и автор обязательны"))
        }
        if (rating !in 1..5) {
            return Result.failure(Exception("Оценка должна быть от 1 до 5"))
        }
        return repository.addBook(title, author, genre, rating, review)
    }
}