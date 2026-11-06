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



