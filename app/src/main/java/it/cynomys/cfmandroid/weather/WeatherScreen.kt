package it.cynomys.cfmandroid.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ============================================================
   WEATHER SCREEN ROOT
   ============================================================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        // CHANGE 1: Set the main screen background color
        containerColor = Color(0xFF2C3E50)
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (state) {
                WeatherState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is WeatherState.Error -> {
                    Text(
                        text = (state as WeatherState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is WeatherState.Success -> {
                    WeatherContent((state as WeatherState.Success).weatherData)
                }
            }
        }
    }
}

/* ============================================================
   MAIN CONTENT
   ============================================================ */

@Composable
fun WeatherContent(data: WeatherResponse) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = data.markerData.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            // CHANGE 2: Set text color to white for visibility on dark background
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))

        CurrentWeatherCard(data.weatherData)

        Spacer(Modifier.height(16.dp))

        DetailedAirPollutionCard(data.airPollutionData)

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Forecast",

            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            // CHANGE 3: Set text color to white for visibility on dark background
            color = Color.White
        )

        Spacer(Modifier.height(10.dp))

        HorizontalDetailedForecastChart(data.fiveDaysData.list)
    }
}

/* ============================================================
   MATERIAL ICON WEATHER MAPPING
   ============================================================ */

@Composable
fun WeatherIconFor(code: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (code) {
        // Thunderstorm
        in listOf("11d", "11n") -> Icons.Default.Thunderstorm

        // Drizzle
        in listOf("09d", "09n") -> Icons.Default.Umbrella

        // Rain
        in listOf("10d", "10n") -> Icons.Default.InvertColors

        // Snow
        in listOf("13d", "13n") -> Icons.Default.AcUnit

        // Fog / Haze / Smoke
        in listOf("50d", "50n") -> Icons.Default.BlurOn

        // Clear day / night
        "01d" -> Icons.Default.WbSunny
        "01n" -> Icons.Default.Nightlight

        // Few clouds / scattered / broken
        "02d", "02n" -> Icons.Default.CloudQueue
        "03d", "03n", "04d", "04n" -> Icons.Default.Cloud

        else -> Icons.Default.Cloud // fallback
    }
}

/* ============================================================
   APPLE-STYLE CONNECTED LINE CHART
   ============================================================ */

