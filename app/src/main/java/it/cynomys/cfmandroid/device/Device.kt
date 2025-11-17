package it.cynomys.cfmandroid.device

import com.google.gson.JsonElement

import java.util.UUID

data class Device(
    val id: UUID,
    val deviceID: String,
    val displayName: String,
    val predictions: Boolean,
    val indexes: String,
    val license: String?, // Added: license name (e.g. "ultimate")
    val licenseId: JsonElement?, // Added: license ID
    val shouldNotify: Boolean, // Added: should notify
    val ownerId: UUID?, // Existing field
    val farmId: UUID?,  // Existing field
    val penId: UUID?    // Existing field
)

data class DeviceDto(
    val deviceID: String,
    val displayName: String,
    val predictions: Boolean,
    val indexes: String,
    val licenseId: UUID?,
    val license:String?,
    val shouldNotify: Boolean // Added: should notify for creation payload
)
// Added: DTO specifically for the Update Endpoint
data class DeviceUpdateDto(
    val id: UUID,
    val deviceID: String,
    val displayName: String,
    val predictions: Boolean,
    val indexes: String,
    val license: String?,
    val licenseId: UUID,
    val shouldNotify: Boolean
)
fun Device.toDeviceDto(): DeviceDto {
    val licenseInfo = licenseId.toLicenseInfoOrNull()
    return DeviceDto(
        deviceID = this.deviceID,
        displayName = this.displayName,
        predictions = this.predictions,
        indexes = this.indexes,
        licenseId = licenseInfo?.id,
        license = this.license,
        shouldNotify = this.shouldNotify
    )
}
// Add this to Silo.kt
data class LicenseInfo(
    val id: UUID,
    val type: String,
    val name: String,
    val inUse: Boolean
)

fun JsonElement?.toLicenseInfoOrNull(): LicenseInfo? {
    if (this == null || !this.isJsonObject) return null

    val obj = this.asJsonObject
    return try {
        LicenseInfo(
            id = UUID.fromString(obj["id"].asString),
            type = obj["type"].asString,
            name = obj["name"].asString,
            inUse = obj["inUse"].asBoolean
        )
    } catch (e: Exception) {
        null
    }
}
