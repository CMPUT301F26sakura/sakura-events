
package com.example.sakuraevents

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

/**
 * File: AdminScreen.kt
 * Purpose: Contains the Compose screens for the admin dashboard
 * and campsite, profile, and image management.
 *
 * User Stories Addressed:
 * - US 03.01.01: As an admin, I want to remove campsites.
 * - US 03.02.01: As an admin, I want to remove profiles.
 * - US 03.03.01: As an admin, I want to remove images.
 * - US 03.04.01: As an admin, I want to browse campsites.
 * - US 03.05.01: As an admin, I want to browse profiles.
 * - US 03.11.01: As an admin, I want to also be an entrant or campsite owner.
 *
 * -image remove is not connected to firebase only in ui and test as this owuld require more complexity to images and firestore driver as that is latter half
 */

@Composable
fun AdminScreen(
    on_back: () -> Unit = {},
    on_manage_campsites: () -> Unit = {},
    on_manage_profiles: () -> Unit = {},
    on_manage_images: () -> Unit = {},
    on_continue_as_entrant: () -> Unit = {},
    on_continue_as_owner: () -> Unit = {},
    show_entrant_button: Boolean = true,
    show_owner_button: Boolean = true
) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = on_back) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text("Admin Dashboard", style = MaterialTheme.typography.headlineMedium)
        }

        Button(onClick = on_manage_campsites) { Text("Manage Campsites") }
        Button(onClick = on_manage_profiles) { Text("Manage Profiles") }
        Button(onClick = on_manage_images) { Text("Manage Images") }

        if (show_entrant_button || show_owner_button) {
            Text("Other Roles")
        }
        if (show_entrant_button) {
            Button(onClick = on_continue_as_entrant) { Text("Continue as Entrant") }
        }
        if (show_owner_button) {
            Button(onClick = on_continue_as_owner) { Text("Continue as Campsite Owner") }
        }
    }
}


/**
 * Shows the list of registered campsites.
 *
 * Admins can view the campsites stored in the database.
 *
 * @param campsites List of registered campsites.
 * @param on_back Returns to the admin dashboard.
 */
@Composable
fun AdminCampsiteScreen(
    campsites: List<Campsite>,
    on_remove: (Campsite) -> Unit,
    on_back: () -> Unit,
    on_edit: (Campsite) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = on_back) { Text("Back") }
        Text("Manage Campsites", style = MaterialTheme.typography.headlineSmall)

        if (campsites.isEmpty()) {
            Text("No campsites found.")
        }

        campsites.forEach { campsite ->
            Text(campsite.getName(), style = MaterialTheme.typography.titleMedium)
            Text(campsite.getLocation())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { on_edit(campsite) }) {
                    Text("Edit")
                }
                Button(
                    onClick = { on_remove(campsite) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

/**
 * Shows all registered user profiles.
 *
 * Admins can view and remove entrants, owners, and admins.
 *
 * @param profiles List of registered profiles.
 * @param on_remove Removes the selected profile.
 * @param on_back Returns to the admin dashboard.
 */
@Composable
fun AdminProfileScreen(
    profiles: List<Entrant>,
    on_remove: (Entrant) -> Unit,
    on_back: () -> Unit,
    on_change_role: (Entrant, Role) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = on_back) {
            Text("Back")
        }

        Text("Manage Profiles", style = MaterialTheme.typography.headlineSmall)

        // Display the details of each user.
        profiles.forEach { profile ->
            Text(profile.getName(), style = MaterialTheme.typography.titleMedium)
            Text(profile.getEmail())
            Text("Role: ${profile.getRole()}")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { on_change_role(profile, Role.ENTRANT) }) { Text("Entrant") }
                Button(onClick = { on_change_role(profile, Role.OWNER) }) { Text("Owner") }
                Button(onClick = { on_change_role(profile, Role.ADMIN) }) { Text("Admin") }
            }

            // Remove the selected profile from the list.
            Button(
                onClick = { on_remove(profile) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Remove")
            }
        }

        if (profiles.isEmpty()) {
            Text("No profiles found.")
        }
    }
}


/**
 * Shows the list of uploaded images.
 *
 * Admins can view and remove images.
 *
 * @param images List of uploaded image identifiers.
 * @param on_remove Removes the selected image.
 * @param on_back Returns to the admin dashboard.
 */
@Composable
fun AdminImageScreen(
    images: List<String>,
    on_remove: (String) -> Unit,
    on_back: () -> Unit
) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Button(onClick = on_back) {
            Text("Back")
        }

        Text("Manage Images")

        // Display each image and its Remove button.
        images.forEach { image ->
            Text(image)

            // Pass the selected image to the removal function.
            Button(onClick = {
                on_remove(image)
            }) {
                Text("Remove")
            }
        }

        if (images.isEmpty()) {
            Text("No images found.")
        }
    }
}
