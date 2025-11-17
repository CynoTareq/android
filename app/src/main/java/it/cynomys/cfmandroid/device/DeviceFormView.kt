package it.cynomys.cfmandroid.device

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import it.cynomys.cfmandroid.farm.Species
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceFormView(
    viewModel: DeviceViewModel,
    navController: NavController,
    ownerId: UUID,
    farmId: UUID,
    ownerEmail: String,
    farmSpecies: Species,
    existingDevice: Device? = null
) {
    val isEditMode = existingDevice != null
    val context = LocalContext.current

    var deviceID by remember { mutableStateOf(existingDevice?.deviceID ?: "") }
    var displayName by remember { mutableStateOf(existingDevice?.displayName ?: "") }
    var predictions by remember { mutableStateOf(existingDevice?.predictions ?: true) }
    var indexes by remember { mutableStateOf(existingDevice?.indexes ?: "") }
    var shouldNotify by remember { mutableStateOf(existingDevice?.shouldNotify ?: false) }
    var selectedLicense by remember { mutableStateOf<License?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val availableLicenses by viewModel.availableLicenses.collectAsState()
    val isLoadingLicenses by viewModel.isLoadingLicenses.collectAsState()

    LaunchedEffect(ownerEmail) {
        viewModel.loadAvailableLicenses(ownerEmail)
    }

    LaunchedEffect(availableLicenses, existingDevice) {
        if (isEditMode && availableLicenses.isNotEmpty() && selectedLicense == null) {
            selectedLicense = availableLicenses.find { it.id == existingDevice?.licenseId }
        }
    }

    LaunchedEffect(selectedLicense) {
        selectedLicense?.let { license ->
            val generatedIndexes = IndexGenerator.getIndexesByLicenseAndSpecies(
                license = license.name,
                species = farmSpecies
            )
            indexes = generatedIndexes
            Log.d("DeviceFormView", "Auto-filled indexes for license: ${license.name}")
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navController.navigate("qr_scanner")
        } else {
            Toast.makeText(context, "Camera permission is required to scan QR codes.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.get<String>("scannedDeviceId")?.let { scannedCode ->
            deviceID = scannedCode
            Log.d("DeviceFormView", "deviceID updated from QR: $deviceID")
            savedStateHandle.remove<String>("scannedDeviceId")
        }
    }

    fun onSave() {
        val license = selectedLicense
        if (license == null) {
            Toast.makeText(context, "Please select a license", Toast.LENGTH_SHORT).show()
            return
        }

        val isLicenseAvailable = !license.inUse || (isEditMode && license.id == existingDevice?.licenseId)
        if (!isLicenseAvailable) {
            Toast.makeText(context, "Please select an available license", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode && existingDevice != null) {
            val updateDto = DeviceUpdateDto(
                id = existingDevice.id,
                deviceID = deviceID,
                displayName = displayName,
                predictions = predictions,
                indexes = indexes,
                license = license.name,
                licenseId = license.id,
                shouldNotify = shouldNotify
            )
            viewModel.updateDevice(updateDto, ownerId, farmId)
        } else {
            val deviceDto = DeviceDto(
                deviceID = deviceID,
                displayName = displayName,
                predictions = predictions,
                indexes = indexes,
                licenseId = license.id,
                license = license.name,
                shouldNotify = shouldNotify
            )
            viewModel.addDevice(ownerId, farmId, deviceDto)
        }
        navController.popBackStack()
    }

    val canSave = !isLoading && selectedLicense != null &&
            (!selectedLicense!!.inUse || (isEditMode && selectedLicense!!.id == existingDevice?.licenseId))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Device" else "Add New Device") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onSave() }, enabled = canSave) {
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
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = deviceID,
                    onValueChange = { deviceID = it.trim().replace(" ","") },
                    label = { Text("Device ID") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                IconButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            navController.navigate("qr_scanner")
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = indexes,
                onValueChange = { },
                label = { Text("Indexes (Auto-generated)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                readOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = shouldNotify, onCheckedChange = { shouldNotify = it })
                Text("Enable Notifications")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Select License", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoadingLicenses) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (availableLicenses.isEmpty()) {
                Text("No licenses available for this owner", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableLicenses.chunked(2)) { pair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LicenseItem(
                                license = pair[0],
                                isSelected = selectedLicense?.id == pair[0].id,
                                onSelect = {
                                    if (!pair[0].inUse || (isEditMode && pair[0].id == existingDevice?.licenseId)) {
                                        selectedLicense = pair[0]
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            if (pair.size > 1) {
                                LicenseItem(
                                    license = pair[1],
                                    isSelected = selectedLicense?.id == pair[1].id,
                                    onSelect = {
                                        if (!pair[1].inUse || (isEditMode && pair[1].id == existingDevice?.licenseId)) {
                                            selectedLicense = pair[1]
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}