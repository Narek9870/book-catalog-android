package com.example.bookcatalog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bookcatalog.data.local.dao.BookDao
import com.example.bookcatalog.data.local.entity.BookEntity

@Database(entities = [BookEntity::class], version = 1, exportSchema = false)
abstract class BookDatabase : RoomDatabase() {
    abstract val bookDao: BookDao
}