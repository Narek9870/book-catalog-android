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
import androidx.navigation.compose.rememberNavController
import com.example.bookcatalog.domain.repository.SettingsRepository
import com.example.bookcatalog.data.local.TokenManager
import com.example.bookcatalog.presentation.navigation.BookCatalogNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var settingsManager: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Определяем стартовый экран до запуска UI
        val isLoggedIn = tokenManager.getToken() != null
        val startDestination = if (isLoggedIn) "home" else "auth"

        setContent {
            val isDarkTheme = remember { mutableStateOf(settingsManager.isDarkTheme()) }

            val toggleTheme = {
                val newTheme = !isDarkTheme.value
                isDarkTheme.value = newTheme
                settingsManager.setDarkTheme(newTheme)
            }

            MaterialTheme(
                colorScheme = if (isDarkTheme.value) darkColorScheme() else lightColorScheme()
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()

                    // Вся навигация теперь изолирована в отдельном месте
                    BookCatalogNavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        isDarkTheme = isDarkTheme.value,
                        onThemeToggle = toggleTheme
                    )

                }
            }
        }
    }
}