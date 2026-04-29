package com.example.bookcatalog.presentation.books

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookcatalog.domain.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    onNavigateToAddBook: () -> Unit,
    onLogout: () -> Unit,
    viewModel: BookViewModel = hiltViewModel()
) {
    val books = viewModel.books.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои книги") },
                actions = {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Передаем ключ (book.id), чтобы список правильно анимировал удаление
            items(books.value, key = { it.id }) { book ->

                // Настраиваем логику свайпа
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.deleteBook(book)
                            true // Разрешаем смахивание
                        } else {
                            false // Отменяем смахивание в другую сторону
                        }
                    }
                )

                // Обертка для смахивания
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false, // Разрешаем свайп только справа налево
                    backgroundContent = {
                        val color by animateColorAsState(
                            targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent,
                            label = "color_anim"
                        )
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color, shape = CardDefaults.shape)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.White)
                        }
                    }
                ) {
                    // Сама карточка книги
                    BookCard(book)
                }
            }
        }
    }
}

@Composable
fun BookCard(book: Book) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = book.title, style = MaterialTheme.typography.titleLarge)
            Text(text = "Автор: ${book.author}", style = MaterialTheme.typography.bodyMedium)
            book.genre?.let { Text(text = "Жанр: $it", style = MaterialTheme.typography.bodySmall) }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Оценка: ${book.rating} / 5", color = MaterialTheme.colorScheme.secondary)
            book.review?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Отзыв: $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}