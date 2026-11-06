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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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

    NavHost(navController = nav_controller, startDestination = "Home") {
        composable("Home") { homeScreen(nav_controller, firestoreDriver, modifier) }
        composable("Search") { search_screen.searchScreen() }
        composable("Profile") { profileScreen(nav_controller, firestoreDriver) }
        composable("AdminDashboard") {
            AdminNavigation(firestore_driver = firestoreDriver, on_exit_admin = { nav_controller.popBackStack() })
        }

        // The following was written with help from Anthropic, Claude (Claude Code), "Add owner campsite support", 2026-11-05
        // Owner campsite pages (US 02.01.01 - 02.01.04, US 02.03.01)
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
            arguments = listOf(navArgument("uid") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { entry ->
            campsiteFormScreen(nav_controller, firestoreDriver, entry.arguments?.getString("uid"))
        }
        /**
         * AI Citation:
         * I, David Schumacher, queried duck.ai on how to get a nav_controller composable to take an
         * argument. I wrote:
            * kotlin compose how to get a NavHost object to take an argument when its .navigate
            * method is invoked?
         * From this it suggested that I use the /{} notation to create extractable values from the
         * route. I used this notation and modified it to match our requirements. The original
         * example the AI gave was to convert /{id} into an Int.
         */
        composable(
            route = "CampsiteDetailScreen/{UID}",
            arguments = listOf(navArgument("UID") { type = NavType.StringType})) { uid ->
            val UID = uid.arguments?.getString("UID") as String
            campsiteDetailsScreen(nav_controller, firestoreDriver, UID) }
    }
}

@Composable
fun bottomBar(nav_controller: NavController) {
    val nav_current_route by nav_controller.currentBackStackEntryAsState()
    val current_icon = nav_current_route?.destination?.route

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = { nav_controller.navigate("Home") }) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                modifier = Modifier
                    .size(40.dp)
                    .background(if (current_icon == "Home") COLOURS.LIGHTYELLOW.colour else Color.Transparent),
                tint =  Color.Black
            )
        }
        IconButton(onClick = { nav_controller.navigate("Search") }) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier
                    .size(40.dp)
                    .background(if (current_icon == "Search") COLOURS.LIGHTYELLOW.colour else Color.Transparent),
                tint = Color.Black
            )
        }
        IconButton(onClick = { nav_controller.navigate("Profile") }) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(40.dp)
                    .background(if (current_icon == "Profile") COLOURS.LIGHTYELLOW.colour else Color.Transparent),
                tint = Color.Black
            )
        }
    }
}

/**
 * AI use citation:
 * I David Schumacher used duck.ai to generate part of this code. I asked it:
 * kotlin compose how to add a back arrow to the top left of a top bar defined using
 * "Scaffold( topBar = { CenterAlignedTopAppBar ..." I know I can use icons, and even make them
 * clickable. But I am unsure how exactly otherwise to get multiple elements to show up and have
 * one of them interactable.
 * and it returned that CenterAlignedTopAppBar has a built in feature for this called "navigationIcon".
 * The AI suggested I use Icons.AutoMirrored.Filled.ArrowBack for the icon, which I did.
 * The AI suggested I use nav_controller.popBackStack() which I did.
 *
 * I reused Hunter's button icon code from his original bottom bar implementation.
 *
 * I fully understand this code and can answer questions about it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun topBar(nav_controller: NavController) {
    val nav_current_route by nav_controller.currentBackStackEntryAsState()
    val current_icon = nav_current_route?.destination?.route

    CenterAlignedTopAppBar(
        title = { if (current_icon in listOf("Home", "Search", "Profile")) {
            Text(current_icon as String)
        }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = COLOURS.LIGHTYELLOW.colour
        ),
        navigationIcon = {
            if (current_icon !in listOf("Home", "Search", "Profile")) {
                IconButton(onClick = { nav_controller.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}