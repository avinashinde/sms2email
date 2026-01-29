package com.example.sms2line.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.example.sms2line.email.EmailResult
import com.example.sms2line.email.SmtpEmailClient
import com.example.sms2line.storage.ForwardedSmsTracker
import com.example.sms2line.storage.PreferencesManager
import com.example.sms2line.storage.SmtpCredentialStorage
import com.example.sms2line.util.MessageFormatter
import com.example.sms2line.util.NotificationHelper
import com.example.sms2line.util.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SmsForwarderService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var smtpStorage: SmtpCredentialStorage
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var smsTracker: ForwardedSmsTracker
    private lateinit var emailClient: SmtpEmailClient
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        smtpStorage = SmtpCredentialStorage(this)
        preferencesManager = PreferencesManager(this)
        smsTracker = ForwardedSmsTracker(this)
        emailClient = SmtpEmailClient()

        // Acquire wake lock to keep CPU running
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SMS2Email::ForwarderWakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L) // 10 minutes max
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NotificationHelper.NOTIFICATION_ID_SERVICE,
            notificationHelper.createServiceNotification()
        )

        when (intent?.action) {
            ACTION_FORWARD_SMS -> handleSmsForwarding(intent)
            ACTION_START_SERVICE -> Log.d(TAG, "Service started")
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun handleSmsForwarding(intent: Intent) {
        val messages = SmsParser.parseSmsFromIntent(intent)

        if (messages.isEmpty()) {
            Log.w(TAG, "No messages parsed from intent")
            return
        }

        val emailConfig = smtpStorage.getConfig()
        if (!emailConfig.isValid()) {
            Log.e(TAG, "SMTP not configured")
            notificationHelper.showForwardErrorNotification("Email not configured")
            return
        }

        val forwardingConfig = preferencesManager.getForwardingConfig("")

        for (sms in messages) {
            // Check if already forwarded
            if (smsTracker.isAlreadyForwarded(sms)) {
                Log.d(TAG, "SMS already forwarded, skipping: ${sms.sender}")
                continue
            }

            // Mark as forwarded immediately to prevent duplicates
            smsTracker.markAsForwarded(sms)

            serviceScope.launch {
                val subject = MessageFormatter.formatSubject(sms)
                val body = MessageFormatter.formatEmailBody(sms, forwardingConfig)

                Log.d(TAG, "Forwarding SMS from ${sms.sender} via email")

                when (val result = emailClient.sendEmail(emailConfig, subject, body)) {
                    is EmailResult.Success -> {
                        Log.d(TAG, "SMS forwarded successfully via email")
                        notificationHelper.showForwardSuccessNotification(sms.sender)
                    }
                    is EmailResult.Error -> {
                        Log.e(TAG, "Failed to forward SMS: ${result.message}")
                        notificationHelper.showForwardErrorNotification(result.message)
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Restart service if task is removed (app swiped away)
        if (preferencesManager.isForwardingEnabled) {
            val restartIntent = Intent(this, SmsForwarderService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            startForegroundService(restartIntent)
        }
    }

    companion object {
        private const val TAG = "SmsForwarderService"
        const val ACTION_FORWARD_SMS = "com.example.sms2line.FORWARD_SMS"
        const val ACTION_START_SERVICE = "com.example.sms2line.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.sms2line.STOP_SERVICE"
    }
}
