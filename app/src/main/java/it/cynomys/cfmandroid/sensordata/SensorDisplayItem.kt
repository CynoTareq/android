package it.cynomys.cfmandroid.sensordata

/**
 * Data class representing a single sensor reading suitable for display in the UI.
 * This acts as a DTO (Data Transfer Object) for UI purposes.
 * @param name The name of the sensor (e.g., "temperature", "humidity").
 * @param value The numerical value of the sensor reading.
 * @param timestamp The timestamp when the reading was taken (e.g., in milliseconds since epoch).
 */
data class SensorDisplayItem(
    val name: String,
    val value: Double,
    val timestamp: Long
)