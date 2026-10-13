package com.tcc.guardiaosms.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tcc.guardiaosms.MainActivity
import com.tcc.guardiaosms.R

private const val CHANNEL_ID = "scam_alerts"
private const val NOTIFICATION_ID_BASE = 1000

class AlertNotifier(private val context: Context) {

    init {
        // Canais de notificação são obrigatórios a partir do Android 8 (API 26),
        // que é o nosso minSdk — por isso não há necessidade de checagem de versão aqui.
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /**
     * Notifica um SMS com "Risco alto". O texto é curto e NÃO inclui o corpo da
     * mensagem (que também ficaria visível na tela de bloqueio); tocar abre o
     * detalhe do alerta com o guia "O que fazer".
     *
     * @param recordId id da mensagem no histórico (Room), usado para abrir o detalhe.
     */
    fun notifyScamDetected(recordId: Long, sender: String) {
        val openDetail = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_RECORD_ID, recordId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        // requestCode = recordId: com requestCode repetido, o Android reaproveitaria
        // o PendingIntent (imutável) da primeira notificação, e um segundo alerta
        // abriria a mensagem errada.
        val pendingIntent = PendingIntent.getActivity(
            context,
            recordId.toInt(),
            openDetail,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val body = context.getString(R.string.notification_body)

        // POST_NOTIFICATIONS (Android 13+) precisa ser concedida em runtime pela
        // MainActivity; se não concedida, a chamada abaixo simplesmente não notifica.
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSubText(sender)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_BASE + recordId.toInt(), notification)
    }
}
