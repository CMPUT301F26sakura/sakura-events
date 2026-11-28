/**
 * File: OwnerLotteryScreen.kt
 * Purpose: Lets a campsite owner see the lotteries they have listed, create a new lottery from
 * one or more of their own campsites, and edit an existing lottery listing. Also holds the
 * checks and model building the lottery form uses.
 *
 * User Stories Addressed:
 * - US 02.04.01: As a campsite owner, I want to list my campsite in a lottery for a certain period.
 * - US 02.04.02: As a campsite owner, I want be able to list multiple campsites in a lottery.
 * - US 02.04.03: As a campsite owner, I want to set a registration period.
 * - US 02.04.06: As a campsite owner, I want to update my campsite lottery listing.
 *
 * Outstanding Issues:
 * - The winner count only changes after the owner presses Confirm in LotteryCapacityField
 *   (a shared component from LotteryComponents.kt); typing a number alone is not enough.
 * - Deleting a campsite does not remove its UID from lottery documents in Firestore; the missing
 *   campsite is just skipped when the lottery is loaded.
 * - Dates are whole days ("yyyy-MM-dd") with no time of day, and past start dates are allowed.
 * - Save failures from Firestore are not shown to the owner yet.
 */
// Name: Arham Ahmed
// Written with help from Anthropic, Claude (Claude Code), "Build owner lottery listing screen for US 02.04.01-02.04.03 and US 02.04.06", 2026-11-26

package com.example.sakuraevents

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** The format lottery registration dates are stored in, matching campsite booking period dates. */
const val LOTTERY_DATE_FORMAT = "yyyy-MM-dd"

/**
 * The checked details from the lottery form, ready to be put into a Lottery.
 * This is a small holder for the form's values, not a model class, so it is not saved to Firestore.
 *
 * @property campsites the owner's campsites to include in the lottery.
 * @property start_date the registration start date ("yyyy-MM-dd").
 * @property end_date the registration end date ("yyyy-MM-dd").
 * @property capacity the number of winners to draw.
 */
data class LotteryFormValues(
    val campsites: List<Campsite>,
    val start_date: String,
    val end_date: String,
    val capacity: Int
)

/**
 * The problems found in the lottery form, one per field, so each field can be highlighted on its
 * own. A null message means that field is fine.
 *
 * @property campsites the message for the campsite checkboxes, or null.
 * @property start_date the message for the registration start date, or null.
 * @property end_date the message for the registration end date, or null.
 * @property capacity the message for the winner count, or null.
 */
data class LotteryFormErrors(
    val campsites: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val capacity: String? = null
) {
    /** True if any field has a problem. */
    val has_errors: Boolean
        get() = campsites != null || start_date != null || end_date != null || capacity != null
}

/**
 * Makes the date formatter used for lottery dates. It is strict (so "2027-02-30" is rejected)
 * and uses UTC, which is what the Material date picker returns.
 *
 * @return a new SimpleDateFormat for [LOTTERY_DATE_FORMAT].
 */
private fun lotteryDateFormatter(): SimpleDateFormat {
    return SimpleDateFormat(LOTTERY_DATE_FORMAT, Locale.US).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }
}

/**
 * Reads a lottery date written as "yyyy-MM-dd".
 *
 * @param text the date as text.
 * @return the Date, or null if the text is blank or not a real date in that format.
 */
fun parseLotteryDate(text: String): Date? {
    if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(text.trim())) return null
    return try {
        lotteryDateFormatter().parse(text.trim())
    } catch (e: ParseException) {
        null
    }
}

/**
 * Writes a date picked in the Material date picker as "yyyy-MM-dd".
 *
 * @param millis the selected date in UTC milliseconds, as given by the date picker.
 * @return the date as text.
 */
fun formatLotteryDate(millis: Long): String {
    return lotteryDateFormatter().format(Date(millis))
}

/**
 * Checks the lottery form. Every field is checked (not just the first bad one) so the screen can
 * highlight all of the fields that need fixing at once.
 *
 * At least one campsite must be picked, both dates must be real "yyyy-MM-dd" dates, the end date
 * must be after the start date, and the winner count must be at least 1.
 *
 * @param selected_campsite_uids the UIDs of the campsites that are ticked.
 * @param start_date the registration start date as text.
 * @param end_date the registration end date as text.
 * @param capacity the winner count.
 * @return the problems found; use [LotteryFormErrors.has_errors] to see if there were any.
 */
