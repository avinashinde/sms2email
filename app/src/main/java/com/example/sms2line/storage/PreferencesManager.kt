package com.example.sms2line.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.sms2line.model.ForwardingConfig

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var isForwardingEnabled: Boolean
        get() = prefs.getBoolean(KEY_FORWARDING_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_FORWARDING_ENABLED, value).apply()

    var includeTimestamp: Boolean
        get() = prefs.getBoolean(KEY_INCLUDE_TIMESTAMP, true)
        set(value) = prefs.edit().putBoolean(KEY_INCLUDE_TIMESTAMP, value).apply()

    var includeSender: Boolean
        get() = prefs.getBoolean(KEY_INCLUDE_SENDER, true)
        set(value) = prefs.edit().putBoolean(KEY_INCLUDE_SENDER, value).apply()

    fun getForwardingConfig(token: String): ForwardingConfig {
        return ForwardingConfig(
            isEnabled = isForwardingEnabled,
            lineToken = token,
            includeTimestamp = includeTimestamp,
            includeSender = includeSender
        )
    }

    companion object {
        private const val PREFS_NAME = "sms2line_prefs"
        private const val KEY_FORWARDING_ENABLED = "forwarding_enabled"
        private const val KEY_INCLUDE_TIMESTAMP = "include_timestamp"
        private const val KEY_INCLUDE_SENDER = "include_sender"
    }
}
