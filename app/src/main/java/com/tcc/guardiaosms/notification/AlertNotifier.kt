package com.tcc.guardiaosms.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

    fun notifyScamDetected(sender: String, messagePreview: String) {
        // POST_NOTIFICATIONS (Android 13+) precisa ser concedida em runtime pela
        // MainActivity; se não concedida, a chamada abaixo simplesmente não notifica.
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(
                context.getString(R.string.notification_body_prefix, sender, messagePreview.take(80))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_BASE + System.currentTimeMillis().toInt(), notification)
    }
}
