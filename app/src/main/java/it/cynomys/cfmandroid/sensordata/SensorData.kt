// SensorData.kt
package it.cynomys.cfmandroid.sensordata

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SensorData(
    val data: Map<String, List<SensorDataItem>>
) {
    companion object {
        private const val TAG = "SensorDataParser"

        fun fromJson(jsonString: String): SensorData {
            val gson = Gson()
            val type = object : TypeToken<Map<String, List<SensorDataItem>>>() {}.type
            return try {
                val dataMap: Map<String, List<SensorDataItem>> = gson.fromJson(jsonString, type)
                Log.d(TAG, "JSON parsing successful. Data map contains ${dataMap.size} sensor types.")
                SensorData(dataMap)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing JSON to SensorData: ${e.message}", e)
                throw e
            }
        }
    }
}

data class SensorDataItem(
    val ts: Long,
    val value: String // CHANGED BACK TO STRING to match iOS app's SensorValue
)