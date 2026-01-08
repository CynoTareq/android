package it.cynomys.cfmandroid.silo

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.auth.Owner
import it.cynomys.cfmandroid.database.OfflineFarm
import it.cynomys.cfmandroid.database.OfflineOwner
import it.cynomys.cfmandroid.device.Contract
import it.cynomys.cfmandroid.device.License
import it.cynomys.cfmandroid.farm.Farm
import it.cynomys.cfmandroid.farm.Species
import it.cynomys.cfmandroid.repository.OfflineRepository
import it.cynomys.cfmandroid.repository.toOfflineSilo
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class SiloViewModel(private val context: Context) : ViewModel() {
    private val networkService = NetworkService()
    private val offlineRepository = OfflineRepository(context)

    // StateFlow to hold the list of silos
    private val _silos = MutableStateFlow<List<Silo>>(emptyList())
    val silos: StateFlow<List<Silo>> = _silos

    // StateFlow to indicate loading status
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // StateFlow to hold any error messages
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow() // Expose as StateFlow
    // StateFlow to hold the currently selected silo for detail/edit views
    private val _selectedSilo = MutableStateFlow<Silo?>(null)
    val selectedSilo: StateFlow<Silo?> = _selectedSilo

    // NEW: StateFlow to hold available silo licenses
    private val _siloLicenses = MutableStateFlow<List<License>>(emptyList())
    val siloLicenses: StateFlow<List<License>> = _siloLicenses.asStateFlow()


    private val _siloLevels = MutableStateFlow<Map<String, Float>>(emptyMap())
    val siloLevels: StateFlow<Map<String, Float>> = _siloLevels

    /**
     * Fetches all silos for a given owner and farm.
     * It first tries to fetch from the network if online, then caches locally.
     * If offline or network fetch fails, it retrieves from local storage.
     *
     * @param ownerId The UUID of the owner.
     * @param farmId The UUID of the farm.

     */




    // New StateFlow to hold the list of available materials
    private val _materials = MutableStateFlow<List<SiloMaterial>>(emptyList())
    val materials: StateFlow<List<SiloMaterial>> = _materials.asStateFlow()

    // Function to fetch the material list
    fun fetchSiloMaterials() {
        if (_materials.value.isNotEmpty()) return // Avoid re-fetching if already loaded

        viewModelScope.launch {
            try {
                // Assuming NetworkService.get can handle deserialization of the list
                val result = networkService.get<Array<SiloMaterial>>(
                    path = "/api/silosMaterialList", responseType = Array<SiloMaterial>::class.java)
                result.onSuccess {
                    _materials.value = it.toList()
                    _error.value = null
                }.onFailure { e ->
                    _error.value = "Failed to load materials: ${e.message}"
                    Log.e("SiloViewModel", "Failed to load materials", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to load materials: ${e.message}"
                Log.e("SiloViewModel", "Exception loading materials", e)
            }
        }
    }




    fun getSilos(ownerId: UUID, farmId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Ensure owner and farm exist locally before proceeding
                ensureOwnerAndFarmExistLocally(ownerId, farmId)

                if (offlineRepository.isOnline()) {
                    // Attempt to fetch from network
                    val result = networkService.get<Array<Silo>>(
                        path = "api/silos/$ownerId/$farmId",
                        responseType = Array<Silo>::class.java
                    )
                    if (result.isSuccess) {
                        val siloList = result.getOrNull()?.toList() ?: emptyList()
                        // Convert network silos to offline silos and insert/update locally
                        val offlineSilosToInsert = siloList.map {
                            it.toOfflineSilo(
                                ownerId = ownerId,
                                farmId = farmId,
                                penId = it.penId ?: UUID.randomUUID() // Provide a default or handle null
                            )
                        }
                        offlineRepository.insertSilosLocally(offlineSilosToInsert)
                        _silos.value = offlineRepository.getSilosByFarmIdLocally(farmId).first()
                        fetchSiloLevels(_silos.value)
                    } else {
                        // If network fetch fails, log error and try to load from local
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to load silos from network"
                        _silos.value = offlineRepository.getSilosByFarmIdLocally(farmId).first()
                    }
                } else {
                    // If offline, load directly from local storage
                    _silos.value = offlineRepository.getSilosByFarmIdLocally(farmId).first()
                }
            } catch (e: Exception) {
                // Catch any exceptions during the process and set error state
                _error.value = e.message ?: "Failed to load silos"
                _silos.value = offlineRepository.getSilosByFarmIdLocally(farmId).first() // Always try to populate from local
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetches a single silo by its ID.
     * It first tries to fetch from the network if online, then caches locally.
     * If offline or network fetch fails, it retrieves from local storage.
     *
     * @param siloId The UUID of the silo to fetch.
     */
    fun getSiloById(siloId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedSilo.value = null // Clear previous selection

            try {
                // No need to ensure owner/farm exist here, as we're fetching a specific silo by ID.
                // The silo object itself should contain ownerId and farmId.

                if (offlineRepository.isOnline()) {
                    // Attempt to fetch from network first
                    val result = networkService.get<Silo>(
                        path = "api/silos/getById/$siloId",
                        responseType = Silo::class.java
                    )
                    if (result.isSuccess) {
                        val silo = result.getOrNull()
                        silo?.let {
                            // Cache the fetched silo locally
                            val offlineSilo = it.toOfflineSilo(
                                ownerId = it.ownerId ?: throw IllegalArgumentException("Owner ID cannot be null"),
                                farmId = it.farmId ?: throw IllegalArgumentException("Farm ID cannot be null"),
                                penId = it.penId ?: UUID.randomUUID() // Provide a default or handle null
                            )
                            offlineRepository.insertSiloLocally(offlineSilo) // Insert or update
                            _selectedSilo.value = it
                        }
                    } else {
                        Log.e("SiloViewModel", "Failed to load silo from network: ${result.exceptionOrNull()?.message}")
                        // Fallback to local if network fails
                        _selectedSilo.value = offlineRepository.getSiloByIdLocally(siloId)
                        if (_selectedSilo.value == null) {
                            _error.value = "Silo not found: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                        }
                    }
                } else {
                    // Only fetch from local if offline
                    _selectedSilo.value = offlineRepository.getSiloByIdLocally(siloId)
                    if (_selectedSilo.value == null) {
                        _error.value = "Silo not found offline."
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load silo details"
                _selectedSilo.value = offlineRepository.getSiloByIdLocally(siloId) // Try local as fallback
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Adds a new silo to the system.
     *
     * @param ownerId The UUID of the owner.
     * @param farmId The UUID of the farm.
     * @param siloDto The SiloDto containing the new silo's data.
     */
    fun addSilo(ownerId: UUID, farmId: UUID, siloDto: SiloDto) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = networkService.post<SiloDto, Silo>(
                    path = "api/silos/$ownerId/$farmId", // Corrected API path for POST
                    body = siloDto,
                    responseType = Silo::class.java
                )
                if (result.isSuccess) {
                    val newSilo = result.getOrNull()
                    newSilo?.let {
                        // Cache the newly added silo locally
                        val offlineSilo = it.toOfflineSilo(
                            ownerId = ownerId,
                            farmId = farmId,
                            penId = siloDto.penId // Use penId from DTO
                        )
                        offlineRepository.insertSiloLocally(offlineSilo)
                        getSilos(ownerId, farmId) // Refresh the list of silos
                    }
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to add silo"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add silo"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates an existing silo.
     *
     * @param silo The Silo object with updated data.
     * @param ownerId The UUID of the owner.
     * @param farmId The UUID of the farm.
     */
    fun updateSilo(silo: Silo, ownerId: UUID, farmId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                silo.id?.let { siloId ->
                    val result = networkService.put<Silo, Silo>(
                        path = "api/silos/edit", // Corrected API path for PUT (as per /api/silos/edit)
                        body = silo,
                        responseType = Silo::class.java
                    )
                    if (result.isSuccess) {
                        val updatedSilo = result.getOrNull()
                        updatedSilo?.let {
                            // Update the cached silo locally
                            val offlineSilo = it.toOfflineSilo(
                                ownerId = it.ownerId ?: ownerId, // Use existing or provided ownerId
                                farmId = it.farmId ?: farmId,    // Use existing or provided farmId
                                penId = it.penId ?: UUID.randomUUID() // Use existing or provided penId
                            )
                            offlineRepository.updateSiloLocally(offlineSilo)
                        }
                    } else {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to update silo"
                    }
                } ?: run {
                    _error.value = "Silo ID is null, cannot update."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update silo"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes a silo by its ID.
     *
     * @param siloId The UUID of the silo to delete.
     * @param ownerId The UUID of the owner (needed for refreshing the list after deletion).
     * @param farmId The UUID of the farm (needed for refreshing the list after deletion).
     */
    fun deleteSilo(siloId: UUID, ownerId: UUID, farmId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = networkService.delete("api/silos/$siloId") // Corrected API path for DELETE
                if (result.isSuccess) {
                    offlineRepository.deleteSiloLocally(siloId) // Delete from local cache
                    getSilos(ownerId, farmId) // Refresh the list of silos
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to delete silo"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete silo"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Ensures that the owner and farm exist in the local offline repository.
     * If they don't exist and the app is online, it attempts to fetch them from the network
     * and store them locally. If offline or network fetch fails, it creates placeholder entries.
     * This is crucial for maintaining data integrity when associating silos with owners/farms.
     *
     * @param ownerId The UUID of the owner.
     * @param farmId The UUID of the farm.
     */
    private suspend fun ensureOwnerAndFarmExistLocally(ownerId: UUID, farmId: UUID) {
        val networkService = NetworkService()

        // Check and ensure Owner
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
                    Log.e("SiloViewModel", "Failed to fetch owner for placeholder: ${ownerResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("SiloViewModel", "Exception fetching owner for placeholder: ${e.message}")
            }
        }
        if (offlineOwner == null) {
            Log.w("SiloViewModel", "Owner with ID $ownerId not found. Creating placeholder.")
            offlineOwner = OfflineOwner(id = ownerId, name = "Unknown Owner", email = "", lastSyncTime = Date())
            offlineRepository.insertOwnerLocally(offlineOwner!!)
        }

        // Check and ensure Farm
        var offlineFarm = offlineRepository.getFarmByIdLocally(farmId)
        if (offlineFarm == null && offlineRepository.isOnline()) {
            try {
                val farmResult = networkService.get<Farm>(
                    path = "api/farm/$farmId",
                    responseType = Farm::class.java
                )
                if (farmResult.isSuccess) {
                    val networkFarm = farmResult.getOrNull()
                    networkFarm?.let {
                        offlineFarm = OfflineFarm(
                            id = it.id ?: UUID.randomUUID(),
                            ownerId = it.ownerId,
                            name = it.name,
                            coordinateX = it.coordinateX,
                            coordinateY = it.coordinateY,
                            address = it.address,
                            area = it.area,
                            species = it.species,
                            lastSyncTime = Date()
                        )
                        offlineRepository.insertFarmLocally(offlineFarm!!)
                    }
                } else {
                    Log.e("SiloViewModel", "Failed to fetch farm for placeholder: ${farmResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("SiloViewModel", "Exception fetching farm for placeholder: ${e.message}")
            }
        }
        if (offlineFarm == null) {
            Log.w("SiloViewModel", "Farm with ID $farmId not found. Creating placeholder.")
            offlineFarm = OfflineFarm(
                id = farmId,
                ownerId = ownerId,
                name = "Unknown Farm",
                coordinateX = 0.0,
                coordinateY = 0.0,
                address = "Unknown",
                area = 0.0,
                species = Species.OTHER, // Assuming Species.OTHER is a valid default
                lastSyncTime = Date()
            )
            offlineRepository.insertFarmLocally(offlineFarm!!)
        }
    }





    fun loadAvailableLicenses(ownerEmail: String) {
        if (_siloLicenses.value.isNotEmpty()) return // Avoid re-fetching if already loaded

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Assuming the API path to fetch contracts is similar to other entities
                val result = networkService.get<Array<Contract>>(
                    path = "api/contract/getAllContracts",
                    responseType = Array<Contract>::class.java
                )

                if (result.isSuccess) {
                    val contracts = result.getOrNull()?.toList() ?: emptyList()
                    val allLicenses = contracts.flatMap { it.licenses }
                    // Filter licenses for silo use (case-insensitive check)
                    val siloLicenses = allLicenses.filter { it.type.lowercase() == "silo" }
                    _siloLicenses.value = siloLicenses
                    Log.e("List of silo licenses ",siloLicenses.toString())
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load licenses."
                    Log.e("SiloViewModel", "Failed to load licenses: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _error.value = "Exception loading licenses: ${e.message}"
                Log.e("SiloViewModel", "Exception loading licenses", e)
            } finally {
                _isLoading.value = false
            }
        }
    }





    private fun fetchSiloLevels(silos: List<Silo>) {
        viewModelScope.launch {
            val levels = mutableMapOf<String, Float>()

            silos.forEach { silo ->
                try {
                    val result = networkService.get<Map<String, Any>>(
                        path = "api/telemetry/latest/${silo.silosID}?isUICall=true",
                        responseType = Map::class.java as Class<Map<String, Any>>
                    )

                    if (result.isSuccess) {
                        val distance = (result.getOrNull()
                            ?.get("distance1") as? List<*>)
                            ?.firstOrNull()
                            ?.let { it as? Map<*, *> }
                            ?.get("value")
                            ?.toString()
                            ?.toDoubleOrNull()

                        if (distance != null) {
                            val level = calculateLevel(
                                distance = distance,
                                siloHeight = silo.silosHeight
                            )
                            levels[silo.silosID] = level
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SiloViewModel", "Failed telemetry for ${silo.silosID}", e)
                }
            }

            _siloLevels.value = levels
        }
    }


}
