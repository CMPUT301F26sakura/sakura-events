
package com.example.sakuraevents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Screen for rating a campsite.
 * Only entrants with completed reservations can submit.
 */
@Composable
fun RatingScreen(
    reservation: Reservation? = null,
    entrant: Entrant? = null,
    firestoreDriver: FirestoreDriver? = null
) {
    var rating by remember { mutableIntStateOf(0) }
    var submitted by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Check if the reservation is completed.
    val canRate = reservation?.getState() == "COMPLETE"

    // Check if this entrant already rated the campsite.
    val alreadyRated = reservation?.getCampsite()?.ratings
        ?.any { pair -> pair.first.UID == entrant?.UID } ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Rate Your Campsite",
            color = Color.Black,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Display five clickable stars.
        Row {
            for (star in 1..5) {
                Text(
                    text = "★",
                    color = if (star <= rating) Color(0xFFFF8C00) else Color.DarkGray,
                    fontSize = 50.sp,
                    modifier = Modifier.clickable(
                        enabled = canRate && !alreadyRated &&
                                !submitted && !isSaving
                    ) {
                        rating = star
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (
                    canRate &&
                    !alreadyRated &&
                    rating > 0 &&
                    entrant != null &&
                    reservation != null &&
                    firestoreDriver != null
                ) {
                    val campsite = reservation.getCampsite()

                    isSaving = true
                    errorMessage = ""

                    // Copy the campsite before changing its ratings.
                    val updatedCampsite = Campsite(
                        UID = campsite.UID,
                        name = campsite.getName(),
                        location = campsite.getLocation(),
                        description = campsite.getDescription(),
                        max_occupancy = campsite.getMaxOccupancy(),
                        qr_code = campsite.getQrCode(),
                        price = campsite.getPrice(),
                        _tags = campsite.tags.toMutableList(),
                        _booking_period_dates =
                            campsite.booking_period_dates.toMutableList(),
                        _reservations = campsite.reservations.toMutableList(),
                        _ratings = campsite.ratings.toMutableList(),
                        average_rating = campsite.getAvgRating().toFloat(),
                        _waiting_list = campsite.waiting_list.toMutableList(),
                        waitlist_capacity = campsite.getWaitlistCapacity()
                    )

                    updatedCampsite.addRating(entrant, rating.toFloat())

                    // Save the updated campsite to Firebase.
                    firestoreDriver.updateCampsite(updatedCampsite)
                        .addOnSuccessListener {
                            submitted = true
                            isSaving = false
                        }
                        .addOnFailureListener {
                            isSaving = false
                            errorMessage =
                                "Failed to save rating. Please try again."
                        }
                }
            },
            enabled = rating > 0 &&
                    canRate &&
                    entrant != null &&
                    firestoreDriver != null &&
                    !submitted &&
                    !alreadyRated &&
                    !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            )
        ) {
            Text(if (isSaving) "Saving..." else "Submit Rating")
        }

        if (submitted) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Rating submitted successfully",
                color = Color(0xFF4CAF50)
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red
            )
        }
    }
}

// Preview only. No Firebase connection.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun RatingScreenPreview() {
    val testCampsite = Campsite(
            UID = "uX5yOhcMYb9fmRdvzZkP",
            name = "Test Campsite",
            location = "Edmonton",
            description = "Testing campsite rating",
            max_occupancy = 4,
            price = 25.0f
        )

        val testEntrant = Entrant(
            UID = "TestEntrant1",
            role = Role.ENTRANT,
            name = "Test User",
            email = "test@example.com",
            phone_number = "1234567890"
        )

        val testReservation = Reservation(
            UID = "testReservation1",
            campsite = testCampsite,
            entrant = testEntrant,
            guest_count = 2,
            beginning_date = "2026-11-01",
            end_date = "2026-11-03",
            reservation_state = ReservationState.COMPLETE
        )

        RatingScreen(
            reservation = testReservation,
            entrant = testEntrant,
            firestoreDriver = remember { FirestoreDriver() }
        )
    }



