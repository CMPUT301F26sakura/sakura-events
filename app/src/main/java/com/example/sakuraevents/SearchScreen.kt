package com.example.sakuraevents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * US01.01.01
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun searchScreen(
    nav_controller: NavController,
    campsites: List<Campsite>,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues) {

    //Location filtering variables.
    val possible_locations = (campsites.map { it.getLocation() }).toSet().sorted()
        //.toSet() removes duplicates, .sorted() returns a sorted list.
    val location_filters = remember { mutableStateListOf<String>() }
    val selected_locations = remember { mutableStateListOf<String>() }
    // Occupancy filtering variables.
    // when campsites is the empty list, set the occupancy to 0
    val highest_occupancy: Int = campsites.maxOfOrNull { it.getMaxOccupancy() } ?:0
    var occupancy_filter by remember { mutableIntStateOf(0) }
    var selected_occupancy by remember { mutableFloatStateOf(0.0f) }
    // Tag filtering variables.
    val possible_tags = (campsites.map { it.tags }).flatten().toSet().sorted()
    val tag_filters = remember { mutableStateListOf<String>() }
    val selected_tags = remember { mutableStateListOf<String>() }
    // Misc variables.
    var search_bar by remember { mutableStateOf("") }
    val showFilterScreen = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    /**
     *
     */
    ModalNavigationDrawer(
        drawerState = showFilterScreen,
        drawerContent = {
            ModalDrawerSheet(
                modifier
                    .padding(innerPadding)
                    .width(300.dp),
                drawerContainerColor = COLOURS.LIGHTYELLOW.colour) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Bottom) {
                    LazyColumn(Modifier
                        .fillMaxSize()
                        .weight(1f)) {

                        selectableItems("Locations", possible_locations, selected_locations)

                        item { Text("Minimum Occupancy:", Modifier.padding(top = 16.dp)) }
                        item { HorizontalDivider(Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp), thickness = 2.dp) }
                        item {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Slider(
                                    value = selected_occupancy,
                                    onValueChange = { selected_occupancy = it },
                                    valueRange = 0f..highest_occupancy.coerceAtLeast(1).toFloat(),
                                    steps = (highest_occupancy - 1).coerceAtLeast(0),
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = selected_occupancy.roundToInt().toString(),
                                    Modifier.weight(0.1f))
                            }
                        }

                        item { Spacer(Modifier.padding(top = 16.dp)) }
                        selectableItems("Tags", possible_tags, selected_tags)
                    }
                    standardButton(
                        {
                            location_filters.clear()
                            location_filters.addAll(selected_locations)
                            occupancy_filter = selected_occupancy.toInt()
                            tag_filters.clear()
                            tag_filters.addAll(selected_tags)
                            scope.launch { showFilterScreen.close() }
                        },
                        "ApplyFilterButton",
                        "Apply")
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
                    {
                        selected_locations.clear()
                        selected_locations.addAll(location_filters)
                        selected_occupancy = occupancy_filter.toFloat()
                        selected_tags.clear()
                        selected_tags.addAll(tag_filters)
                        scope.launch { showFilterScreen.open() }
                    },
                    "FilterButton",
                    "Filters",
                    Icons.Default.FilterList)
                standardButton(
                    {
                        search_bar = ""
                        location_filters.clear()
                        occupancy_filter = 0
                        tag_filters.clear()
                    },
                    "ClearFilters",
                    "Clear Filters")
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(
                    campsites
                ) { campsite ->
                    Spacer(modifier = Modifier.height(12.dp))
                    displayCampsites(nav_controller, campsite, location_filters, occupancy_filter, search_bar)
                }
            }
        }
    }
}


@Composable
private fun displayCampsites(
    nav_controller: NavController,
    campsite: Campsite,
    location_filters: List<String>,
    occupancy_filter: Int,
    search_bar: String,
    modifier: Modifier = Modifier) {
    if ((search_bar == "" || (search_bar.lowercase() in campsite.getName().lowercase())) &&
        (location_filters.isEmpty() || campsite.getLocation() in location_filters) &&
        (campsite.getMaxOccupancy() >= occupancy_filter)) {
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


private fun LazyListScope.selectableItems(title: String, possibles: List<String>, selecteds: MutableList<String>) {
    item { Text("$title:") }
    item { HorizontalDivider(Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp), thickness = 2.dp) }
    items(items = possibles) { location ->
        Row(Modifier
            .padding(start = 8.dp, bottom = 2.dp)
            .clickable(onClick = {
                if (location in selecteds) {
                    selecteds.remove(location)
                } else {
                    selecteds.add(location)
                }
            })) {
            Icon(
                imageVector = if (location in selecteds) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                contentDescription = "Back"
            )
            Text(location)
        }
    }
}