package com.tcc.guardiaosms.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.guardiaosms.data.local.SmsRecordDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    smsRecordDao: SmsRecordDao
) : ViewModel() {

    val lastScamRecord: StateFlow<com.tcc.guardiaosms.data.local.SmsRecordEntity?> =
        smsRecordDao.observeLastScam()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
