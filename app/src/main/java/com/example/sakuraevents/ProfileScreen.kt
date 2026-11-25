/**
 * File: ProfileScreen.kt
 * Purpose: Displays the signed-in user's device-identified profile (device UID and active role),
 * provides navigation to owned campsites for campsite owners and admins, and embeds the
 * Admin Dashboard controls directly inside the Profile tab for administrators (replacing a
 * separate 5th bottom navigation tab).
 *
 * Design Pattern: Uses reactive state observation (Observer pattern via Jetpack Compose state)
 * backed by [FirestoreDriver], automatically recomposing whenever the signed-in user's role or
 * authentication state changes in Firestore.
 *
 * User Stories Addressed:
 * - US 01.09.01: As an entrant, I want to be identified by my device, so that I do not have to use a username and password.
 * - US 02.03.01: As a campsite owner, I want to view a list of my campsites.
 * - US 03.01.01, US 03.04.01: As an admin, I want to browse and remove registered campsites (via Manage Campsites).
 * - US 03.02.01, US 03.05.01: As an admin, I want to browse and remove profiles (via Manage Profiles).
 * - US 03.03.01, US 03.06.01: As an admin, I want to browse and remove uploaded images (via Manage Images).
 * - US 03.11.01: As an admin, I should be able to be a campsite owner and/or entrant with my admin profile.
 *
 * Outstanding Issues:
 * - Editing personal profile fields (name, email, phone number — US 01.07.03), deleting a profile
 *   (US 01.08.01), managing profile images (US 01.08.02), and displaying/opting out of lottery
 *   notifications (US 01.03.01, US 01.03.02, US 01.10.01) are not yet wired into this screen's UI.
 */

package com.example.sakuraevents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Renders the user's Profile screen with role-gated actions.
 *
 * All signed-in users see their anonymous Firebase device UID and current role. Users with
 * campsite owner privileges (`has_owner_access`) also see a button to open their campsites list,
 * and users with administrator privileges (`has_admin_access`) additionally see the Admin
 * Dashboard section for managing campsites, profiles, images.
 *
 * @param nav_controller The [NavController] used to navigate to owner, entrant, and admin routes.
 * @param firestore_driver The [FirestoreDriver] supplying the current user's UID and role access flags.
 * @param innerPadding The [PaddingValues] provided by the parent `Scaffold` to avoid overlapping
 *      the top and bottom bars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun profileScreen(
    nav_controller: NavController,
    firestore_driver: FirestoreDriver,
    innerPadding: PaddingValues
) {
    val current_user = firestore_driver.current_user

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Device UID: ${firestore_driver.current_user_uid ?: "Signing in..."}")
        Text("Roles: ${current_user?.getRole() ?: "Loading..."}")
        Spacer(modifier = Modifier.height(8.dp))

        if (current_user?.has_owner_access == true) {
            Button(onClick = { nav_controller.navigate("OwnerCampsites") }) {
                Text("My Campsites")
            }
        }

        if (current_user?.has_admin_access == true) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text("Admin Dashboard", style = MaterialTheme.typography.headlineSmall)

            Button(onClick = { nav_controller.navigate("AdminCampsites") }) {
                Text("Manage Campsites")
            }
            Button(onClick = { nav_controller.navigate("AdminProfiles") }) {
                Text("Manage Profiles")
            }
            Button(onClick = { nav_controller.navigate("AdminImages") }) {
                Text("Manage Images")
            }
        }
    }
}