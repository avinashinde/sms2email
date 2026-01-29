package com.example.sms2line.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesManagerTest {

    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        preferencesManager = PreferencesManager(context)
        // Reset to defaults
        preferencesManager.isForwardingEnabled = false
        preferencesManager.includeTimestamp = true
        preferencesManager.includeSender = true
    }

    @Test
    fun isForwardingEnabled_defaultsToFalse() {
        assertFalse(preferencesManager.isForwardingEnabled)
    }

    @Test
    fun isForwardingEnabled_canBeSetToTrue() {
        preferencesManager.isForwardingEnabled = true

        assertTrue(preferencesManager.isForwardingEnabled)
    }

    @Test
    fun includeTimestamp_defaultsToTrue() {
        assertTrue(preferencesManager.includeTimestamp)
    }

    @Test
    fun includeTimestamp_canBeSetToFalse() {
        preferencesManager.includeTimestamp = false

        assertFalse(preferencesManager.includeTimestamp)
    }

    @Test
    fun includeSender_defaultsToTrue() {
        assertTrue(preferencesManager.includeSender)
    }

    @Test
    fun includeSender_canBeSetToFalse() {
        preferencesManager.includeSender = false

        assertFalse(preferencesManager.includeSender)
    }

    @Test
    fun getForwardingConfig_returnsCorrectConfig() {
        preferencesManager.isForwardingEnabled = true
        preferencesManager.includeTimestamp = false
        preferencesManager.includeSender = true

        val config = preferencesManager.getForwardingConfig("test-token")

        assertTrue(config.isEnabled)
        assertEquals("test-token", config.lineToken)
        assertFalse(config.includeTimestamp)
        assertTrue(config.includeSender)
    }
}
