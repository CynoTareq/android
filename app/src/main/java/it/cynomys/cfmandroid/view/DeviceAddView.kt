package it.cynomys.cfmandroid.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.cynomys.cfmandroid.model.DeviceDto
import it.cynomys.cfmandroid.viewmodel.DeviceViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceAddView(
    ownerId: UUID,
    farmId: UUID,
    viewModel: DeviceViewModel,
    navController: NavController
) {
    var deviceDto by remember { mutableStateOf(DeviceDto("", "", false, "")) }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Listen for scanned result
    val scannedDeviceId = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("scannedDeviceId")
        ?.observeAsState()

    // Auto-fill Device ID when scanned
    scannedDeviceId?.value?.let {
        deviceDto = deviceDto.copy(deviceID = it)
        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedDeviceId")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Device") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.createDevice(ownerId, farmId, deviceDto)
                            navController.popBackStack()
                        },
                        enabled = deviceDto.deviceID.isNotBlank() &&
                                deviceDto.displayName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
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

            // Device ID with QR Scan Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = deviceDto.deviceID,
                    onValueChange = { deviceDto = deviceDto.copy(deviceID = it) },
                    label = { Text("Device ID") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                IconButton(
                    onClick = {
                        navController.navigate("qr_scan")
                    }
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = deviceDto.displayName,
                onValueChange = { deviceDto = deviceDto.copy(displayName = it) },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = deviceDto.predictions,
                    onCheckedChange = { deviceDto = deviceDto.copy(predictions = it) }
                )
                Text("Enable Predictions")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = deviceDto.indexes,
                onValueChange = { deviceDto = deviceDto.copy(indexes = it) },
                label = { Text("Indexes") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
