// Purpose: Shows the current campsite owner's list of campsites (US 02.03.01) and lets them
// start registering a new one. Also holds the shared top bar used by the owner campsite screens.
// Written with help from Anthropic, Claude (Claude Code), "Build owner campsite screens for US 02.01.01-02.01.04 and US 02.03.01", 2026-11-05

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults

// Name: Arham Ahmed
// Written with help from Anthropic, Claude (Claude Code), "Show Signing in until anonymous sign-in finishes on owner screens", 2026-11-26
/**
 * The page owner screens show until anonymous sign-in has set FirestoreDriver.current_user_uid,
 * so nothing is loaded or saved under the wrong owner in the meantime.
 *
 * @param title the text shown in the middle of the top bar.
 * @param on_back what to do when the back arrow is tapped.
 */
@Composable
fun signingInContent(title: String, on_back: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Text("Signing in…", modifier = Modifier.testTag("signingInText"))
    }
}

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
                    tint = Color.Black
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
    // Name: Arham Ahmed. Written with help from Anthropic, Claude (Claude Code), "Show Signing in until anonymous sign-in finishes on owner screens", 2026-11-26
    // Wait for sign-in instead of showing a placeholder owner's campsites.
    val owner_uid = firestore_driver.current_user_uid
    if (owner_uid == null) {
        signingInContent("My Campsites") { nav_controller.popBackStack() }
        return
    }
    val owner = firestore_driver.owners[owner_uid]
    // True for builds run from Android Studio, false for release builds.
    val is_debug_build = (LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    // Look each campsite up by UID so the list always shows the latest loaded values.
    val owned_campsites = firestore_driver.getOwnedCampsites(owner_uid)

    ownerCampsiteListContent(
        owner_found = owner != null,
        campsites = owned_campsites,
        show_debug_button = is_debug_build,
        on_back = { nav_controller.popBackStack() },
        on_create_test_owner = {
            firestore_driver.updateOwner(
                CampsiteOwner(
                    UID = owner_uid,
                    role = Role.OWNER,
                    name = "Test Owner",
                    email = "test.owner@example.com",
                    phone_number = "780-555-0000"
                )
            )
        },
        on_register = { nav_controller.navigate("CampsiteForm") },
        on_campsite_click = { campsite -> nav_controller.navigate("CampsiteDetails/${campsite.UID}") },
        on_edit = { campsite -> nav_controller.navigate("CampsiteForm?uid=${campsite.UID}") },
        on_delete = { campsite -> firestore_driver.deleteCampsite(campsite) },
        show_delete = firestore_driver.current_user?.has_admin_access == true,
        on_my_lotteries = { nav_controller.navigate("OwnerLotteries") }

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
 * @param on_my_lotteries what to do when "My Lotteries" is tapped (US 02.04.01, US 02.04.06).
 */
@Composable
fun ownerCampsiteListContent(
    owner_found: Boolean,
    campsites: List<Campsite>,
    show_debug_button: Boolean,
    on_back: () -> Unit,
    on_create_test_owner: () -> Unit,
    on_register: () -> Unit,
    on_campsite_click: (Campsite) -> Unit,
    on_edit: (Campsite) -> Unit = {},
    on_delete: (Campsite) -> Unit = {},
    show_delete: Boolean = false,
    on_my_lotteries: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
//            if (!owner_found) {
//                Text("Owner \"$TEST_OWNER_UID\" was not found.")
//                if (show_debug_button) {
//                    Button(onClick = on_create_test_owner) {
//                        Text("Create test owner (debug)")
//                    }
//                }
//                return@Column
//            }

        // Name: Arham Ahmed
        // Written with help from Anthropic, Claude (Claude Code), "Build owner lottery listing screen for US 02.04.01-02.04.03 and US 02.04.06", 2026-11-26
        // "My Lotteries" sits next to "Register campsite" so owners can list their campsites in a lottery.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = on_register, modifier = Modifier.testTag("registerCampsiteButton")) {
                Text("Register campsite")
            }
            Button(onClick = on_my_lotteries, modifier = Modifier.testTag("myLotteriesButton")) {
                Text("My Lotteries")
            }
        }

        if (campsites.isEmpty()) {
            Text("You have no campsites yet.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(campsites, key = { it.UID }) { campsite ->
                    val delete_action: (() -> Unit)? = if (show_delete) ({ on_delete(campsite) }) else null
                    campsiteListItem(
                        campsite = campsite,
                        on_click = { on_campsite_click(campsite) },
                        on_edit = { on_edit(campsite) },
                        on_delete = delete_action
                    )
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
// Name: Devesh Darji
// The following function / lines of code were generated / modified by Google Gemini Neon Model.
// Prompt: "How do I add Edit and optional Delete buttons to each card in campsiteListItem?" 2026-11-22
// Added optional on_edit and on_delete callbacks to render Edit and Delete buttons inside the campsite card.
fun campsiteListItem(
    campsite: Campsite,
    on_click: () -> Unit,
    on_edit: (() -> Unit)? = null,
    on_delete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("campsiteRow_${campsite.UID}")
            .clickable(onClick = on_click),
        colors = CardDefaults.cardColors(containerColor = COLOURS.DARKYELLOW.colour)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = campsite.getName(), fontSize = 20.sp)
            Text(text = campsite.getLocation())
            Text(text = "$%.2f per night".format(campsite.getPrice()))
            if (on_edit != null || on_delete != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (on_edit != null) {
                        Button(onClick = on_edit) {
                            Text("Edit")
                        }
                    }
                    if (on_delete != null) {
                        Button(
                            onClick = on_delete,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
