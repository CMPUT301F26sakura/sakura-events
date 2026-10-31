package com.example.sakuraevents

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier


/**
 * Displays the campsite rating screen.
 * Issue #60: Allow entrant to rate a campsite.
 *
 * TODO: Add star selection and reservation eligibility.
 */
@Composable
fun CampsiteRatingScreen() {
    var rating by remember { mutableStateOf(0) }
    Column {
        // Display the screen title.
        Text(text = "Rate Your Campsite")
        Row {
            for (star in 1..5){
                Text(
                    text = if (star <= rating) " ★ " else " ☆ ",
                    modifier = Modifier.clickable {
                        rating = star
                    }
                )
            }

        }
        // Display the rating buttons.
        RatingButton()
    }
}
@Composable
fun RatingButton() {
    Button(onClick = {}){
        Text("Submit")
    }
}
