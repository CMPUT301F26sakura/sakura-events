

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
 * Handles navigation between the admin pages.
 *
 * Admins can manage campsites, profiles, and images.
 * They can also switch to the entrant or owner interface.
 *
 * TODO: Connect admin navigation with the team's main navigation.
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
