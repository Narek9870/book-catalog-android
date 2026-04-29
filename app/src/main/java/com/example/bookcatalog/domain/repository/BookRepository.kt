package com.example.bookcatalog.domain.repository

import com.example.bookcatalog.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getBooks(): Flow<List<Book>>
    suspend fun syncBooks(): Result<Unit>
    suspend fun addBook(title: String, author: String, genre: String?, rating: Int, review: String?): Result<Unit>
    suspend fun editBook(id: Int, title: String, author: String, genre: String?, rating: Int, review: String?): Result<Unit> // НОВОЕ
    suspend fun deleteBook(id: Int): Result<Unit>
}