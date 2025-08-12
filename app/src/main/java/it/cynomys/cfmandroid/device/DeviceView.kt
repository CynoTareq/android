package it.cynomys.cfmandroid.device

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import it.cynomys.cfmandroid.auth.AuthViewModel
import it.cynomys.cfmandroid.sensordata.HistoricView
import it.cynomys.cfmandroid.sensordata.LatestView
import it.cynomys.cfmandroid.sensordata.PredictionView
import it.cynomys.cfmandroid.sensordata.SensorDataViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceView(
    deviceID: String,
    farmId: String,
    ownerId: UUID,
    navController: NavHostController,
    sensorDataViewModel: SensorDataViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current.applicationContext

    val deviceViewModel: DeviceViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DeviceViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return DeviceViewModel(context) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    LaunchedEffect(deviceID, ownerId, farmId) {
        try {
            deviceViewModel.getDeviceByDeviceId(
                deviceID = deviceID,
                ownerId = ownerId,
                farmId = UUID.fromString(farmId)
            )
        } catch (e: IllegalArgumentException) {
            println("Invalid Device ID or Farm ID format: $deviceID, $farmId")
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Latest", "Historic", "Prediction")
    val deviceDetails by deviceViewModel.selectedDevice.collectAsState()
    val sensorDataState by sensorDataViewModel.latestSensorData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    deviceDetails?.let { device ->
                        Text("${device.displayName}")
                    } ?: Text("Device Data")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> LatestView(
                        deviceID = deviceID,
                        farmId = farmId,
                        viewModel = sensorDataViewModel,
                        authViewModel = authViewModel
                    )
                    1 -> deviceDetails?.let { device ->
                        val sensorNames = sensorDataState.keys?.toList() ?: emptyList()
                        HistoricView(deviceID, sensorNames, sensorDataViewModel)
                    } ?: Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                        )
                        Text(
                            "Loading device details...",
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.Center)
                                .padding(top = 64.dp)
                        )
                    }
                    2 -> deviceDetails?.let { device ->
                        PredictionView(
                            deviceId = deviceID,
                            hasPredictions = device.predictions,
                            sensorDataViewModel = sensorDataViewModel
                        )
                    } ?: Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                        )
                        Text(
                            "Loading device details...",
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.Center)
                                .padding(top = 64.dp)
                        )
                    }
                }
            }
        }
    }

    // REMOVED: Duplicate call to getLatestValues - it's already called in LatestView
    // LaunchedEffect(deviceID) {
    //     sensorDataViewModel.getLatestValues(deviceID)
    // }
}