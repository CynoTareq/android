// SensorDataDto.kt
package it.cynomys.cfmandroid.sensordata

import kotlinx.serialization.Serializable // Required for kotlinx.serialization

/**
 * Data Transfer Object (DTO) for a single sensor data reading.
 * This can be used for sending/receiving individual sensor values via an API.
 */
@Serializable
data class SensorDataDto(
    val sensorName: String,
    val ts: Long,        // Timestamp of the sensor reading
    val value: String    // The value of the sensor reading (matches SensorDataItem's value type)
)