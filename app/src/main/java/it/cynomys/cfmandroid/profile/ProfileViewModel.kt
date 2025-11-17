package it.cynomys.cfmandroid.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.auth.Owner
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileViewModel : ViewModel() {

    private val networkService = NetworkService()

    private val _owner = MutableStateFlow<Owner?>(null)
    val owner: StateFlow<Owner?> = _owner

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    fun fetchOwnerSettings(ownerId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = false
            _updateSuccess.value = false

            try {
                val result = networkService.get(
                    path = "api/owner/$ownerId/settings",
                    responseType = ProfileDto::class.java
                )

                result.fold(
                    onSuccess = { dto ->
                        _owner.value = dto.toOwner()
                        Log.d("ProfileViewModel", "Owner settings fetched successfully for ${dto.email}")
                    },
                    onFailure = { throwable ->
                        _isError.value = true
                        Log.e("ProfileViewModel", "Failed to fetch owner settings", throwable)
                    }
                )
            } catch (e: Exception) {
                _isError.value = true
                Log.e("ProfileViewModel", "Unexpected error during fetch", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOwnerSettings(ownerId: UUID, updatedOwner: Owner) {
        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = false
            _updateSuccess.value = false

            try {
                val result = networkService.put(
                    // Correct endpoint path
                    path = "api/owner/$ownerId/settings",
                    // Uses toProfileUpdateDto() which now formats 'birthday' as "yyyy-MM-dd" String
                    body = updatedOwner.toProfileUpdateDto(),
                    responseType = ProfileUpdateDto::class.java
                )

                result.fold(
                    onSuccess = { responseDto ->
                        // Uses toOwner() which now parses the 'birthday' String back to a Date
                        _owner.value = responseDto.toOwner(updatedOwner)
                        _updateSuccess.value = true
                        Log.d("ProfileViewModel", "Owner settings updated successfully for ${responseDto.email}")
                    },
                    onFailure = { throwable ->
                        _isError.value = true
                        Log.e("ProfileViewModel", "Failed to update owner settings", throwable)
                    }
                )
            } catch (e: Exception) {
                _isError.value = true
                Log.e("ProfileViewModel", "Unexpected error during update", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun clearError() {
        _isError.value = false
    }
}