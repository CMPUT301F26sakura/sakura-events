package com.example.sakuraevents

// NotificationTest.kt: unit tests for Notification.

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationTest {
    private val owner = CampsiteOwner("owner_001", Role.OWNER, "Maria Lopez", "maria@example.com", "780-555-0199")
    private val campsite = Campsite(
        name = "Whistlers Campground",
        location = "Jasper, AB",
        description = "Forested sites near the Athabasca River.",
        max_occupancy = 6,
        price = 32.0f
    )
    private val aisha = Entrant("entrant_001", Role.ENTRANT, "Aisha Khan", "aisha@example.com", "780-555-0101")
    private val ben = Entrant("entrant_002", Role.ENTRANT, "Ben Carter", "ben@example.com", "780-555-0102")

    // Lottery-win notification sent to two entrants.
    private fun mockNotification(): Notification {
        return Notification(
            UID = "notif_001",
            notification_type = NotificationType.LOTTERY_WIN,
            sender = owner,
            campsite = campsite,
            title = "You won the lottery!",
            message = "You won a spot at Whistlers Campground. Pick your dates to reserve.",
            _recipients = listOf(aisha, ben),
            date_sent = "2026-11-04"
        )
    }

    @Test
    fun testGetters() {
        val notification = mockNotification()
        assertEquals("notif_001", notification.UID)
        assertEquals(NotificationType.LOTTERY_WIN, notification.getNotificationType())
        assertSame(owner, notification.getSender())
        assertSame(campsite, notification.getCampsite())
        assertEquals("You won the lottery!", notification.getTitle())
        assertEquals("You won a spot at Whistlers Campground. Pick your dates to reserve.", notification.getMessage())
        assertEquals("2026-11-04", notification.getDateSent())
    }

    @Test
    fun testRecipients() {
        val notification = mockNotification()
        assertEquals(2, notification.recipients.size)
        assertSame(aisha, notification.recipients[0])
        assertSame(ben, notification.recipients[1])
    }

    @Test
    fun testSendToEveryone() {
        val notification = mockNotification()
        assertEquals(listOf(aisha, ben), notification.send())
    }

    @Test
    fun testSendSkipsOptedOutEntrants() {
        val notification = mockNotification()
        val delivered = notification.send(listOf(ben))
        assertEquals(1, delivered.size)
        assertSame(aisha, delivered[0])
    }

    @Test
    fun testSendAddsToEntrantNotifications() {
        val notification = mockNotification()
        notification.send(listOf(ben))
        assertSame(notification, aisha.notifications[0])
        assertTrue(ben.notifications.isEmpty())
    }

    @Test
    fun testSendWhenEveryoneOptedOut() {
        val notification = mockNotification()
        assertTrue(notification.send(listOf(aisha, ben)).isEmpty())
    }

    @Test
    fun testSendDoesNotChangeRecipients() {
        val notification = mockNotification()
        notification.send(listOf(ben))
        assertEquals(2, notification.recipients.size)
    }
}