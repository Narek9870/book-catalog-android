package com.example.bookcatalog.presentation.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    bookId: Int?, // null = новая книга, число = старая книга
    onNavigateBack: () -> Unit,
    viewModel: BookViewModel = hiltViewModel()
) {
    // Подписываемся на список книг из ViewModel
    val books by viewModel.books.collectAsState()

    // Ищем нужную книгу по ID
    val existingBook = books.find { it.id == bookId }

    // Переменные состояния для полей ввода (изначально пустые)
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var review by remember { mutableStateOf("") }

    // НОВОЕ: Как только мы находим старую книгу, мгновенно заполняем все поля её данными!
    LaunchedEffect(existingBook) {
        if (existingBook != null) {
            title = existingBook.title
            author = existingBook.author
            genre = existingBook.genre ?: ""
            rating = existingBook.rating.toString()
            review = existingBook.review ?: ""
        }
    }

    val screenTitle = if (bookId == null) "Добавить книгу" else "Редактировать книгу"

    Scaffold(
        topBar = { TopAppBar(title = { Text(screenTitle) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Автор *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Жанр") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = rating,
                onValueChange = { rating = it },
                label = { Text("Оценка (1-5) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(value = review, onValueChange = { review = it }, label = { Text("Отзыв") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

            if (viewModel.errorMessage.value != null) {
                Text(text = viewModel.errorMessage.value!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveBook(bookId, title, author, genre, rating, review, onSuccess = onNavigateBack) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !viewModel.isLoading.value
            ) {
                if (viewModel.isLoading.value) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (bookId == null) "Сохранить" else "Обновить")
                }
            }
        }
    }
}