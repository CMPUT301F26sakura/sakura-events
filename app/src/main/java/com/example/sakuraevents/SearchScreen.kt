package com.example.sakuraevents

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.mediaQuery
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

/**
 * US01.01.01
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun searchScreen(nav_controller: NavController, campsites: List<Campsite>, modifier: Modifier = Modifier, innerPadding: PaddingValues) {

    var location_filter by remember { mutableStateOf("") }
    var search_bar by remember { mutableStateOf("") }
    val showFilterScreen = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    /**
     * display the select icon with different color to distinguish
     * one variable to track the current route
     */

    ModalNavigationDrawer(
        drawerState = showFilterScreen,
        drawerContent = {
            ModalDrawerSheet(modifier.padding(innerPadding).width(300.dp), drawerContainerColor = COLOURS.LIGHTYELLOW.colour) {
                Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    Text("Beep", color = Color.Black)
                }
            }
        }
    ) {
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
            FlowRow(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                standardButton(
                    { scope.launch { showFilterScreen.open() } },
                    "FilterButton",
                    "Filters",
                    Icons.Default.FilterList)
                standardButton(
                    { search_bar = "" },
                    "ClearFilters",
                    "Clear Filters")
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(
                    campsites
                ) { campsite ->
                    Spacer(modifier = Modifier.height(12.dp))
                    displayCampsites(nav_controller, campsite, search_bar)
                }
            }
        }
    }
}


@Composable
fun displayCampsites(nav_controller: NavController, campsite: Campsite, search_bar: String, modifier: Modifier = Modifier) {
    if (search_bar == "" || (search_bar.lowercase() in campsite.getName().lowercase())) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 0.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(COLOURS.GREEN.colour)
                .clickable(onClick = { nav_controller.navigate("CampsiteDetailScreen/${campsite.UID}") }),
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
                    color = Color.White,
                    textDecoration = TextDecoration.Underline
                )
                if (campsite.booking_period_dates.isNotEmpty()) {
                    Text(
                        text = "\uD83D\uDCC6 ${campsite.booking_period_dates[0]}",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = "\uD83C\uDF0E ${campsite.getLocation()}",
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = "\uD83D\uDC64 ${campsite.getMaxOccupancy()} Occupancy",
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = "⭐ ${campsite.getAvgRating()}",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = "TempImage",
                modifier = Modifier.size(200.dp),
                tint = Color.Black
            )

        }
    }
}

