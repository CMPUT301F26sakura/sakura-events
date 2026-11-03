package com.example.sakuraevents

// NotificationTest.kt: unit tests for Notification.

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationTest {

    // Lottery-win notification with two recipients.
    private fun mockNotification(): Notification {
        val notification = Notification(
            UID = "notif_001",
            notification_type = NotificationType.LOTTERY_WIN,
            sender_uid = "system",
            campsite_uid = "campsite_jasper_whistlers",
            title = "You won the lottery!",
            message = "You won a spot at Whistlers Campground. Pick your dates to reserve."
        )
        notification.addRecipient("entrant_aisha")
        notification.addRecipient("entrant_ben")
        return notification
    }

    @Test
    fun testGetters() {
        val notification = mockNotification()
        assertEquals("notif_001", notification.UID)
        assertEquals(NotificationType.LOTTERY_WIN, notification.getNotificationType())
        assertEquals("system", notification.getSenderUid())
        assertEquals("campsite_jasper_whistlers", notification.getCampsiteUid())
        assertEquals("You won the lottery!", notification.getTitle())
        assertEquals(listOf("entrant_aisha", "entrant_ben"), notification.getRecipientUids())
        assertEquals("", notification.getDateSent())
        assertFalse(notification.isSent())
    }

    @Test
    fun testAddRecipient() {
        val notification = mockNotification()
        assertTrue(notification.addRecipient("entrant_chen"))
        assertTrue(notification.hasRecipient("entrant_chen"))
        assertEquals(3, notification.getRecipientUids().size)
    }

    @Test
    fun testAddDuplicateOrBlankRecipient() {
        val notification = mockNotification()
        assertFalse(notification.addRecipient("entrant_aisha"))
        assertFalse(notification.addRecipient(""))
        assertEquals(2, notification.getRecipientUids().size)
    }

    @Test
    fun testRemoveRecipient() {
        val notification = mockNotification()
        assertTrue(notification.removeRecipient("entrant_ben"))
        assertFalse(notification.hasRecipient("entrant_ben"))
        assertFalse(notification.removeRecipient("entrant_not_here"))
    }

    @Test
    fun testSetters() {
        val notification = mockNotification()
        assertTrue(notification.setTitle("Lottery results"))
        assertTrue(notification.setMessage("Check the app for your lottery result."))
        assertTrue(notification.setNotificationType(NotificationType.LOTTERY_LOSS))
        assertEquals("Lottery results", notification.getTitle())
        assertEquals("Check the app for your lottery result.", notification.getMessage())
        assertEquals(NotificationType.LOTTERY_LOSS, notification.getNotificationType())
        assertFalse(notification.setTitle("   "))
        assertFalse(notification.setMessage(""))
    }

    @Test
    fun testSend() {
        val notification = mockNotification()
        assertTrue(notification.send("2026-11-02"))
        assertTrue(notification.isSent())
        assertEquals("2026-11-02", notification.getDateSent())
    }

    @Test
    fun testSendSkipsOptedOutEntrants() {
        val notification = mockNotification()
        assertTrue(notification.send("2026-11-02", listOf("entrant_ben")))
        assertEquals(listOf("entrant_aisha"), notification.getRecipientUids())
    }

    @Test
    fun testSendFailsWhenEveryoneOptedOut() {
        val notification = mockNotification()
        assertFalse(notification.send("2026-11-02", listOf("entrant_aisha", "entrant_ben")))
        assertFalse(notification.isSent())
    }

    @Test
    fun testSendFailsWithBadDate() {
        val notification = mockNotification()
        assertFalse(notification.send("11/02/2026"))
        assertFalse(notification.send("2026-13-02"))
        assertFalse(notification.isSent())
    }

    @Test
    fun testSendFailsWithoutMessage() {
        val notification = Notification(
            notification_type = NotificationType.WAITLIST_MESSAGE,
            sender_uid = "owner_maria",
            campsite_uid = "campsite_banff_tunnel"
        )
        notification.addRecipient("entrant_aisha")
        assertFalse(notification.send("2026-11-02"))
    }

    @Test
    fun testCannotEditAfterSending() {
        val notification = mockNotification()
        notification.send("2026-11-02")
        assertFalse(notification.send("2026-11-03"))
        assertFalse(notification.setTitle("New title"))
        assertFalse(notification.setMessage("New message"))
        assertFalse(notification.setNotificationType(NotificationType.OWNER_MESSAGE))
        assertFalse(notification.addRecipient("entrant_chen"))
        assertFalse(notification.removeRecipient("entrant_aisha"))
        assertEquals("2026-11-02", notification.getDateSent())
    }
}