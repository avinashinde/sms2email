package com.example.sms2line.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sms2line.data.entity.EmailQueueStatus
import com.example.sms2line.data.entity.PendingEmail
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingEmailDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(email: PendingEmail): Long

    @Update
    suspend fun update(email: PendingEmail)

    @Query("SELECT * FROM pending_emails WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getByStatus(status: EmailQueueStatus): List<PendingEmail>

    @Query("SELECT * FROM pending_emails WHERE status IN (:statuses) ORDER BY createdAt ASC")
    suspend fun getByStatuses(statuses: List<EmailQueueStatus>): List<PendingEmail>

    @Query("SELECT * FROM pending_emails WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPendingOrFailed(limit: Int = 50): List<PendingEmail>

    @Query("SELECT * FROM pending_emails WHERE smsHash = :hash LIMIT 1")
    suspend fun getByHash(hash: String): PendingEmail?

    @Query("SELECT COUNT(*) FROM pending_emails WHERE status = 'PENDING' OR status = 'IN_PROGRESS'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pending_emails WHERE status = 'FAILED'")
    fun getFailedCount(): Flow<Int>

    @Query("UPDATE pending_emails SET status = :status, lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Long, status: EmailQueueStatus, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pending_emails SET status = :status, retryCount = retryCount + 1, lastAttemptAt = :timestamp, errorMessage = :error WHERE id = :id")
    suspend fun markFailed(id: Long, status: EmailQueueStatus = EmailQueueStatus.FAILED, timestamp: Long = System.currentTimeMillis(), error: String?)

    @Query("DELETE FROM pending_emails WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM pending_emails WHERE status = 'SENT'")
    suspend fun deleteSent()

    @Query("DELETE FROM pending_emails WHERE status = 'SENT' AND lastAttemptAt < :olderThan")
    suspend fun deleteSentOlderThan(olderThan: Long)

    @Query("DELETE FROM pending_emails WHERE retryCount >= :maxRetries AND status = 'FAILED'")
    suspend fun deleteExhaustedRetries(maxRetries: Int)
}
