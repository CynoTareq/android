package it.cynomys.cfmandroid.view.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import it.cynomys.cfmandroid.view.TimePickerLayout
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply { time = initialDate }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time
    )
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (!showTimePicker) {
                    // Date Picker
                    Text(
                        text = "Select Date",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                if (datePickerState.selectedDateMillis != null) {
                                    showTimePicker = true
                                }
                            }
                        ) {
                            Text("Next")
                        }
                    }
                } else {
                    // Time Picker
                    Text(
                        text = "Select Time",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TimePickerLayout(
                        hour = selectedHour,
                        minute = selectedMinute,
                        onHourChange = { selectedHour = it },
                        onMinuteChange = { selectedMinute = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Back")
                        }

                        Row {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { dateMillis ->
                                        val selectedCalendar = Calendar.getInstance().apply {
                                            timeInMillis = dateMillis
                                            set(Calendar.HOUR_OF_DAY, selectedHour)
                                            set(Calendar.MINUTE, selectedMinute)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        onDateSelected(selectedCalendar.time)
                                    }
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }
}


