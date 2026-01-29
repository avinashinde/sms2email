package com.example.sms2line.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Telephony
import com.example.sms2line.service.SmsForwarderService
import com.example.sms2line.storage.PreferencesManager
import com.example.sms2line.storage.SmtpCredentialStorage

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val preferencesManager = PreferencesManager(context)
        val smtpStorage = SmtpCredentialStorage(context)

        if (!preferencesManager.isForwardingEnabled) {
            return
        }

        if (!smtpStorage.hasValidConfig()) {
            return
        }

        // Acquire wake lock immediately to ensure processing completes
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SMS2Email::SmsReceiverWakeLock"
        )
        wakeLock.acquire(60 * 1000L) // 1 minute max

        try {
            val serviceIntent = Intent(context, SmsForwarderService::class.java).apply {
                action = SmsForwarderService.ACTION_FORWARD_SMS
                putExtras(intent.extras ?: return)
            }

            context.startForegroundService(serviceIntent)
        } finally {
            // Release wake lock after starting service
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}
