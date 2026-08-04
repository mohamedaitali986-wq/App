package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the current logged-in user session with persistence across app restarts.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("shaghaf_session_prefs", Context.MODE_PRIVATE)

    private val _currentUserId = MutableStateFlow<Long?>(getCurrentUserIdFromPrefs())
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    private val _appLanguage = MutableStateFlow<String>(getLanguageFromPrefs())
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private fun getCurrentUserIdFromPrefs(): Long? {
        val id = prefs.getLong(KEY_USER_ID, -1L)
        return if (id != -1L) id else null
    }

    private fun getLanguageFromPrefs(): String {
        return prefs.getString(KEY_LANGUAGE, "ar") ?: "ar"
    }

    fun saveSession(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
        _currentUserId.value = userId
    }

    fun clearSession() {
        prefs.edit().remove(KEY_USER_ID).apply()
        _currentUserId.value = null
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _appLanguage.value = lang
    }

    companion object {
        private const val KEY_USER_ID = "active_user_id"
        private const val KEY_LANGUAGE = "app_language"
    }
}
