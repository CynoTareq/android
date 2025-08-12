package it.cynomys.cfmandroid.sensordata

// Enum for sensor units
enum class SensorUnit(val unit: String) {
    TEMPERATURE("°C"),
    HUMIDITY("%"),
    THI(""), // Thermal-Humidity Index
    BATTERY_LEVEL("%"),
    VOLTAGE("V"),
    DEW_POINT("°C"),
    LUX("lx"),
    ATMOSPHERIC_PRESSURE("hPa"),
    CO2("ppm"),
    AMMONIA("ppm"),
    GAS("ppm"),
    PM1("µg/m³"),
    PM2_5("µg/m³"),
    PM10("µg/m³"),
    NO2("ppm"),
    OTHER(""); // Default for unknown sensors

    companion object {
        fun unitFor(sensorName: String): String {
            return when (sensorName.lowercase()) {
                "temperature" -> TEMPERATURE.unit
                "humidity" -> HUMIDITY.unit
                "thi" -> THI.unit
                "battery_level" -> BATTERY_LEVEL.unit
                "voltage" -> VOLTAGE.unit
                "dew_point" -> DEW_POINT.unit
                "lux" -> LUX.unit
                "atmospheric_pressure" -> ATMOSPHERIC_PRESSURE.unit
                "co2", "carbondioxide" -> CO2.unit
                "ammonia" -> AMMONIA.unit
                "gas" -> GAS.unit
                "pm1" -> PM1.unit
                "pm2_5" -> PM2_5.unit
                "pm10" -> PM10.unit
                "no2" -> NO2.unit
                else -> OTHER.unit
            }
        }
    }
}