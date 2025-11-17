// DeviceViewModel.kt
package it.cynomys.cfmandroid.device

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.auth.Owner
import it.cynomys.cfmandroid.database.OfflineFarm
import it.cynomys.cfmandroid.database.OfflineOwner
import it.cynomys.cfmandroid.farm.Farm
import it.cynomys.cfmandroid.farm.Species
import it.cynomys.cfmandroid.repository.OfflineRepository
import it.cynomys.cfmandroid.repository.toOfflineDevice
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class DeviceViewModel(private val context: Context) : ViewModel() {
    private val networkService = NetworkService()
    private val offlineRepository = OfflineRepository(context)

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedDevice = MutableStateFlow<Device?>(null)
    val selectedDevice: StateFlow<Device?> = _selectedDevice

    // New: for license loading
    private val _availableLicenses = MutableStateFlow<List<License>>(emptyList())
    val availableLicenses: StateFlow<List<License>> = _availableLicenses

    private val _isLoadingLicenses = MutableStateFlow(false)
    val isLoadingLicenses: StateFlow<Boolean> = _isLoadingLicenses

    fun getDevices(ownerId: UUID, farmId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                ensureOwnerAndFarmExistLocally(ownerId, farmId)

                if (offlineRepository.isOnline()) {
                    val result = networkService.get<Array<Device>>(
                        path = "api/devices/$ownerId/$farmId",
                        responseType = Array<Device>::class.java
                    )
                    if (result.isSuccess) {
                        val deviceList = result.getOrNull()?.toList() ?: emptyList()
                        val offlineDevicesToInsert = deviceList.map {
                            it.toOfflineDevice(
                                ownerId = ownerId,
                                farmId = farmId,
                                penId = it.penId ?: UUID.randomUUID()
                            )
                        }
                        offlineRepository.insertDevicesLocally(offlineDevicesToInsert)
                        _devices.value = offlineRepository.getDevicesByFarmIdLocally(farmId).first()
                    } else {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to load devices from network"
                        _devices.value = offlineRepository.getDevicesByFarmIdLocally(farmId).first()
                    }
                } else {
                    _devices.value = offlineRepository.getDevicesByFarmIdLocally(farmId).first()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load devices"
                _devices.value = offlineRepository.getDevicesByFarmIdLocally(farmId).first()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getDeviceByDeviceId(deviceID: String, ownerId: UUID, farmId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedDevice.value = null

            try {
                ensureOwnerAndFarmExistLocally(ownerId, farmId)

                if (offlineRepository.isOnline()) {
                    val result = networkService.get<Device>(
                        path = "api/devices/getById/$deviceID",
                        responseType = Device::class.java
                    )
                    if (result.isSuccess) {
                        val device = result.getOrNull()
                        device?.let {
                            val offlineDevice = it.toOfflineDevice(
                                ownerId = ownerId,
                                farmId = farmId,
                                penId = it.penId ?: UUID.randomUUID()
                            )
                            offlineRepository.insertDeviceLocally(offlineDevice)
                            _selectedDevice.value = it
                        }
                    } else {
                        Log.e("DeviceViewModel", "Failed to load device from network: ${result.exceptionOrNull()?.message}")
                        _selectedDevice.value = offlineRepository.getDeviceByDeviceIDLocally(deviceID)
                        if (_selectedDevice.value == null) {
                            _error.value = "Device not found: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                        }
                    }
                } else {
                    _selectedDevice.value = offlineRepository.getDeviceByDeviceIDLocally(deviceID)
                    if (_selectedDevice.value == null) {
                        _error.value = "Device not found offline."
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load device details"
                _selectedDevice.value = offlineRepository.getDeviceByDeviceIDLocally(deviceID)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // New: Load available licenses for the owner
    fun loadAvailableLicenses(ownerEmail: String) {
        viewModelScope.launch {
            _isLoadingLicenses.value = true
            _error.value = null
            try {
                // 1. Get all contracts
                val contractsResult = networkService.get<Array<Contract>>(
                    path = "api/contract/getAllContracts",
                    responseType = Array<Contract>::class.java
                )

                if (contractsResult.isSuccess) {
                    val contracts = contractsResult.getOrNull()?.toList() ?: emptyList()

                    // 2. Filter contracts where DashboardUser matches owner email
                    val matchingContracts = contracts.filter { contract ->
                        // Use the Elvis operator (?:) to default to an empty string ("") if DashboardUser is null
                        val dashboardUsers = contract.contractResponseDto.DashboardUser ?: ""

                        // Split the DashboardUser string by comma, trim whitespace, and check if it contains the ownerEmail
                        dashboardUsers
                            .split(',')
                            .map { it.trim() }
                            .contains(ownerEmail)
                    }
                    // 3. Extract all licenses from matching contracts
                    val allLicenses = matchingContracts.flatMap { it.licenses }

                    // 4. Filter for device licenses only (type == "device")
                    val deviceLicenses = allLicenses.filter { it.type == "device" }

                    _availableLicenses.value = deviceLicenses

                    Log.d("DeviceViewModel", "Loaded ${deviceLicenses.size} device licenses for $ownerEmail")
                } else {
                    _error.value = "Failed to load licenses: ${contractsResult.exceptionOrNull()?.message}"
                    _availableLicenses.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to load licenses: ${e.message}"
                _availableLicenses.value = emptyList()
                Log.e("DeviceViewModel", "Exception loading licenses", e)
            } finally {
                _isLoadingLicenses.value = false
            }
        }
    }

    private suspend fun ensureOwnerAndFarmExistLocally(ownerId: UUID, farmId: UUID) {
        val networkService = NetworkService()

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
                    Log.e("DeviceViewModel", "Failed to fetch owner for placeholder: ${ownerResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("DeviceViewModel", "Exception fetching owner for placeholder: ${e.message}")
            }
        }
        if (offlineOwner == null) {
            Log.w("DeviceViewModel", "Owner with ID $ownerId not found. Creating placeholder.")
            offlineOwner = OfflineOwner(id = ownerId, name = "Unknown Owner", email = "", lastSyncTime = Date())
            offlineRepository.insertOwnerLocally(offlineOwner!!)
        }

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
                    Log.e("DeviceViewModel", "Failed to fetch farm for placeholder: ${farmResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("DeviceViewModel", "Exception fetching farm for placeholder: ${e.message}")
            }
        }
        if (offlineFarm == null) {
            Log.w("DeviceViewModel", "Farm with ID $farmId not found. Creating placeholder.")
            offlineFarm = OfflineFarm(
                id = farmId,
                ownerId = ownerId,
                name = "Unknown Farm",
                coordinateX = 0.0,
                coordinateY = 0.0,
                address = "Unknown",
                area = 0.0,
                species = Species.OTHER,
                lastSyncTime = Date()
            )
            offlineRepository.insertFarmLocally(offlineFarm!!)
        }
    }

    fun addDevice(ownerId: UUID, farmId: UUID, deviceDto: DeviceDto) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = networkService.post<DeviceDto, Device>(
                    path = "api/devices/$ownerId/$farmId",
                    body = deviceDto,
                    responseType = Device::class.java
                )
                if (result.isSuccess) {
                    val newDevice = result.getOrNull()
                    newDevice?.let {
                        val offlineDevice = it.toOfflineDevice(
                            ownerId = ownerId,
                            farmId = farmId,
                            penId = it.penId ?: UUID.randomUUID()
                        )
                        offlineRepository.insertDeviceLocally(offlineDevice)
                        getDevices(ownerId, farmId)
                    }
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to add device"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add device"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDevice(device: Device, ownerId: UUID, farmId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                device.id?.let { deviceId ->
                    val result = networkService.put<Device, Device>(
                        path = "api/devices/$deviceId",
                        body = device,
                        responseType = Device::class.java
                    )
                    if (result.isSuccess) {
                        val updatedDevice = result.getOrNull()
                        updatedDevice?.let {
                            val offlineDevice = it.toOfflineDevice(
                                ownerId = it.ownerId ?: UUID.randomUUID(),
                                farmId = it.farmId ?: UUID.randomUUID(),
                                penId = it.penId ?: UUID.randomUUID()
                            )
                            offlineRepository.updateDeviceLocally(offlineDevice)
                        }
                    } else {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to update device"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update device"
            } finally {
                _isLoading.value = false
            }
        }
    }





    // UPDATED: Update Device using the specific edit endpoint
    fun updateDevice(updateDto: DeviceUpdateDto, ownerId: UUID, farmId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = networkService.put<DeviceUpdateDto, Device>(
                    path = "api/devices/edit", // Updated endpoint
                    body = updateDto,
                    responseType = Device::class.java
                )
                if (result.isSuccess) {
                    val updatedDevice = result.getOrNull()
                    updatedDevice?.let {
                        val offlineDevice = it.toOfflineDevice(
                            ownerId = ownerId, // Use passed IDs as response might not have them populated
                            farmId = farmId,
                            penId = it.penId ?: UUID.randomUUID()
                        )
                        offlineRepository.updateDeviceLocally(offlineDevice)
                        // Refresh list
                        getDevices(ownerId, farmId)
                    }
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to update device"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update device"
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun deleteDevice(deviceId: UUID, ownerId: UUID, farmId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = networkService.delete("api/devices/$deviceId")
                if (result.isSuccess) {
                    offlineRepository.deleteDeviceLocally(deviceId)
                    getDevices(ownerId, farmId)
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to delete device"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete device"
            } finally {
                _isLoading.value = false
            }
        }
    }
}