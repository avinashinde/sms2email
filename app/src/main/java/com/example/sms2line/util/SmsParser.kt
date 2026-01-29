package com.example.sms2line.util

import android.content.Intent
import android.provider.Telephony
import com.example.sms2line.model.SmsMessage

object SmsParser {

    fun parseSmsFromIntent(intent: Intent): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()

        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (smsMessages.isNullOrEmpty()) {
            return emptyList()
        }

        val groupedMessages = smsMessages.groupBy { it.originatingAddress ?: "Unknown" }

        for ((sender, parts) in groupedMessages) {
            val combinedBody = parts.joinToString("") { it.messageBody ?: "" }
            val timestamp = parts.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

            if (combinedBody.isNotBlank()) {
                messages.add(
                    SmsMessage(
                        sender = sender,
                        body = combinedBody,
                        timestamp = timestamp
                    )
                )
            }
        }

        return messages
    }
}
