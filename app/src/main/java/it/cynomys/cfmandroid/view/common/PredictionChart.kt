package it.cynomys.cfmandroid.view.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.cynomys.cfmandroid.sensordata.ChartDataPoint
import it.cynomys.cfmandroid.sensordata.Prediction
import it.cynomys.cfmandroid.sensordata.SensorDataItem
import it.cynomys.cfmandroid.sensordata.SensorUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PredictionChart(
    historicalData: List<SensorDataItem>,
    predictions: List<Prediction>,
    from: Date,
    to: Date
) {
    // Convert historical data to chart points
    val historicalPoints = historicalData.mapNotNull { item ->
        val doubleValue = item.value.toDoubleOrNull()
        if (doubleValue != null) {
            ChartDataPoint(
                timestamp = Date(item.ts),
                value = doubleValue
            )
        } else null
    }.sortedBy { it.timestamp }

    // Convert prediction data to chart points
    val predictionPoints = predictions.map { prediction ->
        ChartDataPoint(
            timestamp = Date(prediction.predictionTime),
            value = prediction.predictedTHI
        )
    }.sortedBy { it.timestamp }

    if (historicalPoints.isEmpty() && predictionPoints.isEmpty()) {
        // Show empty state
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "THI - Historical & Predictions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    // Calculate combined value range
    val allValues = historicalPoints.map { it.value } + predictionPoints.map { it.value }
    val minValue = allValues.minOfOrNull { it } ?: 0.0
    val maxValue = allValues.maxOfOrNull { it } ?: 100.0
    val valueRange = maxValue - minValue
    val adjustedMinValue = if (valueRange > 0) minValue - (valueRange * 0.1) else minValue - 1
    val adjustedMaxValue = if (valueRange > 0) maxValue + (valueRange * 0.1) else maxValue + 1

    // Calculate time range
    val actualStartTime = minOf(from.time,
        historicalPoints.minOfOrNull { it.timestamp.time } ?: from.time,
        predictionPoints.minOfOrNull { it.timestamp.time } ?: from.time
    )
    val actualEndTime = maxOf(to.time,
        historicalPoints.maxOfOrNull { it.timestamp.time } ?: to.time,
        predictionPoints.maxOfOrNull { it.timestamp.time } ?: to.time
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "THI - Predictions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (historicalPoints.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.width(20.dp).height(2.dp)) {
                            drawLine(
                                color = Color.Blue,
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Historical",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Blue
                        )
                    }
                }

                if (historicalPoints.isNotEmpty() && predictionPoints.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(16.dp))
                }

                if (predictionPoints.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.width(20.dp).height(2.dp)) {
                            drawLine(
                                color = Color.Red,
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 3.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Predictions",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chart container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Y-axis labels
                    Column(
                        modifier = Modifier
                            .width(50.dp)
                            .fillMaxHeight()
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = String.format("%.1f", adjustedMaxValue),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = String.format("%.1f", (adjustedMinValue + adjustedMaxValue) / 2),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = String.format("%.1f", adjustedMinValue),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Chart area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Combined chart
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            drawCombinedChart(
                                historicalData = historicalPoints,
                                predictionData = predictionPoints,
                                minValue = adjustedMinValue,
                                maxValue = adjustedMaxValue,
                                startTime = actualStartTime,
                                endTime = actualEndTime
                            )
                        }
                    }
                }

                // Unit label (positioned absolutely, rotated 90 degrees)
                Text(
                    text = SensorUnit.unitFor("thi_gen"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                        .rotate(-90f),
                    maxLines = 1,
                    softWrap = false,
                    fontWeight = FontWeight.Bold
                )

                // X-axis labels at bottom
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 58.dp, end = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dateFormat = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
                    Text(
                        text = dateFormat.format(Date(actualStartTime)),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp
                    )
                    Text(
                        text = dateFormat.format(Date(actualEndTime)),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun DrawScope.drawCombinedChart(
    historicalData: List<ChartDataPoint>,
    predictionData: List<ChartDataPoint>,
    minValue: Double,
    maxValue: Double,
    startTime: Long,
    endTime: Long
) {
    val width = size.width
    val height = size.height
    val valueRange = maxValue - minValue
    val timeRange = endTime - startTime

    // Draw historical data with solid blue line
    if (historicalData.size > 1) {
        val historicalPath = Path()
        var isFirstPoint = true

        for (point in historicalData) {
            val x = ((point.timestamp.time - startTime).toFloat() / timeRange.toFloat()) * width
            val y = height - (((point.value - minValue) / valueRange).toFloat() * height)

            if (isFirstPoint) {
                historicalPath.moveTo(x, y)
                isFirstPoint = false
            } else {
                historicalPath.lineTo(x, y)
            }
        }

        drawPath(
            path = historicalPath,
            color = Color.Blue,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }

    // Draw prediction data with dashed red line
    if (predictionData.size > 1) {
        val predictionPath = Path()
        var isFirstPoint = true

        for (point in predictionData) {
            val x = ((point.timestamp.time - startTime).toFloat() / timeRange.toFloat()) * width
            val y = height - (((point.value - minValue) / valueRange).toFloat() * height)

            if (isFirstPoint) {
                predictionPath.moveTo(x, y)
                isFirstPoint = false
            } else {
                predictionPath.lineTo(x, y)
            }
        }

        drawPath(
            path = predictionPath,
            color = Color.Red,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f))
            )
        )
    }

    // Draw single points if there's only one data point
    if (historicalData.size == 1) {
        val point = historicalData.first()
        val x = ((point.timestamp.time - startTime).toFloat() / timeRange.toFloat()) * width
        val y = height - (((point.value - minValue) / valueRange).toFloat() * height)

        drawCircle(
            color = Color.Blue,
            radius = 4.dp.toPx(),
            center = Offset(x, y)
        )
    }

    if (predictionData.size == 1) {
        val point = predictionData.first()
        val x = ((point.timestamp.time - startTime).toFloat() / timeRange.toFloat()) * width
        val y = height - (((point.value - minValue) / valueRange).toFloat() * height)

        drawCircle(
            color = Color.Red,
            radius = 4.dp.toPx(),
            center = Offset(x, y)
        )
    }
}