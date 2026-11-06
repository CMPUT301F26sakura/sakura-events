/**
 * File: HomeScreen.kt
 * Purpose: The main landing screen featuring the reusable top and bottom bars.
 */
package com.example.sakuraevents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
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
fun homeScreen(nav_controller: NavController, firestoreDriver: FirestoreDriver, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            // Calls the function defined below
            topBar(nav_controller)
        },
        bottomBar = {
            // Calls the function defined below (Removed the extra BottomAppBar wrapper!)
            bottomBar(nav_controller)
        },
        containerColor = COLOURS.LIGHTYELLOW.colour
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Welcome to Sakura Events!")
        }
    }
}

// SHARED NAVIGATION BARS (Added here so they can be reused)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topBar(nav_controller: NavController) {
    val nav_current_route by nav_controller.currentBackStackEntryAsState()
    val current_icon = nav_current_route?.destination?.route

    CenterAlignedTopAppBar(
        title = {
            // Show the route name if it's one of the main tabs, otherwise leave it blank
            if (current_icon in listOf("Home", "Search", "Profile")) {
                Text(current_icon as String)
            } else {
                Text("")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = COLOURS.LIGHTYELLOW.colour
        ),
        navigationIcon = {
            // Show a back arrow if we are NOT on the main tabs
            if (current_icon !in listOf("Home", "Search", "Profile")) {
                IconButton(onClick = { nav_controller.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}

@Composable
fun bottomBar(nav_controller: NavController) {
    val nav_current_route by nav_controller.currentBackStackEntryAsState()
    val current_icon = nav_current_route?.destination?.route

    BottomAppBar(
        containerColor = COLOURS.DARKYELLOW.colour
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { nav_controller.navigate("Home") }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (current_icon == "Home") COLOURS.LIGHTYELLOW.colour else Color.Transparent),
                    tint = COLOURS.BLACK.colour
                )
            }
            IconButton(onClick = { nav_controller.navigate("Search") }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (current_icon == "Search") COLOURS.LIGHTYELLOW.colour else Color.Transparent),
                    tint = COLOURS.BLACK.colour
                )
            }
            IconButton(onClick = { nav_controller.navigate("Profile") }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (current_icon == "Profile") COLOURS.LIGHTYELLOW.colour else Color.Transparent),
                    tint = COLOURS.BLACK.colour
                )
            }
        }
    }
}