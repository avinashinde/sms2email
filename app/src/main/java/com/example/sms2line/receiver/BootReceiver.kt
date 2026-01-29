package com.example.sms2line.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sms2line.service.SmsForwarderService
import com.example.sms2line.storage.PreferencesManager
import com.example.sms2line.storage.SmtpCredentialStorage
import com.example.sms2line.worker.EmailQueueManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val validActions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON"
        )

        if (intent.action !in validActions) {
            return
        }

        Log.d(TAG, "Boot completed, checking if service should start")

        val preferencesManager = PreferencesManager(context)
        val smtpStorage = SmtpCredentialStorage(context)

        if (preferencesManager.isForwardingEnabled && smtpStorage.hasValidConfig()) {
            Log.d(TAG, "Starting SMS forwarder service after boot")
            val serviceIntent = Intent(context, SmsForwarderService::class.java).apply {
                action = SmsForwarderService.ACTION_START_SERVICE
            }
            context.startForegroundService(serviceIntent)

            // Trigger immediate retry of any pending emails from the queue
            Log.d(TAG, "Scheduling email queue retry after boot")
            EmailQueueManager.scheduleImmediateRetry(context)
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
