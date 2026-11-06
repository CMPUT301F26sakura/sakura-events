// Purpose: Shows the full details of one of the owner's campsites (US 02.01.04), with a
// button to edit them.

package com.example.sakuraevents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Shows a campsite's name, location, description and amenities, max occupancy, price, and tags.
 * The "Edit" button opens the campsite form filled in with these values.
 *
 * @param nav_controller the app's NavController, used to move between pages.
 * @param firestore_driver the app's FirestoreDriver, which holds the loaded campsites.
 * @param campsite_uid the UID of the campsite to show.
 */
@Composable
fun campsiteDetailsScreen(nav_controller: NavController, firestore_driver: FirestoreDriver, campsite_uid: String) {
    val campsite = firestore_driver.campsites[campsite_uid]

    Scaffold(
        topBar = { ownerTopBar("Campsite Details", nav_controller) },
        containerColor = COLOURS.LIGHTYELLOW.colour
    ) { inner_padding ->
        Column(
            modifier = Modifier
                .padding(inner_padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (campsite == null) {
                Text("Campsite not found. It may still be loading.")
                return@Column
            }

            detailRow("Name", campsite.getName())
            detailRow("Location", campsite.getLocation())
            detailRow("Description and amenities", campsite.getDescription().ifBlank { "None" })
            detailRow("Max occupancy", campsite.getMaxOccupancy().toString())
            detailRow("Price per night", "$%.2f".format(campsite.getPrice()))
            detailRow("Tags", if (campsite.tags.isEmpty()) "None" else campsite.tags.joinToString(", "))

            Button(onClick = { nav_controller.navigate("CampsiteForm?uid=${campsite.UID}") }) {
                Text("Edit")
            }
        }
    }
}

/**
 * Shows a bold label with its value underneath, for one campsite detail.
 *
 * @param label the name of the detail, e.g. "Location".
 * @param value the detail's value.
 */
@Composable
fun detailRow(label: String, value: String) {
    Column {
        Text(text = label, fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}