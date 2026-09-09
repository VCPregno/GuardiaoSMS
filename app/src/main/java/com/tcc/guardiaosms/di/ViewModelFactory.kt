package com.tcc.guardiaosms.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tcc.guardiaosms.ui.history.HistoryViewModel
import com.tcc.guardiaosms.ui.home.HomeViewModel
import com.tcc.guardiaosms.ui.settings.SettingsViewModel
import com.tcc.guardiaosms.ui.test.TestViewModel

/**
 * Factory manual (sem Hilt/Koin) que constrói cada ViewModel a partir do
 * [AppContainer]. Mesma decisão de simplicidade do próprio container: para
 * o tamanho deste projeto, um framework de DI adicionaria configuração sem
 * benefício proporcional.
 */
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(container.database.smsRecordDao()) as T

            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(container.database.smsRecordDao()) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(container.preferencesRepository) as T

            modelClass.isAssignableFrom(TestViewModel::class.java) ->
                TestViewModel(container.classifierRepository, container.preferencesRepository) as T

            else -> throw IllegalArgumentException("ViewModel desconhecida: ${modelClass.name}")
        }
    }
}
