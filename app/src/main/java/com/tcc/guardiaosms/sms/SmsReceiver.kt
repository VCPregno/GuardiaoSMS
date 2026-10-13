package com.tcc.guardiaosms.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.tcc.guardiaosms.GuardiaoSmsApplication
import com.tcc.guardiaosms.data.local.SmsRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Captura mensagens SMS recebidas via `SMS_RECEIVED_ACTION` (fluxo definido
 * nas diretrizes da banca, seção "Fluxo de uso": receiver → tokenização →
 * inferência local → notificação se acima do threshold).
 *
 * Usa `goAsync()` porque a inferência do modelo (mesmo local e quantizado)
 * não é instantânea, e um BroadcastReceiver comum é finalizado pelo sistema
 * assim que `onReceive` retorna. `goAsync()` estende esse tempo de vida o
 * suficiente para concluir a classificação, salvar no histórico e notificar,
 * sem precisar de um Service em primeiro plano nesta etapa do MVP.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Mensagens fragmentadas (concatenadas) chegam como múltiplas partes
        // com o mesmo remetente; juntamos o corpo antes de classificar.
        val sender = messages.first().originatingAddress ?: "Desconhecido"
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }

        val pendingResult = goAsync()
        val app = context.applicationContext as GuardiaoSmsApplication
        val container = app.container

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val currentThreshold = container.preferencesRepository.threshold.first()
                val result = container.classifierRepository.classify(fullBody)
                val isScam = result.isScam(currentThreshold)

                val recordId = container.database.smsRecordDao().insert(
                    SmsRecordEntity(
                        sender = sender,
                        body = fullBody,
                        receivedAtEpochMillis = System.currentTimeMillis(),
                        isScam = isScam,
                        scamProbability = result.scamProbability,
                        thresholdUsed = currentThreshold
                    )
                )

                if (isScam) {
                    val notificationsEnabled = container.preferencesRepository.notificationsEnabled.first()
                    if (notificationsEnabled) {
                        container.alertNotifier.notifyScamDetected(recordId, sender)
                    }
                }
            } catch (e: Exception) {
                // Uma exceção não tratada nesta corrotina derrubaria o processo.
                Log.e(TAG, "Falha ao classificar/registrar SMS recebido", e)
            } finally {
                // Sempre finalizar, mesmo em caso de erro, para não segurar o
                // broadcast indefinidamente (o sistema pode penalizar o app).
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "SmsReceiver"
    }
}
