package it.cynomys.cfmandroid.silo

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
fun SiloListView(
    viewModel: SiloViewModel,
    ownerId: UUID,
    farmId: UUID,
    penId: UUID?, // Now nullable
    navController: NavController,
    onBack: () -> Unit,
    onAddSilo: (farmId: UUID, penId: UUID?) -> Unit, // penId now optional
    onSiloSelected: (silo: Silo) -> Unit, // Callback for silo selection
    onEditSilo: (silo: Silo) -> Unit // NEW: Callback for editing silo
) {
    // Collect the list of silos from the ViewModel
    val silos by viewModel.silos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Log for debugging
    LaunchedEffect(silos) {
        Log.d("SiloListView", "Silos updated: ${silos.size} items for farmId: $farmId, penId: $penId")
        silos.forEach { silo ->
            Log.d("SiloListView", "Silo: ${silo.displayName}, Pen ID: ${silo.penId}, Farm ID: ${silo.farmId}")
        }
    }

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
            } else if (silos.isEmpty()) {
                Text("No silos found for this ${if (penId != null) "pen" else "farm"}.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 64.dp) // Add padding for FAB
                ) {
                    items(silos, key = { it.id ?: UUID.randomUUID() }) { silo ->
                        SiloItem(
                            silo = silo,
                            onItemClick = { onSiloSelected(silo) }, // Pass the whole silo object
                            onEdit = { onEditSilo(silo) }, // NEW: Pass edit callback
                            onDelete = {
                                if (silo.id != null && silo.farmId != null && silo.ownerId != null) {
                                    viewModel.deleteSilo(silo.id, silo.farmId, silo.ownerId)
                                } else {
                                    Log.e("SiloListView", "Cannot delete silo: missing required IDs for silo: ${silo.displayName}")
                                }
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
fun SiloItem(
    silo: Silo,
    onItemClick: () -> Unit,
    onEdit: () -> Unit, // NEW: Edit callback
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() } // Make the whole card clickable
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Silo Visual Representation
            SiloVisualRepresentation(
                silo = silo,
                fillPercentage = 0f,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display silo name and ID
            Text(text = silo.displayName, style = MaterialTheme.typography.titleLarge)
            Text(text = "ID: ${silo.silosID}")
            // Display silo height and diameter
            Text(text = "Height: ${silo.silosHeight} units, Diameter: ${silo.silosDiameter} units")
            // Display silo shape and material
            Text(text = "Shape: ${silo.shape.name}, Material: ${silo.material_name}")
            // Display Pen ID if available
            silo.penId?.let {
                Text(text = "Pen ID: ${it.toString().take(8)}...")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Align buttons to the end
            ) {
                // NEW: Edit button
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                // Delete button
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