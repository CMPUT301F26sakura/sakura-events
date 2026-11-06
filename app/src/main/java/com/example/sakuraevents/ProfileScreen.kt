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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun profileScreen(navController: NavController){

    /**
     * display the select icon with different color to distinguish
     * one variable to track the current route
     */

    val nav_current_route by navController.currentBackStackEntryAsState()
    val current_icon = nav_current_route?.destination?.route


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = COLOURS.LIGHTYELLOW.colour
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = COLOURS.DARKYELLOW.colour
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { navController.navigate("Home") }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(40.dp),
                            tint = COLOURS.BLACK.colour
                        )
                    }
                    IconButton(onClick = { navController.navigate("Search") }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(40.dp),
                            tint = COLOURS.BLACK.colour
                        )
                    }
                    IconButton(onClick = { navController.navigate("Profile") }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(40.dp).background(COLOURS.LIGHTYELLOW.colour),
                            tint = COLOURS.BLACK.colour
                        )
                    }
                }
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