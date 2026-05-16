package com.example.bookcatalog.presentation.books

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookcatalog.data.local.SettingsManager
import com.example.bookcatalog.domain.model.Book
import com.example.bookcatalog.domain.repository.AuthRepository
import com.example.bookcatalog.domain.usecase.AddBookUseCase
import com.example.bookcatalog.domain.usecase.DeleteBookUseCase
import com.example.bookcatalog.domain.usecase.EditBookUseCase
import com.example.bookcatalog.domain.usecase.GetBooksUseCase
import com.example.bookcatalog.domain.usecase.SyncBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    getBooksUseCase: GetBooksUseCase,
    private val syncBooksUseCase: SyncBooksUseCase,
    private val addBookUseCase: AddBookUseCase,
    private val editBookUseCase: EditBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val authRepository: AuthRepository,
    private val settingsManager: SettingsManager //менеджер
) : ViewModel() {

    val books = getBooksUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    // ПЕРЕМЕННЫЕ ДЛЯ ПОИСКА
    var searchQuery = mutableStateOf("")
    var searchHistory = mutableStateOf(settingsManager.getSearchHistory())
    var isSearchLoading = mutableStateOf(false)
    var isSearchFailed = mutableStateOf(false)

    init { syncBooks() }

    fun syncBooks() {
        viewModelScope.launch {
            isLoading.value = true
            syncBooksUseCase()
            isLoading.value = false
        }
    }

    // НОВАЯ ФУНКЦИЯ ПОИСКА
    fun performSearch(query: String) {
        viewModelScope.launch {
            isSearchLoading.value = true
            isSearchFailed.value = false
            updateSearchQuery(query)

            delay(600) // Задержка, чтобы юзер увидел ProgressBar

            if (query.lowercase() == "ошибка") {
                // Слово для показа экрана с кнопкой "Обновить"
                isSearchFailed.value = true
            } else {
                settingsManager.saveSearchQuery(query)
                searchHistory.value = settingsManager.getSearchHistory()
            }

            isSearchLoading.value = false
        }
    }

    fun clearSearchHistory() {
        settingsManager.clearSearchHistory()
        searchHistory.value = emptyList()
    }

    fun saveBook(id: Int?, title: String, author: String, genre: String, rating: String, review: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val ratingInt = rating.toIntOrNull() ?: 0

            val result = if (id == null) {
                addBookUseCase(title, author, genre.takeIf { it.isNotBlank() }, ratingInt, review.takeIf { it.isNotBlank() })
            } else {
                editBookUseCase(id, title, author, genre.takeIf { it.isNotBlank() }, ratingInt, review.takeIf { it.isNotBlank() })
            }

            result.onSuccess { onSuccess() }.onFailure { errorMessage.value = it.message }
            isLoading.value = false
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            deleteBookUseCase(book.id).onFailure { errorMessage.value = "Не удалось удалить книгу" }
        }
    }

    fun getBookById(id: Int): Book? = books.value.find { it.id == id }
    fun updateSearchQuery(query: String) { searchQuery.value = query }
    fun logout() { authRepository.logout() }
}