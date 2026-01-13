package it.cynomys.cfmandroid.sensordata

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SensorDataTable(
    sensorData: SensorData,
    selectedSensors: Set<String>
) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    val allTimestamps: List<Date> = sensorData.data
        .filterKeys { selectedSensors.contains(it) }
        .flatMap { (_, values) -> values.map { Date(it.ts) } }
        .distinct()
        .sortedByDescending { it.time }

    val sensorValueMap: Map<String, Map<Date, Any>> = sensorData.data
        .filterKeys { selectedSensors.contains(it) }
        .mapValues { (_, values) ->
            values.associateBy({ Date(it.ts) }, { it.value })
        }

    val horizontalScrollState = rememberScrollState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Table Header
        item {
            Row(
                modifier = Modifier.horizontalScroll(horizontalScrollState)
            ) {
                Text(
                    text = "Timestamp",
                    modifier = Modifier.width(160.dp),
                    style = MaterialTheme.typography.labelLarge
                )
                selectedSensors.forEach { sensor ->
                    Text(
                        text = titleFromKey(sensor),
                        modifier = Modifier
                            .width(100.dp)
                            .padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Table Rows
        items(allTimestamps) { ts ->
            Row(
                modifier = Modifier.horizontalScroll(horizontalScrollState)
            ) {
                Text(
                    text = formatter.format(ts),
                    modifier = Modifier.width(160.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                selectedSensors.forEach { sensor ->
                    val value = sensorValueMap[sensor]?.get(ts)
                    Text(
                        text = formatValue(value),
                        modifier = Modifier
                            .width(100.dp)
                            .padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Helper to safely format values with exactly 1 decimal point
fun formatValue(value: Any?): String {
    return when (value) {
        is Number -> String.format(Locale.getDefault(), "%.1f", value.toDouble())
        is String -> {
            value.toDoubleOrNull()?.let {
                String.format(Locale.getDefault(), "%.1f", it)
            } ?: value
        }
        else -> "-" // missing/null
    }
}
