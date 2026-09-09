package com.tcc.guardiaosms.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.guardiaosms.data.preferences.DEFAULT_THRESHOLD
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
