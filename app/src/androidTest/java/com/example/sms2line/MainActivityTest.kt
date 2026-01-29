package com.example.sms2line

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun titleIsDisplayed() {
        onView(withId(R.id.textTitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("SMS2Email")))
    }

    @Test
    fun subtitleIsDisplayed() {
        onView(withId(R.id.textSubtitle))
            .check(matches(isDisplayed()))
            .check(matches(withText("Forward SMS to Email")))
    }

    @Test
    fun permissionCardIsDisplayed() {
        onView(withId(R.id.cardPermissions))
            .check(matches(isDisplayed()))
    }

    @Test
    fun emailCardIsDisplayed() {
        onView(withId(R.id.cardEmail))
            .check(matches(isDisplayed()))
    }

    @Test
    fun settingsCardIsDisplayed() {
        onView(withId(R.id.cardSettings))
            .check(matches(isDisplayed()))
    }

    @Test
    fun smtpHostFieldIsDisplayed() {
        onView(withId(R.id.editSmtpHost))
            .check(matches(isDisplayed()))
    }

    @Test
    fun smtpPortFieldIsDisplayed() {
        onView(withId(R.id.editSmtpPort))
            .check(matches(isDisplayed()))
    }

    @Test
    fun usernameFieldIsDisplayed() {
        onView(withId(R.id.editSmtpUsername))
            .check(matches(isDisplayed()))
    }

    @Test
    fun passwordFieldIsDisplayed() {
        onView(withId(R.id.editSmtpPassword))
            .check(matches(isDisplayed()))
    }

    @Test
    fun fromEmailFieldIsDisplayed() {
        onView(withId(R.id.editFromEmail))
            .check(matches(isDisplayed()))
    }

    @Test
    fun toEmailFieldIsDisplayed() {
        onView(withId(R.id.editToEmail))
            .check(matches(isDisplayed()))
    }

    @Test
    fun saveButtonIsDisplayed() {
        onView(withId(R.id.btnSaveConfig))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testEmailButtonIsDisplayed() {
        onView(withId(R.id.btnTestEmail))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun smtpHostFieldAcceptsText() {
        onView(withId(R.id.editSmtpHost))
            .perform(replaceText("smtp.gmail.com"), closeSoftKeyboard())
            .check(matches(withText("smtp.gmail.com")))
    }

    @Test
    fun forwardingSwitchIsDisplayed() {
        onView(withId(R.id.switchForwarding))
            .check(matches(isDisplayed()))
    }

    @Test
    fun timestampSwitchIsDisplayed() {
        onView(withId(R.id.switchIncludeTimestamp))
            .check(matches(isDisplayed()))
    }

    @Test
    fun senderSwitchIsDisplayed() {
        onView(withId(R.id.switchIncludeSender))
            .check(matches(isDisplayed()))
    }
}
