package com.example.sms2line.email

sealed class EmailResult {
    data class Success(val message: String) : EmailResult()
    data class Error(val message: String, val exception: Exception? = null) : EmailResult()
}
