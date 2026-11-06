
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
    on_back: () -> Unit, // Added the back callback tyemporory - Devesh
    on_campsites: () -> Unit,
    on_profiles: () -> Unit,
    on_images: () -> Unit,
    on_entrant: () -> Unit, //dashboards we redesign let user admin adble to sleect user or entrant
    on_owner: () -> Unit
) {
    Column(modifier = Modifier.padding(20.dp)) {
        Spacer(modifier =  Modifier.height(16.dp))
        // 2. Simple Row for Back Button + Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = on_back) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = COLOURS.BLACK.colour
                )
            }
            Text("Admin Dashboard", style = MaterialTheme.typography.headlineMedium)
        }

        // Admin management options
        Button(onClick = on_campsites) {
            Text("Manage Campsites")
        }

        Button(onClick = on_profiles) {
            Text("Manage Profiles")
        }

        Button(onClick = on_images) {
            Text("Manage Images")
        }
        // Admins can also use entrant and campsite owner features.
        Text("Other Roles")

        Button(onClick = on_entrant) {
            Text("Continue as Entrant")
        }

        Button(onClick = on_owner) {
            Text("Continue as Campsite Owner")
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
    on_back: () -> Unit
) {

    Column(

        modifier = Modifier
            .statusBarsPadding()
            .padding(16.dp)

    )

    {
        Button(onClick = on_back) {
            Text("Back")
        }

        Text("Manage Campsites")

        // Display each registered campsite.
        campsites.forEach { campsite ->
            Text(campsite.getName())
            Text(campsite.getLocation())

            // Pass the selected campsite to the removal function.
            Button(onClick = {
                on_remove(campsite)
            }) {
                Text("Remove")
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

        Text("Manage Profiles")

        // Display the details of each user.
        profiles.forEach { profile ->
            Text(profile.getName())
            Text(profile.getEmail())
            Text("Role: ${profile.getRole()}")

            // Remove the selected profile from the list.
            Button(onClick = {
                on_remove(profile)
            }) {
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