fun validateLotteryForm(
    selected_campsite_uids: Collection<String>,
    start_date: String,
    end_date: String,
    capacity: Int
): LotteryFormErrors {
    val start = parseLotteryDate(start_date)
    val end = parseLotteryDate(end_date)
    return LotteryFormErrors(
        campsites = if (selected_campsite_uids.isEmpty()) "Pick at least one campsite." else null,
        start_date = if (start == null) "Pick a registration start date." else null,
        end_date = when {
            end == null -> "Pick a registration end date."
            start != null && !end.after(start) -> "End date must be after the start date."
            else -> null
        },
        capacity = if (capacity < 1) "Set a winner count of at least 1 and press Confirm." else null
    )
}

/**
 * Creates a Lottery from the form's values. Used both for new lotteries (blank UID, so
 * FirestoreDriver picks one) and for edited ones (the existing UID, so the same document is replaced).
 *
 * @param uid the lottery's UID, or "" for a new lottery.
 * @param values the checked form values.
 * @return the new Lottery, including its campsites.
 */
fun buildLotteryFromForm(uid: String, values: LotteryFormValues): Lottery {
    val lottery = Lottery(
        UID = uid,
        capacity = values.capacity,
        start_time = values.start_date,
        end_time = values.end_date
    )
    values.campsites.forEach { lottery.addCampsite(it) }
    return lottery
}

/**
 * The "My Lotteries" page used by the app. Looks up the current owner's lotteries and campsites in
 * FirestoreDriver and saves new or edited lotteries through it.
 *
 * @param nav_controller the app's NavController, used to go back.
 * @param firestore_driver the app's FirestoreDriver, which holds the loaded lotteries and campsites.
 */
@Composable
fun ownerLotteryScreen(nav_controller: NavController, firestore_driver: FirestoreDriver) {
    // Wait for sign-in so lotteries are never loaded or saved under the wrong owner.
    val owner_uid = firestore_driver.current_user_uid
    if (owner_uid == null) {
        signingInContent("My Lotteries") { nav_controller.popBackStack() }
        return
    }

    ownerLotteryContent(
        lotteries = firestore_driver.getLotteriesOwnedBy(owner_uid),
        owned_campsites = firestore_driver.getOwnedCampsites(owner_uid),
        on_back = { nav_controller.popBackStack() },
        on_save = { lottery_uid, values ->
            if (lottery_uid == null) {
                firestore_driver.addLottery(buildLotteryFromForm("", values), owner_uid)
            } else {
                firestore_driver.updateLottery(buildLotteryFromForm(lottery_uid, values), owner_uid)
            }
        },
        on_delete = { lottery -> firestore_driver.deleteLottery(lottery) }
    )
}

/**
 * Switches between the lottery list and the lottery form. It does not load or save anything
 * itself, so tests can use it with fake data.
 *
 * @param lotteries the owner's lotteries to list.
 * @param owned_campsites the owner's campsites, offered as checkboxes in the form.
 * @param on_back what to do when the back arrow on the list is tapped.
 * @param on_save called with the lottery's UID (null for a new lottery) and the checked form values.
 * @param on_delete called when the owner confirms deleting a lottery.
 */
@Composable
fun ownerLotteryContent(
    lotteries: List<Lottery>,
    owned_campsites: List<Campsite>,
    on_back: () -> Unit,
    on_save: (String?, LotteryFormValues) -> Unit,
    on_delete: (Lottery) -> Unit
) {
    var form_open by remember { mutableStateOf(false) }
    var editing_lottery by remember { mutableStateOf<Lottery?>(null) }

    // The system back button closes the form instead of leaving the page.
    BackHandler(enabled = form_open) { form_open = false }

    if (form_open) {
        lotteryFormContent(
            lottery = editing_lottery,
            owned_campsites = owned_campsites,
            on_back = { form_open = false },
            on_save = { values ->
                on_save(editing_lottery?.UID, values)
                form_open = false
            }
        )
    } else {
        ownerLotteryListContent(
            lotteries = lotteries,
            on_back = on_back,
            on_create = {
                editing_lottery = null
                form_open = true
            },
            on_edit = { lottery ->
                editing_lottery = lottery
                form_open = true
            },
            on_delete = on_delete
        )
    }
}

/**
 * Draws the owner's lottery list with a "Create lottery" button.
 *
 * @param lotteries the lotteries to list.
 * @param on_back what to do when the back arrow is tapped.
 * @param on_create what to do when "Create lottery" is tapped.
 * @param on_edit what to do when a lottery's Edit button is tapped.
 * @param on_delete what to do once the owner confirms deleting a lottery.
 */
