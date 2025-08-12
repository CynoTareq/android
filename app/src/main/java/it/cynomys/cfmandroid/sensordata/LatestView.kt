package it.cynomys.cfmandroid.sensordata

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.cynomys.cfmandroid.auth.AuthViewModel

@Composable
fun LatestView(
    deviceID: String,
    farmId: String,
    viewModel: SensorDataViewModel,
    authViewModel: AuthViewModel
) {
    val latestSensorData by viewModel.latestSensorData.collectAsState()
    val isLoadingLatest by viewModel.isLoadingLatest.collectAsState()
    val latestErrorMessage by viewModel.errorMessageLatest.collectAsState()

    val indexes by viewModel.indexes.collectAsState()
    val isLoadingIndexes by viewModel.isLoadingIndexes.collectAsState()
    val indexErrorMessage by viewModel.indexErrorMessage.collectAsState()

    val currentDeviceId by rememberUpdatedState(deviceID)
    val currentFarmId by rememberUpdatedState(farmId)

    // Debug logging
    Log.d("LatestView", "Device ID: $currentDeviceId, Farm ID: $currentFarmId")
    Log.d("LatestView", "Latest sensor data size: ${latestSensorData.size}")
    Log.d("LatestView", "Is loading latest: $isLoadingLatest")
    Log.d("LatestView", "Latest error message: $latestErrorMessage")
    Log.d("LatestView", "Indexes size: ${indexes.size}")

    // LaunchedEffect to fetch data when deviceID or farmId changes
    LaunchedEffect(currentDeviceId, currentFarmId) {
        Log.d("LatestView", "LaunchedEffect triggered for deviceID: $currentDeviceId, farmId: $currentFarmId")

        // Fetch latest sensor data
        viewModel.getLatestValues(currentDeviceId)

        // Fetch indexes
        viewModel.getIndexes(
            farmId = currentFarmId,
            typeId = currentDeviceId,
            type = "DEVICE"
        )
    }

    if (isLoadingLatest || isLoadingIndexes) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (!latestErrorMessage.isNullOrBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Error: $latestErrorMessage", color = MaterialTheme.colorScheme.error)
        }
    } else if (!indexErrorMessage.isNullOrBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Index Error: $indexErrorMessage", color = MaterialTheme.colorScheme.error)
        }
    } else {
        val allSensorDisplayItems: List<SensorDisplayItem> = latestSensorData.values.toList().sortedByDescending { it.timestamp }
        val allIndexDisplayItems = indexes

        Log.d("LatestView", "Sensor display items: ${allSensorDisplayItems.size}")
        Log.d("LatestView", "Index display items: ${allIndexDisplayItems.size}")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(16.dp))
        ) {
            if (allIndexDisplayItems.isNotEmpty()) {
                Text(
                    text = "Indexes",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Changed from LazyVerticalGrid to LazyHorizontalGrid for horizontal scrolling
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2), // Defines 2 rows for horizontal scrolling
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp) // Explicit height for the horizontal grid
                ) {
                    items(allIndexDisplayItems.size) { index ->
                        IndexCard(
                            title = allIndexDisplayItems[index].name,
                            value = allIndexDisplayItems[index].score,
                            description = allIndexDisplayItems[index].description,
                            status = allIndexDisplayItems[index].status,
                            maxValue = 100.00
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text(
                    text = "No indexes available for this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (allSensorDisplayItems.isNotEmpty()) {
                Text(
                    text = "Latest Readings",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Display items in 2 columns
                    contentPadding = PaddingValues(4.dp), // Add padding around the grid content
                    verticalArrangement = Arrangement.spacedBy(8.dp), // Spacing between rows
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between columns
                    modifier = Modifier.fillMaxSize() // Fill available space
                ) {
                    items(allSensorDisplayItems.size) { index ->
                        SensorItem(
                            title = allSensorDisplayItems[index].name,
                            value = "%.1f".format(allSensorDisplayItems[index].value),
                            unit = SensorUnit.unitFor(allSensorDisplayItems[index].name)
                        )
                    }
                }
            } else {
                Text(
                    text = "No latest sensor data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun SensorItem(title: String, value: String, unit: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = title.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}