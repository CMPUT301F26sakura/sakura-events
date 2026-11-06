package com.example.sakuraevents

import android.R.attr.text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDefaults.dateFormatter
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedStartDate
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.identity.cbor.Uint
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * providing a calendar view to pick the range of time for booking
 * it allows the campsite owner to update the old booking period
 *
 * @param campsite the campsite will be edited if new booking period selected
 * @param onCampsiteChanged called for firestore to update the new booking period
 */


@Composable
fun CampsiteBookingPeriodField(
    campsite: Campsite,
    onCampsiteChanged: (Campsite) -> Uint,
    modifier: Modifier = Modifier
){
    var show_date by remember { mutableStateOf(false) }
    val  dateRangePickerState = rememberDateRangePickerState()

    //format of the date string
    val date_format = remember { SimpleDateFormat("yyyy-MM--dd", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxWidth()){
        val dates = campsite.booking_period_dates
        val display_data = if (dates.isNotEmpty()) {
            val sorted_dates = dates.sorted()
            "${sorted_dates.first()} to ${sorted_dates.last()}"
        } else {
            "Not Date Information."
        }

        Text(
            text = "Booking Period: $display_data",
            modifier = Modifier.padding().testTag("period_displayed")
        )

        Button(
            onClick = { show_date = true},
            modifier = Modifier.testTag("select_period_button")
        ){
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