@Composable
fun ownerLotteryListContent(
    lotteries: List<Lottery>,
    on_back: () -> Unit,
    on_create: () -> Unit,
    on_edit: (Lottery) -> Unit,
    on_delete: (Lottery) -> Unit
) {
    var lottery_to_delete by remember { mutableStateOf<Lottery?>(null) }


    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = on_create, modifier = Modifier.testTag("createLotteryButton")) {
            Text("Create lottery")
        }

        if (lotteries.isEmpty()) {
            Text("You have no lotteries yet.", modifier = Modifier.testTag("noLotteriesText"))
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lotteries, key = { it.UID }) { lottery ->
                    lotteryListItem(
                        lottery = lottery,
                        on_edit = { on_edit(lottery) },
                        on_delete = { lottery_to_delete = lottery }
                    )
                }
            }
        }
    }


    // Ask before deleting, since a deleted lottery cannot be brought back.
    lottery_to_delete?.let { lottery ->
        AlertDialog(
            onDismissRequest = { lottery_to_delete = null },
            title = { Text("Delete lottery?") },
            text = { Text("This removes the lottery listing. Your campsites are not changed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        on_delete(lottery)
                        lottery_to_delete = null
                    },
                    modifier = Modifier.testTag("confirmDeleteLotteryButton")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { lottery_to_delete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * One card in the owner's lottery list, showing the lottery's campsites, registration period,
 * and winner count, with Edit and Delete buttons.
 *
 * @param lottery the Lottery to show.
 * @param on_edit what to do when Edit is tapped.
 * @param on_delete what to do when Delete is tapped.
 */
@Composable
fun lotteryListItem(lottery: Lottery, on_edit: () -> Unit, on_delete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lotteryRow_${lottery.UID}"),
        colors = CardDefaults.cardColors(containerColor = COLOURS.DARKYELLOW.colour)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = lottery.campsites_list.joinToString(", ") { it.getName() }.ifBlank { "No campsites" },
                fontSize = 20.sp
            )
            Text("Registration: ${lottery.getStartTime()} to ${lottery.getEndTime()}")
            Text("Winners: ${lottery.getCapacity()}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = on_edit, modifier = Modifier.testTag("editLotteryButton_${lottery.UID}")) {
                    Text("Edit")
                }
                Button(
                    onClick = on_delete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.testTag("deleteLotteryButton_${lottery.UID}")
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

/**
 * Draws the lottery form. It only shows the fields and checks them; what happens on save is
 * decided by on_save, so tests can use this with fake data.
 *
 * After the first Save attempt, each field is re-checked as the owner changes it, so the red
 * highlight goes away as soon as that field is fixed.
 *
 * @param lottery the Lottery to fill the form with when editing, or null for an empty form.
 * @param owned_campsites the owner's campsites, shown as checkboxes.
 * @param on_back what to do when the back arrow is tapped.
 * @param on_save called with the checked values when Save is tapped and every field is valid.
 */
@Composable
fun lotteryFormContent(
    lottery: Lottery?,
    owned_campsites: List<Campsite>,
    on_back: () -> Unit,
    on_save: (LotteryFormValues) -> Unit
) {
    // Each field starts from the lottery being edited, or empty for a new lottery.
    val selected_uids = remember(lottery?.UID) {
        mutableStateListOf<String>().apply { addAll(lottery?.campsites_list?.map { it.UID } ?: emptyList()) }
    }
    var start_date by remember(lottery?.UID) { mutableStateOf(lottery?.getStartTime() ?: "") }
    var end_date by remember(lottery?.UID) { mutableStateOf(lottery?.getEndTime() ?: "") }
    var capacity by remember(lottery?.UID) { mutableIntStateOf(lottery?.getCapacity() ?: 0) }
    // LotteryCapacityField edits a Lottery, so it gets a scratch copy; the real one is only replaced on Save.
    val capacity_holder = remember(lottery?.UID) { Lottery(capacity = lottery?.getCapacity() ?: 0) }
    var save_attempted by remember { mutableStateOf(false) }
    var show_start_picker by remember { mutableStateOf(false) }
    var show_end_picker by remember { mutableStateOf(false) }

    // Only campsites the owner still owns can be saved in the lottery.
    val chosen_campsites = owned_campsites.filter { it.UID in selected_uids }
    val errors = if (save_attempted) {
        validateLotteryForm(chosen_campsites.map { it.UID }, start_date, end_date, capacity)
    } else {
        LotteryFormErrors()
    }
    val error_colour = MaterialTheme.colorScheme.error


    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Campsites (US 02.04.01, US 02.04.02): a red border and message if none are ticked.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (errors.campsites != null) {
                        Modifier.border(2.dp, error_colour, RoundedCornerShape(4.dp))
                    } else {
                        Modifier
                    }
                )
                .padding(8.dp)
                .testTag("lotteryCampsitesSection")
        ) {
            Text("Campsites in this lottery", fontWeight = FontWeight.Bold)
            if (owned_campsites.isEmpty()) {
                Text("You have no campsites to list. Register a campsite first.")
            }
            owned_campsites.forEach { campsite ->
                val checked = campsite.UID in selected_uids
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = checked,
                            role = SemanticsRole.Checkbox,
                            onValueChange = { now_checked ->
                                if (now_checked) selected_uids.add(campsite.UID) else selected_uids.remove(campsite.UID)
                            }
                        )
                        .testTag("lotteryCampsiteCheckbox_${campsite.UID}")
                ) {
                    Checkbox(checked = checked, onCheckedChange = null)
                    Text(campsite.getName(), modifier = Modifier.padding(start = 8.dp))
                }
            }
            errors.campsites?.let {
                Text(it, color = error_colour, modifier = Modifier.testTag("lotteryCampsitesError"))
            }
        }

        // Registration period (US 02.04.03).
        lotteryDateField(
            label = "Registration start",
            value = start_date,
            error = errors.start_date,
            tag_prefix = "lotteryStartDate",
            on_pick = { show_start_picker = true }
        )
        lotteryDateField(
            label = "Registration end",
            value = end_date,
            error = errors.end_date,
            tag_prefix = "lotteryEndDate",
            on_pick = { show_end_picker = true }
        )

        // Winner count, reusing the shared LotteryCapacityField. It has no error state of its own,
        // so the whole field gets a red border when the count is not set.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (errors.capacity != null) {
                        Modifier.border(2.dp, error_colour, RoundedCornerShape(4.dp))
                    } else {
                        Modifier
                    }
                )
                .padding(8.dp)
                .testTag("lotteryCapacitySection")
        ) {
            LotteryCapacityField(
                lottery = capacity_holder,
                onLotteryChanged = { capacity = it.getCapacity() }
            )
            Text("Winners set: $capacity", modifier = Modifier.testTag("lotteryCapacityValue"))
            errors.capacity?.let {
                Text(it, color = error_colour, modifier = Modifier.testTag("lotteryCapacityError"))
            }
        }

        if (errors.has_errors) {
            Text(
                "Please fix the highlighted fields.",
                color = error_colour,
                modifier = Modifier.testTag("lotteryFormErrorText")
            )
        }

        Button(
            onClick = {
                save_attempted = true
                val check = validateLotteryForm(chosen_campsites.map { it.UID }, start_date, end_date, capacity)
                if (check.has_errors) return@Button
                on_save(
                    LotteryFormValues(
                        campsites = chosen_campsites,
                        start_date = start_date.trim(),
                        end_date = end_date.trim(),
                        capacity = capacity
                    )
                )
            },
            modifier = Modifier.testTag("saveLotteryButton")
        ) {
            Text(if (lottery != null) "Save changes" else "Create lottery")
        }
    }


    if (show_start_picker) {
        lotteryDatePickerDialog(
            initial_date = start_date,
            on_dismiss = { show_start_picker = false },
            on_confirm = { start_date = it }
        )
    }
    if (show_end_picker) {
        lotteryDatePickerDialog(
            initial_date = end_date.ifBlank { start_date },
            on_dismiss = { show_end_picker = false },
            on_confirm = { end_date = it }
        )
    }
}

