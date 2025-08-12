package it.cynomys.cfmandroid.silo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiloEditView(
    siloId: String?, // Silo ID passed as a navigation argument
    farmId: UUID, // Farm ID associated with the silo
    ownerId: UUID, // Owner ID associated with the silo
    penId: UUID, // Pen ID associated with the silo
    viewModel: SiloViewModel,
    navController: NavController,
    onBack: () -> Unit // Callback for back navigation
) {
    // Collect the selected silo, loading state, and error state from the ViewModel
    val silo by viewModel.selectedSilo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Mutable state to hold the editable silo data, initialized with default values
    var editableSilo by remember {
        mutableStateOf(
            Silo(
                id = null,
                silosID = "",
                displayName = "",
                silosHeight = 0.0,
                silosDiameter = 0.0,
                coneHeight = 0.0,
                bottomDiameter = 0.0,
                shape = SiloShape.FULL_CYLINDRICAL,
                penId = null,
                farmId = null,
                ownerId = null,
                model = SiloModel("", ""),
                material_name = "",
                material_density = 0.0,
                lastSyncTime = null
            )
        )
    }

    // Fetch silo details when the composable is launched or siloId/ownerId/farmId/penId changes
    LaunchedEffect(siloId, ownerId, farmId, penId) {
        if (siloId != null) {
            try {
                viewModel.getSiloById(UUID.fromString(siloId))
            } catch (e: IllegalArgumentException) {
                // Handle invalid UUID format

            }
        }
    }

    // Update editableSilo state when the `silo` (from ViewModel) changes
    LaunchedEffect(silo) {
        silo?.let {
            editableSilo = it // Populate editable fields with fetched silo data
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Silo") },
                navigationIcon = {
                    // Back button
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save button, enabled only when not loading and a silo is loaded
                    IconButton(
                        onClick = {
                            silo?.let { originalSilo ->
                                // Create an updated Silo object from the editable state
                                val updatedSilo = originalSilo.copy(
                                    silosID = editableSilo.silosID,
                                    displayName = editableSilo.displayName,
                                    silosHeight = editableSilo.silosHeight,
                                    silosDiameter = editableSilo.silosDiameter,
                                    coneHeight = editableSilo.coneHeight,
                                    bottomDiameter = editableSilo.bottomDiameter,
                                    shape = editableSilo.shape,
                                    material_name = editableSilo.material_name,
                                    material_density = editableSilo.material_density,
                                    model = editableSilo.model,
                                    // Ensure ownerId, farmId, and penId are carried over from original or provided
                                    ownerId = originalSilo.ownerId ?: ownerId,
                                    farmId = originalSilo.farmId ?: farmId,
                                    penId = originalSilo.penId ?: penId
                                )
                                viewModel.updateSilo(updatedSilo, ownerId, farmId)
                                navController.popBackStack() // Navigate back after saving
                            }
                        },
                        enabled = !isLoading && silo != null // Disable button while loading or if no silo is loaded
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
            // Display loading indicator
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // Display error message
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Input field for Silo ID
            OutlinedTextField(
                value = editableSilo.silosID,
                onValueChange = { editableSilo = editableSilo.copy(silosID = it) },
                label = { Text("Silo ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Display Name
            OutlinedTextField(
                value = editableSilo.displayName,
                onValueChange = { editableSilo = editableSilo.copy(displayName = it) },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Silo Height
            OutlinedTextField(
                value = editableSilo.silosHeight.toString(),
                onValueChange = {
                    editableSilo = editableSilo.copy(silosHeight = it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Silo Height (units)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Silo Diameter
            OutlinedTextField(
                value = editableSilo.silosDiameter.toString(),
                onValueChange = {
                    editableSilo = editableSilo.copy(silosDiameter = it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Silo Diameter (units)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Cone Height (nullable)
            OutlinedTextField(
                value = editableSilo.coneHeight?.toString() ?: "",
                onValueChange = {
                    editableSilo = editableSilo.copy(coneHeight = it.toDoubleOrNull())
                },
                label = { Text("Cone Height (units, optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Bottom Diameter (nullable)
            OutlinedTextField(
                value = editableSilo.bottomDiameter?.toString() ?: "",
                onValueChange = {
                    editableSilo = editableSilo.copy(bottomDiameter = it.toDoubleOrNull())
                },
                label = { Text("Bottom Diameter (units, optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Material Name
            OutlinedTextField(
                value = editableSilo.material_name,
                onValueChange = { editableSilo = editableSilo.copy(material_name = it) },
                label = { Text("Material Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Material Density
            OutlinedTextField(
                value = editableSilo.material_density.toString(),
                onValueChange = {
                    editableSilo = editableSilo.copy(material_density = it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Material Density (units)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input fields for Silo Model
            OutlinedTextField(
                value = editableSilo.model.manufacturer,
                onValueChange = { editableSilo = editableSilo.copy(model = editableSilo.model.copy(manufacturer = it)) },
                label = { Text("Manufacturer") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = editableSilo.model.model,
                onValueChange = { editableSilo = editableSilo.copy(model = editableSilo.model.copy(model = it)) },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth()
            )

            // TODO: Add UI for selecting SiloShape
        }
    }
}
