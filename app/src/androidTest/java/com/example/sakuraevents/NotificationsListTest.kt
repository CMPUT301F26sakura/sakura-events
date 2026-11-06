package com.example.sakuraevents

// NotificationsListTest.kt: UI tests for NotificationsList.
// Written with help from Anthropic, Claude, "Compose UI tests for notifications list", 2026-11-05

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationsListTest {

    @get:Rule
    val compose_rule = createComposeRule()

    private val owner = CampsiteOwner("owner_001", Role.OWNER, "Maria Lopez", "maria@example.com", "780-555-0199")
    private val campsite = Campsite(
        name = "Whistlers Campground",
        location = "Jasper, AB",
        description = "Forested sites near the Athabasca River.",
        max_occupancy = 6,
        price = 32.0f
    )

    private fun mockEntrant(): Entrant {
        return Entrant("entrant_001", Role.ENTRANT, "Aisha Khan", "aisha@example.com", "780-555-0101")
    }

    private fun mockNotification(entrant: Entrant, title: String, message: String, date: String = ""): Notification {
        return Notification(
            notification_type = NotificationType.LOTTERY_WIN,
            sender = owner,
            campsite = campsite,
            title = title,
            message = message,
            _recipients = listOf(entrant),
            date_sent = date
        )
    }

    @Test
    fun testNoNotifications() {
        val entrant = mockEntrant()
        compose_rule.setContent { NotificationsList(entrant = entrant) }

        compose_rule.onNodeWithTag("notifications_empty").assertTextEquals("No notifications yet")
    }

    @Test
    fun testShowsWinNotification() {
        val entrant = mockEntrant()
        mockNotification(
            entrant,
            "Lottery Winner!",
            "Congratulations! You won the lottery for Whistlers Campground.",
            "2026-11-05"
        ).send()
        compose_rule.setContent { NotificationsList(entrant = entrant) }

        compose_rule.onNodeWithText("Lottery Winner!").assertExists()
        compose_rule.onNodeWithText("Congratulations! You won the lottery for Whistlers Campground.").assertExists()
        compose_rule.onNodeWithText("2026-11-05").assertExists()
    }

    @Test
    fun testShowsLossNotification() {
        val entrant = mockEntrant()
        mockNotification(
            entrant,
            "Lottery Result",
            "Unfortunately, you were not selected in the lottery for Whistlers Campground."
        ).send()
        compose_rule.setContent { NotificationsList(entrant = entrant) }

        compose_rule.onNodeWithText("Lottery Result").assertExists()
        compose_rule.onNodeWithText("Unfortunately, you were not selected in the lottery for Whistlers Campground.").assertExists()
    }

    @Test
    fun testNewestFirst() {
        val entrant = mockEntrant()
        mockNotification(entrant, "First notification", "Sent first.").send()
        mockNotification(entrant, "Second notification", "Sent second.").send()
        compose_rule.setContent { NotificationsList(entrant = entrant) }

        val titles = compose_rule.onAllNodesWithTag("notification_title")
        titles.assertCountEquals(2)
        titles[0].assertTextEquals("Second notification")
        titles[1].assertTextEquals("First notification")
    }

    @Test
    fun testOptedOutEntrantGetsNothing() {
        val entrant = mockEntrant()
        mockNotification(entrant, "Lottery Winner!", "You won.").send(listOf(entrant))
        compose_rule.setContent { NotificationsList(entrant = entrant) }

        compose_rule.onNodeWithTag("notifications_empty").assertExists()
    }
}