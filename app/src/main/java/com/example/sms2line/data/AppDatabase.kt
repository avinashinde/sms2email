package com.example.sms2line.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sms2line.data.dao.PendingEmailDao
import com.example.sms2line.data.entity.PendingEmail

@Database(
    entities = [PendingEmail::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pendingEmailDao(): PendingEmailDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sms2line_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
