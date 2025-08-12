// FarmViewModel.kt
package it.cynomys.cfmandroid.farm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.auth.Owner
import it.cynomys.cfmandroid.database.OfflineOwner
import it.cynomys.cfmandroid.repository.OfflineRepository
import it.cynomys.cfmandroid.repository.toOfflineFarm
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

// FarmViewModel now takes Context
class FarmViewModel(private val context: Context) : ViewModel() {
    private val networkService = NetworkService()
    private val offlineRepository = OfflineRepository(context) // Initialize repository with context

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
                ensureOwnerExistsLocally(ownerId) // Ensure owner exists before fetching farms

                if (offlineRepository.isOnline()) {
                    val result = networkService.get<Array<Farm>>(
                        path = "api/farm/$ownerId",
                        responseType = Array<Farm>::class.java
                    )

                    if (result.isSuccess) {
                        val networkFarms = result.getOrNull()?.toList() ?: emptyList()
                        // Cache network farms locally
                        offlineRepository.insertFarmsLocally(networkFarms.map { it.toOfflineFarm() })
                        _farms.value = offlineRepository.getFarmsByOwnerIdLocally(ownerId).first()
                    } else {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to load farms from network"
                        // Fallback to local data if network call fails
                        _farms.value = offlineRepository.getFarmsByOwnerIdLocally(ownerId).first()
                    }
                } else {
                    // Only load from local if offline
                    _farms.value = offlineRepository.getFarmsByOwnerIdLocally(ownerId).first()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load farms"
                _farms.value = offlineRepository.getFarmsByOwnerIdLocally(ownerId).first() // Ensure local load on exception
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun ensureOwnerExistsLocally(ownerId: UUID) {
        var offlineOwner = offlineRepository.getOwnerByIdLocally(ownerId)
        if (offlineOwner == null && offlineRepository.isOnline()) {
            try {
                val ownerResult = networkService.get<Owner>(
                    path = "api/owners/$ownerId",
                    responseType = Owner::class.java
                )
                if (ownerResult.isSuccess) {
                    val networkOwner = ownerResult.getOrNull()
                    networkOwner?.let {
                        offlineOwner = OfflineOwner(
                            id = it.id ?: UUID.randomUUID(),
                            name = it.name,
                            email = it.email,
                            lastSyncTime = Date()
                        )
                        offlineRepository.insertOwnerLocally(offlineOwner!!)
                    }
                } else {
                    Log.e("FarmViewModel", "Failed to fetch owner for placeholder: ${ownerResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("FarmViewModel", "Exception fetching owner for placeholder: ${e.message}")
            }
        }
        if (offlineOwner == null) {
            Log.w("FarmViewModel", "Owner with ID $ownerId not found. Creating placeholder.")
            offlineOwner = OfflineOwner(id = ownerId, name = "Unknown Owner", email = "", lastSyncTime = Date())
            offlineRepository.insertOwnerLocally(offlineOwner!!)
        }
    }


    fun deleteFarm(farmId: UUID, ownerId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (offlineRepository.isOnline()) {
                    val result = networkService.delete("api/farm/$farmId")
                    if (result.isSuccess) {
                        offlineRepository.deleteFarmLocally(farmId)
                        getFarms(ownerId)
                    } else {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to delete farm from network"
                        // Try to delete locally even if network fails, or revert? For now, assume local delete if network fails to synchronize
                    }
                } else {
                    // Offline delete only
                    offlineRepository.deleteFarmLocally(farmId)
                }
                getFarms(ownerId) // Refresh list
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete farm"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFarm(ownerId: UUID, farmDto: FarmDto) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (offlineRepository.isOnline()) {
                    val result = networkService.post<FarmDto, Farm>(
                        path = "api/farm",
                        body = farmDto,
                        responseType = Farm::class.java
                    )
                    if (result.isSuccess) {
                        val newFarm = result.getOrNull()
                        newFarm?.let {
                            offlineRepository.insertFarmLocally(it.toOfflineFarm())
                            getFarms(ownerId)
                        }
                    } else {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to add farm"
                    }
                } else {
                    _error.value = "Cannot add farm offline."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add farm"
            } finally {
                _isLoading.value = false
            }
        }
    }
}