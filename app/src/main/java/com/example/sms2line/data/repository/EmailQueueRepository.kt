package com.example.sms2line.data.repository

import android.content.Context
import com.example.sms2line.data.AppDatabase
import com.example.sms2line.data.entity.EmailQueueStatus
import com.example.sms2line.data.entity.PendingEmail
import com.example.sms2line.model.SmsMessage
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class EmailQueueRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).pendingEmailDao()

    companion object {
        const val MAX_RETRIES = 5
        private const val CLEANUP_AGE_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    suspend fun queueEmail(
        sms: SmsMessage,
        subject: String,
        body: String
    ): Boolean {
        val hash = computeSmsHash(sms)

        // Check if already queued
        val existing = dao.getByHash(hash)
        if (existing != null) {
            return false
        }

        val pendingEmail = PendingEmail(
            smsHash = hash,
            subject = subject,
            body = body,
            sender = sms.sender,
            smsTimestamp = sms.timestamp
        )

        val id = dao.insert(pendingEmail)
        return id > 0
    }

    suspend fun getPendingEmails(): List<PendingEmail> {
        return dao.getPendingOrFailed()
    }

    suspend fun markAsInProgress(id: Long) {
        dao.updateStatus(id, EmailQueueStatus.IN_PROGRESS)
    }

    suspend fun markAsSent(id: Long) {
        dao.updateStatus(id, EmailQueueStatus.SENT)
    }

    suspend fun markAsFailed(id: Long, errorMessage: String?) {
        dao.markFailed(id, EmailQueueStatus.FAILED, System.currentTimeMillis(), errorMessage)
    }

    suspend fun getById(hash: String): PendingEmail? {
        return dao.getByHash(hash)
    }

    suspend fun shouldRetry(email: PendingEmail): Boolean {
        return email.retryCount < MAX_RETRIES
    }

    fun getPendingCount(): Flow<Int> {
        return dao.getPendingCount()
    }

    fun getFailedCount(): Flow<Int> {
        return dao.getFailedCount()
    }

    suspend fun cleanup() {
        // Delete sent emails older than 24 hours
        val cutoff = System.currentTimeMillis() - CLEANUP_AGE_MS
        dao.deleteSentOlderThan(cutoff)

        // Delete emails that have exhausted retries
        dao.deleteExhaustedRetries(MAX_RETRIES)
    }

    suspend fun isAlreadyQueued(sms: SmsMessage): Boolean {
        val hash = computeSmsHash(sms)
        return dao.getByHash(hash) != null
    }

    private fun computeSmsHash(sms: SmsMessage): String {
        val data = "${sms.sender}|${sms.timestamp}|${sms.body}"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return hash.take(16).joinToString("") { "%02x".format(it) }
    }
}
