package com.example.sms2line.email

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailConfigTest {

    @Test
    fun `valid config returns true`() {
        val config = EmailConfig(
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            username = "user@gmail.com",
            password = "password123",
            fromAddress = "user@gmail.com",
            toAddress = "recipient@example.com",
            useTls = true
        )

        assertTrue(config.isValid())
    }

    @Test
    fun `empty host returns false`() {
        val config = EmailConfig(
            smtpHost = "",
            smtpPort = 587,
            username = "user@gmail.com",
            password = "password123",
            fromAddress = "user@gmail.com",
            toAddress = "recipient@example.com"
        )

        assertFalse(config.isValid())
    }

    @Test
    fun `invalid port returns false`() {
        val config = EmailConfig(
            smtpHost = "smtp.gmail.com",
            smtpPort = 0,
            username = "user@gmail.com",
            password = "password123",
            fromAddress = "user@gmail.com",
            toAddress = "recipient@example.com"
        )

        assertFalse(config.isValid())
    }

    @Test
    fun `empty username returns false`() {
        val config = EmailConfig(
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            username = "",
            password = "password123",
            fromAddress = "user@gmail.com",
            toAddress = "recipient@example.com"
        )

        assertFalse(config.isValid())
    }

    @Test
    fun `empty password returns false`() {
        val config = EmailConfig(
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            username = "user@gmail.com",
            password = "",
            fromAddress = "user@gmail.com",
            toAddress = "recipient@example.com"
        )

        assertFalse(config.isValid())
    }

    @Test
    fun `invalid from address returns false`() {
        val config = EmailConfig(
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            username = "user@gmail.com",
            password = "password123",
            fromAddress = "invalid-email",
            toAddress = "recipient@example.com"
        )

        assertFalse(config.isValid())
    }

    @Test
    fun `invalid to address returns false`() {
        val config = EmailConfig(
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            username = "user@gmail.com",
            password = "password123",
            fromAddress = "user@gmail.com",
            toAddress = "invalid-email"
        )

        assertFalse(config.isValid())
    }

    @Test
    fun `EMPTY config is invalid`() {
        assertFalse(EmailConfig.EMPTY.isValid())
    }
}
