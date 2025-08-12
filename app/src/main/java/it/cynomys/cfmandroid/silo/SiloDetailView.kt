package it.cynomys.cfmandroid.silo

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun SiloDetailView(
    siloId: String?, // Silo ID passed as a navigation argument
    farmId: UUID, // Farm ID associated with the silo
    ownerId: UUID, // Owner ID associated with the silo
    viewModel: SiloViewModel,
    navController: NavController
) {
    // Collect the selected silo, loading state, and error state from the ViewModel
    val silo by viewModel.selectedSilo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Fetch silo details when the composable is launched or siloId/ownerId/farmId changes
    LaunchedEffect(siloId, ownerId, farmId) {
        if (siloId != null) {
            try {
                viewModel.getSiloById(UUID.fromString(siloId))
            } catch (e: IllegalArgumentException) {
                Log.e("SiloDetailView", "Invalid Silo ID format: $siloId", e)
                // Optionally show an error to the user or navigate back
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Silo Details") },
                navigationIcon = {
                    // Back button
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Edit button, visible only if a silo is loaded
                    silo?.let {
                        IconButton(onClick = {
                            // Navigate to edit screen, passing silo ID, farm ID, owner ID, and pen ID
                            navController.navigate("edit_silo/${it.id}/${it.farmId}/${it.ownerId}/${it.penId}")
                        }) {
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

            // Display silo details if a silo is loaded
            silo?.let {
                SiloDetailContent(silo = it)
            }
        }
    }
}

@Composable
fun SiloDetailContent(silo: Silo) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between detail items
    ) {
        Log.d("SiloDetail", "Silo: ${silo.displayName}, ID: ${silo.silosID}")

        // Display individual detail items
        DetailItem("Display Name", silo.displayName)
        DetailItem("Silo ID", silo.silosID)
        DetailItem("Silo Height", "${silo.silosHeight} units")
        DetailItem("Silo Diameter", "${silo.silosDiameter} units")
        DetailItem("Cone Height", silo.coneHeight?.let { "$it units" } ?: "N/A")
        DetailItem("Bottom Diameter", silo.bottomDiameter?.let { "$it units" } ?: "N/A")
        DetailItem("Shape", silo.shape.name)
        DetailItem("Material Name", silo.material_name)
        DetailItem("Material Density", "${silo.material_density} units")
        DetailItem("Manufacturer", silo.model.manufacturer)
        DetailItem("Model", silo.model.model)
        DetailItem("Farm ID", silo.farmId?.toString())
        DetailItem("Owner ID", silo.ownerId?.toString())
        DetailItem("Pen ID", silo.penId?.toString())
        DetailItem("Internal ID", silo.id?.toString()) // Display internal UUID
    }
}

@Composable
fun DetailItem(label: String, value: String?) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium) // Label for the detail
        Text(text = value ?: "N/A", style = MaterialTheme.typography.bodyLarge) // Value, "N/A" if null
    }
}
