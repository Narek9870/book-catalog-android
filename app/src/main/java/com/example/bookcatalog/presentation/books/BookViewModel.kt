package com.example.bookcatalog.presentation.books

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookcatalog.domain.model.Book
import com.example.bookcatalog.domain.repository.AuthRepository
import com.example.bookcatalog.domain.usecase.AddBookUseCase
import com.example.bookcatalog.domain.usecase.DeleteBookUseCase
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
    private val deleteBookUseCase: DeleteBookUseCase, // НОВОЕ
    private val authRepository: AuthRepository
) : ViewModel() {

    val books = getBooksUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    init {
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
                onSuccess()
            }.onFailure {
                errorMessage.value = it.message
            }
            isLoading.value = false
        }
    }

    // НОВОЕ: Функция удаления книги
    fun deleteBook(book: Book) {
        viewModelScope.launch {
            val result = deleteBookUseCase(book.id)
            result.onFailure {
                errorMessage.value = "Не удалось удалить книгу"
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}