// FarmListScreen.kt
package it.cynomys.cfmandroid.farm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.cynomys.cfmandroid.R
import androidx.navigation.NavController
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmListScreen(
    viewModel: FarmViewModel,
    navController: NavController,
    ownerId: UUID,
    onAddFarm: () -> Unit,
    onFarmSelected: (UUID) -> Unit,
    onProfileSelected: () -> Unit,
    // NEW: Add a required parameter to navigate to the AddEditScreen for editing
    onEditFarm: (UUID) -> Unit // We only need the Farm ID for navigation
) {
    val farms by viewModel.farms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(ownerId) {
        viewModel.refreshFarms(ownerId)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddFarm,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Farm",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        topBar = {
            Column {
                TopAppBar(

                    title = {
                        Text(
                            stringResource(R.string.my_farms),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    actions = {
                        IconButton(onClick = onProfileSelected) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                // TabRow should be separate from TopAppBar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("List") },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = "List View"
                                )
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Map") },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Map View"
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            when (selectedTabIndex) {
                0 -> FarmListView(
                    farms = farms,
                    isLoading = isLoading,
                    onFarmSelected = onFarmSelected,
                    onEdit = { farm ->
                        // FIX: Implement navigation to the edit screen
                        if (farm.id != null) {
                            // You need to pass the farm ID to the navigation function
                            onEditFarm(farm.id!!)
                        }
                    },
                    onDelete = { farm -> viewModel.deleteFarm(farm.id!!, ownerId) }
                )
                1 -> FarmMapView(
                    farms = farms,
                    isLoading = isLoading,
                    onFarmSelected = onFarmSelected
                )
            }
        }
    }
}