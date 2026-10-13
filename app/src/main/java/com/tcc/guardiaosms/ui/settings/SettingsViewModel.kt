package com.tcc.guardiaosms.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.guardiaosms.data.preferences.DEFAULT_THRESHOLD
import com.tcc.guardiaosms.data.preferences.ThemeMode
import com.tcc.guardiaosms.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val threshold: StateFlow<Float> = preferencesRepository.threshold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_THRESHOLD)

    val notificationsEnabled: StateFlow<Boolean> = preferencesRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun onThresholdChanged(value: Float) {
        viewModelScope.launch { preferencesRepository.setThreshold(value) }
    }

    fun onResetThreshold() {
        viewModelScope.launch { preferencesRepository.resetThresholdToDefault() }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setNotificationsEnabled(enabled) }
    }
}
