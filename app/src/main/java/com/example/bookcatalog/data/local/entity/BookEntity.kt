package com.example.bookcatalog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bookcatalog.domain.model.Book

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val author: String,
    val genre: String?,
    val rating: Int,
    val review: String?
) {
    // Функция для превращения локальной книги в ту, что понимает UI
    fun toDomainModel(): Book {
        return Book(id, title, author, genre, rating, review)
    }
}