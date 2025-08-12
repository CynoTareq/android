// Farm.kt
package it.cynomys.cfmandroid.farm

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
fun Farm.toFarmDto(): FarmDto {
    return FarmDto(
        id = this.id,
        name = this.name,
        coordinateX = this.coordinateX,
        coordinateY = this.coordinateY,
        address = this.address,
        area = this.area,
        species = this.species
        // The ownerId field is part of Farm but is excluded from FarmDto,
        // so it is correctly not mapped here.
    )
}