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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// TODO: Replace with the logged-in owner's UID once sign-in is implemented.
/** The UID of the owner whose campsites these screens show, until there is a login. */
const val TEST_OWNER_UID = "0"

/**
 * The top bar used by the owner campsite screens: a title in the middle and a back arrow
 * on the left that returns to the previous page.
 *
 * @param title the text shown in the middle of the bar.
 * @param nav_controller the app's NavController, used to go back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ownerTopBar(title: String, nav_controller: NavController) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { nav_controller.popBackStack() }) {
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
 * Shows every campsite the current owner has registered. Tapping a campsite opens its
 * details, and the "Register campsite" button opens an empty campsite form.
 *
 * If the test owner does not exist in Firestore yet, debug builds show a button that creates it.
 *
 * @param nav_controller the app's NavController, used to move between pages.
 * @param firestore_driver the app's FirestoreDriver, which holds the loaded owners and campsites.
 */
@Composable
fun ownerCampsiteListScreen(nav_controller: NavController, firestore_driver: FirestoreDriver) {
    val owner = firestore_driver.owners[TEST_OWNER_UID]
    // True for builds run from Android Studio, false for release builds.
    val is_debug_build = (LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    Scaffold(
        topBar = { ownerTopBar("My Campsites", nav_controller) },
        containerColor = COLOURS.LIGHTYELLOW.colour
    ) { inner_padding ->
        Column(
            modifier = Modifier
                .padding(inner_padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (owner == null) {
                Text("Owner \"$TEST_OWNER_UID\" was not found.")
                if (is_debug_build) {
                    Button(onClick = {
                        firestore_driver.updateOwner(
                            CampsiteOwner(
                                UID = TEST_OWNER_UID,
                                role = Role.OWNER,
                                name = "Test Owner",
                                email = "test.owner@example.com",
                                phone_number = "780-555-0000"
                            )
                        )
                    }) {
                        Text("Create test owner (debug)")
                    }
                }
                return@Column
            }

            Button(onClick = { nav_controller.navigate("CampsiteForm") }) {
                Text("Register campsite")
            }

            // Look each campsite up by UID so the list always shows the latest loaded values.
            val owned_campsites = owner.owned_campsites.mapNotNull { firestore_driver.campsites[it.UID] }

            if (owned_campsites.isEmpty()) {
                Text("You have no campsites yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(owned_campsites, key = { it.UID }) { campsite ->
                        campsiteListItem(campsite) {
                            nav_controller.navigate("CampsiteDetails/${campsite.UID}")
                        }
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