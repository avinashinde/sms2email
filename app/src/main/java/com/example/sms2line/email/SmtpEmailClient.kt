package com.example.sms2line.email

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SmtpEmailClient {

    suspend fun sendEmail(
        config: EmailConfig,
        subject: String,
        body: String
    ): EmailResult {
        return withContext(Dispatchers.IO) {
            try {
                val properties = createSmtpProperties(config)
                val session = createSession(properties, config)
                val message = createMessage(session, config, subject, body)

                Transport.send(message)

                Log.d(TAG, "Email sent successfully to ${config.toAddress}")
                EmailResult.Success("Email sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send email", e)
                EmailResult.Error("Failed to send email: ${e.message}", e)
            }
        }
    }

    private fun createSmtpProperties(config: EmailConfig): Properties {
        return Properties().apply {
            put("mail.smtp.host", config.smtpHost)
            put("mail.smtp.port", config.smtpPort.toString())
            put("mail.smtp.auth", "true")

            if (config.useTls) {
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
            }

            // SSL settings for port 465
            if (config.smtpPort == 465) {
                put("mail.smtp.socketFactory.port", "465")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.socketFactory.fallback", "false")
            }

            // Timeout settings - optimized for speed
            put("mail.smtp.connectiontimeout", CONNECT_TIMEOUT_MS.toString())
            put("mail.smtp.timeout", READ_TIMEOUT_MS.toString())
            put("mail.smtp.writetimeout", WRITE_TIMEOUT_MS.toString())

            // Enable connection pooling and keep-alive
            put("mail.smtp.quitwait", "false")
        }
    }

    private fun createSession(properties: Properties, config: EmailConfig): Session {
        return Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(config.username, config.password)
            }
        }).apply {
            debug = false // Disable debug for speed
        }
    }

    private fun createMessage(
        session: Session,
        config: EmailConfig,
        subject: String,
        body: String
    ): MimeMessage {
        return MimeMessage(session).apply {
            setFrom(InternetAddress(config.fromAddress))
            setRecipient(Message.RecipientType.TO, InternetAddress(config.toAddress))
            setSubject(subject)
            setContent(body, "text/html; charset=utf-8")
        }
    }

    suspend fun testConnection(
        config: EmailConfig,
        subject: String,
        body: String
    ): EmailResult {
        return sendEmail(
            config = config,
            subject = subject,
            body = body
        )
    }

    companion object {
        private const val TAG = "SmtpEmailClient"
        private const val CONNECT_TIMEOUT_MS = 10000  // 10 seconds
        private const val READ_TIMEOUT_MS = 10000     // 10 seconds
        private const val WRITE_TIMEOUT_MS = 10000    // 10 seconds
    }
}
