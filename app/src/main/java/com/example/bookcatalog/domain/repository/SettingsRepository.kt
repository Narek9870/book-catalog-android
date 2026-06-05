package com.example.bookcatalog.domain.repository

interface SettingsRepository {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
    fun getSearchHistory(): List<String>
    fun saveSearchQuery(query: String)
    fun clearSearchHistory()
}