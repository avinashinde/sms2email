package com.example.sms2line.util

import com.example.sms2line.model.ForwardingConfig
import com.example.sms2line.model.SmsMessage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageFormatterTest {

    private val testSms = SmsMessage(
        sender = "+1234567890",
        body = "Hello, this is a test message",
        timestamp = 1704067200000L // 2024-01-01 00:00:00 UTC
    )

    @Test
    fun `formatSubject includes sender and time`() {
        val result = MessageFormatter.formatSubject(testSms)

        assertTrue(result.contains("New SMS from"))
        assertTrue(result.contains("+1234567890"))
        assertTrue(result.contains("at"))
    }

    @Test
    fun `formatEmailBody includes sender when configured`() {
        val config = ForwardingConfig(
            isEnabled = true,
            lineToken = "",
            includeSender = true,
            includeTimestamp = false
        )

        val result = MessageFormatter.formatEmailBody(testSms, config)

        assertTrue(result.contains("From:"))
        assertTrue(result.contains("+1234567890"))
    }

    @Test
    fun `formatEmailBody excludes sender when not configured`() {
        val config = ForwardingConfig(
            isEnabled = true,
            lineToken = "",
            includeSender = false,
            includeTimestamp = false
        )

        val result = MessageFormatter.formatEmailBody(testSms, config)

        assertFalse(result.contains("From:"))
    }

    @Test
    fun `formatEmailBody includes timestamp when configured`() {
        val config = ForwardingConfig(
            isEnabled = true,
            lineToken = "",
            includeSender = false,
            includeTimestamp = true
        )

        val result = MessageFormatter.formatEmailBody(testSms, config)

        assertTrue(result.contains("Received:"))
    }

    @Test
    fun `formatEmailBody excludes timestamp when not configured`() {
        val config = ForwardingConfig(
            isEnabled = true,
            lineToken = "",
            includeSender = false,
            includeTimestamp = false
        )

        val result = MessageFormatter.formatEmailBody(testSms, config)

        assertFalse(result.contains("Received:"))
    }

    @Test
    fun `formatEmailBody contains message body`() {
        val config = ForwardingConfig(
            isEnabled = true,
            lineToken = "",
            includeSender = false,
            includeTimestamp = false
        )

        val result = MessageFormatter.formatEmailBody(testSms, config)

        assertTrue(result.contains("Hello, this is a test message"))
    }

    @Test
    fun `formatEmailBody is HTML formatted`() {
        val config = ForwardingConfig(
            isEnabled = true,
            lineToken = "",
            includeSender = true,
            includeTimestamp = true
        )

        val result = MessageFormatter.formatEmailBody(testSms, config)

        assertTrue(result.contains("<!DOCTYPE html>"))
        assertTrue(result.contains("<html>"))
        assertTrue(result.contains("</html>"))
    }

    @Test
    fun `formatSimple includes sender and body`() {
        val result = MessageFormatter.formatSimple(testSms)

        assertTrue(result.contains("+1234567890"))
        assertTrue(result.contains("Hello, this is a test message"))
    }
}
