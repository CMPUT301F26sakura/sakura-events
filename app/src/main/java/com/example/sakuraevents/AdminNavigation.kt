

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
 * TODO: Connect admin navigation to the team's main navigation.
 * TODO: Replace the temporary owner page with the actual owner interface.
 */


@Composable
fun AdminNavigation (
    campsites: List<Campsite>,
    profiles: List<Entrant>,
    images: List<String>,
    on_remove: (Campsite) -> Unit,  //remove campsite
    on_remove_profile: (Entrant) -> Unit, //remove user
    on_remove_image: (String) -> Unit, //image remove
    firestore_driver: FirestoreDriver? = null
){
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
                },
                on_entrant = {
                    nav_controller.navigate("entrant")
                },
                on_owner = {
                    nav_controller.navigate("owner")
                }
            )
        }

        // Campsite management page
        composable("campsites") {
            AdminCampsiteScreen(
                campsites = campsites,
                on_remove = on_remove,
                on_back = {
                    nav_controller.popBackStack()
                }
            )
        }

        // Profile management page
        composable("profiles") {
            AdminProfileScreen(
                profiles = profiles,
                on_remove = on_remove_profile,
                on_back = {
                    nav_controller.popBackStack()
                }
            )
        }


        // Image management page
        composable("images") {
            AdminImageScreen(
                images = images,
                on_remove = on_remove_image,
                on_back = {
                    nav_controller.popBackStack()
                }
            )
        }

        // Temporary entrant page for testing navigation

        // Admin can switch to the team's entrant interface.
        composable("entrant") {
            Column(
                modifier = Modifier.statusBarsPadding()
            ) {
                Button(onClick = {
                    nav_controller.popBackStack()
                }) {
                    Text("Back to Admin")
                }

                // Reuse the team's existing entrant navigation.
                // Use the same driver instead of creating another one.
                if (firestore_driver != null) {
                    mainNavigator(firestore_driver)
                } else {
                    Text("Entrant interface not connected yet")
                }
            }
        }


        // Temporary owner page for testing navigation no owner screen yet ask teammakes or i make one
        composable("owner") {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Text("Campsite Owner Dashboard")

                Button(onClick = {
                    nav_controller.popBackStack()
                }) {
                    Text("Back to Admin")
                }
            }
        }
    }
}
