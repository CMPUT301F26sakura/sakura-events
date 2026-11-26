

package com.example.sakuraevents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController



/**
 * File: AdminNavigation.kt
 * Purpose: Handles navigation between the admin management pages
 * and allows an admin to switch to entrant or campsite owner mode. the admin later on is assigned a firebase account admin, and would open
 * up the admin screen, in admin screen dashboard it allows admin to switch to both the user and owner roles and come back,
 * so a normal user cant get to admin curr, maybe add a button? to show or integrate firebase roles ?
 *
 * User Stories Addressed:
 * - US 03.01.01: As an admin, I want to remove campsites.
 * - US 03.02.01: As an admin, I want to remove profiles.
 * - US 03.03.01: As an admin, I want to remove images. ( in ui and data test yes actual images no later half)
 * - US 03.04.01: As an admin, I want to browse campsites.
 * - US 03.05.01: As an admin, I want to browse profiles.
 * - US 03.11.01: As an admin, I want to also be an entrant or campsite owner.
 *
 */

@Composable
fun AdminNavigation(
    firestore_driver: FirestoreDriver,
    on_exit_admin: () -> Unit,
    on_open_entrant: () -> Unit = {},
    on_open_owner: () -> Unit = {},
    on_edit_campsite: (Campsite) -> Unit = {}
) {
    val nav_controller = rememberNavController()


    // Starts at the admin dashboard
    NavHost(navController = nav_controller, startDestination = "admin") {

        composable("admin") {
            AdminScreen(
                on_back = on_exit_admin,
                on_manage_campsites = { nav_controller.navigate("campsites") },
                on_manage_profiles = { nav_controller.navigate("profiles") },
                on_manage_images = { nav_controller.navigate("images") },
            )
        }

        // Campsite management page
        composable("campsites") {
            AdminCampsiteScreen(
                campsites = firestore_driver.campsites.values.toList(), // Pull real data from driver
                on_remove = { campsite ->
                    // Delete from Firebase
                    firestore_driver.deleteCampsite(campsite)
                },
                on_back = { nav_controller.popBackStack() },
                on_edit = on_edit_campsite
            )
        }

        // Profile management page
        composable("profiles") {
            AdminProfileScreen(
                profiles = firestore_driver.all_users.values.toList(), // Pull real data from driver
                on_remove = { profile ->
                    // Delete from Firebase
                    firestore_driver.deleteEntrant(profile)
                },
                on_back = { nav_controller.popBackStack() },
                on_change_role = { profile, new_role ->
                    firestore_driver.updateUserRole(profile, new_role)
                }
            )
        }

        // Image management page
        composable("images") {
            AdminImageScreen(
                images = listOf("image1.png", "image2.png"), // Mock images for now
                on_remove = { /* TODO in second half */ },
                on_back = { nav_controller.popBackStack() }
            )
        }
    }
}
