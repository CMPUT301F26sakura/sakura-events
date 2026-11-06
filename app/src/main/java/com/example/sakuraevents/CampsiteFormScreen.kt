// Purpose: A form for registering a new campsite (US 02.01.01, US 02.01.02) or editing an
// existing one (US 02.01.03), plus the checks and model updates the form uses.

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * The checked details from the campsite form, ready to be put into a Campsite.
 * This is a small holder for the form's values, not a model class, so it is not saved to Firestore.
 *
 * @property name the campsite name.
 * @property location the campsite location.
 * @property description the campsite description and amenities.
 * @property max_occupancy the most people allowed at the campsite.
 * @property price the price per night.
 * @property tags the campsite's tags.
 */
data class CampsiteFormValues(
    val name: String,
    val location: String,
    val description: String,
    val max_occupancy: Int,
    val price: Float,
    val tags: List<String>
)

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
 * Creates a new Campsite from the form's values.
 *
 * @param uid the UID the new campsite should have (from FirestoreDriver.nextCampsiteUid in the app).
 * @param values the checked form values.
 * @return the new Campsite, including its tags.
 */
fun buildCampsiteFromForm(uid: String, values: CampsiteFormValues): Campsite {
    val campsite = Campsite(
        UID = uid,
        name = values.name,
        location = values.location,
        description = values.description,
        max_occupancy = values.max_occupancy,
        price = values.price
    )
    values.tags.forEach { campsite.addTag(it) }
    return campsite
}

/**
 * Copies the form's values into an existing Campsite. Tags that were removed in the form
 * are removed from the campsite, and new ones are added.
 *
 * @param campsite the Campsite being edited.
 * @param values the checked form values.
 * @return Nothing
 */
fun applyFormToCampsite(campsite: Campsite, values: CampsiteFormValues) {
    campsite.setName(values.name)
    campsite.setLocation(values.location)
    campsite.setDescription(values.description)
    campsite.setMaxOccupancy(values.max_occupancy)
    campsite.setPrice(values.price)
    campsite.tags.filter { it !in values.tags }.forEach { campsite.removeTag(it) }
    values.tags.forEach { campsite.addTag(it) }
}

/**
 * The campsite form page used by the app. With no campsite_uid it registers a new campsite for
 * the current owner; with a campsite_uid it edits that campsite. Saving goes through FirestoreDriver.
 *
 * When registering, the new campsite is given its UID first, added to the owner with
 * CampsiteOwner.addCampsite, and then both the campsite and the owner are saved.
 *
 * @param nav_controller the app's NavController, used to go back.
 * @param firestore_driver the app's FirestoreDriver, used to look up and save data.
 * @param campsite_uid the UID of the campsite to edit, or null to register a new one.
 */
@Composable
fun campsiteFormScreen(nav_controller: NavController, firestore_driver: FirestoreDriver, campsite_uid: String?) {
    val campsite = campsite_uid?.let { firestore_driver.campsites[it] }

    campsiteFormContent(
        campsite = campsite,
        is_editing = campsite_uid != null,
        on_back = { nav_controller.popBackStack() },
        on_save = save@{ values ->
            if (campsite_uid != null) {
                // Editing: change the existing campsite, then save it under the same UID.
                if (campsite == null) return@save "Campsite not found."
                applyFormToCampsite(campsite, values)
                firestore_driver.updateCampsite(campsite)
            } else {
                // Registering: the campsite needs its UID before it is added to the owner.
                val owner = firestore_driver.owners[TEST_OWNER_UID]
                    ?: return@save "Owner \"$TEST_OWNER_UID\" was not found."
                val new_campsite = buildCampsiteFromForm(firestore_driver.nextCampsiteUid(), values)
                owner.addCampsite(new_campsite)
                firestore_driver.addCampsite(new_campsite)
                firestore_driver.updateOwner(owner)
            }
            nav_controller.popBackStack()
            null
        }
    )
}

/**
 * Draws the campsite form. It only shows the fields and checks them; what happens on save is
 * decided by on_save, so tests can use this with fake data.
 *
 * @param campsite the Campsite to fill the form with when editing, or null for an empty form.
 * @param is_editing true when editing an existing campsite, false when registering a new one.
 * @param on_back what to do when the back arrow is tapped.
 * @param on_save called with the checked values when Save is tapped. Returns an error message
 *      to show, or null if saving worked.
 */
@Composable
fun campsiteFormContent(
    campsite: Campsite?,
    is_editing: Boolean,
    on_back: () -> Unit,
    on_save: (CampsiteFormValues) -> String?
) {
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
        topBar = { ownerTopBar(if (is_editing) "Edit Campsite" else "Register Campsite", on_back) },
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
                singleLine = true, modifier = Modifier.fillMaxWidth().testTag("campsiteNameInput"))
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") },
                singleLine = true, modifier = Modifier.fillMaxWidth().testTag("campsiteLocationInput"))
            OutlinedTextField(value = description, onValueChange = { description = it },
                label = { Text("Description and amenities") }, minLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("campsiteDescriptionInput"))
            OutlinedTextField(value = max_occupancy_text, onValueChange = { max_occupancy_text = it },
                label = { Text("Max occupancy") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("maxOccupancyInput"))
            OutlinedTextField(value = price_text, onValueChange = { price_text = it },
                label = { Text("Price per night") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("priceInput"))

            // Tags: type one and press Add; tap a tag to remove it.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = new_tag, onValueChange = { new_tag = it }, label = { Text("Tag") },
                    singleLine = true, modifier = Modifier.weight(1f).testTag("tagInput"))
                Button(
                    onClick = {
                        val tag = new_tag.trim()
                        if (tag.isNotEmpty() && !tags.contains(tag)) tags.add(tag)
                        new_tag = ""
                    },
                    modifier = Modifier.testTag("addTagButton")
                ) {
                    Text("Add")
                }
            }
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    AssistChip(
                        onClick = { tags.remove(tag) },
                        label = { Text(tag) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove $tag") },
                        modifier = Modifier.testTag("tagChip_$tag")
                    )
                }
            }

            error_message?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("formErrorText"))
            }

            Button(
                onClick = {
                    error_message = validateCampsiteForm(name, location, max_occupancy_text, price_text)
                    if (error_message != null) return@Button
                    error_message = on_save(
                        CampsiteFormValues(
                            name = name.trim(),
                            location = location.trim(),
                            description = description.trim(),
                            max_occupancy = max_occupancy_text.trim().toInt(),
                            price = price_text.trim().toFloat(),
                            tags = tags.toList()
                        )
                    )
                },
                modifier = Modifier.testTag("saveCampsiteButton")
            ) {
                Text(if (is_editing) "Save changes" else "Register")
            }
        }
    }
}