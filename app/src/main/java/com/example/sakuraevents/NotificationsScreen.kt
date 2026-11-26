/**
 * File: NotificationsScreen.kt
 * Purpose: The Notifications page (received notifications, sent notifications for owners, and the
 * opt-out switch), plus the owner's "message my waitlist" form shown on campsite details.
 * Written with help from Anthropic, Claude, "Notifications screen, owner waitlist messages and opt-out", 2026-11-25
 *
 * User Stories Addressed:
 * - US 01.03.01, US 01.03.02: Entrants see their lottery win/loss notifications.
 * - US 02.10.01: As a campsite owner, I want to send notifications to entrants who have joined the waitlist.
 * - US 02.10.02: As a campsite owner, I want to view a list of all notifications I have sent.
 * - US 01.10.01: As an entrant, I want to opt out of receiving notifications.
 *
 * Outstanding Issues:
 * - Lottery draws create notifications in memory only; they are not saved to Firestore yet.
 */

package com.example.sakuraevents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Returns only the entrants who have not opted out of notifications (US 01.10.01).
 *
 * @param entrants the entrants to filter, e.g. a campsite's waiting list.
 * @return the entrants whose notifications are enabled.
 */
fun optedInRecipients(entrants: List<Entrant>): List<Entrant> {
    return entrants.filter { it.getNotificationsEnabled() }
}

/**
 * Returns today's date in the team's ISO 8601 format ("YYYY-MM-DD").
 * Uses SimpleDateFormat because java.time needs a newer Android version than our minSdk.
 *
 * @return today's date as a String.
 */
fun todayIsoDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

/**
 * The Notifications page used by the app. Reads the signed-in user and their notifications
 * from FirestoreDriver and saves the opt-out switch.
 *
 * @param firestore_driver the app's FirestoreDriver.
 * @param inner_padding padding from the app's Scaffold.
 */
@Composable
fun notificationsScreen(firestore_driver: FirestoreDriver, inner_padding: PaddingValues) {
    val current_user = firestore_driver.current_user

    if (current_user == null) {
        Text("Signing in...", modifier = Modifier.padding(inner_padding).padding(16.dp))
        return
    }

    val received = (firestore_driver.getNotificationsFor(current_user.UID) +
            current_user.notifications.reversed()).distinct()

    notificationsContent(
        received = received,
        sent = firestore_driver.getNotificationsSentBy(current_user.UID),
        show_sent = current_user.has_owner_access,
        notifications_enabled = current_user.getNotificationsEnabled(),
        on_notifications_enabled_changed = { enabled ->
            current_user.setNotificationsEnabled(enabled)
            firestore_driver.addEntrant(current_user)
        },
        modifier = Modifier.padding(inner_padding)
    )
}

/**
 * Draws the Notifications page: an opt-out switch, then the received notifications and,
 * for owners, a "Sent" tab. It loads nothing itself, so tests can use it with fake data.
 *
 * @param received notifications this user received, newest first.
 * @param sent notifications this user sent as an owner, newest first.
 * @param show_sent whether to show the "Sent" tab (only for owners).
 * @param notifications_enabled whether this user currently receives notifications.
 * @param on_notifications_enabled_changed called when the switch is flipped.
 */
@Composable
fun notificationsContent(
    received: List<Notification>,
    sent: List<Notification>,
    show_sent: Boolean,
    notifications_enabled: Boolean,
    on_notifications_enabled_changed: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selected_tab by remember { mutableIntStateOf(0) }
    var switch_on by remember { mutableStateOf(notifications_enabled) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Receive notifications", fontWeight = FontWeight.Bold)
            Switch(
                checked = switch_on,
                onCheckedChange = {
                    switch_on = it
                    on_notifications_enabled_changed(it)
                },
                colors = SwitchDefaults.colors(checkedTrackColor = COLOURS.DARKGREEN.colour),
                modifier = Modifier.testTag("notifications_switch")
            )
        }
        if (!switch_on) {
            Text(
                text = "You have opted out. Owners and lotteries won't send you new notifications.",
                modifier = Modifier.padding(horizontal = 16.dp).testTag("opted_out_message")
            )
        }

        if (show_sent) {
            TabRow(selectedTabIndex = selected_tab, containerColor = COLOURS.DARKYELLOW.colour) {
                Tab(
                    selected = selected_tab == 0,
                    onClick = { selected_tab = 0 },
                    text = { Text("Received") },
                    modifier = Modifier.testTag("received_tab")
                )
                Tab(
                    selected = selected_tab == 1,
                    onClick = { selected_tab = 1 },
                    text = { Text("Sent") },
                    modifier = Modifier.testTag("sent_tab")
                )
            }
        }

        if (show_sent && selected_tab == 1) {
            NotificationsList(notifications = sent, empty_text = "You haven't sent any notifications yet")
        } else {
            NotificationsList(notifications = received)
        }
    }
}

/**
 * Form on the campsite details page that lets the owner send a message to everyone on the
 * campsite's waiting list (US 02.10.01). Entrants who opted out are skipped (US 01.10.01).
 * Empty fields are highlighted in red instead of showing a generic message.
 *
 * @param campsite the campsite whose waiting list receives the message.
 * @param sender the owner sending the message.
 * @param on_send called with the new Notification, e.g. to save it to Firestore.
 */
@Composable
fun SendWaitlistNotificationSection(
    campsite: Campsite,
    sender: CampsiteOwner,
    on_send: (Notification) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var title_error by remember { mutableStateOf(false) }
    var message_error by remember { mutableStateOf(false) }
    var result_text by remember { mutableStateOf("") }

    val recipients = optedInRecipients(campsite.waiting_list)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Message your waitlist", fontWeight = FontWeight.Bold)
        Text(
            text = "${recipients.size} of ${campsite.waiting_list.size} entrants on the waitlist will receive it",
            modifier = Modifier.testTag("waitlist_recipient_count")
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it; title_error = false },
            label = { Text("Title") },
            singleLine = true,
            isError = title_error,
            supportingText = { if (title_error) Text("Title is required") },
            modifier = Modifier.fillMaxWidth().testTag("notification_title_input")
        )
        OutlinedTextField(
            value = message,
            onValueChange = { message = it; message_error = false },
            label = { Text("Message") },
            isError = message_error,
            supportingText = { if (message_error) Text("Message is required") },
            modifier = Modifier.fillMaxWidth().testTag("notification_message_input")
        )

        Button(
            onClick = {
                title_error = title.isBlank()
                message_error = message.isBlank()
                if (title_error || message_error) return@Button

                val notification = Notification(
                    notification_type = NotificationType.WAITLIST_MESSAGE,
                    sender = sender,
                    campsite = campsite,
                    title = title.trim(),
                    message = message.trim(),
                    _recipients = recipients,
                    date_sent = todayIsoDate()
                )
                on_send(notification)
                result_text = "Sent to ${recipients.size} entrant(s)"
                title = ""
                message = ""
            },
            enabled = recipients.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = COLOURS.DARKGREEN.colour,
                contentColor = Color.White
            ),
            modifier = Modifier.testTag("send_waitlist_notification_button")
        ) {
            Text(if (recipients.isEmpty()) "No one to notify" else "Send to waitlist")
        }

        if (result_text.isNotEmpty()) {
            Text(text = result_text, color = COLOURS.DARKGREEN.colour, modifier = Modifier.testTag("send_result"))
        }
    }
}
