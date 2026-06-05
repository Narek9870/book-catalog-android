package com.example.bookcatalog.data.local

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bookcatalog.data.local.dao.BookDao
import com.example.bookcatalog.data.local.entity.BookEntity
import com.example.bookcatalog.data.remote.BookRequest
import com.example.bookcatalog.data.remote.BookResponse
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val bookDao: BookDao,
    private val httpClient: HttpClient
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedBooks = bookDao.getUnsyncedBooks()

            // Отправляем все долги на сервер
            for (book in unsyncedBooks) {
                val request = BookRequest(book.title, book.author, book.genre, book.rating, book.review)
                when (book.syncAction) {
                    "ADD" -> httpClient.post("books") { setBody(request) }
                    "EDIT" -> httpClient.put("books/${book.id}") { setBody(request) }
                    "DELETE" -> httpClient.delete("books/${book.id}")
                }
            }

            // Скачиваем свежий список с реальными ID сервера
            val response = httpClient.get("books")
            if (response.status.isSuccess()) {
                val remoteBooks: List<BookResponse> = response.body()
                val entities = remoteBooks.map {
                    BookEntity(it.id, it.title, it.author, it.genre, it.rating, it.review, "NONE")
                }
                // Полностью обновляем локальную базу
                bookDao.clearBooks()
                bookDao.insertBooks(entities)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry() // Если снова нет сети, worker попробоует снова
        }
    }
}