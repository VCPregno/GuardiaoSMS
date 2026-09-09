package com.tcc.guardiaosms.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsRecordDao {

    @Insert
    suspend fun insert(record: SmsRecordEntity): Long

    // Flow: a tela de histórico observa mudanças automaticamente (Compose + StateFlow),
    // sem precisar de recarregamento manual quando uma nova mensagem chega.
    @Query("SELECT * FROM sms_records ORDER BY receivedAtEpochMillis DESC")
    fun observeAll(): Flow<List<SmsRecordEntity>>

    @Query("SELECT * FROM sms_records WHERE isScam = 1 ORDER BY receivedAtEpochMillis DESC LIMIT 1")
    fun observeLastScam(): Flow<SmsRecordEntity?>
}
