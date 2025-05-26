package it.cynomys.cfmandroid.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.cynomys.cfmandroid.model.DeviceDto
import it.cynomys.cfmandroid.viewmodel.DeviceViewModel
import java.util.UUID
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun DeviceEditView(
    deviceId: UUID,
    viewModel: DeviceViewModel,
    navController: NavController
) {
    val device by viewModel.selectedDevice.collectAsState()
    var deviceDto by remember { mutableStateOf(DeviceDto("", "", false, "")) }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(deviceId) {
        viewModel.getDeviceById(deviceId)
    }

    LaunchedEffect(device) {
        device?.let {
            deviceDto = DeviceDto(
                deviceID = it.deviceID,
                displayName = it.displayName,
                predictions = it.predictions,
                indexes = it.indexes
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Device") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.updateDevice(deviceDto)
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

            OutlinedTextField(
                value = deviceDto.deviceID,
                onValueChange = { deviceDto = deviceDto.copy(deviceID = it) },
                label = { Text("Device ID") },
                modifier = Modifier.fillMaxWidth()
            )

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