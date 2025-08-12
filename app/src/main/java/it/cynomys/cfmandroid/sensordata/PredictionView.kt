package it.cynomys.cfmandroid.sensordata

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.cynomys.cfmandroid.view.common.PredictionChart
import java.util.Calendar
import java.util.Date

@Composable
fun PredictionView(
    deviceId: String,
    hasPredictions: Boolean,
    sensorDataViewModel: SensorDataViewModel
) {
    val predictions by sensorDataViewModel.predictions.collectAsState()
    val isLoading by sensorDataViewModel.isLoadingPredictions.collectAsState()
    val errorMessage by sensorDataViewModel.predictionErrorMessage.collectAsState()

    var historicalData by remember { mutableStateOf<List<SensorDataItem>>(emptyList()) }
    var isLoadingHistorical by remember { mutableStateOf(false) }
    var historicalError by remember { mutableStateOf<String?>(null) }

    // Calculate time range for last 48 hours
    val now = Calendar.getInstance()
    val endTime = now.timeInMillis
    val startTime = endTime - (48 * 60 * 60 * 1000L) // 48 hours ago

    // Load predictions when the view is displayed and device has predictions enabled
    LaunchedEffect(deviceId, hasPredictions) {
        if (hasPredictions) {
            sensorDataViewModel.getPredictions(deviceId)

            // Fetch historical THI data for last 48 hours
            isLoadingHistorical = true
            historicalError = null

            sensorDataViewModel.fetchHistoricData(
                deviceID = deviceId,
                attributes = "thi_gen",
                aggregation = "NONE",
                limit = 1000,
                interval = 300000, // 5 minutes
                orderBy = "ASC",
                startTime = startTime,
                endTime = endTime
            ) { result -> // Use the callback
                isLoadingHistorical = false
                result.fold(
                    onSuccess = { sensorData ->
                        // Correctly access data from the SensorData object
                        historicalData = sensorData.data["thi_gen"] ?: emptyList()
                    },
                    onFailure = { error ->
                        historicalError = error.message
                    }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !hasPredictions -> {
                // Device doesn't have predictions enabled
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(100.dp))
                    Text(
                        text = "Predictions not available",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This device does not have prediction capabilities enabled.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            isLoading || isLoadingHistorical -> {
                // Loading state
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isLoading) "Loading predictions..." else "Loading historical data...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            errorMessage != null || historicalError != null -> {
                // Error state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(100.dp))
                    Text(
                        text = "Error loading data",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: historicalError ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            predictions.isNullOrEmpty() -> {
                // No predictions available
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(100.dp))
                    Text(
                        text = "No predictions available",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No prediction data found for this device.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            else -> {
                // Display combined chart with historical and prediction data
                val predictionsList = predictions!!

                // Calculate combined time range
                val historicalMinTime = historicalData.minOfOrNull { it.ts } ?: startTime
                val historicalMaxTime = historicalData.maxOfOrNull { it.ts } ?: endTime
                val predictionMinTime = predictionsList.minOfOrNull { it.predictionTime } ?: endTime
                val predictionMaxTime = predictionsList.maxOfOrNull { it.predictionTime } ?: endTime

                val combinedStartTime = minOf(historicalMinTime, predictionMinTime, startTime)
                val combinedEndTime = maxOf(historicalMaxTime, predictionMaxTime, endTime)

                val fromDate = Date(combinedStartTime)
                val toDate = Date(combinedEndTime)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Predictions",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    PredictionChart(
                        historicalData = historicalData,
                        predictions = predictionsList,
                        from = fromDate,
                        to = toDate
                    )
                }
            }
        }
    }
}