@Composable
fun HorizontalDetailedForecastChart(list: List<ForecastEntry>) {

    if (list.isEmpty()) return

    val itemWidth = 110.dp
    val chartHeight = 120.dp
    val topPadding = 30.dp
    val bottomPadding = 40.dp

    val times = SimpleDateFormat("E HH:mm", Locale.getDefault())

    val temps = list.map { it.main.temp.toFloat() }
    val minTemp = temps.minOrNull() ?: 0f
    val maxTemp = temps.maxOrNull() ?: minTemp
    val range = (maxTemp - minTemp).takeIf { it > 0f } ?: 1f

    val normalized = temps.map { (it - minTemp) / range }

    val scroll = rememberScrollState()
    val total = itemWidth * list.size

    val curveColor = Color(0xFFFFD54F)

    Box(
        modifier = Modifier
            .horizontalScroll(scroll)
            .width(total)
            .height(chartHeight + topPadding + bottomPadding)
    ) {

        // Whole scroll container
        Row(modifier = Modifier.width(total)) {

            Box(modifier = Modifier.width(total).fillMaxHeight()) {

                // ===== CANVAS LAYER =====
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = topPadding, bottom = bottomPadding)
                ) {
                    val pWidth = itemWidth.toPx()
                    val height = size.height

                    val points = normalized.mapIndexed { i, n ->
                        Offset(
                            x = i * pWidth + pWidth / 2,
                            y = height - (n * height)
                        )
                    }

                    if (points.isNotEmpty()) {
                        val path = Path().apply {
                            moveTo(points[0].x, points[0].y)

                            for (i in 0 until points.lastIndex) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val c = (p0.x + p1.x) / 2f
                                cubicTo(c, p0.y, c, p1.y, p1.x, p1.y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = curveColor,
                            style = Stroke(width = 6f)
                        )

                        points.forEach { p ->
                            drawCircle(Color.White, radius = 10f, center = p)
                            drawCircle(curveColor, radius = 6f, center = p)
                        }
                    }
                }

                // ===== FORECAST ITEMS (overlay) =====
                Row(
                    modifier = Modifier
                        .width(total)
                        .padding(horizontal = 0.dp)
                        .fillMaxHeight()
                ) {

                    list.forEachIndexed { index, item ->

                        val temp = item.main.temp.toInt()
                        val humidity = item.main.humidity
                        val time = times.format(Date(item.dt * 1000))
                        val iconCode = item.weather.firstOrNull()?.icon ?: "01d"

                        Column(
                            modifier = Modifier
                                .width(itemWidth)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Spacer(Modifier.height(2.dp))

                            Text(
                                text = time,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )

                            Spacer(Modifier.height(4.dp))

                            Icon(
                                imageVector = WeatherIconFor(iconCode),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                "$temp°",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(Modifier.height(24.dp))

                            Text(
                                "${humidity}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xAAFFFFFF)
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================
   CURRENT WEATHER CARD
   ============================================================ */

@Composable
fun CurrentWeatherCard(data: CurrentWeatherData) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${data.main.temp.toInt()}°C",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Light
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        data.weather.firstOrNull()?.main ?: "",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        data.weather.firstOrNull()?.description ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem("Feels Like", "${data.main.feelsLike.toInt()}°C")
                WeatherDetailItem("Humidity", "${data.main.humidity}%")
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem("Wind", "${data.wind.speed} m/s")
                WeatherDetailItem("Pressure", "${data.main.pressure} hPa")
            }
        }
    }
}

/* ============================================================
   DETAIL ITEM
   ============================================================ */

@Composable
fun WeatherDetailItem(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

/* ============================================================
   AIR POLLUTION CARD
   ============================================================ */

@Composable
fun AirPollutionCard(data: AirPollutionData) {
    val aqi = data.list.firstOrNull()?.main?.aqi ?: 0

    val text = when (aqi) {
        1 -> "Good"
        2 -> "Fair"
        3 -> "Moderate"
        4 -> "Poor"
        5 -> "Very Poor"
        else -> "-"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("AQI", style = MaterialTheme.typography.titleMedium)
            Text("$aqi - $text", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(10.dp))
        }
    }
}

/* ============================================================
   DETAILED AIR POLLUTION CARD (with all components)
   ============================================================ */

@Composable
fun DetailedAirPollutionCard(data: AirPollutionData) {
    val pollutionData = data.list.firstOrNull()
    val aqi = pollutionData?.main?.aqi ?: 0
    val components = pollutionData?.components

    val (aqiText, aqiColor) = when (aqi) {
        1 -> "Good" to Color(0xFF4CAF50)
        2 -> "Fair" to Color(0xFF8BC34A)
        3 -> "Moderate" to Color(0xFFFFC107)
        4 -> "Poor" to Color(0xFFFF9800)
        5 -> "Very Poor" to Color(0xFFF44336)
        else -> "Unknown" to Color.Gray
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header with AQI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Air Quality Index",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = aqiText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = aqiColor
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(aqiColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = aqi.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = aqiColor
                    )
                }
            }

            if (components != null) {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Pollutant Concentrations (μg/m³)",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // First row of components
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PollutantItem("CO", components.co.toString(), "Carbon Monoxide")
                    PollutantItem("NO", components.no.toString(), "Nitric Oxide")
                    PollutantItem("NO₂", components.no2.toString(), "Nitrogen Dioxide")
                }

                Spacer(Modifier.height(12.dp))

                // Second row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PollutantItem("O₃", components.o3.toString(), "Ozone")
                    PollutantItem("SO₂", components.so2.toString(), "Sulfur Dioxide")
                    PollutantItem("PM2.5", components.pm25.toString(), "Fine Particles")
                }

                Spacer(Modifier.height(12.dp))

                // Third row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PollutantItem("PM10", components.pm10.toString(), "Coarse Particles")
                    PollutantItem("NH₃", components.nh3.toString(), "Ammonia")
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun RowScope.PollutantItem(label: String, value: String, description: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 11.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}