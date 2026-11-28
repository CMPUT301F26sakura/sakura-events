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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cabin
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

enum class COLOURS(val colour: Color) {
    LIGHTYELLOW(Color(0xFFEEEEB3)),
    DARKYELLOW(Color(0xFFD8D870)),
    GREEN(Color(0xFF62B266)),
    DARKGREEN(Color(0xFF419874)),
}

@Composable
fun mainNavigator(firestoreDriver: FirestoreDriver, modifier: Modifier = Modifier) {
    val nav_controller = rememberNavController()
    val nav_current_route by nav_controller.currentBackStackEntryAsState()
    val current_location = nav_current_route?.destination?.route
    val current_user = firestoreDriver.current_user

    // Name: Devesh Darji
    // The following function / lines of code were generated / modified by Google Gemini Neon Model.
    // Prompt: "How do I redirect users off restricted screens if their role is turned off in Firebase, and fix duplicate top bars on owner/admin screens?" 2026-11-22
    // Added a real-time role-gating LaunchedEffect, hid MainNavigator's topBar on screens with their own top bar, and padded nested routes inside NavHost.
    // Immediately leave a role-restricted screen if the role flag is turned off in Firebase
    val admin_routes = listOf("AdminDashboard", "AdminCampsites", "AdminProfiles", "AdminImages")

    LaunchedEffect(current_user?.role, current_location) {
        if (current_user == null) return@LaunchedEffect
        val blocked =
            (current_location == "Search" && !current_user.has_entrant_access) ||
                    (current_location == "OwnerCampsites" && !current_user.has_owner_access) ||
                    (current_location?.startsWith("CampsiteForm") == true && !current_user.has_owner_access && !current_user.has_admin_access) ||
                    (current_location in admin_routes && !current_user.has_admin_access)
        if (blocked) {
            nav_controller.navigate("Home") { popUpTo("Home") { inclusive = true } }
        }
    }

    val has_own_top_bar = current_location in listOf(
        "AdminDashboard",
        "AdminCampsites",
        "AdminProfiles",
        "AdminImages",
        "OwnerCampsites",
        "CampsiteDetails/{uid}",
        "CampsiteDetailScreen/{UID}",
        "CampsiteForm?uid={uid}"
    )

    Scaffold(
        topBar = {
            if (!has_own_top_bar) {
                topBar(nav_controller, current_location)
            }
        },
        bottomBar = {
            bottomBar(nav_controller, current_location, current_user)
        },
        containerColor = COLOURS.LIGHTYELLOW.colour
    ) { innerPadding ->
        // put your US stories code under
        NavHost(navController = nav_controller, startDestination = "Home") {
            composable("Home") { homeScreen(modifier, innerPadding) }
            composable("Search") { searchScreen(nav_controller, firestoreDriver.campsites.values.toList(), modifier, innerPadding) }
            // Allows entrants to rate and review a campsite after completing their reservation.
            composable("Rating/{reservationUID}") { backStackEntry ->
                val reservationUID = backStackEntry.arguments ?.getString("reservationUID")
                val entrant = firestoreDriver.current_user
                val reservation = firestoreDriver.reservations[reservationUID]?.takeIf { it.getEntrant().UID == entrant?.UID && it.getState() == "COMPLETE" }
                Box(modifier = Modifier.padding(innerPadding)) { RatingScreen(reservation = reservation, entrant = entrant, firestoreDriver = firestoreDriver)} }
            composable("Profile") { profileScreen(nav_controller, firestoreDriver, innerPadding) }
            // US 01.04.03: Open the Lotteries Won screen.
            composable("LotteriesWon") {lotteriesWonScreen(firestoreDriver, innerPadding) }
            // Notifications page (US 01.03.01, 01.03.02, 01.10.01, 02.10.02). Written with help from Anthropic, Claude, "Notifications page route", 2026-11-25
            composable("AdminCampsites") {
                Box(modifier = Modifier.padding(innerPadding)) {
                    AdminCampsiteScreen(
                        campsites = firestoreDriver.campsites.values.toList(),
                        on_remove = { campsite -> firestoreDriver.deleteCampsite(campsite) },
                        on_back = { nav_controller.popBackStack() },
                        on_edit = { campsite -> nav_controller.navigate("CampsiteForm?uid=${campsite.UID}") }
                    )
                }
            }
            composable("AdminProfiles") {
                Box(modifier = Modifier.padding(innerPadding)) {
                    AdminProfileScreen(
                        profiles = firestoreDriver.all_users.values.toList(),
                        on_remove = { profile -> firestoreDriver.deleteEntrant(profile) },
                        on_back = { nav_controller.popBackStack() },
                        on_change_role = { profile, new_role ->
                            firestoreDriver.updateUserRole(profile, new_role)
                        }
                    )
                }
            }
            composable("AdminImages") {
                Box(modifier = Modifier.padding(innerPadding)) {
                    AdminImageScreen(
                        images = listOf("image1.png", "image2.png"),
                        on_remove = { /* TODO */ },
                        on_back = { nav_controller.popBackStack() }
                    )
                }
            }
            composable("Notifications") { notificationsScreen(firestoreDriver, innerPadding) }
            composable("AdminDashboard") {
                Box(modifier = Modifier.padding(innerPadding)) {
                    AdminNavigation(
                        firestore_driver = firestoreDriver,
                        on_exit_admin = { nav_controller.navigate("Home") },
                        on_edit_campsite = { campsite -> nav_controller.navigate("CampsiteForm?uid=${campsite.UID}") }
                    )
                }
            }

            // The following was written with help from Anthropic, Claude (Claude Code), "Add owner campsite support", 2026-11-05
            // Owner campsite pages (US 02.01.01 - 02.01.04, US 02.03.01)
            composable("OwnerCampsites") {
                Box(modifier = Modifier.padding(innerPadding)) {
                    ownerCampsiteListScreen(nav_controller, firestoreDriver)
                }
            }
            composable("CampsiteDetails/{uid}") { entry ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    campsiteDetailsScreen(
                        nav_controller,
                        firestoreDriver,
                        entry.arguments?.getString("uid") ?: ""
                    )
                }
            }
            composable(
                "CampsiteForm?uid={uid}",
                arguments = listOf(navArgument("uid") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                })
            ) { entry ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    campsiteFormScreen(nav_controller, firestoreDriver, entry.arguments?.getString("uid"))
                }
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
                arguments = listOf(navArgument("UID") { type = NavType.StringType })
            ) { uid ->
                val UID = uid.arguments?.getString("UID") ?: ""
                Box(modifier = Modifier.padding(innerPadding)) {
                    campsiteDetailsScreen(nav_controller, firestoreDriver, UID)
                }
            }
        }
    }
}

