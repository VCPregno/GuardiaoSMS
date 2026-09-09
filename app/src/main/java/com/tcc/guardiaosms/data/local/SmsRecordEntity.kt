package com.tcc.guardiaosms.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa uma mensagem SMS já analisada pelo classificador local.
 *
 * O histórico fica salvo apenas neste banco local (Room/SQLite) — nenhuma
 * mensagem é enviada para fora do dispositivo, mantendo a promessa de
 * privacidade que sustenta o uso do processamento on-device (decisão 16
 * do log de arquitetura).
 */
@Entity(tableName = "sms_records")
data class SmsRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val body: String,
    val receivedAtEpochMillis: Long,
    val isScam: Boolean,
    val scamProbability: Float,
    val thresholdUsed: Float
)
