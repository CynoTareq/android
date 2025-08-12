package it.cynomys.cfmandroid.device

import java.util.UUID

data class Device(
    val id: UUID,
    val deviceID: String,
    val displayName: String,
    val predictions: Boolean,
    val indexes: String,
    val ownerId: UUID?, // Made nullable
    val farmId: UUID?,  // Made nullable
    val penId: UUID?    // Made nullable
)


data class DeviceDto(
    val deviceID: String,
    val displayName: String,
    val predictions: Boolean,
    val indexes: String
)
fun Device.toDeviceDto(): DeviceDto {
    return DeviceDto(
        deviceID = this.deviceID,
        displayName = this.displayName,
        predictions = this.predictions,
        indexes = this.indexes
        // The 'id', 'ownerId', 'farmId', and 'penId' fields are part of Device but are
        // excluded from DeviceDto as per your definition, so they are correctly not mapped here.
    )
}
