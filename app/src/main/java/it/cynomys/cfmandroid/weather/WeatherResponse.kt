package it.cynomys.cfmandroid.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val markerData: MarkerData,
    val weatherData: CurrentWeatherData,
    val airPollutionData: AirPollutionData,
    val fiveDaysData: FiveDaysForecast
)

@Serializable
data class MarkerData(
    val latitude: Double,
    val longitude: Double,
    val title: String
)

// --- Current Weather Data ---
@Serializable
data class CurrentWeatherData(
    val coord: Coordinates,
    val weather: List<WeatherDetail>,
    val base: String,
    val main: MainData,
    val visibility: Int,
    val wind: WindData,
    val clouds: CloudsData,
    val dt: Long,
    val sys: SysData,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int
)

@Serializable
data class Coordinates(val lon: Double, val lat: Double)

@Serializable
data class WeatherDetail(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class MainData(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min") val tempMin: Double,
    @SerialName("temp_max") val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    @SerialName("sea_level") val seaLevel: Int? = null,
    @SerialName("grnd_level") val groundLevel: Int? = null,
    @SerialName("temp_kf") val tempKf: Double? = null
)

@Serializable
data class WindData(
    val speed: Double,
    val deg: Int,
    val gust: Double? = null
)

@Serializable
data class CloudsData(val all: Int)

@Serializable
data class SysData(
    val country: String? = null,
    val sunrise: Long? = null,
    val sunset: Long? = null,
    val pod: String? = null
)

// --- Air Pollution Data ---
@Serializable
data class AirPollutionData(
    val coord: Coordinates,
    val list: List<AirPollutionEntry>
)

@Serializable
data class AirPollutionEntry(
    val main: AirPollutionMain,
    val components: AirPollutionComponents,
    val dt: Long
)

@Serializable
data class AirPollutionMain(val aqi: Int)

@Serializable
data class AirPollutionComponents(
    val co: Double,
    val no: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    @SerialName("pm2_5") val pm25: Double,
    val pm10: Double,
    val nh3: Double
)

// --- 5-Day Forecast Data ---
@Serializable
data class FiveDaysForecast(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<ForecastEntry>
)

@Serializable
data class ForecastEntry(
    val dt: Long,
    val main: MainData,
    val weather: List<WeatherDetail>,
    val clouds: CloudsData,
    val wind: WindData,
    val visibility: Int,
    val pop: Double,
    val sys: SysData,
    @SerialName("dt_txt") val dtTxt: String,
    val rain: RainData? = null
)

@Serializable
data class RainData(
    @SerialName("3h") val rainVolume3h: Double
)