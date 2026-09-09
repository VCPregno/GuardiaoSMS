package com.tcc.guardiaosms.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.guardiaosms.data.local.SmsRecordDao
import com.tcc.guardiaosms.data.local.SmsRecordEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    smsRecordDao: SmsRecordDao
) : ViewModel() {

    val records: StateFlow<List<SmsRecordEntity>> =
        smsRecordDao.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
