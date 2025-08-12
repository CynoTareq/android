package it.cynomys.cfmandroid.silo

import java.util.Date
import java.util.UUID

// Enum for different silo shapes
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
    val penId: UUID, // Assuming penId is required for DTO creation/update
    val farmId: UUID, // Assuming farmId is required for DTO creation/update
    val ownerId: UUID, // Assuming ownerId is required for DTO creation/update
    val model: SiloModel,
    val material_name: String,
    val material_density: Double
)

// Extension function to convert a Silo object to a SiloDto object for API requests
fun Silo.toSiloDto(): SiloDto {
    // Ensure ownerId, farmId, and penId are not null when converting to DTO
    // This assumes that when converting an existing Silo to DTO, these IDs will be present.
    // For new silos, they will be provided during the add operation.
    return SiloDto(
        silosID = this.silosID,
        displayName = this.displayName,
        silosHeight = this.silosHeight,
        silosDiameter = this.silosDiameter,
        coneHeight = this.coneHeight,
        bottomDiameter = this.bottomDiameter,
        shape = this.shape,
        penId = this.penId ?: throw IllegalArgumentException("Pen ID cannot be null for SiloDto"),
        farmId = this.farmId ?: throw IllegalArgumentException("Farm ID cannot be null for SiloDto"),
        ownerId = this.ownerId ?: throw IllegalArgumentException("Owner ID cannot be null for SiloDto"),
        model = this.model,
        material_name = this.material_name,
        material_density = this.material_density
    )
}


