/**
 * File: ProfileScreen.kt
 * Purpose: Displays the user's device-identified profile, allowing them to update
 * personal contact details, view lottery notifications, switch roles (Entrant, Owner, Admin),
 * and access the Admin Dashboard.
 *
 * User Stories Addressed:
 * - US 01.03.01: As an entrant, I want to receive a notification when I win the campsite lottery.
 * - US 01.03.02: As an entrant, I want to receive a notification when I lose the campsite lottery.
 * - US 01.07.03: As an entrant, I want to update my personal information, such as name, email, and phone number, on my profile.
 * - US 01.09.01: As an entrant, I want to be identified by my device, so that I do not have to use a username and password.
 * - US 03.11.01: As an admin, I should be able to be a campsite owner and/or entrant with my admin profile.
 */

package com.example.sakuraevents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.unit.dp



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun profileScreen(nav_controller: NavController, firestore_driver: FirestoreDriver, innerPadding: PaddingValues){

    /**
     * display the select icon with different color to distinguish
     * one variable to track the current route
     */
        //put your US stories code under
    val current_user = firestore_driver.current_user

    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // The following was written with help from Anthropic, Claude (Claude Code), "Add owner campsite support", 2026-11-05
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Device UID: ${firestore_driver.current_user_uid ?: "Signing in..."}")
            Text("Roles: ${current_user?.getRole() ?: "Loading..."}")
            Spacer(modifier = Modifier.height(16.dp))
            // US 01.13.01: Rate completed campsite reservations.
            if (current_user?.has_entrant_access == true) {
                firestore_driver.reservations.forEach { (uid, reservation) ->
                    if (reservation.getEntrant().UID == current_user.UID &&
                        reservation.getState() == "COMPLETE"
                    ) {
                        standardButton(
                            clicked = { nav_controller.navigate("Rating/$uid") },
                            test_tag = "RateCampsiteButton_$uid",
                            button_text = "Rate Campsite"
                        )
                    }
                }
            }

            if (current_user?.has_owner_access == true) {
                Button(onClick = { nav_controller.navigate("OwnerCampsites") }) {
                    Text("My Campsites")
                }
            }
        }
    }

}
