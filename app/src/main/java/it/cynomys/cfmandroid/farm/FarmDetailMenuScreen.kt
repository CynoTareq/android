// FarmDetailMenuScreen.kt
package it.cynomys.cfmandroid.farm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.cynomys.cfmandroid.R
import java.util.UUID

// Data class to represent menu items
data class MenuItemData(
    val icon: ImageVector,
    val text: Int,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDetailMenuScreen(
    navController: NavController,
    farmId: UUID?,
    onDeviceSelected: () -> Unit,
    onWeatherSelected: () -> Unit,
    onSilosSelected: () -> Unit,
    onWebcamsSelected: () -> Unit,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp

    // Determine layout based on screen width
    val useGridLayout = screenWidth >= 600 || isLandscape
    val gridColumns = when {
        screenWidth >= 900 -> 3
        screenWidth >= 600 -> 2
        isLandscape -> 2 // Phone landscape
        else -> 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Menu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        // Prepare menu items data
        val deviceManagementItems = listOf(
            MenuItemData(Icons.Default.Devices,R.string.devices, onDeviceSelected),
            MenuItemData(Icons.Default.Storage, R.string.silos, onSilosSelected)
        )

        val farmManagementItems = listOf(
            MenuItemData(Icons.Default.WbSunny, R.string.weather, onWeatherSelected),
            MenuItemData(Icons.Default.Videocam, R.string.webcams, onWebcamsSelected)
        )

        if (useGridLayout) {
            // Grid layout uses LazyVerticalGrid (for scrolling) and square cards
            AdaptiveGridLayout(
                padding = padding,
                columns = gridColumns,
                deviceItems = deviceManagementItems,
                farmItems = farmManagementItems
            )
        } else {
            // Linear layout for portrait phones
            AdaptiveListLayout(
                padding = padding,
                deviceItems = deviceManagementItems,
                farmItems = farmManagementItems
            )
        }
    }
}

// FIX: The verticalScroll modifier is now applied directly to the outer column
// that receives the scaffold padding, resolving the non-scrolling issue.
@Composable
fun AdaptiveListLayout(
    padding: PaddingValues,
    deviceItems: List<MenuItemData>,
    farmItems: List<MenuItemData>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding) // Apply padding from Scaffold
            .verticalScroll(rememberScrollState()) // Ensures scrollability
            .padding(horizontal = 16.dp, vertical = 8.dp) // Apply margins inside the scrollable view
    ) {
        MenuSection(
            title = "Device Management",
            items = deviceItems,
            isGridLayout = false
        )

        // Reduced spacing to help content fit on smaller screens
        Spacer(modifier = Modifier.padding(vertical = 4.dp))

        MenuSection(
            title = "Farm Management",
            items = farmItems,
            isGridLayout = false
        )

        Spacer(modifier = Modifier.padding(bottom = 8.dp))
    }
}

// FIX: This now uses LazyVerticalGrid and the correct spanning logic
@Composable
fun AdaptiveGridLayout(
    padding: PaddingValues,
    columns: Int,
    deviceItems: List<MenuItemData>,
    farmItems: List<MenuItemData>
) {
    // Combine items and titles into a list of pairs for easier iteration
    val menuItemsWithSections = listOf(
        Pair("Device Management", deviceItems),
        Pair("Farm Management", farmItems)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        menuItemsWithSections.forEach { (title, items) ->
            // Use the built-in item block with GridItemSpan
            item(span = { GridItemSpan(columns) }) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Add the menu items
            items(items) { item ->
                MenuItem(
                    icon = item.icon,
                    textRes = item.text,
                    onClick = item.onClick,
                    isCompact = true // Compact for square grid card layout
                )
            }
        }
    }
}

// MenuSection is only used for the List layout now
@Composable
fun MenuSection(
    title: String,
    items: List<MenuItemData>,
    isGridLayout: Boolean
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    if (!isGridLayout) {
        items.forEach { item ->
            MenuItem(
                icon = item.icon,
                textRes = item.text,
                onClick = item.onClick,
                isCompact = false
            )
        }
    }
}

@Composable
fun MenuItem(
    icon: ImageVector,
    textRes: Int,
    onClick: () -> Unit,
    isCompact: Boolean = false
) {
    val text = stringResource(textRes)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // 1. Apply vertical padding only in list mode.
            .then(if (!isCompact) Modifier.padding(vertical = 6.dp) else Modifier)
            // 2. Enforce square shape for compact (grid/landscape) view
            .then(if (isCompact) Modifier.aspectRatio(1f) else Modifier)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // 3. FIX: Use Column for square card content to stack icon and text
        if (isCompact) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1 // Ensure text doesn't wrap/overflow
                )
            }
        } else {
            // 4. List Mode (Portrait) keeps the original Row layout
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    )
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
}