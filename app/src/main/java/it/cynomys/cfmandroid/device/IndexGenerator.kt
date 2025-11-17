package it.cynomys.cfmandroid.device

import it.cynomys.cfmandroid.farm.Species

// Index generation logic based on species and license
object IndexGenerator {
    private val speciesIndexGroups = mapOf(
        Species.RUMINANT to mapOf(
            "CORE" to listOf("ventilation_index", "lighting_index"),
            "PERFORMANCE" to listOf("ventilation_index", "lighting_index", "productivity_index", "feed_management", "environmental_index"),
            "ULTIMATE" to listOf("ventilation_index", "lighting_index", "productivity_index", "feed_management", "environmental_index", "respiratory_index")
        ),
        Species.SWINE to mapOf(
            "CORE" to listOf("ventilation_index", "lighting_index"),
            "PERFORMANCE" to listOf("ventilation_index", "lighting_index", "productivity_index", "gastrointestinal_index", "environmental_index"),
            "ULTIMATE" to listOf("ventilation_index", "lighting_index", "productivity_index", "gastrointestinal_index", "environmental_index", "respiratory_index", "stress_index")
        ),
        Species.POULTRY to mapOf(
            "CORE" to listOf("lighting_index"),
            "PERFORMANCE" to listOf("lighting_index", "gastrointestinal_index", "environmental_index"),
            "ULTIMATE" to listOf("lighting_index", "gastrointestinal_index", "environmental_index", "stress_index")
        )
    )

    fun getIndexesByLicenseAndSpecies(license: String, species: Species): String {
        val normalizedLicense = license.uppercase()
        val indexes = speciesIndexGroups[species]?.get(normalizedLicense) ?: emptyList()
        return indexes.joinToString(",")
    }
    private val siloIndexGroups = mapOf( // Added the silo index map
        "CORE" to listOf("fill_level_index"),
        "PERFORMANCE" to listOf("fill_level_index", "water_infiltration_risk_index"),
        "ULTIMATE" to listOf("fill_level_index", "water_infiltration_risk_index", "aspergillus_risk_index"),
    )
    fun getSiloIndexesByLicense(license: String): String { // Added the new function
        val normalizedLicense = license.uppercase()
        val indexes = siloIndexGroups[normalizedLicense] ?: emptyList()
        return indexes.joinToString(",")
    }
}