package com.example.bookcatalog.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.bookcatalog.domain.repository.SettingsRepository

class SettingsRepositoryImpl(context: Context) : SettingsRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    override fun isDarkTheme(): Boolean = prefs.getBoolean("dark_theme", false)

    override fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("dark_theme", isDark).apply()
    }

    override fun getSearchHistory(): List<String> {
        val history = prefs.getString("search_history", "") ?: ""
        return if (history.isEmpty()) emptyList() else history.split("||")
    }

    override fun saveSearchQuery(query: String) {
        if (query.isBlank()) return
        val current = getSearchHistory().toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > 10) current.removeAt(current.lastIndex)
        prefs.edit().putString("search_history", current.joinToString("||")).apply()
    }

    override fun clearSearchHistory() {
        prefs.edit().remove("search_history").apply()
    }
}