// The following are common composable elements.
@Composable
// Name: Devesh Darji
// The following function / lines of code were generated / modified by Google Gemini Neon Model.
// Prompt: "How do I show and hide bottom bar tabs dynamically based on entrant, owner, and admin role flags?" 2026-11-22
// Passed current_user into bottomBar and conditionally rendered the Search, My Campsites, and Admin Dashboard tabs using has_*_access flags.
fun bottomBar(nav_controller: NavController, current_location: String?, current_user: Entrant?) {
    BottomAppBar(
        containerColor = COLOURS.DARKYELLOW.colour
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { nav_controller.navigate("Home") { launchSingleTop = true } }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (current_location == "Home") COLOURS.LIGHTYELLOW.colour else Color.Transparent),
                    tint = Color.Black
                )
            }
            if (current_user?.has_entrant_access == true) {
                IconButton(onClick = { nav_controller.navigate("Search") { launchSingleTop = true } }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier
                            .size(40.dp)
                            .background(if (current_location == "Search") COLOURS.LIGHTYELLOW.colour else Color.Transparent),
                        tint = Color.Black
                    )
                }
            }
            IconButton(onClick = { nav_controller.navigate("Profile") { launchSingleTop = true } }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (current_location == "Profile") COLOURS.LIGHTYELLOW.colour else Color.Transparent),
                    tint = Color.Black
                )
            }
            if (current_user?.has_owner_access == true) {
                IconButton(onClick = { nav_controller.navigate("OwnerCampsites") { launchSingleTop = true } }) {
                    Icon(
                        imageVector = Icons.Default.Cabin,
                        contentDescription = "My Campsites",
                        modifier = Modifier
                            .size(40.dp)
                            .background(if (current_location == "OwnerCampsites") COLOURS.LIGHTYELLOW.colour else Color.Transparent),
                        tint = Color.Black
                    )
                }
            }
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
@Composable
// Name: Devesh Darji
// The following function / lines of code were generated / modified by Google Gemini Neon Model.
// Prompt: "How do I fix topBar crashing when navigating to owner campsite routes?" 2026-11-22
// Added OwnerCampsites, CampsiteDetails, and CampsiteForm to display_names, made the title lookup null-safe, and hid the back arrow on OwnerCampsites.
fun topBar(nav_controller: NavController, current_location: String?) {
    val display_names: Map<String, String> = mapOf(
        "Home" to "Home",
        "Search" to "Search",
        "Profile" to "Profile",
        "LotteriesWon" to "Lotteries Won", // Set the screen title
        "Notifications" to "Notifications",
        "OwnerCampsites" to "My Campsites",
        "AdminDashboard" to "Admin Dashboard",
        "CampsiteDetails/{uid}" to "Campsite Details",
        "CampsiteDetailScreen/{UID}" to "Campsite Details",
        "CampsiteForm?uid={uid}" to "Campsite Form",
        "" to ""
    )
    CenterAlignedTopAppBar(
        title = {
            Text(display_names[current_location] ?: "")
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = COLOURS.LIGHTYELLOW.colour
        ),
        navigationIcon = {
            if (current_location !in listOf("Home", "Search", "Profile", "OwnerCampsites", "AdminDashboard")) {
                IconButton(onClick = { nav_controller.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        // Bell icon that opens the Notifications page. Written with help from Anthropic, Claude, "Notifications bell in top bar", 2026-11-25
        actions = {
            if (current_location != "Notifications") {
                IconButton(
                    onClick = { nav_controller.navigate("Notifications") { launchSingleTop = true } },
                    modifier = Modifier.testTag("notifications_bell")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Black
                    )
                }
            }
        }
    )
}

@Composable
fun standardButton(clicked: () -> Unit, test_tag: String, button_text: String, icon: ImageVector? = null) {
    Button(
        onClick = { clicked() },
        Modifier.testTag((test_tag)),
        colors = ButtonColors(
            containerColor = COLOURS.DARKGREEN.colour,
            contentColor = Color.White,
            disabledContainerColor = Color.DarkGray,
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = "Back"
            )
        }
        Text(
            text = button_text,
            fontSize = 20.sp,
            color = Color.White
        )
    }
}