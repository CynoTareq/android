package it.cynomys.cfmandroid.farm

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.auth.Owner
import it.cynomys.cfmandroid.database.OfflineOwner
import it.cynomys.cfmandroid.repository.OfflineRepository
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import java.util.UUID

class FarmViewModel(private val context: Context) : ViewModel() {

    private val networkService = NetworkService()
    private val offlineRepository = OfflineRepository(context)

    private val _farms = MutableStateFlow<List<Farm>>(emptyList())
    val farms: StateFlow<List<Farm>> = _farms

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var searchJob: Job? = null

    private val _addressSuggestions = MutableStateFlow<List<AddressSuggestion>>(emptyList())
    val addressSuggestions: StateFlow<List<AddressSuggestion>> = _addressSuggestions

    /* -------------------- Farms -------------------- */

    fun refreshFarms(ownerId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                ensureOwnerExistsLocally(ownerId)

                if (offlineRepository.isOnline()) {
                    val result = networkService.get<Array<Farm>>(
                        path = "api/farm/$ownerId",
                        responseType = Array<Farm>::class.java
                    )

                    if (result.isSuccess) {
                        offlineRepository.replaceFarmsForOwner(
                            ownerId,
                            result.getOrNull()?.toList() ?: emptyList()
                        )
                    }
                }

                _farms.value =
                    offlineRepository.getFarmsByOwnerIdLocally(ownerId).first()

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load farms"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFarm(farmId: UUID, ownerId: UUID) {
        viewModelScope.launch {
            try {
                if (offlineRepository.isOnline()) {
                    networkService.delete("api/farm/$farmId")
                }
                offlineRepository.deleteFarmLocally(farmId)
                refreshFarms(ownerId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addFarm(ownerId: UUID, farmDto: FarmDto) {
        viewModelScope.launch {
            try {
                if (!offlineRepository.isOnline()) {
                    _error.value = "Cannot add farm offline"
                    return@launch
                }

                val result = networkService.post<FarmDto, Farm>(
                    path = "api/farm/$ownerId",
                    body = farmDto,
                    responseType = Farm::class.java
                )

                if (result.isSuccess) {
                    refreshFarms(ownerId)
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /* -------------------- Owner -------------------- */

    private suspend fun ensureOwnerExistsLocally(ownerId: UUID) {
        var offlineOwner = offlineRepository.getOwnerByIdLocally(ownerId)

        if (offlineOwner == null && offlineRepository.isOnline()) {
            try {
                val ownerResult = networkService.get<Owner>(
                    path = "api/owners/$ownerId",
                    responseType = Owner::class.java
                )

                ownerResult.getOrNull()?.let {
                    offlineOwner = OfflineOwner(
                        id = it.id ?: ownerId,
                        name = it.name,
                        email = it.email,
                        lastSyncTime = Date()
                    )
                    offlineRepository.insertOwnerLocally(offlineOwner!!)
                }
            } catch (e: Exception) {
                Log.e("FarmViewModel", "Owner sync failed", e)
            }
        }

        if (offlineOwner == null) {
            offlineRepository.insertOwnerLocally(
                OfflineOwner(
                    id = ownerId,
                    name = "Unknown Owner",
                    email = "",
                    lastSyncTime = Date()
                )
            )
        }
    }

    /* -------------------- Address search (unchanged) -------------------- */

    fun searchAddress(query: String) {
        if (query.length < 2) {
            _addressSuggestions.value = emptyList()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = geocoder.getFromLocationName(query, 5)
                _addressSuggestions.value = results?.map {
                    AddressSuggestion(
                        displayName = it.getAddressLine(0),
                        lat = it.latitude,
                        lon = it.longitude
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                _addressSuggestions.value = emptyList()
            }
        }
    }

    fun clearSuggestions() {
        _addressSuggestions.value = emptyList()
    }



    fun getFarms(ownerId: UUID) {
        refreshFarms(ownerId)
    }

    /**
     * COMPATIBILITY METHOD
     * Used by AddEditFarmScreen
     */
    fun getAddressFromLocation(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val list = geocoder.getFromLocation(lat, lon, 1)
            list?.firstOrNull()?.getAddressLine(0) ?: "Lat: $lat, Lon: $lon"
        } catch (e: Exception) {
            "Lat: $lat, Lon: $lon"
        }
    }

    /**
     * REQUIRED for Edit Farm screen
     */
    fun updateFarm(farmId: UUID, farmDto: FarmDto) {
        viewModelScope.launch {
            try {
                if (!offlineRepository.isOnline()) {
                    _error.value = "Cannot update farm offline"
                    return@launch
                }

                val result = networkService.put<FarmDto, Farm>(
                    path = "api/farm/edit",
                    body = farmDto,
                    responseType = Farm::class.java
                )

                if (result.isSuccess) {
                    refreshFarms(farmDto.ownerId)
                } else {
                    _error.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }}
}
