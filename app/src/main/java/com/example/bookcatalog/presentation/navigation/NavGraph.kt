package com.example.bookcatalog.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bookcatalog.presentation.auth.AuthScreen
import com.example.bookcatalog.presentation.books.AddBookScreen
import com.example.bookcatalog.presentation.books.BookListScreen

@Composable
fun BookCatalogNavGraph(
    navController: NavHostController,
    startDestination: String,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
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
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
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