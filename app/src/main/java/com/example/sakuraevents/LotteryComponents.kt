/**
 * Purpose: 
 * Contains reusable UI components for the Lottery management system. This file currently 
 * houses the UI for setting the number of lottery winners.
 * 
 * User Stories Addressed:
 * - US 02.04.05: As a campsite owner, I want to set the number of lottery winners.
 * 
 * Outstanding Issues:
 * - None.
 */

package com.example.sakuraevents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Campsite's owner can set the winner count for the lottery
 *
 * @param lottery the lottery will be setted with a new winner count if it is a valid input and confirmatiom
 * @param onLotteryChanged called to update the firestore after valid input and confirmation
 */

@Composable
fun LotteryCapacityField(
    lottery: Lottery,
    onLotteryChanged: (Lottery) -> Unit,
    modifier: Modifier = Modifier
) {
    var capacity by remember { mutableStateOf(lottery.getCapacity().toString()) }
    var error_message by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = capacity,
                onValueChange = { capacity = it },
                label = { Text("Winner Count:") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).testTag("winner_count_input")
            )

            Button(
                onClick = {
                    val new_capacity = capacity.trim().toIntOrNull()
                    if ((new_capacity != null) && (new_capacity >= 0)) {
                        lottery.receiveCapacity(new_capacity)
                        error_message = ""
                        onLotteryChanged(lottery)
                    } else {
                        error_message = "Enter an integer(greater than zero)"
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = COLOURS.DARKGREEN.colour,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("lottery_winner_count_saved")
            ) {
                Text("Comfirm")
            }
        }
        if(error_message.isNotEmpty()){
            Text(text = error_message, modifier = Modifier.testTag("setting_winner_count_error") )
        }
    }
}
@Composable
fun JoinLotterySection(
    campsite: Campsite,
    entrant: Entrant?,
    onCampsiteChanged: (Campsite) -> Unit,
    modifier: Modifier = Modifier
) {
    val current_count = campsite.waiting_list.size
    val max_occupancy = campsite.getMaxOccupancy()
    val waitlist_cap = campsite.getWaitlistCapacity()

    val joined_entrant = entrant?.let { e ->
        campsite.waiting_list.firstOrNull { it.UID == e.UID }
    }
    val is_already_joined = joined_entrant != null
    val reached_max_occupancy = current_count >= max_occupancy
    val reached_waitlist_cap = waitlist_cap > 0 && current_count >= waitlist_cap
    val can_join = entrant != null && !is_already_joined && !reached_max_occupancy && !reached_waitlist_cap

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.testTag("lottery_waiting_list_info")) {
            Text(text = "Lottery Waiting List", fontWeight = FontWeight.Bold)
            Text(text = "$current_count / $max_occupancy entrants")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (entrant != null && campsite.addToWaitingList(entrant)) {
                        onCampsiteChanged(campsite)
                    }
                },
                enabled = can_join,
                colors = ButtonDefaults.buttonColors(
                    containerColor = COLOURS.DARKGREEN.colour,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("join_lottery_button")
            ) {
                Text(
                    when {
                        is_already_joined -> "Joined Lottery"
                        reached_max_occupancy || reached_waitlist_cap -> "Lottery Full"
                        entrant == null -> "No Entrant Signed In"
                        else -> "Join Lottery"
                    }
                )
            }

            Button(
                onClick = {
                    if (joined_entrant != null) {
                        campsite.removeFromWaitingList(joined_entrant)
                        onCampsiteChanged(campsite)
                    }
                },
                enabled = is_already_joined,
                modifier = Modifier.testTag("leave_lottery_button")
            ) {
                Text("Leave Lottery")
            }
        }
    }
}