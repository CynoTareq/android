package it.cynomys.cfmandroid.silo

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import it.cynomys.cfmandroid.auth.AuthViewModel
import it.cynomys.cfmandroid.sensordata.HistoricView
import it.cynomys.cfmandroid.sensordata.LatestView
import it.cynomys.cfmandroid.sensordata.PredictionView
import it.cynomys.cfmandroid.sensordata.SensorDataViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiloView(
    siloId: String,
    farmId: String,
    ownerId: String,
    penId: String?,
    navController: NavHostController,
    siloViewModel: SiloViewModel,
    sensorDataViewModel: SensorDataViewModel,
    authViewModel: AuthViewModel,
) {
    // 1. Fetch specific Silo details on launch, just like DeviceView does
    LaunchedEffect(siloId) {
        try {
            siloViewModel.getSiloById(UUID.fromString(siloId))
        } catch (e: Exception) {
            Log.e("SiloView", "Error fetching silo by ID: $siloId")
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val titles = listOf("Latest", "Historic", "Predictions")

    // 2. Observe the state from the ViewModel
    val deviceDetails by siloViewModel.selectedSilo.collectAsState()
    val sensorDataState by sensorDataViewModel.latestSensorData.collectAsState()

    // The serial number (silosID) is used as the deviceID for sensor data calls
    val deviceID = deviceDetails?.silosID ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Use the loaded details for the title, or a fallback
                    Text(deviceDetails?.displayName ?: "Silo Detail")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> deviceDetails?.let { silo ->
                        LatestView(
                            deviceID = silo.silosID, // Use the hardware serial number
                            farmId = farmId,
                            viewModel = sensorDataViewModel,
                            authViewModel = authViewModel,
                            type = "SILO"
                        )
                    } ?: LoadingState()

                    // Tab 1: Historic (Wait for details to get sensor names)
                    1 -> deviceDetails?.let { silo ->
                        val sensorNames = sensorDataState.keys.toList()
                        HistoricView(deviceID, sensorNames, sensorDataViewModel)
                    } ?: LoadingState()

                    // Tab 2: Prediction (Wait for details to check hasPredictions)
                    2 -> deviceDetails?.let { silo ->
                        PredictionView(
                            deviceId = deviceID,
                            hasPredictions = silo.predictions == true,
                            sensorDataViewModel = sensorDataViewModel
                        )
                    } ?: LoadingState()
                }
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
        )
        Text(
            "Loading silo details...",
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.Center)
                .padding(top = 64.dp)
        )
    }
}