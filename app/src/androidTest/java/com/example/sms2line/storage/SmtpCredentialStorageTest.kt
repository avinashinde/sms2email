package com.example.sms2line.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sms2line.email.EmailConfig
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmtpCredentialStorageTest {

    private lateinit var storage: SmtpCredentialStorage

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        storage = SmtpCredentialStorage(context)
        storage.clearConfig()
    }

    @After
    fun tearDown() {
        storage.clearConfig()
    }

    @Test
    fun saveConfig_storesConfigCorrectly() {
        val config = EmailConfig(
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            username = "test@gmail.com",
            password = "testpass",
            fromAddress = "test@gmail.com",
            toAddress = "recipient@example.com",
            useTls = true
        )

        storage.saveConfig(config)

        val retrieved = storage.getConfig()
        assertEquals(config.smtpHost, retrieved.smtpHost)
        assertEquals(config.smtpPort, retrieved.smtpPort)
        assertEquals(config.username, retrieved.username)
        assertEquals(config.password, retrieved.password)
        assertEquals(config.fromAddress, retrieved.fromAddress)
        assertEquals(config.toAddress, retrieved.toAddress)
        assertEquals(config.useTls, retrieved.useTls)
    }

    @Test
    fun hasValidConfig_returnsFalseWhenEmpty() {
        assertFalse(storage.hasValidConfig())
    }

    @Test
    fun hasValidConfig_returnsTrueWhenConfigured() {
        val config = EmailConfig(
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            username = "test@gmail.com",
            password = "testpass",
            fromAddress = "test@gmail.com",
            toAddress = "recipient@example.com"
        )

        storage.saveConfig(config)

        assertTrue(storage.hasValidConfig())
    }

    @Test
    fun clearConfig_removesStoredConfig() {
        val config = EmailConfig(
            smtpHost = "smtp.gmail.com",
            smtpPort = 587,
            username = "test@gmail.com",
            password = "testpass",
            fromAddress = "test@gmail.com",
            toAddress = "recipient@example.com"
        )

        storage.saveConfig(config)
        assertTrue(storage.hasValidConfig())

        storage.clearConfig()

        assertFalse(storage.hasValidConfig())
    }
}
