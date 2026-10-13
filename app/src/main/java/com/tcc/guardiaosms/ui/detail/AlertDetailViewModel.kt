package com.tcc.guardiaosms.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.guardiaosms.data.local.SmsRecordDao
import com.tcc.guardiaosms.data.local.SmsRecordEntity
import com.tcc.guardiaosms.risk.RiskLevel
import com.tcc.guardiaosms.tips.AttentionPoint
import com.tcc.guardiaosms.tips.AttentionPointsDetector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AlertDetailUiState {
    data object Loading : AlertDetailUiState
    data object NotFound : AlertDetailUiState
    data class Loaded(
        val record: SmsRecordEntity,
        val riskLevel: RiskLevel,
        val attentionPoints: Set<AttentionPoint>
    ) : AlertDetailUiState
}

class AlertDetailViewModel(
    smsRecordDao: SmsRecordDao,
    recordId: Long
) : ViewModel() {

    val uiState: StateFlow<AlertDetailUiState> = smsRecordDao.observeById(recordId)
        .map { record ->
            if (record == null) {
                AlertDetailUiState.NotFound
            } else {
                AlertDetailUiState.Loaded(
                    record = record,
                    riskLevel = RiskLevel.fromRecord(record.isScam, record.scamProbability, record.thresholdUsed),
                    attentionPoints = AttentionPointsDetector.detect(record.body)
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlertDetailUiState.Loading)
}
