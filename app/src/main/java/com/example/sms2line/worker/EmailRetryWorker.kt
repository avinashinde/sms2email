package com.example.sms2line.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sms2line.data.entity.EmailQueueStatus
import com.example.sms2line.data.repository.EmailQueueRepository
import com.example.sms2line.email.EmailResult
import com.example.sms2line.email.SmtpEmailClient
import com.example.sms2line.storage.SmtpCredentialStorage
import com.example.sms2line.util.NotificationHelper

class EmailRetryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = EmailQueueRepository(context)
    private val emailClient = SmtpEmailClient()
    private val smtpStorage = SmtpCredentialStorage(context)
    private val notificationHelper = NotificationHelper(context)

    override suspend fun doWork(): Result {
        Log.d(TAG, "EmailRetryWorker started")

        val emailConfig = smtpStorage.getConfig()
        if (!emailConfig.isValid()) {
            Log.e(TAG, "SMTP not configured, cannot retry emails")
            return Result.failure()
        }

        val pendingEmails = repository.getPendingEmails()
        if (pendingEmails.isEmpty()) {
            Log.d(TAG, "No pending emails to process")
            repository.cleanup()
            return Result.success()
        }

        Log.d(TAG, "Processing ${pendingEmails.size} pending emails")

        var allSuccess = true
        var anySuccess = false

        for (email in pendingEmails) {
            if (email.status == EmailQueueStatus.IN_PROGRESS) {
                // Skip emails already being processed (shouldn't happen with unique work)
                continue
            }

            if (!repository.shouldRetry(email)) {
                Log.d(TAG, "Email ${email.id} has exhausted retries, skipping")
                continue
            }

            repository.markAsInProgress(email.id)

            when (val result = emailClient.sendEmail(emailConfig, email.subject, email.body)) {
                is EmailResult.Success -> {
                    Log.d(TAG, "Email ${email.id} sent successfully")
                    repository.markAsSent(email.id)
                    notificationHelper.showForwardSuccessNotification(email.sender)
                    anySuccess = true
                }
                is EmailResult.Error -> {
                    Log.e(TAG, "Email ${email.id} failed: ${result.message}")
                    repository.markAsFailed(email.id, result.message)
                    allSuccess = false

                    // Show notification for permanent failures
                    if (!repository.shouldRetry(email.copy(retryCount = email.retryCount + 1))) {
                        notificationHelper.showForwardErrorNotification(
                            "Failed to send SMS from ${email.sender}: ${result.message}"
                        )
                    }
                }
            }
        }

        // Cleanup old sent emails
        repository.cleanup()

        return if (allSuccess) {
            Result.success()
        } else if (anySuccess) {
            // Some succeeded, some failed - retry the failed ones
            Result.retry()
        } else {
            // All failed - retry with backoff
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "EmailRetryWorker"
        const val WORK_NAME = "email_retry_work"
    }
}
