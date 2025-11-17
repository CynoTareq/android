// DeviceDetailView.kt
package it.cynomys.cfmandroid.device

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailView(
    deviceId: String?, // Changed to String? to match navigation argument
    farmId: UUID, // Added farmId parameter
    ownerId: UUID, // Added ownerId parameter
    viewModel: DeviceViewModel,
    navController: NavController
) {
    val device by viewModel.selectedDevice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(deviceId, ownerId, farmId) { // Add ownerId and farmId to the key
        if (deviceId != null) {
            viewModel.getDeviceByDeviceId(
                deviceID = deviceId,
                ownerId = ownerId,
                farmId = farmId
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    device?.let {
                        IconButton(onClick = { navController.navigate("edit_device/${it.id}/${it.farmId}") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            device?.let {
                DeviceDetailContent(device = it)
            }
        }
    }
}

@Composable
fun DeviceDetailContent(device: Device) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Log.d("DeviceDetail", "model=${device.deviceID}, serial=${device.displayName}")

        DetailItem("Device ID", device.deviceID)
        DetailItem("Display Name", device.displayName)
        DetailItem("Predictions", if (device.predictions) "Enabled" else "Disabled")
        DetailItem("Indexes", device.indexes)
    }
}

@Composable
fun DetailItem(label: String, value: String?) { // Value can be null
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value ?: "N/A", style = MaterialTheme.typography.bodyLarge)
    }
}
