package com.example.sms2line.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.sms2line.model.SmsMessage
import java.security.MessageDigest

class ForwardedSmsTracker(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun isAlreadyForwarded(sms: SmsMessage): Boolean {
        val hash = generateHash(sms)
        return prefs.contains(hash)
    }

    fun markAsForwarded(sms: SmsMessage) {
        val hash = generateHash(sms)
        prefs.edit()
            .putLong(hash, System.currentTimeMillis())
            .apply()

        // Clean up old entries periodically
        cleanupOldEntries()
    }

    private fun generateHash(sms: SmsMessage): String {
        val data = "${sms.sender}|${sms.timestamp}|${sms.body}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(32)
    }

    private fun cleanupOldEntries() {
        val now = System.currentTimeMillis()
        val cutoff = now - RETENTION_PERIOD_MS

        val allEntries = prefs.all
        if (allEntries.size > MAX_ENTRIES) {
            val editor = prefs.edit()
            var removed = 0

            for ((key, value) in allEntries) {
                if (value is Long && value < cutoff) {
                    editor.remove(key)
                    removed++
                }
                // Stop if we've removed enough
                if (allEntries.size - removed <= MAX_ENTRIES / 2) break
            }

            editor.apply()
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "forwarded_sms_tracker"
        private const val RETENTION_PERIOD_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
        private const val MAX_ENTRIES = 1000
    }
}
