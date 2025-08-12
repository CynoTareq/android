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
fun SiloAddView(
    viewModel: SiloViewModel,
    navController: NavController,
    ownerId: UUID,
    farmId: UUID,
    penId: UUID // Assuming penId is passed to add a silo to a specific pen
) {
    // State to hold the data for the new silo being added
    // Initialize with default values and provided ownerId, farmId, penId
    var siloDto by remember {
        mutableStateOf(
            SiloDto(
                silosID = "",
                displayName = "",
                silosHeight = 0.0,
                silosDiameter = 0.0,
                coneHeight = 0.0,
                bottomDiameter = 0.0,
                shape = SiloShape.FULL_CYLINDRICAL, // Default shape
                penId = penId,
                farmId = farmId,
                ownerId = ownerId,
                model = SiloModel("", ""), // Default empty model
                material_name = "",
                material_density = 0.0
            )
        )
    }

    // Collect loading and error states from the ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Silo") },
                navigationIcon = {
                    // Back button
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save button, enabled only when not loading
                    IconButton(
                        onClick = {
                            viewModel.addSilo(ownerId, farmId, siloDto)
                            navController.popBackStack() // Navigate back after saving
                        },
                        enabled = !isLoading // Disable button while loading
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
        ) {
            // Display loading indicator
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // Display error message if present
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Input field for Silo ID
            OutlinedTextField(
                value = siloDto.silosID,
                onValueChange = { siloDto = siloDto.copy(silosID = it) },
                label = { Text("Silo ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Display Name
            OutlinedTextField(
                value = siloDto.displayName,
                onValueChange = { siloDto = siloDto.copy(displayName = it) },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Silo Height
            OutlinedTextField(
                value = siloDto.silosHeight.toString(),
                onValueChange = {
                    siloDto = siloDto.copy(silosHeight = it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Silo Height (units)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Silo Diameter
            OutlinedTextField(
                value = siloDto.silosDiameter.toString(),
                onValueChange = {
                    siloDto = siloDto.copy(silosDiameter = it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Silo Diameter (units)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Cone Height (nullable)
            OutlinedTextField(
                value = siloDto.coneHeight?.toString() ?: "",
                onValueChange = {
                    siloDto = siloDto.copy(coneHeight = it.toDoubleOrNull())
                },
                label = { Text("Cone Height (units, optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Bottom Diameter (nullable)
            OutlinedTextField(
                value = siloDto.bottomDiameter?.toString() ?: "",
                onValueChange = {
                    siloDto = siloDto.copy(bottomDiameter = it.toDoubleOrNull())
                },
                label = { Text("Bottom Diameter (units, optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Material Name
            OutlinedTextField(
                value = siloDto.material_name,
                onValueChange = { siloDto = siloDto.copy(material_name = it) },
                label = { Text("Material Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field for Material Density
            OutlinedTextField(
                value = siloDto.material_density.toString(),
                onValueChange = {
                    siloDto = siloDto.copy(material_density = it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Material Density (units)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input fields for Silo Model
            OutlinedTextField(
                value = siloDto.model.manufacturer,
                onValueChange = { siloDto = siloDto.copy(model = siloDto.model.copy(manufacturer = it)) },
                label = { Text("Manufacturer") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = siloDto.model.model,
                onValueChange = { siloDto = siloDto.copy(model = siloDto.model.copy(model = it)) },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth()
            )

            // TODO: Add a dropdown or radio buttons for SiloShape
            // For simplicity, it's currently hardcoded to FULL_CYLINDRICAL in the initial state.
            // A more complete implementation would include UI for selecting the shape.
        }
    }
}
