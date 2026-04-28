package com.example.bookcatalog.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bookcatalog.data.local.TokenManager
import com.example.bookcatalog.presentation.auth.AuthScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем, есть ли уже сохраненный токен
        val isLoggedIn = tokenManager.getToken() != null

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()

                    // NavHost управляет экранами
                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) "home" else "auth"
                    ) {

                        // Экран авторизации
                        composable("auth") {
                            AuthScreen(onNavigateToHome = {
                                // Переходим на главный экран и очищаем историю (чтобы кнопка "Назад" не вернула на авторизацию)
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            })
                        }

                        // ВРЕМЕННЫЙ главный экран (Каталог мы сделаем на следующем шаге)
                        composable("home") {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "Добро пожаловать в Каталог Книг!", style = MaterialTheme.typography.headlineMedium)
                            }
                        }

                    }
                }
            }
        }
    }
}