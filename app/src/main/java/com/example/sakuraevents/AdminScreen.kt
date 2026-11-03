

package com.example.sakuraevents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays the admin management screen.
 *
 * Allows admins to browse and remove campsites, profiles,
 * and images, as well as view notification and message logs.
 *
 * Currently working on campsite management.
 */
@Composable
fun AdminScreen() {

    val repository = remember { AdminRepository() }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Admin - Campsites")

        // Display the campsites from Firestore
        repository.campsites.forEach { campsite ->
            Text(campsite.name)
            Text(campsite.location)

            // Remove the campsite when Delete is pressed
            Button(onClick = {
                repository.deleteCampsite(campsite.id)
            }) {
                Text("Delete")
            }
        }

        // TODO: Profile management
        // Browse user profiles (US 03.05.01)
        // Remove user profiles (US 03.02.01)

        // TODO: Image management
        // Browse uploaded images (US 03.06.01)
        // Remove uploaded images (US 03.03.01)

        // TODO: Campsite owner management
        // Remove owners who violate app policy (US 03.07.01)

        // TODO: Notification logs
        // View all notification logs (US 03.08.01)

        // TODO: Message logs
        // View owner-to-entrant messages (US 03.12.01)

        // TODO: Admin roles
        // Allow admins to use owner and entrant features (US 03.11.01)
    }
}
