
package com.example.sakuraevents

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays the admin dashboard.
 *
 * Admins can access campsite, profile, and image management.
 * The other admin pages will be added as they are implemented.
 */
@Composable
fun AdminScreen(
    on_campsites: () -> Unit,
    on_profiles: () -> Unit,
    on_images: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Text("Admin Dashboard")

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
    }
}