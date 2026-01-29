package com.example.sms2line.model

data class ForwardingConfig(
    val isEnabled: Boolean,
    val lineToken: String,
    val includeTimestamp: Boolean = true,
    val includeSender: Boolean = true
)
