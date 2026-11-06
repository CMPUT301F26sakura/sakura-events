/**
 * File: ProfileScreen.kt
 * Purpose: Displays user profile and acts as a hub to access the Admin Dashboard.
 */
package com.example.sakuraevents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun profileScreen(navController: NavController, firestoreDriver: FirestoreDriver) {
    val nav_current_route by navController.currentBackStackEntryAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = COLOURS.LIGHTYELLOW.colour)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = COLOURS.DARKYELLOW.colour) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = { navController.navigate("Home") }) {
                        Icon(Icons.Default.Home, "Home", modifier = Modifier.size(40.dp), tint = COLOURS.BLACK.colour)
                    }
                    IconButton(onClick = { navController.navigate("Search") }) {
                        Icon(Icons.Default.Search, "Search", modifier = Modifier.size(40.dp), tint = COLOURS.BLACK.colour)
                    }
                    IconButton(onClick = { navController.navigate("Profile") }) {
                        Icon(Icons.Default.Person, "Profile", modifier = Modifier.size(40.dp).background(COLOURS.LIGHTYELLOW.colour), tint = COLOURS.BLACK.colour)
                    }
                }
            }
        },
        containerColor = COLOURS.LIGHTYELLOW.colour
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("User Profile", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            // Admin Dashboard Button (For Demo Navigation)
            Button(
                onClick = { navController.navigate("AdminDashboard") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = COLOURS.DARKGREEN.colour)
            ) {
                Text("Open Admin Dashboard")
            }
        }
    }
}