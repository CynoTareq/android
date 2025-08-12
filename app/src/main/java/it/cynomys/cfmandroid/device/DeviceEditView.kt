package it.cynomys.cfmandroid.device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceEditView(
    deviceId: String?,
    farmId: UUID,
    ownerId: UUID,
    viewModel: DeviceViewModel,
    navController: NavController,
    onBack: () -> Unit // Added onBack parameter
) {
    val device by viewModel.selectedDevice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var deviceDto by remember { mutableStateOf(DeviceDto("", "", false, "")) }

    LaunchedEffect(deviceId, ownerId, farmId) { // Added ownerId and farmId to the key
        if (deviceId != null) {
            // Corrected: Call getDeviceByDeviceId and pass ownerId and farmId
            viewModel.getDeviceByDeviceId(
                deviceID = deviceId,
                ownerId = ownerId,
                farmId = farmId
            )
        }
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
                    IconButton(onClick = onBack) { // Used onBack here
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            device?.let { originalDevice ->
                                // Corrected: Create a Device object with updated fields
                                val updatedDevice = originalDevice.copy(
                                    deviceID = deviceDto.deviceID,
                                    displayName = deviceDto.displayName,
                                    predictions = deviceDto.predictions,
                                    indexes = deviceDto.indexes,
                                    // Ensure ownerId, farmId, and penId are carried over or updated
                                    ownerId = ownerId,
                                    farmId = farmId,
                                    penId = originalDevice.penId // Keep original penId
                                )
                                // Corrected: Pass the updated Device object, ownerId, and farmId
                                viewModel.updateDevice(
                                    device = updatedDevice,
                                    ownerId = ownerId,
                                    farmId = farmId
                                )
                                navController.popBackStack()
                            }
                        },
                        enabled = !isLoading
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
