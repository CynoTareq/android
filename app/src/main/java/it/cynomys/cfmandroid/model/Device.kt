package it.cynomys.cfmandroid.model

import java.util.UUID

data class Device(
    val id: UUID,
    val deviceID: String,
    val displayName: String,
    val predictions: Boolean,
    val indexes: String,
    val ownerId: UUID,
    val farmId: UUID,
    val penId: UUID
)


data class DeviceDto(
    val deviceID: String,
    val displayName: String,
    val predictions: Boolean,
    val indexes: String
)