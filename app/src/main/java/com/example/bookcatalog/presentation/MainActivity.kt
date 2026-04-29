package com.example.bookcatalog.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookcatalog.data.local.TokenManager
import com.example.bookcatalog.presentation.auth.AuthScreen
import com.example.bookcatalog.presentation.books.AddBookScreen
import com.example.bookcatalog.presentation.books.BookListScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLoggedIn = tokenManager.getToken() != null

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) "home" else "auth"
                    ) {
                        // ЭКРАН АВТОРИЗАЦИИ
                        composable("auth") {
                            AuthScreen(onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            })
                        }

                        // ГЛАВНЫЙ ЭКРАН (СПИСОК КНИГ)
                        composable("home") {
                            BookListScreen(
                                onNavigateToAddBook = { navController.navigate("add_edit_book") },
                                onNavigateToEditBook = { bookId -> navController.navigate("add_edit_book?bookId=$bookId") },
                                onLogout = {
                                    navController.navigate("auth") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // ЭКРАН СОЗДАНИЯ И РЕДАКТИРОВАНИЯ КНИГИ
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