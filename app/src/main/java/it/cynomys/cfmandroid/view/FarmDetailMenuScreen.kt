
// FarmDetailMenuScreen.kt
package it.cynomys.cfmandroid.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.util.UUID
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun FarmDetailMenuScreen(
    farmId: UUID?,
    onDeviceSelected: () -> Unit,
    onWeatherSelected: () -> Unit,
    onSilosSelected: () -> Unit,
    onWebcamsSelected: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Menu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            MenuItem(
                icon = Icons.Default.Devices,
                text = "Devices",
                onClick = onDeviceSelected
            )
            MenuItem(
                icon = Icons.Default.WbSunny,
                text = "Weather",
                onClick = onWeatherSelected
            )
            MenuItem(
                icon = Icons.Default.Storage,
                text = "Silos",
                onClick = onSilosSelected
            )
            MenuItem(
                icon = Icons.Default.Videocam,
                text = "Webcams",
                onClick = onWebcamsSelected
            )
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}