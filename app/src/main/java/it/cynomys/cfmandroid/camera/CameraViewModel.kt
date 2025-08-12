// CameraViewModel.kt
package it.cynomys.cfmandroid.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CameraViewModel : ViewModel() {
    private val networkService = NetworkService()

    private val _cameras = MutableStateFlow<List<Camera>>(emptyList())
    val cameras: StateFlow<List<Camera>> = _cameras

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getCameras(farmId: UUID, ownerId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = networkService.get<Array<Camera>>(
                    path = "api/cameras/$farmId/$ownerId",
                    responseType = Array<Camera>::class.java
                )

                if (result.isSuccess) {
                    _cameras.value = result.getOrNull()?.toList() ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load cameras"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load cameras"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCamera(farmId: UUID, ownerId: UUID, cameraDto: CameraDto) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = networkService.post<CameraDto, Camera>(
                    path = "api/cameras/$farmId/$ownerId",
                    body = cameraDto,
                    responseType = Camera::class.java
                )

                if (result.isSuccess) {
                    getCameras(farmId, ownerId) // Refresh the list
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to add camera"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add camera"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCamera(cameraId: UUID, cameraDto: CameraDto) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = networkService.put<CameraDto, Camera>(
                    path = "api/cameras/edit",
                    body = cameraDto,
                    responseType = Camera::class.java
                )

                if (result.isSuccess) {
                    // Update the local state
                    _cameras.value = _cameras.value.map {
                        if (it.id == cameraId) result.getOrNull() ?: it else it
                    }
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to update camera"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update camera"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCamera(cameraId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = networkService.delete("api/cameras/$cameraId")

                if (result.isSuccess) {
                    // Remove from local state
                    _cameras.value = _cameras.value.filter { it.id != cameraId }
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to delete camera"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete camera"
            } finally {
                _isLoading.value = false
            }
        }
    }
}