package it.cynomys.cfmandroid.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.cynomys.cfmandroid.Screen
import it.cynomys.cfmandroid.model.Device
import it.cynomys.cfmandroid.viewmodel.DeviceViewModel
import java.util.UUID
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun DeviceListView(
    viewModel: DeviceViewModel,
    ownerId: UUID,
    farmId: UUID,
    navController: NavController
) {
    val devices by viewModel.devices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(ownerId, farmId) {
        viewModel.getDevices(ownerId, farmId)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_device/$ownerId/$farmId") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Device")
            }
        },
        topBar = {
            TopAppBar(title = { Text("Devices") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
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

            if (devices.isEmpty() && !isLoading) {
                Text(
                    text = "No devices found",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(devices) { device ->
                        DeviceItem(
                            device = device,
                            onItemClick = {
                                device.id?.let {
                                    navController.navigate(Screen.DeviceDetail.createRoute(device.deviceID))
                                }
                            }
                            ,
                            onDelete = {
                                viewModel.deleteDevice(device.id!!, ownerId, farmId)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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