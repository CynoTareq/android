// Farm.kt
package it.cynomys.cfmandroid.model

import java.util.UUID

enum class Species {
    RUMINANT, SWINE, POULTRY, EQUINE, OTHER
}

data class Farm(
    val id: UUID?,
    val name: String,
    val coordinateX: Double,
    val coordinateY: Double,
    val address: String,
    val ownerId: UUID,
    val area: Double,
    val species: Species
)

data class FarmDto(
    val id: UUID?,
    val name: String,
    val coordinateX: Double,
    val coordinateY: Double,
    val address: String,
    val area: Double,
    val species: Species
)