/**
 * A read-only date field with a calendar button. When there is an error, the field turns red and
 * the message is shown under it.
 *
 * Test tags: "<tag_prefix>Field", "<tag_prefix>Button", and "<tag_prefix>Error".
 *
 * @param label the field's label.
 * @param value the date shown, or "" if none is picked.
 * @param error the message to show, or null if the field is fine.
 * @param tag_prefix the start of this field's test tags.
 * @param on_pick what to do when the calendar button is tapped.
 */
@Composable
fun lotteryDateField(label: String, value: String, error: String?, tag_prefix: String, on_pick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text(LOTTERY_DATE_FORMAT) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { message ->
            { Text(message, modifier = Modifier.testTag("${tag_prefix}Error")) }
        },
        trailingIcon = {
            IconButton(onClick = on_pick, modifier = Modifier.testTag("${tag_prefix}Button")) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick $label date")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${tag_prefix}Field")
    )
}

/**
 * A calendar dialog for picking one lottery date.
 *
 * @param initial_date the date to start on ("yyyy-MM-dd"), or "" for none.
 * @param on_dismiss called when the dialog closes.
 * @param on_confirm called with the picked date as "yyyy-MM-dd" when OK is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun lotteryDatePickerDialog(initial_date: String, on_dismiss: () -> Unit, on_confirm: (String) -> Unit) {
    val picker_state = rememberDatePickerState(initialSelectedDateMillis = parseLotteryDate(initial_date)?.time)
    DatePickerDialog(
        onDismissRequest = on_dismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    picker_state.selectedDateMillis?.let { on_confirm(formatLotteryDate(it)) }
                    on_dismiss()
                },
                enabled = picker_state.selectedDateMillis != null,
                modifier = Modifier.testTag("lotteryDateConfirmButton")
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = on_dismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = picker_state)
    }
}
