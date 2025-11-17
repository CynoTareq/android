package it.cynomys.cfmandroid.silo

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.gson.JsonPrimitive
import it.cynomys.cfmandroid.device.IndexGenerator
import it.cynomys.cfmandroid.device.License
import it.cynomys.cfmandroid.device.LicenseItem
import java.util.UUID


// Step definitions
data class SiloStep(
    val title: String,
    val description: String
)

val siloSteps = listOf(
    SiloStep("Shape", "Select silo type"),
    SiloStep("Dimensions", "Enter measurements"),
    SiloStep("Details", "Material & info"),
    SiloStep("Review", "Confirm details")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiloFormView(
    viewModel: SiloViewModel,
    navController: NavController,
    ownerId: UUID,
    ownerEmail:String,
    farmId: UUID,
    penId: UUID?,
    siloToEdit: Silo? = null
) {
    // Determine edit mode and original license ID for license re-selection logic
    val isEditMode = siloToEdit != null
    val siloToEditLicenseId = siloToEdit?.licenseId

    var currentStep by remember { mutableStateOf(0) }
    var siloDto by remember {
        mutableStateOf(
            siloToEdit?.toSiloDto() ?: SiloDto(
                silosID = "",
                displayName = "",
                silosHeight = 0.0,
                silosDiameter = 0.0,
                coneHeight = null,
                bottomDiameter = null,
                shape = SiloShape.FULL_CYLINDRICAL,
                penId = penId,
                farmId = farmId,
                ownerId = ownerId,
                model = SiloModel("", ""),
                material_name = "",
                material_density = 0.0,
                license = null,
                licenseId = null,
                indexes = "",
                shouldNotify = true,
                predictions = true
            )
        )
    }

    var usePresetModel by remember { mutableStateOf(false) }
    var selectedManufacturer by remember { mutableStateOf<String?>(null) }
    var selectedModel by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val siloLicenses by viewModel.siloLicenses.collectAsState()

    val context = LocalContext.current

    // Fetch materials and licenses when the view is composed
    LaunchedEffect(Unit) {
        viewModel.fetchSiloMaterials()
        viewModel.loadAvailableLicenses(ownerEmail = ownerEmail)
    }

    // Get manufacturers and filtered models
    val manufacturers = remember {
        SiloModels.SILO_MODELS.map { it.manufacturer }.distinct()
    }
    val filteredModels = remember(selectedManufacturer) {
        selectedManufacturer?.let { manufacturer ->
            SiloModels.SILO_MODELS
                .filter { it.manufacturer == manufacturer }
                .map { it.model }
        } ?: emptyList()
    }

    // Update dimensions when preset model is selected
    LaunchedEffect(selectedManufacturer, selectedModel, usePresetModel) {
        if (usePresetModel && selectedManufacturer != null && selectedModel != null) {
            val modelSpec = SiloModels.SILO_MODELS.find {
                it.manufacturer == selectedManufacturer && it.model == selectedModel
            }
            modelSpec?.let {
                siloDto = siloDto.copy(
                    silosHeight = it.lengthInMM / 10.0,
                    silosDiameter = it.diameterInMM / 10.0,
                    model = SiloModel(it.manufacturer, it.model)
                )
            }
        }
    }

    // --- QR Scanner Logic Start ---
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
        savedStateHandle?.get<String>("scannedSiloId")?.let { scannedCode ->
            siloDto = siloDto.copy(silosID = scannedCode)
            Log.d("SiloAddView", "silosID updated from QR: $scannedCode")
            savedStateHandle.remove<String>("scannedSiloId")
        }
    }

    val launchQRScanner: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            navController.navigate("qr_scanner")
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    // --- QR Scanner Logic End ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (siloToEdit != null) "Edit Silo" else "Add New Silo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Custom Stepper (defined in CustomStepper.kt)
            CustomStepper(
                steps = siloSteps,
                currentStep = currentStep,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            )

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    0 -> ShapeSelectionStep( // defined in CustomStepper.kt
                        selectedShape = siloDto.shape,
                        onShapeSelected = { shape ->
                            siloDto = siloDto.copy(shape = shape)
                        }
                    )
                    1 -> DimensionsStep( // defined in CustomStepper.kt
                        siloDto = siloDto,
                        usePresetModel = usePresetModel,
                        selectedManufacturer = selectedManufacturer,
                        selectedModel = selectedModel,
                        manufacturers = manufacturers,
                        filteredModels = filteredModels,
                        onUsePresetModelChange = { usePresetModel = it },
                        onManufacturerSelected = {
                            selectedManufacturer = it
                            selectedModel = null
                        },
                        onModelSelected = { selectedModel = it },
                        onDimensionChange = { field, value ->
                            siloDto = when (field) {
                                "height" -> siloDto.copy(silosHeight = value)
                                "diameter" -> siloDto.copy(silosDiameter = value)
                                "coneHeight" -> siloDto.copy(coneHeight = value)
                                "bottomDiameter" -> siloDto.copy(bottomDiameter = value)
                                else -> siloDto
                            }
                        }
                    )
                    2 -> DetailsStep( // MODIFIED IMPLEMENTATION BELOW
                        siloDto = siloDto,
                        isEditMode = isEditMode,
                        availableMaterials = materials,
                        availableLicenses = siloLicenses,
                        siloToEditLicenseId = siloToEdit?.licenseId.toLicenseInfoOrNull()?.id,
                        onFieldChange = { field, value ->
                            siloDto = when (field) {
                                "silosID" -> siloDto.copy(silosID = value as String)
                                "displayName" -> siloDto.copy(displayName = value as String)
                                "materialName" -> {
                                    val material = value as SiloMaterial
                                    siloDto.copy(
                                        material_name = material.materialName,
                                        material_density = material.density
                                    )
                                }
                                "license" -> {
                                    val license = value as License
                                    val newIndexes = IndexGenerator.getSiloIndexesByLicense(license.name)
                                    siloDto.copy(
                                        license = license.name,
                                        licenseId = license.id,
                                        indexes = newIndexes
                                    )
                                }
                                "shouldNotify" -> siloDto.copy(shouldNotify = value as Boolean)
                                "predictions" -> siloDto.copy(predictions = value as Boolean)
                                else -> siloDto
                            }
                        },
                        onScanQR = launchQRScanner
                    )
                    3 -> ReviewStep(siloDto = siloDto)
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentStep == 0) {
                            navController.popBackStack()
                        } else {
                            currentStep--
                        }
                    }
                ) {
                    Text(if (currentStep == 0) "Cancel" else "Back")
                }

                Button(
                    onClick = {
                        if (currentStep == siloSteps.size - 1) {
                            // Save logic remains the same
                            if (siloToEdit != null) {
                                val selectedLicenseInfo = siloLicenses.find { it.id == siloDto.licenseId }
                                val updatedSilo = Silo(
                                    id = siloToEdit.id,
                                    silosID = siloDto.silosID,
                                    displayName = siloDto.displayName,
                                    silosHeight = siloDto.silosHeight,
                                    silosDiameter = siloDto.silosDiameter,
                                    coneHeight = siloDto.coneHeight,
                                    bottomDiameter = siloDto.bottomDiameter,
                                    shape = siloDto.shape,
                                    penId = siloDto.penId,
                                    farmId = siloDto.farmId,
                                    ownerId = siloDto.ownerId,
                                    model = siloDto.model,
                                    material_name = siloDto.material_name,
                                    material_density = siloDto.material_density,
                                    license = siloDto.license,
                                    licenseId = selectedLicenseInfo?.let {
                                       JsonPrimitive(it.id.toString())
                                    },
                                    indexes = siloDto.indexes,
                                    shouldNotify = siloDto.shouldNotify,
                                    predictions = siloDto.predictions,
                                    lastSyncTime = siloToEdit.lastSyncTime
                                )
                                viewModel.updateSilo(updatedSilo, ownerId, farmId)
                            } else {
                                viewModel.addSilo(ownerId, farmId, siloDto)
                            }
                            navController.popBackStack()
                        } else {
                            currentStep++
                        }
                    },
                    enabled = isStepValid(currentStep, siloDto)
                ) {
                    Text(if (currentStep == siloSteps.size - 1) "Save" else "Next")
                }
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


