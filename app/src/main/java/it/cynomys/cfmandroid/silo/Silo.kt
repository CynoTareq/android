package it.cynomys.cfmandroid.silo

import com.google.gson.JsonElement
import java.util.Date
import java.util.UUID

enum class SiloShape {
    FULL_CYLINDRICAL,
    CONICAL_BOTTOM,
    FLAT_BOTTOM,
    // Add other shapes as needed
}

// Data class for the manufacturer and model information of a silo
data class SiloModel(
    val manufacturer: String,
    val model: String
)

// Data class representing a Silo with comprehensive details
data class Silo(
    val id: UUID?, // Nullable for new silos before they get an ID from the backend
    val silosID: String, // Unique identifier for the silo (e.g., serial number)
    val displayName: String, // User-friendly name for the silo
    val silosHeight: Double, // Total height of the cylindrical part of the silo
    val silosDiameter: Double, // Diameter of the cylindrical part of the silo
    val coneHeight: Double?, // Height of the conical bottom, nullable if shape is not conical
    val bottomDiameter: Double?, // Diameter of the very bottom opening, nullable
    val shape: SiloShape, // Shape of the silo, using the enum
    val penId: UUID?, // ID of the pen this silo is associated with, nullable
    val farmId: UUID?,  // ID of the farm this silo belongs to, nullable
    val ownerId: UUID?, // ID of the owner of this silo, nullable
    val model: SiloModel, // Manufacturer and model details of the silo
    val material_name: String, // Name of the material the silo is made from
    val material_density: Double, // Density of the material in the silo (e.g., kg/m^3)
    val license: String?, // Added: license name (e.g. "ultimate")
    val licenseId: JsonElement?, // Change from UUID? to LicenseInfo?, // Added: license ID
    val indexes: String?, // Added: indexes
    val shouldNotify: Boolean, // Added: should notify
    val predictions: Boolean?, // Added: predictions
    val lastSyncTime: Date? // For offline synchronization, nullable
)

// Data Transfer Object for creating or updating a Silo via API
// This DTO includes all necessary fields for API interaction, excluding generated IDs and sync times.
data class SiloDto(
    val silosID: String,
    val displayName: String,
    val silosHeight: Double,
    val silosDiameter: Double,
    val coneHeight: Double?,
    val bottomDiameter: Double?,
    val shape: SiloShape,
    val penId: UUID?,
    val farmId: UUID,
    val ownerId: UUID,
    val model: SiloModel,
    val material_name: String,
    val material_density: Double,
    val license: String?,
    val licenseId: UUID?, // Keep as UUID? for outgoing API requests
    val indexes: String?,
    val shouldNotify: Boolean,
    val predictions: Boolean
)

fun Silo.toSiloDto(): SiloDto {
    val licenseInfo = licenseId.toLicenseInfoOrNull()

    return SiloDto(
        silosID = silosID,
        displayName = displayName,
        silosHeight = silosHeight,
        silosDiameter = silosDiameter,
        coneHeight = coneHeight,
        bottomDiameter = bottomDiameter,
        shape = shape,
        penId = penId,
        farmId = farmId ?: UUID.randomUUID(),
        ownerId = ownerId ?: UUID.randomUUID(),
        model = model,
        material_name = material_name,
        material_density = material_density,
        license = license,
        licenseId = licenseInfo?.id,
        indexes = indexes,
        shouldNotify = shouldNotify,
        predictions = predictions ?: false
    )
}


data class SiloModelSpec(
    val model: String,
    val manufacturer: String,
    val cubicMeters: Double,
    val tons: Double,
    val lengthInMM: Int,
    val widthInMM: Int,
    val heightInMM: Int,
    val diameterInMM: Int,
    val exteriorDiameterInMM: Int,
    val numberOfLegs: Int
)

data class SiloMaterial(
    val materialName: String,
    val density: Double // Changed to Double to match the DTO in SiloFormView.kt
)

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
