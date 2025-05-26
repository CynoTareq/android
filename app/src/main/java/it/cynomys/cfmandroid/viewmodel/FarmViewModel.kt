// FarmViewModel.kt
package it.cynomys.cfmandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.model.Farm
import it.cynomys.cfmandroid.model.FarmDto
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
class FarmViewModel : ViewModel() {
    private val networkService = NetworkService()

    private val _farms = MutableStateFlow<List<Farm>>(emptyList())
    val farms: StateFlow<List<Farm>> = _farms

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getFarms(ownerId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = networkService.get<Array<Farm>>( // Changed to Array<Farm>
                    path = "api/farm/$ownerId",
                    responseType = Array<Farm>::class.java
                )

                if (result.isSuccess) {
                    _farms.value = result.getOrNull()?.toList() ?: emptyList() // Convert array to list
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ... rest of the code remains the same ...
    // In FarmViewModel.kt
    fun deleteFarm(farmId: UUID, ownerId: UUID) {  // Now accepts both parameters
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = networkService.delete("api/farm/$farmId")

                if (result.isSuccess) {
                    // Refresh the list after deletion
                    getFarms(ownerId)  // Now we have ownerId available
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to delete farm"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete farm"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFarm(ownerId: UUID, farmDto: FarmDto) {

    }

}