// AddEditFarmScreen.kt
package it.cynomys.cfmandroid.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import it.cynomys.cfmandroid.model.FarmDto
import it.cynomys.cfmandroid.model.Species
import it.cynomys.cfmandroid.viewmodel.FarmViewModel
import java.util.UUID
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFarmScreen(
    viewModel: FarmViewModel,
    ownerId: UUID,
    initialFarm: FarmDto? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(initialFarm?.name ?: "") }
    var coordinateX by remember { mutableStateOf(initialFarm?.coordinateX?.toString() ?: "") }
    var coordinateY by remember { mutableStateOf(initialFarm?.coordinateY?.toString() ?: "") }
    var address by remember { mutableStateOf(initialFarm?.address ?: "") }
    var area by remember { mutableStateOf(initialFarm?.area?.toString() ?: "") }
    var species by remember { mutableStateOf(initialFarm?.species ?: Species.RUMINANT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Farm Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = coordinateX,
            onValueChange = { coordinateX = it },
            label = { Text("Coordinate X") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = coordinateY,
            onValueChange = { coordinateY = it },
            label = { Text("Coordinate Y") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = area,
            onValueChange = { area = it },
            label = { Text("Area (ha)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Species dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = species.name,
                onValueChange = {},
                label = { Text("Species") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Species.values().forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            species = item
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val farmDto = FarmDto(
                    id = initialFarm?.id,
                    name = name,
                    coordinateX = coordinateX.toDoubleOrNull() ?: 0.0,
                    coordinateY = coordinateY.toDoubleOrNull() ?: 0.0,
                    address = address,
                    area = area.toDoubleOrNull() ?: 0.0,
                    species = species
                )
                viewModel.addFarm(ownerId, farmDto)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (initialFarm == null) "Add Farm" else "Update Farm")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text("Cancel")
        }
    }
}