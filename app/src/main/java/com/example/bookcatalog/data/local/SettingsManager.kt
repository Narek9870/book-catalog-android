package com.example.bookcatalog.data.local

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    //ТЕМНАЯ ТЕМА
    fun isDarkTheme(): Boolean = prefs.getBoolean("dark_theme", false)

    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("dark_theme", isDark).apply()
    }

    //ИСТОРИЯ ПОИСКА
    fun getSearchHistory(): List<String> {
        val history = prefs.getString("search_history", "") ?: ""
        return if (history.isEmpty()) emptyList() else history.split("||")
    }

    fun saveSearchQuery(query: String) {
        if (query.isBlank()) return
        val current = getSearchHistory().toMutableList()
        current.remove(query) // Удаляем дубликат, если такой запрос уже был
        current.add(0, query) // Добавляем новый запрос в самый верх
        if (current.size > 10) current.removeLast() // Храним строго не больше 10 элементов
        prefs.edit().putString("search_history", current.joinToString("||")).apply()
    }

    fun clearSearchHistory() {
        prefs.edit().remove("search_history").apply()
    }
}