package it.cynomys.cfmandroid.farm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.osmdroid.util.GeoPoint
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFarmScreen(
    viewModel: FarmViewModel,
    ownerId: UUID,
    initialFarm: FarmDto? = null,
    onBack: () -> Unit
) {
    // -------------------- STATE --------------------
    var name by remember { mutableStateOf(initialFarm?.name ?: "") }
    var address by remember { mutableStateOf(initialFarm?.address ?: "") }
    var latitude by remember { mutableStateOf(initialFarm?.coordinateY?.toString() ?: "") }
    var longitude by remember { mutableStateOf(initialFarm?.coordinateX?.toString() ?: "") }
    var area by remember { mutableStateOf(initialFarm?.area?.toString() ?: "") }
    var species by remember { mutableStateOf(initialFarm?.species ?: Species.RUMINANT) }

    var showMap by remember { mutableStateOf(false) }

    val suggestions by viewModel.addressSuggestions.collectAsState()

    // -------------------- FORM VALIDATION --------------------
    val isFormValid by remember(name, latitude, longitude, species) {
        derivedStateOf {
            name.isNotBlank() &&
                    latitude.isNotBlank() &&
                    longitude.isNotBlank() &&
                    species.name.isNotBlank()
        }
    }

    // -------------------- MAP PICKER --------------------
    if (showMap) {
        FarmMapPicker(
            initialPoint =
                if (latitude.isNotBlank() && longitude.isNotBlank()) {
                    GeoPoint(latitude.toDouble(), longitude.toDouble())
                } else null,
            onLocationSelected = { geoPoint ->
                latitude = geoPoint.latitude.toString()
                longitude = geoPoint.longitude.toString()
                address = viewModel.getAddressFromLocation(
                    geoPoint.latitude,
                    geoPoint.longitude
                )
                showMap = false
            },
            onCancel = { showMap = false }
        )
        return
    }

    // -------------------- FORM --------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(16.dp)
    ) {

        // -------- Farm name (required) --------
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Farm name *") },
            isError = name.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // -------- Address search --------
        Box {
            OutlinedTextField(
                value = address,
                onValueChange = {
                    address = it
                    viewModel.searchAddress(it)
                },
                label = { Text("Search address") },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = suggestions.isNotEmpty(),
                onDismissRequest = { viewModel.clearSuggestions() },
                modifier = Modifier.fillMaxWidth()
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion.displayName) },
                        onClick = {
                            address = suggestion.displayName
                            latitude = suggestion.lat.toString()
                            longitude = suggestion.lon.toString()
                            viewModel.clearSuggestions()
                            showMap = true
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // -------- Map button --------
        Button(
            onClick = { showMap = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select location on map")
        }

        Spacer(Modifier.height(12.dp))

        // -------- Latitude (required) --------
        OutlinedTextField(
            value = latitude,
            onValueChange = {},
            readOnly = true,
            label = { Text("Latitude *") },
            isError = latitude.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // -------- Longitude (required) --------
        OutlinedTextField(
            value = longitude,
            onValueChange = {},
            readOnly = true,
            label = { Text("Longitude *") },
            isError = longitude.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // -------- Area (optional) --------
        OutlinedTextField(
            value = area,
           // onValueChange = { area = it.trim().replace(" ","") },
            onValueChange = {
                input ->
                val cleaned = input.replace("\\s".toRegex(),"")
                    .filter { it.isDigit() || it == '.' }
                if(cleaned.count{
                    it == '.'
                    } <= 1 ){
                    area = cleaned

                }
            },
            label = { Text("Area (ha)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // -------- Species (required) --------
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = species.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Species *") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Species.values().forEach {
                    DropdownMenuItem(
                        text = { Text(it.name) },
                        onClick = {
                            species = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // -------- Save (enabled only when valid) --------
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid,
            onClick = {
                val farmDto = FarmDto(
                    id = initialFarm?.id,
                    name = name,
                    coordinateX = longitude.toDoubleOrNull() ?: 0.0,
                    coordinateY = latitude.toDoubleOrNull() ?: 0.0,
                    address = address,
                    area = area.toDoubleOrNull() ?: 0.0,
                    species = species,
                    ownerId = ownerId
                )

                if (initialFarm == null) {
                    viewModel.addFarm(ownerId, farmDto)
                } else {
                    viewModel.updateFarm(initialFarm.id!!, farmDto)
                }
                onBack()
            }
        ) {
            Text(if (initialFarm == null) "Add Farm" else "Update Farm")
        }

        Spacer(Modifier.height(8.dp))

        // -------- Cancel --------
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("Cancel")
        }
    }
}
