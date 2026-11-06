// Purpose: A form for registering a new campsite (US 02.01.01, US 02.01.02) or editing an
// existing one (US 02.01.03), plus the checks that decide whether the entered details are valid.

package com.example.sakuraevents

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Checks the details typed into the campsite form.
 *
 * Name and location must not be blank, max occupancy must be a whole number above 0, and
 * price must be a number that is 0 or more. Description is optional.
 *
 * @param name the campsite name.
 * @param location the campsite location.
 * @param max_occupancy_text the max occupancy as typed (still text).
 * @param price_text the price as typed (still text).
 * @return a message describing the first problem found, or null if everything is valid.
 */
fun validateCampsiteForm(name: String, location: String, max_occupancy_text: String, price_text: String): String? {
    if (name.isBlank()) return "Please enter a name."
    if (location.isBlank()) return "Please enter a location."
    val max_occupancy = max_occupancy_text.trim().toIntOrNull()
    if (max_occupancy == null || max_occupancy <= 0) return "Max occupancy must be a whole number above 0."
    val price = price_text.trim().toFloatOrNull()
    if (price == null || price < 0.0f) return "Price must be a number that is 0 or more."
    return null
}

/**
 * The campsite form. With no campsite_uid it registers a new campsite for the current owner;
 * with a campsite_uid it edits that campsite.
 *
 * When registering, the new campsite is given its UID first, added to the owner with
 * CampsiteOwner.addCampsite, and then both the campsite and the owner are saved.
 *
 * @param nav_controller the app's NavController, used to go back after saving.
 * @param firestore_driver the app's FirestoreDriver, used to look up and save data.
 * @param campsite_uid the UID of the campsite to edit, or null to register a new one.
 */
@Composable
fun campsiteFormScreen(nav_controller: NavController, firestore_driver: FirestoreDriver, campsite_uid: String?) {
    val is_editing = campsite_uid != null
    val campsite = if (is_editing) firestore_driver.campsites[campsite_uid] else null

    // Each field remembers what the user typed. They are filled from the campsite once it is found.
    var name by remember(campsite?.UID) { mutableStateOf(campsite?.getName() ?: "") }
    var location by remember(campsite?.UID) { mutableStateOf(campsite?.getLocation() ?: "") }
    var description by remember(campsite?.UID) { mutableStateOf(campsite?.getDescription() ?: "") }
    var max_occupancy_text by remember(campsite?.UID) { mutableStateOf(campsite?.getMaxOccupancy()?.toString() ?: "") }
    var price_text by remember(campsite?.UID) { mutableStateOf(campsite?.getPrice()?.toString() ?: "") }
    val tags = remember(campsite?.UID) { mutableStateListOf<String>().apply { addAll(campsite?.tags ?: emptyList()) } }
    var new_tag by remember { mutableStateOf("") }
    var error_message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { ownerTopBar(if (is_editing) "Edit Campsite" else "Register Campsite", nav_controller) },
        containerColor = COLOURS.LIGHTYELLOW.colour
    ) { inner_padding ->
        Column(
            modifier = Modifier
                .padding(inner_padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (is_editing && campsite == null) {
                Text("Campsite not found. It may still be loading.")
                return@Column
            }

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it },
                label = { Text("Description and amenities") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = max_occupancy_text, onValueChange = { max_occupancy_text = it },
                label = { Text("Max occupancy") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price_text, onValueChange = { price_text = it },
                label = { Text("Price per night") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            // Tags: type one and press Add; tap a tag to remove it.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = new_tag, onValueChange = { new_tag = it }, label = { Text("Tag") },
                    singleLine = true, modifier = Modifier.weight(1f))
                Button(onClick = {
                    val tag = new_tag.trim()
                    if (tag.isNotEmpty() && !tags.contains(tag)) tags.add(tag)
                    new_tag = ""
                }) {
                    Text("Add")
                }
            }
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    AssistChip(
                        onClick = { tags.remove(tag) },
                        label = { Text(tag) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove $tag") }
                    )
                }
            }

            error_message?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = {
                error_message = validateCampsiteForm(name, location, max_occupancy_text, price_text)
                if (error_message != null) return@Button

                val max_occupancy = max_occupancy_text.trim().toInt()
                val price = price_text.trim().toFloat()

                if (campsite != null) {
                    // Editing: change the existing campsite, then save it under the same UID.
                    campsite.setName(name.trim())
                    campsite.setLocation(location.trim())
                    campsite.setDescription(description.trim())
                    campsite.setMaxOccupancy(max_occupancy)
                    campsite.setPrice(price)
                    campsite.tags.filter { it !in tags }.forEach { campsite.removeTag(it) }
                    tags.forEach { campsite.addTag(it) }
                    firestore_driver.updateCampsite(campsite)
                } else {
                    // Registering: the campsite needs its UID before it is added to the owner.
                    val owner = firestore_driver.owners[TEST_OWNER_UID]
                    if (owner == null) {
                        error_message = "Owner \"$TEST_OWNER_UID\" was not found."
                        return@Button
                    }
                    val new_campsite = Campsite(
                        UID = firestore_driver.nextCampsiteUid(),
                        name = name.trim(),
                        location = location.trim(),
                        description = description.trim(),
                        max_occupancy = max_occupancy,
                        price = price
                    )
                    tags.forEach { new_campsite.addTag(it) }
                    owner.addCampsite(new_campsite)
                    firestore_driver.addCampsite(new_campsite)
                    firestore_driver.updateOwner(owner)
                }
                nav_controller.popBackStack()
            }) {
                Text(if (is_editing) "Save changes" else "Register")
            }
        }
    }
}