package com.example.bookcatalog.presentation.books

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookcatalog.domain.model.Book
import com.example.bookcatalog.domain.usecase.AddBookUseCase
import com.example.bookcatalog.domain.usecase.ClearSearchHistoryUseCase
import com.example.bookcatalog.domain.usecase.DeleteBookUseCase
import com.example.bookcatalog.domain.usecase.EditBookUseCase
import com.example.bookcatalog.domain.usecase.GetBooksUseCase
import com.example.bookcatalog.domain.usecase.GetSearchHistoryUseCase
import com.example.bookcatalog.domain.usecase.LogoutUseCase
import com.example.bookcatalog.domain.usecase.SaveSearchQueryUseCase
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
    private val logoutUseCase: LogoutUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val saveSearchQueryUseCase: SaveSearchQueryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel() {

    val books = getBooksUseCase().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    // ПЕРЕМЕННЫЕ ДЛЯ ПОИСКА
    var searchQuery = mutableStateOf("")
    var searchHistory = mutableStateOf(getSearchHistoryUseCase())
    var isSearchLoading = mutableStateOf(false)
    var isSearchFailed = mutableStateOf(false)

    // ФИЛЬТР ПО ЖАНРАМ
    val availableGenres = listOf("Фантастика", "Детектив", "Роман", "Бизнес", "Психология", "Наука", "Классика", "Фэнтези", "Другое")
    var selectedGenreFilter = mutableStateOf("Все жанры")

    init { syncBooks() }

    fun syncBooks() {
        viewModelScope.launch {
            isLoading.value = true
            syncBooksUseCase()
            isLoading.value = false
        }
    }

    // ФУНКЦИЯ ПОИСКА
    fun performSearch(query: String) {
        viewModelScope.launch {
            isSearchLoading.value = true
            isSearchFailed.value = false
            updateSearchQuery(query)

            delay(600) // Задержка, чтобы юзер увидел ProgressBar

            if (query.lowercase() == "ошибка") {
                isSearchFailed.value = true
            } else {
                saveSearchQueryUseCase(query)
                searchHistory.value = getSearchHistoryUseCase()
            }

            isSearchLoading.value = false
        }
    }

    fun clearSearchHistory() {
        clearSearchHistoryUseCase()
        searchHistory.value = emptyList()
    }

    fun updateGenreFilter(genre: String) {
        selectedGenreFilter.value = genre
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

    fun logout() { logoutUseCase() }
}