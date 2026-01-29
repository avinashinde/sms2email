package com.example.sms2line.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_emails",
    indices = [Index(value = ["smsHash"], unique = true)]
)
data class PendingEmail(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val smsHash: String,
    val subject: String,
    val body: String,
    val sender: String,
    val smsTimestamp: Long,
    val retryCount: Int = 0,
    val status: EmailQueueStatus = EmailQueueStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long? = null,
    val errorMessage: String? = null
)
