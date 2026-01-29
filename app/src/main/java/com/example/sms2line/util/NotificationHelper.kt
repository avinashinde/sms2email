package com.example.sms2line.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.sms2line.MainActivity
import com.example.sms2line.R

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            "SMS Forwarding Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notification for SMS forwarding background service"
            setShowBadge(false)
        }

        val statusChannel = NotificationChannel(
            CHANNEL_STATUS,
            "Forwarding Status",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications about SMS forwarding status"
        }

        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(statusChannel)
    }

    fun createServiceNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setContentTitle("SMS2Email Active")
            .setContentText("Forwarding SMS messages to email")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun showForwardSuccessNotification(sender: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setContentTitle("SMS Forwarded")
            .setContentText("Message from $sender sent via email")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_STATUS, notification)
    }

    fun showForwardErrorNotification(error: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setContentTitle("Forwarding Failed")
            .setContentText(error)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_ERROR, notification)
    }

    fun showQueuedNotification(sender: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setContentTitle("SMS Queued")
            .setContentText("Message from $sender will be sent when online")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_QUEUED, notification)
    }

    fun showQueueStatusNotification(pendingCount: Int, failedCount: Int) {
        if (pendingCount == 0 && failedCount == 0) {
            notificationManager.cancel(NOTIFICATION_ID_QUEUE_STATUS)
            return
        }

        val text = buildString {
            if (pendingCount > 0) {
                append("$pendingCount pending")
            }
            if (failedCount > 0) {
                if (pendingCount > 0) append(", ")
                append("$failedCount failed")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setContentTitle("Email Queue")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setOngoing(pendingCount > 0)
            .build()

        notificationManager.notify(NOTIFICATION_ID_QUEUE_STATUS, notification)
    }

    companion object {
        const val CHANNEL_SERVICE = "sms2line_service"
        const val CHANNEL_STATUS = "sms2line_status"
        const val NOTIFICATION_ID_SERVICE = 1
        const val NOTIFICATION_ID_STATUS = 2
        const val NOTIFICATION_ID_ERROR = 3
        const val NOTIFICATION_ID_QUEUED = 4
        const val NOTIFICATION_ID_QUEUE_STATUS = 5
    }
}
