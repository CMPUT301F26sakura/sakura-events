package com.example.sakuraevents


import android.R.attr.enabled
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * US 01.04.03
 * Displays pending reservations and allows
 * entrants to decline them.
 *
 * AI Citation:
 * OpenAI ChatGPT, November 26, 2026.
 * Used to help design the screen and decline functionality.
 */
@Composable
fun lotteriesWonScreen(
    firestoreDriver: FirestoreDriver,
    innerPadding: PaddingValues
) {
    val user = firestoreDriver.current_user
    var selected by remember {
        mutableStateOf<Reservation?>(null)
    }
    var message by remember {
        mutableStateOf("")
    }
    var busy by remember {
        mutableStateOf(false)
    }
    // only allow signed in entrants to access this screen.
    if (user == null || !user.has_entrant_access) {
        Text("Entrant access required")
        return
    }
    // Get pending reservations belonging to the current user.
    val reservations = firestoreDriver.reservations.values.filter {
        it.getEntrant().UID == user.UID &&
                it.getState() == "NEW"
    }
    LazyColumn( // Creates a scrollable list
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Check if there is a message to display.
        if (message.isNotEmpty()) {
            item {
                Text(message) // Show the success or error message
            }
        }
        // Show a message if there are no reservations.
        if (reservations.isEmpty()) {
            item {
                Text("No pending reservations")
            }
        }

// Display each reservation in a separate card.
        items(reservations, key = { it.UID }) { reservation ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = COLOURS.GREEN.colour
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp), // space inside the card.
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display the campsite name.
                    Text(
                        reservation.getCampsite().getName(),
                        style = MaterialTheme.typography.titleLarge
                    )
                    // Display the reservation dates.
                    Text("Beginning: ${reservation.getBeginningDate()}")
                    Text("Ending: ${reservation.getEndDate()}")

                    // Select the reservation to decline.
                    Button(
                        onClick = { selected = reservation },
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Decline")
                    }
                }
            }

        }

    }
    selected?.let { reservation ->

        AlertDialog(
            onDismissRequest = {
                if (!busy) selected = null // Close the dialog
            },
            title = {
                Text("Decline Reservation?") // Display the dialog title
            },
            text = {
                // Ask the user to confirm.
                Text("Are you sure you want to decline?")
            },

            confirmButton = {
                TextButton(
                    enabled = !busy, // Prevent repeated clicks
                    onClick = {
                        busy = true
                        // Decline the selected reservation in Firebase.
                        firestoreDriver.declineReservation(reservation.UID)
                            .addOnSuccessListener {
                                message = "Reservation declined successfully"
                                selected = null // Close the dialog
                                busy = false // Finish processing
                            }
                            .addOnFailureListener {
                                message = "Unable to decline reservation"
                                selected = null // Close the dialog
                                busy = false // Allow the user to try again
                            }
                    }
                ) {
                    Text("Decline", color = Color.Red)
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        selected = null // Close the dialog
                    }
                ) {
                    Text("Keep Reservation")

                }
            }
        )
    }
}




