/**
 * File: HomeScreen.kt
 * Purpose: Displays the home dashboard for Entrants, Campsite Owners, and Admins,
 * providing access to owned campsites, campsite registration/editing, lottery management,
 * and active lottery winner drawing.
 *
 * User Stories Addressed:
 * - US 01.03.01: As an entrant, I want to receive a notification when I win the campsite lottery.
 * - US 01.03.02: As an entrant, I want to receive a notification when I lose the campsite lottery.
 * - US 02.01.01: As a campsite owner, I want to register my campsite with the app.
 * - US 02.01.02: As a campsite owner, I want to provide campsite details including name, location, description (amenities), maximum occupancy, map location, optional image, and tags.
 * - US 02.01.03: As a campsite owner, I want to update details about my campsite.
 * - US 02.01.04: As a campsite owner, I want to view details of my campsite.
 * - US 02.02.01: As a campsite owner, I want the system to generate a unique QR code that links to the campsite details.
 * - US 02.03.01: As a campsite owner, I want to view a list of my campsites.
 * - US 02.04.01: As a campsite owner, I want to list my campsite in a lottery for a certain period.
 * - US 02.04.02: As a campsite owner, I want be able to list multiple campsites in a lottery for a certain period.
 * - US 02.04.03: As a campsite owner, I want to set a registration period.
 * - US 02.04.04: As a campsite owner, I want to set a booking period.
 * - US 02.04.05: As a campsite owner, I want to set the number of lottery winners.
 * - US 02.04.06: As a campsite owner, I want to update my campsite lottery listing.
 * - US 02.04.07: As a campsite owner, I want to optionally set a capacity for my lottery waitlist.
 * - US 02.05.01: As a campsite owner, I want the system to automatically draw winners after the registration period ends.
 */

package com.example.sakuraevents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun homeScreen(nav_controller: NavController, firestoreDriver: FirestoreDriver, modifier: Modifier = Modifier){

    /**
     * display the select icon with different color to distinguish
     * one variable to track the current route
     */
    Scaffold(
        topBar = {
            topBar(nav_controller)
        },
        bottomBar = {
            BottomAppBar(
                containerColor = COLOURS.DARKYELLOW.colour
            ) {
                bottomBar(nav_controller)
            }

        },
        containerColor = COLOURS.LIGHTYELLOW.colour
    ) { innerPadding ->
        //put your US stories code under


        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Main Screen Content")
        }
    }
}