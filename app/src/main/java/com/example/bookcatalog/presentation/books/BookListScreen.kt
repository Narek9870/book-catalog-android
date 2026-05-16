package com.example.bookcatalog.presentation.books

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookcatalog.domain.model.Book
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    onNavigateToEditBook: (Int) -> Unit, // Функция для перехода на экран редактирования
    onLogout: () -> Unit,
    viewModel: BookViewModel = hiltViewModel()
) {
    val books by viewModel.books.collectAsState()
    val searchQuery = viewModel.searchQuery.value
    var searchActive by remember { mutableStateOf(false) }

    // Фильтруем книги по поиску
    val filteredBooks = if (searchQuery.isBlank()) {
        books
    } else {
        books.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.author.contains(searchQuery, ignoreCase = true)
        }
    }

    // Считаем статистику
    val totalBooks = books.size
    val avgRatingRaw = if (books.isNotEmpty()) books.map { it.rating }.average() else 0.0
    val avgRating = (round(avgRatingRaw * 10) / 10).toString() // Округляем до 1 знака

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои книги") },
                actions = {
                    // КНОПКА ТЕМЫ (Светлая / Темная)
                    IconButton(onClick = onThemeToggle) {
                        Text(if (isDarkTheme) "☀️" else "🌙", style = MaterialTheme.typography.titleLarge)
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Выйти")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddBook) {
                Icon(Icons.Default.Add, contentDescription = "Добавить книгу")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // БЛОК СТАТИСТИКИ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticCard("Прочитано", "$totalBooks шт.", Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                StatisticCard("Ср. Оценка", "$avgRating ⭐", Modifier.weight(1f))
            }

            // СТРОКА ПОИСКА (умный поиск с историей)
            // УМНЫЙ ПОИСК С ИСТОРИЕЙ (Требование методички)
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = {
                    searchActive = false
                    viewModel.performSearch(it)
                },
                active = searchActive,
                onActiveChange = { searchActive = it },
                placeholder = { Text("Поиск по названию или автору...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty() || searchActive) {
                        IconButton(onClick = {
                            if (searchQuery.isNotEmpty()) viewModel.updateSearchQuery("") else searchActive = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = if (searchActive) 0.dp else 16.dp)
            ) {
                val history = viewModel.searchHistory.value
                if (history.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("История поиска", style = MaterialTheme.typography.labelLarge)
                        TextButton(onClick = { viewModel.clearSearchHistory() }) { Text("Очистить") }
                    }
                    LazyColumn {
                        items(history) { item ->
                            ListItem(
                                headlineContent = { Text(item) },
                                leadingContent = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    searchActive = false
                                    viewModel.performSearch(item)
                                }
                            )
                        }
                    }
                } else {
                    Text("История пуста", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center)
                }
            }

            // ИНДИКАТОР ЗАГРУЗКИ ПОИСКА
            if (viewModel.isSearchLoading.value) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ОТОБРАЖЕНИЕ КОНТЕНТА: ошибки, пустой список или список книг
            // ПЛЕЙСХОЛДЕРЫ (В случае ошибки поиска или пустого списка)
            if (viewModel.isSearchFailed.value) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Не удалось завершить поиск", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.performSearch(searchQuery) }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Обновить")
                    }
                }
            } else if (filteredBooks.isEmpty() && !viewModel.isSearchLoading.value) {
                // СПИСОК КНИГ ИЛИ ПУСТОЙ ЭКРАН
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isBlank()) "Ваш каталог пуст.\nДобавьте первую книгу!" else "По вашему запросу\nничего не найдено.",
                        textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (!viewModel.isSearchLoading.value) {
                // СПИСОК КНИГ
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredBooks, key = { it.id }) { book ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) { viewModel.deleteBook(book); true } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState, enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color by animateColorAsState(targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent, label = "")
                                Box(Modifier.fillMaxSize().background(color, shape = CardDefaults.shape).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) { Icon(Icons.Default.Delete, tint = Color.White, contentDescription = null) }
                            }
                        ) {
                            // САМА КАРТОЧКА с передачей клика для редактирования
                            BookCard(book = book, onClick = { onNavigateToEditBook(book.id) })
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательный элемент для красивой статистики
@Composable
fun StatisticCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

// Одиночная карточка книги
@Composable
fun BookCard(book: Book, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) { // карточка кликабельная
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = book.title, style = MaterialTheme.typography.titleLarge)
            Text(text = "Автор: ${book.author}", style = MaterialTheme.typography.bodyMedium)
            book.genre?.let { Text(text = "Жанр: $it", style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Оценка: ${book.rating} / 5", color = MaterialTheme.colorScheme.secondary)
            book.review?.let { Spacer(modifier = Modifier.height(4.dp)); Text(text = "Отзыв: $it", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}