package com.example.bookcatalog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookcatalog.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    // Не показываем пользователю книги, которые он "удалил" в оффлайне
    @Query("SELECT * FROM books WHERE syncAction != 'DELETE' ORDER BY id DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    // Найти все книги, которые ждут отправки на сервер
    @Query("SELECT * FROM books WHERE syncAction != 'NONE'")
    suspend fun getUnsyncedBooks(): List<BookEntity>

    // Найти конкретную книгу по ID
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: Int): BookEntity?

    // Сохранить одну книгу (для оффлайн добавления/изменения)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Query("DELETE FROM books")
    suspend fun clearBooks()

    //Удаление одной книги локально
    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: Int)
}