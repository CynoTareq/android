package it.cynomys.cfmandroid.sensordata

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.repository.OfflineRepository
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SensorDataViewModel(private val context: Context) : ViewModel() {
    private val networkService = NetworkService()
    private val offlineRepository = OfflineRepository(context)

    private val _latestSensorData = MutableStateFlow<Map<String, SensorDisplayItem>>(emptyMap())
    val latestSensorData: StateFlow<Map<String, SensorDisplayItem>> = _latestSensorData

    private val _historicalData = MutableStateFlow<Map<String, List<SensorDataItem>>>(emptyMap())
    val historicalData: StateFlow<Map<String, List<SensorDataItem>>> = _historicalData

    private val _predictions = MutableStateFlow<List<Prediction>>(emptyList())
    val predictions: StateFlow<List<Prediction>> = _predictions

    private val _indexes = MutableStateFlow<List<IndexDisplayItem>>(emptyList())
    val indexes: StateFlow<List<IndexDisplayItem>> = _indexes

    private val _isLoadingLatest = MutableStateFlow(false)
    val isLoadingLatest: StateFlow<Boolean> = _isLoadingLatest

    private val _isLoadingHistorical = MutableStateFlow(false)
    val isLoadingHistorical: StateFlow<Boolean> = _isLoadingHistorical

    private val _isLoadingPredictions = MutableStateFlow(false)
    val isLoadingPredictions: StateFlow<Boolean> = _isLoadingPredictions

    private val _isLoadingIndexes = MutableStateFlow(false)
    val isLoadingIndexes: StateFlow<Boolean> = _isLoadingIndexes

    private val _errorMessageLatest = MutableStateFlow<String?>(null)
    val errorMessageLatest: StateFlow<String?> = _errorMessageLatest

    private val _errorMessageHistorical = MutableStateFlow<String?>(null)
    val errorMessageHistorical: StateFlow<String?> = _errorMessageHistorical

    private val _predictionErrorMessage = MutableStateFlow<String?>(null)
    val predictionErrorMessage: StateFlow<String?> = _predictionErrorMessage

    private val _indexErrorMessage = MutableStateFlow<String?>(null)
    val indexErrorMessage: StateFlow<String?> = _indexErrorMessage

    /**
     * Fetches the latest sensor values for a given device.
     * Updates _latestSensorData and _errorMessageLatest.
     * @param deviceId The ID of the device to fetch data for.
     */
    fun getLatestValues(deviceId: String) {
        viewModelScope.launch {
            _isLoadingLatest.value = true
            _errorMessageLatest.value = null // Clear previous error
            try {
                // 1. Change to get the response as a String
                val result = networkService.get<String>(
                    path = "api/telemetry/latest/$deviceId",
                    responseType = String::class.java
                )

                if (result.isSuccess) {
                    val jsonString = result.getOrNull()
                    if (!jsonString.isNullOrBlank()) {
                        // 2. Use SensorData.fromJson to parse the string
                        val sensorDataWrapper = SensorData.fromJson(jsonString)
                        val dataMap = sensorDataWrapper.data // Now 'data' will correctly hold the map from JSON

                        Log.d("SensorDataViewModel", "SensorData.data is not null/empty. Contains ${dataMap.size} sensor types.")
                        val latestMap = dataMap.mapValues { (_, items) ->
                            items.maxByOrNull { it.ts }
                        }.filterValues { it != null }
                            .mapValues { (name, item) ->
                                SensorDisplayItem(name, item!!.value.toDoubleOrNull() ?: 0.0, item.ts)
                            }
                        _latestSensorData.value = latestMap
                        Log.d("SensorDataViewModel", "Updated _latestSensorData with ${latestMap.size} items.")
                    } else {
                        Log.e("SensorDataViewModel", "Received empty or null JSON string for latest sensor data.")
                        _errorMessageLatest.value = "Empty response for latest sensor data."
                    }
                } else {
                    _errorMessageLatest.value = result.exceptionOrNull()?.message ?: "Failed to fetch latest sensor data"
                    Log.e("SensorDataViewModel", "Network call for latest sensor data failed: ${_errorMessageLatest.value}")
                }
            } catch (e: Exception) {
                _errorMessageLatest.value = "Error fetching latest sensor data: ${e.message}"
                Log.e("SensorDataViewModel", "Error fetching latest sensor data: ${e.message}", e)
            } finally {
                _isLoadingLatest.value = false
            }
        }
    }

    fun fetchHistoricData(
        deviceID: String,
        attributes: String,
        aggregation: String,
        limit: Int,
        interval: Long,
        orderBy: String,
        startTime: Long,
        endTime: Long,
        onComplete: (Result<SensorData>) -> Unit
    ) {
        // Assuming authViewModel and _errorMessage are part of a larger ViewModel or context
        // For this specific file, I'll keep the network call logic and omit auth check if not defined here.
        // If authViewModel is supposed to be part of SensorDataViewModel, it needs to be passed in or initialized.

        _isLoadingHistorical.value = true
        _errorMessageHistorical.value = null

        viewModelScope.launch {
            try {
                val path = "api/telemetry/$deviceID/$startTime/$endTime"
                val queryParams = mapOf(
                    "attributes" to attributes,
                    "setAgg" to aggregation,
                    "setLimit" to limit.toString(),
                    "interval" to interval.toString(),
                    "orderBy" to orderBy
                )

                val jsonResult = networkService.get<String>(
                    path = path,
                    queryParams = queryParams,
                    responseType = String::class.java
                )

                _isLoadingHistorical.value = false
                // Assuming SensorData.fromJson is available to parse the JSON string
                onComplete(jsonResult.map { json -> SensorData.fromJson(json) })
            } catch (e: Exception) {
                _isLoadingHistorical.value = false
                _errorMessageHistorical.value = "Error fetching historical sensor data: ${e.message}"
                onComplete(Result.failure(e))
            }
        }
    }

    /**
     * Fetches predictions for a given device.
     * @param deviceId The ID of the device.
     */
    fun getPredictions(deviceId: String) {
        viewModelScope.launch {
            _isLoadingPredictions.value = true
            _predictionErrorMessage.value = null
            try {

                val result = networkService.get<Array<Prediction>>(
                    path = "api/predict/getPredictions/$deviceId",
                    responseType = Array<Prediction>::class.java
                )
                if (result.isSuccess) {
                    _predictions.value = result.getOrNull()?.toList() ?: emptyList()
                } else {
                    _predictionErrorMessage.value = result.exceptionOrNull()?.message ?: "Failed to fetch predictions"
                }
            } catch (e: Exception) {
                _predictionErrorMessage.value = "Error fetching predictions: ${e.message}"
            } finally {
                _isLoadingPredictions.value = false
            }
        }
    }

    /**
     * Fetches indexes for a given farm, type ID, and type.
     * @param farmId The ID of the farm.
     * @param typeId The ID of the specific type (e.g., device ID).
     * @param type The category of the type (e.g., "DEVICE", "FARM").
     */
    fun getIndexes(farmId: String, typeId: String, type: String) {
        viewModelScope.launch {
            _isLoadingIndexes.value = true
            _indexErrorMessage.value = null
            try {
                val result = networkService.get<IndexResponse>(
                    path = "api/ai/indexes/$farmId/$typeId/$type",
                    responseType = IndexResponse::class.java
                )
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    response?.indexes?.let { indexMap ->
                        val displayItems = indexMap.map { (name, data) ->
                            IndexDisplayItem(name, data.score, data.description, data.status)
                        }
                        _indexes.value = displayItems
                    }
                } else {
                    _indexErrorMessage.value = result.exceptionOrNull()?.message ?: "Failed to fetch indexes"
                }
            } catch (e: Exception) {
                _indexErrorMessage.value = "Error fetching indexes: ${e.message}"
            } finally {
                _isLoadingIndexes.value = false
            }
        }
    }
}