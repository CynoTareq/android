package it.cynomys.cfmandroid.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.cynomys.cfmandroid.model.Device
import it.cynomys.cfmandroid.viewmodel.DeviceViewModel
import java.util.UUID
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun DeviceDetailView(
    deviceId: UUID,
    viewModel: DeviceViewModel,
    navController: NavController
) {
    val device by viewModel.selectedDevice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(deviceId) {
        viewModel.getDeviceById(deviceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device?.displayName ?: "Device Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate("edit_device/${device?.id}")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
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
        DetailItem("Device ID", device.deviceID)
        DetailItem("Display Name", device.displayName)
        DetailItem("Predictions", if (device.predictions) "Enabled" else "Disabled")
        DetailItem("Indexes", device.indexes)
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}