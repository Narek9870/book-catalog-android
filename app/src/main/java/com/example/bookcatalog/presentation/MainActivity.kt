package com.example.bookcatalog.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookcatalog.data.local.SettingsManager
import com.example.bookcatalog.data.local.TokenManager
import com.example.bookcatalog.presentation.auth.AuthScreen
import com.example.bookcatalog.presentation.books.AddBookScreen
import com.example.bookcatalog.presentation.books.BookListScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var settingsManager: SettingsManager //менеджер настроек

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLoggedIn = tokenManager.getToken() != null

        setContent {
            // Читаем сохраненную тему из памяти
            val isDarkTheme = remember { mutableStateOf(settingsManager.isDarkTheme()) }

            // Функция для переключения темы
            val toggleTheme = {
                val newTheme = !isDarkTheme.value
                isDarkTheme.value = newTheme
                settingsManager.setDarkTheme(newTheme)
            }

            // Обертка темы
            MaterialTheme(
                colorScheme = if (isDarkTheme.value) darkColorScheme() else lightColorScheme()
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) "home" else "auth"
                    ) {
                        composable("auth") {
                            AuthScreen(onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            })
                        }

                        composable("home") {
                            BookListScreen(
                                isDarkTheme = isDarkTheme.value, // Передаем состояние темы
                                onThemeToggle = toggleTheme,     // Передаем функцию переключения
                                onNavigateToAddBook = { navController.navigate("add_edit_book") },
                                onNavigateToEditBook = { bookId -> navController.navigate("add_edit_book?bookId=$bookId") },
                                onLogout = {
                                    navController.navigate("auth") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "add_edit_book?bookId={bookId}",
                            arguments = listOf(navArgument("bookId") {
                                type = NavType.IntType
                                defaultValue = -1
                            })
                        ) { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getInt("bookId") ?: -1
                            AddBookScreen(
                                bookId = if (bookId == -1) null else bookId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}