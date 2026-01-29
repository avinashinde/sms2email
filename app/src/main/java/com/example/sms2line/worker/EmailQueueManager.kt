package com.example.sms2line.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object EmailQueueManager {

    private const val INITIAL_BACKOFF_MINUTES = 1L
    private const val MAX_BACKOFF_MINUTES = 16L // 1 -> 2 -> 4 -> 8 -> 16

    fun scheduleRetry(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<EmailRetryWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                INITIAL_BACKOFF_MINUTES,
                TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                EmailRetryWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    fun scheduleImmediateRetry(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<EmailRetryWorker>()
            .setConstraints(constraints)
            .setInitialDelay(0, TimeUnit.SECONDS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                INITIAL_BACKOFF_MINUTES,
                TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                EmailRetryWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    fun cancelRetry(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(EmailRetryWorker.WORK_NAME)
    }
}
