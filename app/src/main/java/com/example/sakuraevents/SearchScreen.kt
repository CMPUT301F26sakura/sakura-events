package com.example.sakuraevents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.mediaQuery
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * US01.01.01
 */

class SearchScreen(val nav_controller: NavController, val firestore_driver: FirestoreDriver, val modifier: Modifier = Modifier) {

    private var location_filter by mutableStateOf("")
    private var search_bar by mutableStateOf("")


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun searchScreen() {

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

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Row(
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
                        singleLine = true,
                        modifier = Modifier
                            .testTag("searchBarInput")
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {}, modifier
                            .testTag(("LocatiomFilter")),
                        colors = ButtonColors(
                            containerColor = COLOURS.DARKGREEN.colour,
                            contentColor = COLOURS.WHITE.colour,
                            disabledContainerColor = COLOURS.DARKYELLOW.colour,
                            disabledContentColor = COLOURS.DARKGREEN.colour
                        )
                    ) {
                        Text(
                            text = "Location",
                            fontSize = 20.sp,
                            color = COLOURS.WHITE.colour
                        )
                    }
                    Button(
                        onClick = {}, modifier
                            .testTag(("DateFilter")),
                        colors = ButtonColors(
                            containerColor = COLOURS.DARKGREEN.colour,
                            contentColor = COLOURS.WHITE.colour,
                            disabledContainerColor = COLOURS.DARKYELLOW.colour,
                            disabledContentColor = COLOURS.DARKGREEN.colour
                        )
                    ) {
                        Text(
                            text = "Date",
                            fontSize = 20.sp,
                            color = COLOURS.WHITE.colour
                        )
                    }
                    Button(
                        onClick = {}, modifier
                            .testTag(("GuestsFilter")),
                        colors = ButtonColors(
                            containerColor = COLOURS.DARKGREEN.colour,
                            contentColor = COLOURS.WHITE.colour,
                            disabledContainerColor = COLOURS.DARKYELLOW.colour,
                            disabledContentColor = COLOURS.DARKGREEN.colour
                        )
                    ) {
                        Text(
                            text = "Guests",
                            fontSize = 20.sp,
                            color = COLOURS.WHITE.colour
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {}, modifier
                            .testTag(("ClearFilter")),
                        colors = ButtonColors(
                            containerColor = COLOURS.DARKGREEN.colour,
                            contentColor = COLOURS.WHITE.colour,
                            disabledContainerColor = COLOURS.DARKYELLOW.colour,
                            disabledContentColor = COLOURS.DARKGREEN.colour
                        ), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Clear Filters",
                            fontSize = 20.sp,
                            color = COLOURS.WHITE.colour
                        )
                    }
                }
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(
                        firestore_driver.campsites.entries.toList(),
                        key = { it.key }) { campsite ->
                        Spacer(modifier = Modifier.height(12.dp))
                        displayCampsites(campsite.value)
                    }
                }
            }
        }
    }

    @Composable
    fun displayCampsites(campsite: Campsite) {
        if (search_bar == "" || (search_bar.lowercase() in campsite.getName().lowercase())) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 0.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(COLOURS.GREEN.colour),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = modifier
                        .weight(0.5f)
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = campsite.getName(),
                        fontSize = 24.sp,
                        color = COLOURS.WHITE.colour,
                        textDecoration = TextDecoration.Underline
                    )
                    if (campsite.booking_period_dates.isNotEmpty()) {
                        Text(
                            text = "\uD83D\uDCC6 ${campsite.booking_period_dates[0]}",
                            fontSize = 20.sp,
                            color = COLOURS.WHITE.colour
                        )
                    }
                    Text(
                        text = "\uD83C\uDF0E ${campsite.getLocation()}",
                        fontSize = 20.sp,
                        color = COLOURS.WHITE.colour
                    )
                    Text(
                        text = "\uD83D\uDC64 ${campsite.getMaxOccupancy()} Occupancy",
                        fontSize = 20.sp,
                        color = COLOURS.WHITE.colour
                    )
                    Text(
                        text = "⭐ ${campsite.getAvgRating()}",
                        fontSize = 20.sp,
                        color = COLOURS.WHITE.colour
                    )
                }
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = "TempImage",
                    modifier = Modifier.size(200.dp),
                    tint = COLOURS.BLACK.colour
                )

            }
        }
    }
}
