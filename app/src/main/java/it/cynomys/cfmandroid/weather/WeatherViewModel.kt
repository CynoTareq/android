package it.cynomys.cfmandroid.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.util.NetworkService // Import your existing NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define the UI State
sealed interface WeatherState {
    data object Loading : WeatherState
    data class Success(val weatherData: WeatherResponse) : WeatherState
    data class Error(val message: String) : WeatherState
}

class WeatherViewModel(
    private val farmId: String,
    private val networkService: NetworkService
) : ViewModel() {

    private val _state = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val state: StateFlow<WeatherState> = _state.asStateFlow()

    init {
        getWeatherData()
    }

    private fun getWeatherData() {
        viewModelScope.launch {
            _state.value = WeatherState.Loading
            try {
                val url = "api/weather/$farmId"

                // FIX: Passing WeatherResponse::class.java as the responseType
                val result: Result<WeatherResponse> = networkService.get(
                    path = url,
                    responseType = WeatherResponse::class.java
                )

                result.onSuccess { weatherResponse ->
                    _state.value = WeatherState.Success(weatherResponse)
                }.onFailure { exception ->
                    _state.value = WeatherState.Error(
                        exception.message ?: "Failed to fetch weather data."
                    )
                }
            } catch (e: Exception) {
                _state.value = WeatherState.Error(e.message ?: "Network request failed.")
            }
        }
    }
}

// ViewModel Factory to handle the farmId parameter
class WeatherViewModelFactory(
    private val farmId: String,
    private val networkService: NetworkService = NetworkService()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(farmId, networkService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}