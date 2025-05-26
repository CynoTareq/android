package it.cynomys.cfmandroid.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.model.Device
import it.cynomys.cfmandroid.model.DeviceDto
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DeviceViewModel : ViewModel() {
    private val networkService = NetworkService()
    
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices
    
    private val _selectedDevice = MutableStateFlow<Device?>(null)
    val selectedDevice: StateFlow<Device?> = _selectedDevice
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun getDevices(ownerId: UUID, farmId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = networkService.get<Array<Device>>(
                    path = "api/devices/$ownerId/$farmId",
                    responseType = Array<Device>::class.java
                )
                if (result.isSuccess) {
                    val deviceList = result.getOrNull()?.toList() ?: emptyList()
                    deviceList.forEach {
                        Log.d("DeviceViewModel", "Loaded device: id=${it.id}, deviceID=${it.deviceID}")
                    }
                    _devices.value = deviceList
                }
                else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load devices"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load devices"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getDeviceById(deviceId: UUID) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = networkService.get<Device>(
                    path = "api/devices/getById/$deviceId",
                    responseType = Device::class.java
                )
                if (result.isSuccess) {
                    _selectedDevice.value = result.getOrNull()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Device not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load device"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createDevice(ownerId: UUID, farmId: UUID, deviceDto: DeviceDto) {
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
                    getDevices(ownerId, farmId) // Refresh list
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to create device"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create device"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateDevice(deviceDto: DeviceDto) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = networkService.put<DeviceDto, Device>(
                    path = "api/devices/edit",
                    body = deviceDto,
                    responseType = Device::class.java
                )
                if (result.isSuccess) {
                    _selectedDevice.value = result.getOrNull()
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
                    getDevices(ownerId, farmId) // Refresh list
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