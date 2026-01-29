package com.example.sms2line.model

data class SmsMessage(
    val sender: String,
    val body: String,
    val timestamp: Long
)
