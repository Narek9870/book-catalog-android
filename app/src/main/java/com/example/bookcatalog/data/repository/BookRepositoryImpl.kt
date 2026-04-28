package com.example.bookcatalog.data.repository

import com.example.bookcatalog.data.local.dao.BookDao
import com.example.bookcatalog.data.local.entity.BookEntity
import com.example.bookcatalog.data.remote.BookRequest
import com.example.bookcatalog.data.remote.BookResponse
import com.example.bookcatalog.domain.model.Book
import com.example.bookcatalog.domain.repository.BookRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepositoryImpl(
    private val httpClient: HttpClient,
    private val bookDao: BookDao
) : BookRepository {

    override fun getBooks(): Flow<List<Book>> {
        // Читаем из локальной БД и превращаем в Domain модели
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun syncBooks(): Result<Unit> {
        return try {
            // Идем на сервер за книгами
            val response = httpClient.get("http://10.0.2.2:8080/books")
            if (response.status.isSuccess()) {
                val remoteBooks: List<BookResponse> = response.body()

                // Превращаем в Entity и сохраняем в Room
                val entities = remoteBooks.map {
                    BookEntity(it.id, it.title, it.author, it.genre, it.rating, it.review)
                }
                bookDao.clearBooks() // Удаляем старые
                bookDao.insertBooks(entities) // Записываем свежие

                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка загрузки книг: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.localizedMessage}"))
        }
    }

    override suspend fun addBook(title: String, author: String, genre: String?, rating: Int, review: String?): Result<Unit> {
        return try {
            val request = BookRequest(title, author, genre, rating, review)
            val response = httpClient.post("http://10.0.2.2:8080/books") {
                setBody(request)
            }
            if (response.status.isSuccess()) {
                syncBooks() // Сразу после добавления скачиваем обновленный список
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка добавления: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка сети: ${e.localizedMessage}"))
        }
    }
}