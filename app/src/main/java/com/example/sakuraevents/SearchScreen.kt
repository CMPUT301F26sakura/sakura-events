package com.example.sakuraevents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


class SearchScreen(val navController: NavController, val firestoreDriver: FirestoreDriver, val modifier: Modifier = Modifier) {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun searchScreen(){

        /**
         * display the select icon with different color to distinguish
         * one variable to track the current route
         */

        val nav_current_route by navController.currentBackStackEntryAsState()
        val current_icon = nav_current_route?.destination?.route
        var search_bar by remember { mutableStateOf("") }


        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Search") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = COLOURS.LIGHTYELLOW.colour
                    )
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = COLOURS.GREEN.colour
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
                                tint = if (current_icon == "Home") COLOURS.DARKGREEN.colour else COLOURS.BLACK.colour
                            )
                        }
                        IconButton(onClick = { navController.navigate("Search") }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(40.dp),
                                tint = if (current_icon == "Search") COLOURS.DARKGREEN.colour else COLOURS.BLACK.colour
                            )
                        }
                        IconButton(onClick = { navController.navigate("Profile") }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(40.dp),
                                tint = if (current_icon == "Profile") COLOURS.DARKGREEN.colour else COLOURS.BLACK.colour
                            )
                        }
                    }
                }

            }
        ) { innerPadding ->
            //put your US stories code under

            Column (modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextField(
                        value = search_bar,
                        onValueChange = {
                            search_bar = it
                        },
                        label = {
                            Text("Search")
                        },
                        modifier = Modifier.testTag("searchBarInput")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    /*Button(
                        onClick = {
                            if (search_bar.isNotBlank()) {
                                races.add(location)
                                location = ""
                            }
                        },
                        modifier = Modifier.testTag("addRaceButton") // for testing
                    ) {
                        Text("Add")
                    }*/

                }
                LazyColumn {
                    items(firestoreDriver.campsites.entries.toList(), key = { it.key }) { campsite ->
                        Spacer(modifier = Modifier.height(12.dp))
                        displayCampsites(campsite.value, search_bar)
                    }
                }
                Text(text = "Main Screen Content")
            }
        }
    }

    @Composable
    fun displayCampsites(campsite: Campsite, search_bar: String) {
        if (search_bar == "" || (search_bar.lowercase() in campsite.getName().lowercase())) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = modifier.weight(0.5f)) {
                    Text(text = campsite.getName(), fontSize = 24.sp)
                    Text(text = campsite.getLocation(), fontSize = 25.sp)
                }
            }

            Text(
                text = campsite.UID,
                fontSize = 24.sp
            )

        }
    }
}
