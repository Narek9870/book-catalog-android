package com.example.bookcatalog.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bookcatalog.data.local.SyncWorker
import com.example.bookcatalog.data.local.dao.BookDao
import com.example.bookcatalog.data.local.entity.BookEntity
import com.example.bookcatalog.domain.model.Book
import com.example.bookcatalog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val context: Context
) : BookRepository {

    override fun getBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities -> entities.map { it.toDomainModel() } }
    }

    // Запуск фонового работника (УМНЫЙ ВАРИАНТ)
    private fun enqueueSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        // Запускаем уникальную задачу, чтобы не было "клонов" при добавлении книг
        WorkManager.getInstance(context).enqueueUniqueWork(
            "UniqueSyncWork",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    override suspend fun syncBooks(): Result<Unit> {
        enqueueSync()
        return Result.success(Unit)
    }

    override suspend fun addBook(title: String, author: String, genre: String?, rating: Int, review: String?): Result<Unit> {
        val tempId = -(System.currentTimeMillis() % 100000).toInt()
        val entity = BookEntity(tempId, title, author, genre, rating, review, "ADD")

        bookDao.insertBook(entity) // Сохраняем локально
        enqueueSync() // Пинаем воркера
        return Result.success(Unit)
    }

    override suspend fun editBook(id: Int, title: String, author: String, genre: String?, rating: Int, review: String?): Result<Unit> {
        val entity = BookEntity(id, title, author, genre, rating, review, "EDIT")
        bookDao.insertBook(entity)
        enqueueSync()
        return Result.success(Unit)
    }

    override suspend fun deleteBook(id: Int): Result<Unit> {
        val existingBook = bookDao.getBookById(id)
        if (existingBook != null) {
            // Копируем книгу, меняя ей статус на "УДАЛИТЬ"
            val entity = existingBook.copy(syncAction = "DELETE")
            bookDao.insertBook(entity)
            enqueueSync()
        }
        return Result.success(Unit)
    }
}