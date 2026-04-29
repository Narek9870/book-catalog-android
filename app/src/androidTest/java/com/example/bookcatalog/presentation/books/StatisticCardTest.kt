package com.example.bookcatalog.presentation.books

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class StatisticCardTest {

    // Правило для запуска Jetpack Compose в тестовом режиме
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun statisticCard_displaysCorrectTitleAndValue() {
        // Запускаем наш интерфейс в изоляции (как будто на экране)
        composeTestRule.setContent {
            StatisticCard(title = "Прочитано", value = "10 шт.")
        }

        // Проверяем, что текст реально появился и виден юзеру
        composeTestRule.onNodeWithText("Прочитано").assertIsDisplayed()
        composeTestRule.onNodeWithText("10 шт.").assertIsDisplayed()
    }
}