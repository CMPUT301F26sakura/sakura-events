/**
 * Purpose: Provides a calendar view for a campsite owner to select and update the booking period for a campsite.
 * 
 * User Stories Addressed:
 * - US 02.04.04: As a campsite owner, I want to set a booking period.
 * 
 * Outstanding Issues:
 * - None?
 */

package com.example.sakuraevents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * providing a calendar view to pick the range of time for booking
 * it allows the campsite owner to update the old booking period
 *
 * @param campsite The campsite being edited. Its booking dates will be modified.
 * @param onCampsiteChanged Callback triggered when the user confirms a new date range, so the parent can update Firestore.
 * @param modifier Compose modifier for styling.
 */

@Composable
fun CampsiteBookingPeriodField(
    campsite: Campsite,
    onCampsiteChanged: (Campsite) -> Unit,
    modifier: Modifier = Modifier
){
    var show_date by remember { mutableStateOf(false) }
    val  dateRangePickerState = rememberDateRangePickerState()

    //format of the date string
    val date_format = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply{
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    } }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val dates = campsite.booking_period_dates
        val display_data = if (dates.isNotEmpty()) {
            val sorted_dates = dates.sorted()
            "${sorted_dates.first()} to ${sorted_dates.last()}"
        } else {
            "None"
        }

        Column(modifier = Modifier.testTag("period_displayed")) {
            Text(text = "Booking Period", fontWeight = FontWeight.Bold)
            Text(text = display_data)
        }

        Button(
            onClick = { show_date = true },
            modifier = Modifier.testTag("select_period_button")
        ) {
            Text("Select Period")
        }
    }

    if(show_date) {
        DatePickerDialog(
            onDismissRequest = {show_date = false},
            confirmButton = {
                TextButton(
                    onClick = {
                        val start_date = dateRangePickerState.selectedStartDateMillis
                        val end_date = dateRangePickerState.selectedEndDateMillis

                        if(start_date != null && end_date != null){
                            val old_dates = campsite.booking_period_dates.toList()
                            old_dates.forEach { campsite.removeBookingPeriodDate(it) }

                            val calendar = java.util.Calendar.getInstance().apply {
                                timeInMillis = start_date
                            }

                            while (calendar.timeInMillis <= end_date) {
                                val dateStr = date_format.format(calendar.time)
                                campsite.addBookingPeriod(dateStr)
                                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                            }

                            onCampsiteChanged(campsite)
                        }
                        show_date = false
                    },
                    enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {show_date = false}) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f)
            )
        }
    }







}
