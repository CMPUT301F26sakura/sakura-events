// Purpose: Shows the full details of one of the owner's campsites (US 02.01.04), with a
// button to edit them.
// Written with help from Anthropic, Claude (Claude Code), "Build owner campsite screens for US 02.01.01-02.01.04 and US 02.03.01", 2026-11-05

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
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color

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
    // Name: Devesh Darji
    // The following function / lines of code were generated / modified by Google Gemini Neon Model.
    // Prompt: "How do I gate Edit, Delete, and Waitlist controls on CampsiteDetailsScreen by user role so owners edit their own sites, admins manage all sites, and entrants join waitlists?" 2026-11-22
    // Computed role-based permissions using has_owner_access, has_admin_access, and has_entrant_access, restricting Delete to admins only.
    val current_user = firestore_driver.current_user
    val is_owner_of_site = current_user?.has_owner_access == true &&
            firestore_driver.getOwnedCampsites(current_user.UID).any { it.UID == campsite_uid }
    val can_manage = is_owner_of_site || current_user?.has_admin_access == true

    // Name: Devesh Darji
    // The following function / lines of code were generated / modified by Google Gemini Neon Model.
    // Prompt: "How do I add role-gated Edit, Delete, and Waitlist parameters to campsiteDetailsContent without breaking existing tests?" 2026-11-22
    // Added optional show_edit, show_delete, on_delete, entrant, and on_waitlist_changed parameters with default values.
    campsiteDetailsContent(
        campsite = firestore_driver.campsites[campsite_uid],
        on_back = { nav_controller.popBackStack() },
        on_edit = { nav_controller.navigate("CampsiteForm?uid=$campsite_uid") },
        on_campsite_changed = { updated -> firestore_driver.updateCampsite(updated) },
        show_edit = can_manage,
        show_delete = current_user?.has_admin_access == true,
        on_delete = { campsite ->
            firestore_driver.deleteCampsite(campsite)
            nav_controller.popBackStack()
        },
        entrant = if (current_user?.has_entrant_access == true) current_user else null,
        on_waitlist_changed = { firestore_driver.updateCampsite(it) }
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
fun campsiteDetailsContent(
    campsite: Campsite?,
    on_back: () -> Unit,
    on_edit: () -> Unit,
    on_campsite_changed: (Campsite) -> Unit = {},
    show_edit: Boolean = true,
    show_delete: Boolean = false,
    on_delete: (Campsite) -> Unit = {},
    entrant: Entrant? = null,
    on_waitlist_changed: (Campsite) -> Unit = {}
) {
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

            if (show_edit) {
                CampsiteBookingPeriodField(
                    campsite = campsite,
                    onCampsiteChanged = on_campsite_changed
                )
            }

            if (entrant != null) {
                JoinLotterySection(
                    campsite = campsite,
                    entrant = entrant,
                    onCampsiteChanged = on_campsite_changed
                )
            }

            // Name: Devesh Darji
            // The following function / lines of code were generated / modified by Google Gemini Neon Model.
            // Prompt: "How do I display Edit/Delete buttons and the entrant WaitlistButton together when a user has multiple roles?" 2026-11-22
            // Conditionally rendered the Edit and Delete buttons in a Row and displayed WaitlistButton whenever entrant access is granted.
            if (show_edit || show_delete) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (show_edit) {
                        Button(onClick = on_edit, modifier = Modifier.testTag("editCampsiteButton")) {
                            Text("Edit")
                        }
                    }
                    if (show_delete) {
                        Button(
                            onClick = { on_delete(campsite) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.testTag("deleteCampsiteButton")
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
//            if (entrant != null) {
//                WaitlistButton(
//                    campsite = campsite,
//                    entrant = entrant,
//                    onWaitlistChanged = on_waitlist_changed
//                )
//            }
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