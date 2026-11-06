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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * The campsite details page used by the app. Looks the campsite up in FirestoreDriver and
 * opens the edit form when "Edit" is tapped.
 *
 * @param nav_controller the app's NavController, used to move between pages.
 * @param firestore_driver the app's FirestoreDriver, which holds the loaded campsites.
 * @param campsite_uid the UID of the campsite to show.
 */
@Composable
fun campsiteDetailsScreen(nav_controller: NavController, firestore_driver: FirestoreDriver, campsite_uid: String) {
    campsiteDetailsContent(
        campsite = firestore_driver.campsites[campsite_uid],
        on_back = { nav_controller.popBackStack() },
        on_edit = { nav_controller.navigate("CampsiteForm?uid=$campsite_uid") }
    )
}

/**
 * Draws a campsite's name, location, description and amenities, max occupancy, price, and tags,
 * with an "Edit" button. It does not load anything itself, so tests can use it with fake data.
 *
 * @param campsite the Campsite to show, or null if it was not found.
 * @param on_back what to do when the back arrow is tapped.
 * @param on_edit what to do when "Edit" is tapped.
 */
@Composable
fun campsiteDetailsContent(campsite: Campsite?, on_back: () -> Unit, on_edit: () -> Unit) {
    Scaffold(
        topBar = { ownerTopBar("Campsite Details", on_back) },
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

            Button(onClick = on_edit, modifier = Modifier.testTag("editCampsiteButton")) {
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