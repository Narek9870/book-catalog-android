package com.example.bookcatalog.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class UserCredentials(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String
)