package com.example.sakuraevents

// NotificationsList.kt: shows an entrant's notifications (e.g. lottery win/loss), newest first.
// Written with help from Anthropic, Claude, "Kotlin Compose notifications list screen", 2026-11-05
// User stories: US 01.03.01, US 01.03.02


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shows all of an entrant's notifications, newest first,
 * or a "No notifications yet" message if there are none.
 *
 * @param entrant the entrant whose notifications are shown.
 */
@Composable
fun NotificationsList(entrant: Entrant, modifier: Modifier = Modifier) {
    val notifications = entrant.notifications.reversed()

    if (notifications.isEmpty()) {
        Text(
            text = "No notifications yet",
            modifier = modifier.padding(16.dp).testTag("notifications_empty")
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp).testTag("notifications_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notifications) { notification ->
            NotificationCard(notification)
        }
    }
}

/**
 * One notification: its title, message, and send date (if it has one).
 *
 * @param notification the notification to show.
 */
@Composable
fun NotificationCard(notification: Notification, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = COLOURS.LIGHTYELLOW.colour),
        modifier = modifier.fillMaxWidth().testTag("notification_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notification.getTitle(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = COLOURS.DARKGREEN.colour,
                modifier = Modifier.testTag("notification_title")
            )
            Text(
                text = notification.getMessage(),
                color = COLOURS.BLACK.colour,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (notification.getDateSent().isNotBlank()) {
                Text(
                    text = notification.getDateSent(),
                    fontSize = 12.sp,
                    color = COLOURS.BLACK.colour,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}