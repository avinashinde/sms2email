package com.example.sms2line.email

data class EmailConfig(
    val smtpHost: String,
    val smtpPort: Int,
    val username: String,
    val password: String,
    val fromAddress: String,
    val toAddress: String,
    val useTls: Boolean = true
) {
    fun isValid(): Boolean {
        return smtpHost.isNotBlank() &&
                smtpPort > 0 &&
                username.isNotBlank() &&
                password.isNotBlank() &&
                fromAddress.contains("@") &&
                toAddress.contains("@")
    }

    companion object {
        val EMPTY = EmailConfig(
            smtpHost = "",
            smtpPort = 587,
            username = "",
            password = "",
            fromAddress = "",
            toAddress = "",
            useTls = true
        )
    }
}
