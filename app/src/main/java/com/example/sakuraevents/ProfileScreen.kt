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
 * - Editing personal profile fields (name, email, phone number â€” US 01.07.03), deleting a profile
 *   (US 01.08.01), managing profile images (US 01.08.02), and displaying/opting out of lottery
 *   notifications (US 01.03.01, US 01.03.02, US 01.10.01) are not yet wired into this screen's UI.
 */

package com.example.sakuraevents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var show_edit by remember { mutableStateOf(false) }
    var name_input by remember { mutableStateOf("") }
    var email_input by remember { mutableStateOf("") }
    var phone_input by remember { mutableStateOf("") }
    var name_error by remember { mutableStateOf(false) }

    //Name: Yangshun
    // // Name: Arham Ahmed
    //// Written with help from Gemini, "match the displaying text with UI ", 2026-11-28

        val slate_navy = Color(0xFF3E4A59)
        val card_green = Color(0xFF5FA360)


        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Welcome ${current_user?.getName()?.ifBlank { "Not set" } ?: "Loading..."}!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = slate_navy
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = BorderStroke(2.dp, slate_navy)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(card_green)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProfileFieldLine(
                        label = "Roles:",
                        value = current_user?.getRole() ?: "Loading..."
                    )
                    ProfileFieldLine(
                        label = "Email:",
                        value = current_user?.getEmail()?.ifBlank { "Not set" } ?: "Loading..."
                    )
                    ProfileFieldLine(
                        label = "Phone:",
                        value = current_user?.getPhoneNumber()?.ifBlank { "Not set" }
                            ?: "Loading..."
                    )
                    ProfileFieldLine(
                        label = "Device UID:",
                        value = firestore_driver.current_user_uid ?: "Signing in..."
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            standardButton(
                clicked = { nav_controller.navigate("Notifications") },
                test_tag = "view_notifications_button",
                button_text = "View Notifications"
            )

            standardButton(
                clicked = {
                    if (current_user != null) {
                        name_input = current_user.getName()
                        email_input = current_user.getEmail()
                        phone_input = current_user.getPhoneNumber()
                        name_error = false
                        show_edit = true
                    }
                },
                test_tag = "edit_profile_button",
                button_text = "Edit Profile"
            )


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
                standardButton(
                    clicked = { nav_controller.navigate("OwnerCampsites") },
                    test_tag = "OwnerCampsites_button",
                    button_text = "My Campsites"
                )

            }

            if (current_user?.has_admin_access == true) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Admin Dashboard", style = MaterialTheme.typography.headlineSmall)

                standardButton(
                    clicked = { nav_controller.navigate("AdminCampsites") },
                    test_tag = "AdminCampsites_button",
                    button_text = "Manage Campsites"
                )
                standardButton(
                    clicked = { nav_controller.navigate("AdminProfiles") },
                    test_tag = "AdminProfiles_button",
                    button_text = "Manage Profiles"
                )
                standardButton(
                    clicked = { nav_controller.navigate("AdminImages") },
                    test_tag = "AdminImages_button",
                    button_text = "Manage Images"
                )
            }
        }
        if (show_edit && current_user != null) {
            AlertDialog(
                onDismissRequest = { show_edit = false },
                title = { Text("Edit Profile") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name_input,
                            onValueChange = {
                                name_input = it
                                name_error = false
                            },
                            label = { Text("Name") },
                            singleLine = true,
                            isError = name_error,
                            supportingText = { if (name_error) Text("Name is required") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_name_input")
                        )
                        OutlinedTextField(
                            value = email_input,
                            onValueChange = { email_input = it },
                            label = { Text("Email") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_email_input")
                        )
                        OutlinedTextField(
                            value = phone_input,
                            onValueChange = { phone_input = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_phone_input")
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (name_input.isBlank()) {
                                name_error = true
                                return@TextButton
                            }
                            current_user.setName(name_input.trim())
                            current_user.setEmail(email_input.trim())
                            current_user.setPhoneNumber(phone_input.trim())
                            firestore_driver.addEntrant(current_user)
                            show_edit = false
                        },
                        modifier = Modifier.testTag("save_profile_button")
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { show_edit = false },
                        modifier = Modifier.testTag("cancel_edit_profile_button")
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }



}
@Composable
private fun ProfileFieldLine(label: String, value: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(label)
            }
            append(" ")
            append(value)
        },
        color = Color.White,
        fontSize = 16.sp
    )
}