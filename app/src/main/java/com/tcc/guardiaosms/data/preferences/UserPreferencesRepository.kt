package com.tcc.guardiaosms.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "guardiao_sms_prefs")

/**
 * Threshold padrão definido nos experimentos do TCC 1 (0,3), calibrado para
 * priorizar recall da classe "golpe". A tela de configurações permite ajustar
 * esse valor, mas sempre volta a esse padrão em "restaurar padrão".
 */
const val DEFAULT_THRESHOLD = 0.3f

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THRESHOLD = floatPreferencesKey("scam_threshold")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val threshold: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.THRESHOLD] ?: DEFAULT_THRESHOLD
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    /** Tema escolhido nos Ajustes; [ThemeMode.SYSTEM] (segue o celular) por padrão. */
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.SYSTEM
    }

    suspend fun setThreshold(value: Float) {
        context.dataStore.edit { it[Keys.THRESHOLD] = value.coerceIn(0.05f, 0.95f) }
    }

    suspend fun resetThresholdToDefault() {
        context.dataStore.edit { it[Keys.THRESHOLD] = DEFAULT_THRESHOLD }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}
