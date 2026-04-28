package com.example.bookcatalog.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class BookRequest(
    val title: String,
    val author: String,
    val genre: String? = null,
    val rating: Int,
    val review: String? = null
)

@Serializable
data class BookResponse(
    val id: Int,
    val title: String,
    val author: String,
    val genre: String? = null,
    val rating: Int,
    val review: String? = null
)