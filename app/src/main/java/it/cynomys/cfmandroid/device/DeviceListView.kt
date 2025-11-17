// DeviceListView.kt
package it.cynomys.cfmandroid.device

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.cynomys.cfmandroid.util.shimmer.FullScreenShimmer
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListView(
    viewModel: DeviceViewModel,
    ownerId: UUID,
    farmId: UUID,
    navController: NavController,
    onBack: () -> Unit,
    onAddDevice: (farmId: UUID) -> Unit,
    onEditDevice: (Device) -> Unit, // Added Callback
    onDeviceSelected: (Device, farmId: UUID) -> Unit
) {
    val devices by viewModel.devices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(ownerId, farmId) {
        viewModel.getDevices(ownerId, farmId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Devices") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onAddDevice(farmId) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Device")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ... [Keep loading/error logic same as before] ...
            if (isLoading) {
                FullScreenShimmer(title = "Loading Devices...")
            } else if (error != null) {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (devices.isEmpty()) {
                Text("No devices found. Add a new device to get started!")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(devices) { device ->
                        DeviceItem(
                            device = device,
                            onItemClick = {
                                onDeviceSelected(device, farmId)
                            },
                            onEdit = { onEditDevice(device) }, // Pass to item
                            onDelete = {
                                device.id?.let { viewModel.deleteDevice(it, ownerId, farmId) }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItem(
    device: Device,
    onItemClick: () -> Unit,
    onEdit: () -> Unit, // Added
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = device.displayName, style = MaterialTheme.typography.titleLarge)
            Text(text = "ID: ${device.deviceID}")
            Text(text = "Predictions: ${if (device.predictions) "Enabled" else "Disabled"}")

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Edit Button
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Delete Button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}