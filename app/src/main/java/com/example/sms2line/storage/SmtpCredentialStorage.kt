package com.example.sms2line.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.sms2line.email.EmailConfig

class SmtpCredentialStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveConfig(config: EmailConfig) {
        encryptedPrefs.edit()
            .putString(KEY_SMTP_HOST, config.smtpHost)
            .putInt(KEY_SMTP_PORT, config.smtpPort)
            .putString(KEY_USERNAME, config.username)
            .putString(KEY_PASSWORD, config.password)
            .putString(KEY_FROM_ADDRESS, config.fromAddress)
            .putString(KEY_TO_ADDRESS, config.toAddress)
            .putBoolean(KEY_USE_TLS, config.useTls)
            .apply()
    }

    fun getConfig(): EmailConfig {
        return EmailConfig(
            smtpHost = encryptedPrefs.getString(KEY_SMTP_HOST, "") ?: "",
            smtpPort = encryptedPrefs.getInt(KEY_SMTP_PORT, 587),
            username = encryptedPrefs.getString(KEY_USERNAME, "") ?: "",
            password = encryptedPrefs.getString(KEY_PASSWORD, "") ?: "",
            fromAddress = encryptedPrefs.getString(KEY_FROM_ADDRESS, "") ?: "",
            toAddress = encryptedPrefs.getString(KEY_TO_ADDRESS, "") ?: "",
            useTls = encryptedPrefs.getBoolean(KEY_USE_TLS, true)
        )
    }

    fun hasValidConfig(): Boolean {
        return getConfig().isValid()
    }

    fun clearConfig() {
        encryptedPrefs.edit()
            .remove(KEY_SMTP_HOST)
            .remove(KEY_SMTP_PORT)
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .remove(KEY_FROM_ADDRESS)
            .remove(KEY_TO_ADDRESS)
            .remove(KEY_USE_TLS)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "sms2email_secure_prefs"
        private const val KEY_SMTP_HOST = "smtp_host"
        private const val KEY_SMTP_PORT = "smtp_port"
        private const val KEY_USERNAME = "smtp_username"
        private const val KEY_PASSWORD = "smtp_password"
        private const val KEY_FROM_ADDRESS = "from_address"
        private const val KEY_TO_ADDRESS = "to_address"
        private const val KEY_USE_TLS = "use_tls"
    }
}