// --- Details Step (Implementation of LicenseItem card view) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsStep(
    siloDto: SiloDto,
    isEditMode: Boolean,
    availableMaterials: List<SiloMaterial>,
    availableLicenses: List<License>,
    siloToEditLicenseId: UUID?,
    onFieldChange: (String, Any) -> Unit,
    onScanQR: () -> Unit
) {
    var expandedMaterial by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Silo Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = siloDto.displayName,
            onValueChange = { onFieldChange("displayName", it) },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Silo ID with QR Scanner Button
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = siloDto.silosID,
                onValueChange = { onFieldChange("silosID", it.trim().replace(" ", "")) },
                label = { Text("Silo ID") },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            IconButton(onClick = onScanQR) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR for Silo ID")
            }
        }

        // Material Name Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedMaterial,
            onExpandedChange = { expandedMaterial = !expandedMaterial }
        ) {
            OutlinedTextField(
                value = siloDto.material_name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Material Name") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMaterial) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedMaterial,
                onDismissRequest = { expandedMaterial = false }
            ) {
                availableMaterials.forEach { material ->
                    DropdownMenuItem(
                        text = { Text(material.materialName) },
                        onClick = {
                            onFieldChange("materialName", material)
                            expandedMaterial = false
                        }
                    )
                }
            }
        }

        // Display Density as read-only text
        Text(
            text = "Material Density: ${siloDto.material_density} kg/m³",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // --- NEW: License Selection Card View using LicenseItem ---
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Select License",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (availableLicenses.isEmpty()) {
            Text("No silo licenses available.", color = MaterialTheme.colorScheme.error)
        } else {
            // Two-column layout for LicenseItem cards
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableLicenses.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // License Item 1
                        LicenseItem(
                            license = pair[0],
                            isSelected = siloDto.licenseId == pair[0].id,
                            onSelect = {
                                // Logic to allow selection: not in use, OR it's the current one being edited
                                val isAvailable = !pair[0].inUse || (isEditMode && pair[0].id == siloToEditLicenseId)
                                if (isAvailable) {
                                    onFieldChange("license", pair[0])
                                } else {
                                    Toast.makeText(context, "This license is already in use.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        // License Item 2 (if present)
                        if (pair.size > 1) {
                            LicenseItem(
                                license = pair[1],
                                isSelected = siloDto.licenseId == pair[1].id,
                                onSelect = {
                                    // Logic to allow selection: not in use, OR it's the current one being edited
                                    val isAvailable = !pair[1].inUse || (isEditMode && pair[1].id == siloToEditLicenseId)
                                    if (isAvailable) {
                                        onFieldChange("license", pair[1])
                                    } else {
                                        Toast.makeText(context, "This license is already in use.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Empty spacer to maintain two-column layout symmetry
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        // --- END License Selection Card View ---

        Spacer(modifier = Modifier.height(16.dp))

        // Indexes field
        OutlinedTextField(
            value = siloDto.indexes ?: "Not Selected",
            onValueChange = {  },
            label = { Text("Indexes") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Auto generated") }
        )

        // Predictions toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Enable Predictions", fontWeight = FontWeight.Medium)
                Text(
                    "Prediction for Gases",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = siloDto.predictions,
                onCheckedChange = { onFieldChange("predictions", it) }
            )
        }

        // Notifications toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Enable Notifications", fontWeight = FontWeight.Medium)
                Text(
                    "Receive alerts about this silo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = siloDto.shouldNotify,
                onCheckedChange = { onFieldChange("shouldNotify", it) }
            )
        }
    }
}

// --- Auxiliary functions local to SiloFormView.kt ---

@Composable
fun ReviewStep(siloDto: SiloDto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Review Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReviewItem("Display Name", siloDto.displayName)
                ReviewItem("Silo ID", siloDto.silosID)
                ReviewItem("Shape", siloDto.shape.name.replace("_", " "))
                ReviewItem("Height", "${siloDto.silosHeight} cm")
                ReviewItem("Diameter", "${siloDto.silosDiameter} cm")

                if (siloDto.shape == SiloShape.CONICAL_BOTTOM) {
                    ReviewItem("Cone Height", "${siloDto.coneHeight} cm")
                    ReviewItem("Bottom Diameter", "${siloDto.bottomDiameter} cm")
                }

                ReviewItem("Material", siloDto.material_name)
                ReviewItem("Density", "${siloDto.material_density} kg/m³")

                ReviewItem("License", siloDto.license ?: "Not assigned")
                ReviewItem("Indexes", siloDto.indexes?.takeIf { it.isNotBlank() } ?: "None")
                ReviewItem("Predictions", if (siloDto.predictions) "Enabled" else "Disabled")
                ReviewItem("Notifications", if (siloDto.shouldNotify) "Enabled" else "Disabled")

                if (siloDto.model.manufacturer.isNotEmpty()) {
                    ReviewItem("Manufacturer", siloDto.model.manufacturer)
                    ReviewItem("Model", siloDto.model.model)
                }
            }
        }
    }
}

@Composable
fun ReviewItem(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value ?: "N/A",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

fun isStepValid(step: Int, siloDto: SiloDto): Boolean {
    return when (step) {
        0 -> true
        1 -> {
            val hasModel = siloDto.model.manufacturer.isNotEmpty() && siloDto.model.model.isNotEmpty()
            val hasBasicDimensions = siloDto.silosHeight > 0 && siloDto.silosDiameter > 0
            val hasConicalDimensions = if (siloDto.shape == SiloShape.CONICAL_BOTTOM) {
                siloDto.coneHeight != null && siloDto.coneHeight!! > 0 &&
                        siloDto.bottomDiameter != null && siloDto.bottomDiameter!! > 0
            } else true

            hasModel || (hasBasicDimensions && hasConicalDimensions)
        }
        2 -> {
            siloDto.displayName.isNotBlank() &&
                    siloDto.material_name.isNotBlank() &&
                    siloDto.silosID.isNotBlank() &&
                    siloDto.material_density > 0 &&
                    siloDto.license != null &&
                    siloDto.licenseId != null
        }
        3 -> true
        else -> false
    }
}