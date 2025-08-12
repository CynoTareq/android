package it.cynomys.cfmandroid.sensordata

import com.google.gson.Gson

data class IndexResponse(
    val id: String,
    val ownerId: String,
    val farmId: String,
    val type: String,
    val typeId: String,
    val indexes: Map<String, IndexData>
) {
    companion object {
        fun fromJson(jsonString: String): IndexResponse {
            val gson = Gson()
            return gson.fromJson(jsonString, IndexResponse::class.java)
        }
    }
}

data class IndexData(
    val score: Double,
    val description: String,
    val status: String,
    val parameters: List<Map<String, Double>>
)

data class IndexDisplayItem(
    val name: String,
    val score: Double,
    val description: String,
    val status: String,
    val maxScore: Double = 100.0 // Default max score, can be adjusted
)