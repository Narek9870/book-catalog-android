package com.example.bookcatalog.domain.model

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val rating: Int,
    val review: String
)