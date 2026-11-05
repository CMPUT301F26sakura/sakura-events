
package com.example.sakuraevents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Handles navigation between admin pages.
 *
 * The management pages are placeholders for now.
 * They will be connected to the real screens and
 * individual admin nav files for now, later can merge if we are doing a unified file
 */
@Composable
fun AdminNavigation() {

    val nav_controller = rememberNavController()

    // Starts at the admin dashboard
    NavHost(navController = nav_controller, startDestination = "admin") {

        composable("admin") {
            AdminScreen(
                on_campsites = {
                    nav_controller.navigate("campsites")
                },
                on_profiles = {
                    nav_controller.navigate("profiles")
                },
                on_images = {
                    nav_controller.navigate("images")
                }
            )
        }

        // Campsite management page
        composable("campsites") {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Manage Campsites")

                Button(onClick = { nav_controller.popBackStack() }) {
                    Text("Back")
                }
            }
        }

        // Profile management page
        composable("profiles") {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Manage Profiles")

                Button(onClick = { nav_controller.popBackStack() }) {
                    Text("Back")
                }
            }
        }

        // Image management page
        composable("images") {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Manage Images")

                Button(onClick = { nav_controller.popBackStack() }) {
                    Text("Back")
                }
            }
        }
    }
}
