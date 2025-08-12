package it.cynomys.cfmandroid.sensordata

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Prediction(
    val id: String,
    val predictionTime: Long,
    val predictedTemperature: Double,
    val predictedHumidity: Double,
    val predictedTHI: Double,
    val deviceId: String
) {
    companion object {
        fun fromJsonArray(jsonString: String): List<Prediction> {
            val gson = Gson()
            val type = object : TypeToken<List<Prediction>>() {}.type
            return gson.fromJson(jsonString, type)
        }
    }
}
