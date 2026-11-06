/**
 * File: MainNavigator.kt
 * Purpose: Configures the Jetpack Compose NavHost connecting Home, Search, Profile,
 * and Admin screens, and defines the shared COLOURS palette.
 *
 * User Stories Addressed:
 * - US 01.01.01 - US 01.02.02 (Search, Filter, Campsite Details, Waiting List)
 * - US 01.07.03, US 01.09.01 (Profile & Device Identification)
 * - US 02.01.01 - US 02.05.01 (Campsite Owner Management & Lotteries)
 * - US 03.01.01 - US 03.11.01 (Admin Dashboard & Moderation)
 */

package com.example.sakuraevents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

enum class COLOURS(val colour: Color){
    LIGHTYELLOW(Color(0xFFEEEEB3)),
    DARKYELLOW(Color(0xFFD8D870)),
    GREEN(Color(0xFF62B266)),
    DARKGREEN(Color(0xFF419874)),
    BLACK(Color(0xFF000000)),
    WHITE(Color(0xFFFFFFFF))
}
@Composable
fun mainNavigator(firestoreDriver: FirestoreDriver, modifier: Modifier = Modifier) {
    val nav_controller = rememberNavController()
    val search_screen = SearchScreen(nav_controller, firestoreDriver, modifier)

    NavHost(navController = nav_controller, startDestination = "Home"){
        composable("Home") { homeScreen(nav_controller, firestoreDriver, modifier) }
        composable("Search") { search_screen.searchScreen() }
        
        // Keep your updated Profile route
        composable("Profile") { profileScreen(nav_controller, firestoreDriver) }
        
        // Keep your Admin route
        composable("AdminDashboard") { 
            AdminNavigation(firestore_driver = firestoreDriver, on_exit_admin = { nav_controller.popBackStack() })
        }

        // Restore the incoming Owner routes
        composable("OwnerCampsites") { ownerCampsiteListScreen(nav_controller, firestoreDriver) }
        composable("CampsiteDetails/{uid}") { entry ->
            campsiteDetailsScreen(
                nav_controller,
                firestoreDriver,
                entry.arguments?.getString("uid") ?: ""
            )
        }
        composable(
            "CampsiteForm?uid={uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { entry ->
            campsiteFormScreen(nav_controller, firestoreDriver, entry.arguments?.getString("uid"))
        }
    }
}
