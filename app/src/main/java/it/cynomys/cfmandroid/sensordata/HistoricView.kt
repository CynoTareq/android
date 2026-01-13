package it.cynomys.cfmandroid.sensordata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.cynomys.cfmandroid.view.common.SensorChart
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricView(
    deviceID: String,
    sensorNames: List<String>,
    viewModel: SensorDataViewModel = viewModel()
) {
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var selectedSensors by remember { mutableStateOf(sensorNames.toSet()) }
    var attributes by remember { mutableStateOf(sensorNames.joinToString(",")) }
    var aggregation by remember { mutableStateOf("AVG") }
    var limit by remember { mutableIntStateOf(100) }
    var interval by remember { mutableStateOf("2 hours") }
    var orderBy by remember { mutableStateOf("") }
    var from by remember {
        mutableStateOf(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
        )
    }
    var to by remember { mutableStateOf(Date()) }

    var historicData by remember { mutableStateOf<SensorData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val aggregationOptions = listOf("NONE", "AVG")
    val intervalOptions = listOf("1 hour", "2 hours", "1 day", "7 days")
    val limitOptions = listOf(100, 500, 1000, 5000, 10000, 20000, 50000)

    var showChart by remember { mutableStateOf(true) }

    val fetchHistoricData: () -> Unit = remember {
        {
            val startTime = from.time
            val endTime = to.time
            val intervalMs = intervalToMilliseconds(interval)

            isLoading = true
            errorMessage = null

            viewModel.fetchHistoricData(
                deviceID = deviceID,
                attributes = attributes,
                aggregation = aggregation,
                limit = limit,
                interval = intervalMs,
                orderBy = orderBy,
                startTime = startTime,
                endTime = endTime
            ) { result ->
                isLoading = false
                result.fold(
                    onSuccess = { sensorData ->
                        historicData = sensorData
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Failed to fetch data"
                    }
                )
            }
        }
    }

    // Load data on first composition
    LaunchedEffect(deviceID) {
        fetchHistoricData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historic Data") },
                actions = {
                    IconButton(onClick = { showSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { showChart = !showChart }) {
                        Icon(
                            imageVector = if (showChart) Icons.Default.TableChart else Icons.Filled.InsertChart,
                            contentDescription = "Toggle View"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                errorMessage != null -> {
                    Text(
                        text = "Error: $errorMessage",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                historicData?.data?.isEmpty() != false -> {
                    Text(
                        text = "No data available",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    if (showChart) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = historicData!!.data.filterKeys { selectedSensors.contains(it) }
                                    .toList(),
                                key = { (sensorName, _) -> sensorName }
                            ) { (sensorName, values) ->
                                SensorChart(
                                    titleFromKey(sensorName),
                                    values,
                                    from,
                                    to)
                            }
                        }
                    } else {
                        SensorDataTable(historicData!!, selectedSensors)
                    }
                }
            }
        }
    }

    // Filter sheet
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false }
        ) {
            QueryFormView(
                attributes = attributes,
                onAttributesChange = { attributes = it },
                aggregation = aggregation,
                onAggregationChange = { aggregation = it },
                limit = limit,
                onLimitChange = { limit = it },
                interval = interval,
                onIntervalChange = { interval = it },
                orderBy = orderBy,
                onOrderByChange = { orderBy = it },
                from = from,
                onFromChange = { from = it },
                to = to,
                onToChange = { to = it },
                selectedSensors = selectedSensors,
                onSelectedSensorsChange = { selectedSensors = it },
                sensorNames = sensorNames,
                aggregationOptions = aggregationOptions,
                intervalOptions = intervalOptions,
                limitOptions = limitOptions,
                onSave = {
                    showSheet = false
                    fetchHistoricData()
                }
            )
        }
    }
}


data class ChartDataPoint(
    val timestamp: Date,
    val value: Double
)

fun DrawScope.drawLineChart(
    chartData: List<ChartDataPoint>,
    minValue: Double,
    maxValue: Double,
    startTime: Long,
    endTime: Long
) {
    if (chartData.size < 2) return

    val width = size.width
    val height = size.height
    val valueRange = maxValue - minValue
    val timeRange = endTime - startTime

    // Create path for the line
    val path = Path()
    var isFirstPoint = true

    chartData.forEach { point ->
        val x = ((point.timestamp.time - startTime).toFloat() / timeRange.toFloat()) * width
        val y = height - (((point.value - minValue) / valueRange).toFloat() * height)

        if (isFirstPoint) {
            path.moveTo(x, y)
            isFirstPoint = false
        } else {
            path.lineTo(x, y)
        }
    }

    // Draw the line
    drawPath(
        path = path,
        color = Color(0xFF2196F3), // Blue color similar to iOS
        style = Stroke(width = 2.dp.toPx())
    )

    // Draw data points
    chartData.forEach { point -> // Corrected typo from drawChartData.forEach
        val x = ((point.timestamp.time - startTime).toFloat() / timeRange.toFloat()) * width
        val y = height - (((point.value - minValue) / valueRange).toFloat() * height)

        drawCircle(
            color = Color(0xFF2196F3),
            radius = 3.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

// Helper function to convert interval string to milliseconds
private fun intervalToMilliseconds(interval: String): Long {
    return when (interval) {
        "1 hour" -> 3600000L
        "2 hours" -> 7200000L
        "1 day" -> 86400000L
        "7 days" -> 604800000L
        else -> 7200000L // Default to 2 hours
    }
}