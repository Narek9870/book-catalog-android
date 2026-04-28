package com.example.bookcatalog.presentation.books

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookcatalog.domain.repository.AuthRepository
import com.example.bookcatalog.domain.usecase.AddBookUseCase
import com.example.bookcatalog.domain.usecase.GetBooksUseCase
import com.example.bookcatalog.domain.usecase.SyncBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    getBooksUseCase: GetBooksUseCase,
    private val syncBooksUseCase: SyncBooksUseCase,
    private val addBookUseCase: AddBookUseCase,
    private val authRepository: AuthRepository // для выхода из аккаунта
) : ViewModel() {

    // Автоматически получаем список книг из локальной БД
    val books = getBooksUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    init {
        // При открытии экрана сразу пытаемся стянуть новые данные с сервера
        syncBooks()
    }

    fun syncBooks() {
        viewModelScope.launch {
            isLoading.value = true
            syncBooksUseCase()
            isLoading.value = false
        }
    }

    fun addBook(title: String, author: String, genre: String, rating: String, review: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            val ratingInt = rating.toIntOrNull() ?: 0
            val result = addBookUseCase(title, author, genre.takeIf { it.isNotBlank() }, ratingInt, review.takeIf { it.isNotBlank() })

            result.onSuccess {
                onSuccess() // Закрываем экран добавления
            }.onFailure {
                errorMessage.value = it.message
            }
            isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
    }
}