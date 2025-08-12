package it.cynomys.cfmandroid.sensordata

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.cynomys.cfmandroid.view.common.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryFormView(
    attributes: String,
    onAttributesChange: (String) -> Unit,
    aggregation: String,
    onAggregationChange: (String) -> Unit,
    limit: Int,
    onLimitChange: (Int) -> Unit,
    interval: String,
    onIntervalChange: (String) -> Unit,
    orderBy: String,
    onOrderByChange: (String) -> Unit,
    from: Date,
    onFromChange: (Date) -> Unit,
    to: Date,
    onToChange: (Date) -> Unit,
    selectedSensors: Set<String>,
    onSelectedSensorsChange: (Set<String>) -> Unit,
    sensorNames: List<String>,
    aggregationOptions: List<String>,
    intervalOptions: List<String>,
    limitOptions: List<Int>,
    onSave: () -> Unit
) {
    var expandedAggregation by remember { mutableStateOf(false) }
    var expandedInterval by remember { mutableStateOf(false) }
    var expandedLimit by remember { mutableStateOf(false) }
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Filter Options",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Date Range Selection
        Text(
            text = "Date Range:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // From Date Picker
        OutlinedTextField(
            value = dateFormat.format(from),
            onValueChange = { },
            readOnly = true,
            label = { Text("From Date") },
            trailingIcon = {
                IconButton(onClick = { showFromDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select from date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // To Date Picker
        OutlinedTextField(
            value = dateFormat.format(to),
            onValueChange = { },
            readOnly = true,
            label = { Text("To Date") },
            trailingIcon = {
                IconButton(onClick = { showToDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select to date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sensor Selection
        Text(
            text = "Select Sensors:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        sensorNames.forEach { sensor ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = selectedSensors.contains(sensor),
                    onCheckedChange = { checked ->
                        if (checked) {
                            onSelectedSensorsChange(selectedSensors + sensor)
                        } else {
                            onSelectedSensorsChange(selectedSensors - sensor)
                        }
                        onAttributesChange(selectedSensors.joinToString(","))
                    }
                )
                Text(
                    text = sensor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Aggregation Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedAggregation,
            onExpandedChange = { expandedAggregation = !expandedAggregation }
        ) {
            OutlinedTextField(
                value = aggregation,
                onValueChange = { },
                readOnly = true,
                label = { Text("Aggregation") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAggregation) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedAggregation,
                onDismissRequest = { expandedAggregation = false }
            ) {
                aggregationOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onAggregationChange(option)
                            expandedAggregation = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Conditionally show Interval Dropdown (only when aggregation is AVG)
        if (aggregation == "AVG") {
            ExposedDropdownMenuBox(
                expanded = expandedInterval,
                onExpandedChange = { expandedInterval = !expandedInterval }
            ) {
                OutlinedTextField(
                    value = interval,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Interval") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInterval) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedInterval,
                    onDismissRequest = { expandedInterval = false }
                ) {
                    intervalOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onIntervalChange(option)
                                expandedInterval = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Conditionally show Limit Dropdown (only when aggregation is NONE)
        if (aggregation == "NONE") {
            ExposedDropdownMenuBox(
                expanded = expandedLimit,
                onExpandedChange = { expandedLimit = !expandedLimit }
            ) {
                OutlinedTextField(
                    value = limit.toString(),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Limit") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLimit) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedLimit,
                    onDismissRequest = { expandedLimit = false }
                ) {
                    limitOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.toString()) },
                            onClick = {
                                onLimitChange(option)
                                expandedLimit = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Filters")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // From Date Picker Dialog
    if (showFromDatePicker) {
        DatePickerDialog(
            initialDate = from,
            onDateSelected = { selectedDate ->
                onFromChange(selectedDate)
                showFromDatePicker = false
            },
            onDismiss = { showFromDatePicker = false }
        )
    }

    // To Date Picker Dialog
    if (showToDatePicker) {
        DatePickerDialog(
            initialDate = to,
            onDateSelected = { selectedDate ->
                onToChange(selectedDate)
                showToDatePicker = false
            },
            onDismiss = { showToDatePicker = false }
        )
    }
}