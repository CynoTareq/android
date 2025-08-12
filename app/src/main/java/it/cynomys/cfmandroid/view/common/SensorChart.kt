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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.cynomys.cfmandroid.sensordata.ChartDataPoint
import it.cynomys.cfmandroid.sensordata.SensorDataItem
import it.cynomys.cfmandroid.sensordata.SensorUnit
import it.cynomys.cfmandroid.sensordata.drawLineChart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SensorChart(
    sensorName: String,
    values: List<SensorDataItem>,
    from: Date,
    to: Date
) {
    // Convert string values to doubles and filter out invalid values
    val chartData = values.mapNotNull { item ->
        val doubleValue = item.value.toDoubleOrNull()
        if (doubleValue != null) {
            ChartDataPoint(
                timestamp = Date(item.ts),
                value = doubleValue
            )
        } else null
    }.sortedBy { it.timestamp }

    if (chartData.isEmpty()) {
        // Show empty state
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = sensorName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No valid data points available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    val minValue = chartData.minOf { it.value }
    val maxValue = chartData.maxOf { it.value }
    val valueRange = maxValue - minValue
    val adjustedMinValue = if (valueRange > 0) minValue - (valueRange * 0.1) else minValue - 1
    val adjustedMaxValue = if (valueRange > 0) maxValue + (valueRange * 0.1) else maxValue + 1

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = sensorName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chart container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                        // Line chart
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            drawLineChart(
                                chartData = chartData,
                                minValue = adjustedMinValue,
                                maxValue = adjustedMaxValue,
                                startTime = from.time,
                                endTime = to.time
                            )
                        }
                    }
                }

                // Unit label (positioned absolutely, rotated 90 degrees)
                Text(
                    text = SensorUnit.unitFor(sensorName),
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
                        .padding(start = 58.dp, end = 8.dp, bottom = 4.dp), // Back to original padding
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    Text(
                        text = dateFormat.format(from),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp
                    )
                    Text(
                        text = dateFormat.format(to),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


        }
    }
}