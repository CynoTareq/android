package it.cynomys.cfmandroid.silo


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomStepper(
    steps: List<SiloStep>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        steps.forEachIndexed { index, step ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step indicator with connector
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Connector line
                    if (index < steps.size - 1) {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .offset(x = 32.dp),
                            thickness = 2.dp,
                            color = if (index < currentStep)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    // Step circle
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = when {
                            index < currentStep -> MaterialTheme.colorScheme.primary
                            index == currentStep -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (index < currentStep) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = (index + 1).toString(),
                                    color = when {
                                        index == currentStep -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = step.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = step.description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ShapeSelectionStep(
    selectedShape: SiloShape,
    onShapeSelected: (SiloShape) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select Silo Shape",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        ShapeCard(
            title = "Full Cylindrical",
            description = "Standard cylindrical silo",
            isSelected = selectedShape == SiloShape.FULL_CYLINDRICAL,
            onClick = { onShapeSelected(SiloShape.FULL_CYLINDRICAL) }
        )

        ShapeCard(
            title = "Cylindrical with Conical Bottom",
            description = "Cylindrical silo with conical bottom",
            isSelected = selectedShape == SiloShape.CONICAL_BOTTOM,
            onClick = { onShapeSelected(SiloShape.CONICAL_BOTTOM) }
        )
    }
}

@Composable
fun ShapeCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DimensionsStep(
    siloDto: SiloDto,
    usePresetModel: Boolean,
    selectedManufacturer: String?,
    selectedModel: String?,
    manufacturers: List<String>,
    filteredModels: List<String>,
    onUsePresetModelChange: (Boolean) -> Unit,
    onManufacturerSelected: (String) -> Unit,
    onModelSelected: (String) -> Unit,
    onDimensionChange: (String, Double) -> Unit
) {
    var expandedManufacturer by remember { mutableStateOf(false) }
    var expandedModel by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter Dimensions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Toggle for preset model
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Use preset model")
            Switch(
                checked = usePresetModel,
                onCheckedChange = onUsePresetModelChange
            )
        }

        if (usePresetModel) {
            // Manufacturer dropdown
            ExposedDropdownMenuBox(
                expanded = expandedManufacturer,
                onExpandedChange = { expandedManufacturer = !expandedManufacturer }
            ) {
                OutlinedTextField(
                    value = selectedManufacturer ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Manufacturer") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedManufacturer) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedManufacturer,
                    onDismissRequest = { expandedManufacturer = false }
                ) {
                    manufacturers.forEach { manufacturer ->
                        DropdownMenuItem(
                            text = { Text(manufacturer) },
                            onClick = {
                                onManufacturerSelected(manufacturer)
                                expandedManufacturer = false
                            }
                        )
                    }
                }
            }

            // Model dropdown
            if (selectedManufacturer != null) {
                ExposedDropdownMenuBox(
                    expanded = expandedModel,
                    onExpandedChange = { expandedModel = !expandedModel }
                ) {
                    OutlinedTextField(
                        value = selectedModel ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Model") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedModel,
                        onDismissRequest = { expandedModel = false }
                    ) {
                        filteredModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    onModelSelected(model)
                                    expandedModel = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // Manual dimension inputs
            OutlinedTextField(
                value = if (siloDto.silosHeight == 0.0 && siloDto.silosID.isBlank()) "" else siloDto.silosHeight.toString(),
                onValueChange = {
                    it.toDoubleOrNull()?.let { value ->
                        onDimensionChange("height", value)
                    } ?: run {
                        if (it.isBlank()) onDimensionChange("height", 0.0)
                    }
                },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = if (siloDto.silosDiameter == 0.0 && siloDto.silosID.isBlank()) "" else siloDto.silosDiameter.toString(),
                onValueChange = {
                    it.toDoubleOrNull()?.let { value ->
                        onDimensionChange("diameter", value)
                    } ?: run {
                        if (it.isBlank()) onDimensionChange("diameter", 0.0)
                    }
                },
                label = { Text("Diameter (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (siloDto.shape == SiloShape.CONICAL_BOTTOM) {
                OutlinedTextField(
                    value = siloDto.coneHeight?.toString() ?: "",
                    onValueChange = {
                        it.toDoubleOrNull()?.let { value ->
                            onDimensionChange("coneHeight", value)
                        } ?: run {
                            if (it.isBlank()) onDimensionChange("coneHeight", 0.0)
                        }
                    },
                    label = { Text("Cone Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = siloDto.bottomDiameter?.toString() ?: "",
                    onValueChange = {
                        it.toDoubleOrNull()?.let { value ->
                            onDimensionChange("bottomDiameter", value)
                        } ?: run {
                            if (it.isBlank()) onDimensionChange("bottomDiameter", 0.0)
                        }
                    },
                    label = { Text("Bottom Diameter (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}