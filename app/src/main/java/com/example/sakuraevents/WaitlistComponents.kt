package com.example.sakuraevents

// WaitlistComponents.kt: UI pieces for joining/leaving a campsite waitlist and setting its capacity.
// User stories: US 01.02.01, US 01.02.02, US 02.04.07

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Join/Leave button for a campsite's waitlist, with the current waitlist count.
 * Shows "Waitlist Full" (disabled) when the list is capped and the entrant is not on it.
 *
 * @param campsite the campsite whose waitlist is shown.
 * @param entrant the entrant using the app.
 * @param onWaitlistChanged called after a join or leave, e.g. to save the campsite to Firestore.
 */
@Composable
fun WaitlistButton(
    campsite: Campsite,
    entrant: Entrant,
    onWaitlistChanged: (Campsite) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var is_on_list by remember { mutableStateOf(campsite.waiting_list.any { it === entrant }) }
    var waitlist_size by remember { mutableIntStateOf(campsite.waiting_list.size) }

    val capacity = campsite.getWaitlistCapacity()
    val is_full = capacity != 0 && waitlist_size >= capacity

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (capacity == 0) "$waitlist_size on waitlist" else "$waitlist_size / $capacity on waitlist",
            modifier = Modifier.testTag("waitlist_count")
        )
        Button(
            onClick = {
                val changed = if (is_on_list) {
                    campsite.removeFromWaitingList(entrant)
                    true
                } else {
                    campsite.addToWaitingList(entrant)
                }
                if (changed) {
                    is_on_list = campsite.waiting_list.any { it === entrant }
                    waitlist_size = campsite.waiting_list.size
                    onWaitlistChanged(campsite)
                }
            },
            enabled = is_on_list || !is_full,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (is_on_list) Color.Red else COLOURS.DARKGREEN.colour,
                contentColor =  Color.White
            ),
            modifier = Modifier.padding(top = 8.dp).testTag("waitlist_button")
        ) {
            Text(
                when {
                    is_on_list -> "Leave Waitlist"
                    is_full -> "Waitlist Full"
                    else -> "Join Waitlist"
                }
            )
        }
    }
}

/**
 * Lets a campsite owner set the waitlist capacity. 0 means no limit.
 *
 * @param campsite the campsite being edited.
 * @param onCapacityChanged called after a valid capacity is saved, e.g. to save the campsite to Firestore.
 */
@Composable
fun WaitlistCapacityField(
    campsite: Campsite,
    onCapacityChanged: (Campsite) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var capacity_text by remember { mutableStateOf(campsite.getWaitlistCapacity().toString()) }
    var error_message by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = capacity_text,
                onValueChange = { capacity_text = it },
                label = { Text("Waitlist capacity (0 = no limit)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).testTag("capacity_input")
            )
            Button(
                onClick = {
                    val new_capacity = capacity_text.trim().toIntOrNull()
                    if (new_capacity != null && campsite.setWaitlistCapacity(new_capacity)) {
                        error_message = ""
                        onCapacityChanged(campsite)
                    } else {
                        error_message = "Enter a whole number, 0 or higher."
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = COLOURS.DARKGREEN.colour,
                    contentColor =  Color.White
                ),
                modifier = Modifier.testTag("capacity_save")
            ) {
                Text("Save")
            }
        }
        if (error_message.isNotEmpty()) {
            Text(text = error_message, color = Color.Red, modifier = Modifier.testTag("capacity_error"))
        }
    }
}