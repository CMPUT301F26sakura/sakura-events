// Purpose: Shows the current campsite owner's list of campsites (US 02.03.01) and lets them
// start registering a new one. Also holds the shared top bar used by the owner campsite screens.

package com.example.sakuraevents

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// TODO: Replace with the logged-in owner's UID once sign-in is implemented.
/** The UID of the owner whose campsites these screens show, until there is a login. */
const val TEST_OWNER_UID = "0"

/**
 * The top bar used by the owner campsite screens: a title in the middle and a back arrow
 * on the left.
 *
 * @param title the text shown in the middle of the bar.
 * @param on_back what to do when the back arrow is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ownerTopBar(title: String, on_back: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = on_back) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = COLOURS.BLACK.colour
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = COLOURS.LIGHTYELLOW.colour
        )
    )
}

/**
 * The "My Campsites" page used by the app. Looks up the current owner and their campsites in
 * FirestoreDriver. If the test owner does not exist yet, debug builds show a button that creates it.
 *
 * @param nav_controller the app's NavController, used to move between pages.
 * @param firestore_driver the app's FirestoreDriver, which holds the loaded owners and campsites.
 */
@Composable
fun ownerCampsiteListScreen(nav_controller: NavController, firestore_driver: FirestoreDriver) {
    val owner = firestore_driver.owners[TEST_OWNER_UID]
    // True for builds run from Android Studio, false for release builds.
    val is_debug_build = (LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    // Look each campsite up by UID so the list always shows the latest loaded values.
    val owned_campsites = owner?.owned_campsites?.mapNotNull { firestore_driver.campsites[it.UID] } ?: emptyList()

    ownerCampsiteListContent(
        owner_found = owner != null,
        campsites = owned_campsites,
        show_debug_button = is_debug_build,
        on_back = { nav_controller.popBackStack() },
        on_create_test_owner = {
            firestore_driver.updateOwner(
                CampsiteOwner(
                    UID = TEST_OWNER_UID,
                    role = Role.OWNER,
                    name = "Test Owner",
                    email = "test.owner@example.com",
                    phone_number = "780-555-0000"
                )
            )
        },
        on_register = { nav_controller.navigate("CampsiteForm") },
        on_campsite_click = { campsite -> nav_controller.navigate("CampsiteDetails/${campsite.UID}") }
    )
}

/**
 * Draws the owner's campsite list with a "Register campsite" button. It does not load anything
 * itself, so tests can use it with fake data.
 *
 * @param owner_found false if the current owner could not be found.
 * @param campsites the owner's campsites to list.
 * @param show_debug_button true to offer the "Create test owner" button when the owner is missing.
 * @param on_back what to do when the back arrow is tapped.
 * @param on_create_test_owner what to do when "Create test owner" is tapped.
 * @param on_register what to do when "Register campsite" is tapped.
 * @param on_campsite_click what to do when a campsite in the list is tapped.
 */
@Composable
fun ownerCampsiteListContent(
    owner_found: Boolean,
    campsites: List<Campsite>,
    show_debug_button: Boolean,
    on_back: () -> Unit,
    on_create_test_owner: () -> Unit,
    on_register: () -> Unit,
    on_campsite_click: (Campsite) -> Unit
) {
    Scaffold(
        topBar = { ownerTopBar("My Campsites", on_back) },
        containerColor = COLOURS.LIGHTYELLOW.colour
    ) { inner_padding ->
        Column(
            modifier = Modifier
                .padding(inner_padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!owner_found) {
                Text("Owner \"$TEST_OWNER_UID\" was not found.")
                if (show_debug_button) {
                    Button(onClick = on_create_test_owner) {
                        Text("Create test owner (debug)")
                    }
                }
                return@Column
            }

            Button(onClick = on_register, modifier = Modifier.testTag("registerCampsiteButton")) {
                Text("Register campsite")
            }

            if (campsites.isEmpty()) {
                Text("You have no campsites yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(campsites, key = { it.UID }) { campsite ->
                        campsiteListItem(campsite) { on_campsite_click(campsite) }
                    }
                }
            }
        }
    }
}

/**
 * One row in the owner's campsite list, showing the campsite's name, location, and price.
 *
 * @param campsite the Campsite to show.
 * @param on_click what to do when the row is tapped.
 */
@Composable
fun campsiteListItem(campsite: Campsite, on_click: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("campsiteRow_${campsite.UID}")
            .clickable(onClick = on_click),
        colors = CardDefaults.cardColors(containerColor = COLOURS.DARKYELLOW.colour)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = campsite.getName(), fontSize = 20.sp)
            Text(text = campsite.getLocation())
            Text(text = "$%.2f per night".format(campsite.getPrice()))
        }
    }
}
