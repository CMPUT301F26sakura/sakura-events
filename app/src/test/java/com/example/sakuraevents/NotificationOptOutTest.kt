package com.example.sakuraevents

// NotificationOptOutTest.kt: unit tests for opting out of notifications (US 01.10.01).
// Written with help from Anthropic, Claude, "Unit tests for notification opt-out", 2026-11-25

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationOptOutTest {

    private fun mockEntrant(uid: String, name: String): Entrant {
        return Entrant(uid, Role.ENTRANT, name, "${name.lowercase()}@example.com", "780-555-0100")
    }

    @Test
    fun testNotificationsEnabledByDefault() {
        val aisha = mockEntrant("entrant_001", "Aisha")
        assertTrue(aisha.getNotificationsEnabled())
    }

    @Test
    fun testOptOut() {
        val aisha = mockEntrant("entrant_001", "Aisha")
        aisha.setNotificationsEnabled(false)
        assertFalse(aisha.getNotificationsEnabled())
        aisha.setNotificationsEnabled(true)
        assertTrue(aisha.getNotificationsEnabled())
    }

    @Test
    fun testOptedInRecipientsSkipsOptedOutEntrants() {
        val aisha = mockEntrant("entrant_001", "Aisha")
        val ben = mockEntrant("entrant_002", "Ben")
        val chen = mockEntrant("entrant_003", "Chen")
        ben.setNotificationsEnabled(false)

        val recipients = optedInRecipients(listOf(aisha, ben, chen))

        assertEquals(listOf(aisha, chen), recipients)
    }

    @Test
    fun testOptedInRecipientsWhenEveryoneOptedOut() {
        val aisha = mockEntrant("entrant_001", "Aisha")
        aisha.setNotificationsEnabled(false)
        assertTrue(optedInRecipients(listOf(aisha)).isEmpty())
    }

    @Test
    fun testTodayIsoDateFormat() {
        assertTrue(Regex("""\d{4}-\d{2}-\d{2}""").matches(todayIsoDate()))
    }
}
