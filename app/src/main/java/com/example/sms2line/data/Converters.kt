package com.example.sms2line.data

import androidx.room.TypeConverter
import com.example.sms2line.data.entity.EmailQueueStatus

class Converters {
    @TypeConverter
    fun fromEmailQueueStatus(status: EmailQueueStatus): String {
        return status.name
    }

    @TypeConverter
    fun toEmailQueueStatus(value: String): EmailQueueStatus {
        return EmailQueueStatus.valueOf(value)
    }
}
