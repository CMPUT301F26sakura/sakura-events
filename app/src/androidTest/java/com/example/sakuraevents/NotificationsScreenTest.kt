package com.example.sakuraevents

// NotificationsScreenTest.kt: UI tests for the Notifications page and the owner's "message your waitlist" form.
// Written with help from Anthropic, Claude, "UI tests for notifications screen", 2026-11-25

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationsScreenTest {

    @get:Rule
    val compose_rule = createComposeRule()

    private val owner = CampsiteOwner("owner_001", Role.OWNER, "Maria Lopez", "maria@example.com", "780-555-0199")
    private val aisha = Entrant("entrant_001", Role.ENTRANT, "Aisha Khan", "aisha@example.com", "780-555-0101")
    private val ben = Entrant("entrant_002", Role.ENTRANT, "Ben Carter", "ben@example.com", "780-555-0102")

    private fun mockCampsite(): Campsite {
        return Campsite(
            name = "Whistlers Campground",
            location = "Jasper, AB",
            description = "Forested sites near the Athabasca River.",
            max_occupancy = 6,
            price = 32.0f
        )
    }

    private fun mockNotification(title: String, message: String): Notification {
        return Notification(
            notification_type = NotificationType.LOTTERY_WIN,
            sender = owner,
            campsite = mockCampsite(),
            title = title,
            message = message,
            _recipients = listOf(aisha),
            date_sent = "2026-11-25"
        )
    }

    @Test
    fun testShowsReceivedNotifications() {
        compose_rule.setContent {
            notificationsContent(
                received = listOf(mockNotification("Lottery Winner!", "You won a spot at Whistlers Campground.")),
                sent = listOf(),
                show_sent = false,
                notifications_enabled = true,
                on_notifications_enabled_changed = {}
            )
        }
        compose_rule.onNodeWithText("Lottery Winner!").assertExists()
        compose_rule.onNodeWithText("You won a spot at Whistlers Campground.").assertExists()
        compose_rule.onNodeWithTag("sent_tab").assertDoesNotExist()
    }

    @Test
    fun testOwnerSeesSentNotifications() {
        compose_rule.setContent {
            notificationsContent(
                received = listOf(),
                sent = listOf(mockNotification("Site maintenance", "The water tap is fixed.")),
                show_sent = true,
                notifications_enabled = true,
                on_notifications_enabled_changed = {}
            )
        }
        compose_rule.onNodeWithTag("sent_tab").performClick()
        compose_rule.onNodeWithText("Site maintenance").assertExists()
    }

    @Test
    fun testOptOutSwitch() {
        var saved_value: Boolean? = null
        compose_rule.setContent {
            notificationsContent(
                received = listOf(),
                sent = listOf(),
                show_sent = false,
                notifications_enabled = true,
                on_notifications_enabled_changed = { saved_value = it }
            )
        }
        compose_rule.onNodeWithTag("notifications_switch").performClick()
        assertEquals(false, saved_value)
        compose_rule.onNodeWithTag("opted_out_message").assertExists()
    }

    @Test
    fun testSendToWaitlistSkipsOptedOutEntrants() {
        val campsite = mockCampsite()
        campsite.addToWaitingList(aisha)
        campsite.addToWaitingList(ben)
        ben.setNotificationsEnabled(false)
        var sent_notification: Notification? = null
        compose_rule.setContent {
            SendWaitlistNotificationSection(campsite = campsite, sender = owner, on_send = { sent_notification = it })
        }

        compose_rule.onNodeWithTag("waitlist_recipient_count")
            .assertTextEquals("1 of 2 entrants on the waitlist will receive it")
        compose_rule.onNodeWithTag("notification_title_input").performTextInput("Fire ban")
        compose_rule.onNodeWithTag("notification_message_input").performTextInput("No campfires this weekend.")
        compose_rule.onNodeWithTag("send_waitlist_notification_button").performClick()

        val notification = sent_notification!!
        assertEquals(NotificationType.WAITLIST_MESSAGE, notification.getNotificationType())
        assertSame(owner, notification.getSender())
        assertEquals(listOf(aisha), notification.recipients)
        assertEquals("Fire ban", notification.getTitle())
        compose_rule.onNodeWithTag("send_result").assertTextEquals("Sent to 1 entrant(s)")
    }

    @Test
    fun testEmptyFieldsAreHighlightedAndNothingIsSent() {
        val campsite = mockCampsite()
        campsite.addToWaitingList(aisha)
        var sent_notification: Notification? = null
        compose_rule.setContent {
            SendWaitlistNotificationSection(campsite = campsite, sender = owner, on_send = { sent_notification = it })
        }

        compose_rule.onNodeWithTag("send_waitlist_notification_button").performClick()

        compose_rule.onNodeWithText("Title is required").assertExists()
        compose_rule.onNodeWithText("Message is required").assertExists()
        assertNull(sent_notification)
    }

    @Test
    fun testCannotSendToEmptyWaitlist() {
        compose_rule.setContent {
            SendWaitlistNotificationSection(campsite = mockCampsite(), sender = owner, on_send = {})
        }
        compose_rule.onNodeWithTag("send_waitlist_notification_button").assertIsNotEnabled()
    }
}
