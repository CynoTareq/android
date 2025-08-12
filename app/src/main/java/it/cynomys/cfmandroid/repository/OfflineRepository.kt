// OfflineRepository.kt
package it.cynomys.cfmandroid.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import it.cynomys.cfmandroid.database.OfflineDatabase
import it.cynomys.cfmandroid.database.OfflineDevice
import it.cynomys.cfmandroid.database.OfflineFarm
import it.cynomys.cfmandroid.database.OfflineOwner
import it.cynomys.cfmandroid.database.OfflineSensorData
import it.cynomys.cfmandroid.database.OfflineSilo // THIS IS THE CORRECT IMPORT FOR THE @Entity
import it.cynomys.cfmandroid.device.Device
import it.cynomys.cfmandroid.farm.Farm
import it.cynomys.cfmandroid.silo.Silo // This is your domain model Silo
import it.cynomys.cfmandroid.silo.SiloModel
import it.cynomys.cfmandroid.silo.SiloShape
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import java.util.UUID

class OfflineRepository(private val context: Context) {
    private val database = OfflineDatabase.getDatabase(context)
    private val networkService = NetworkService()

    private val deviceDao = database.deviceDao()
    private val sensorDataDao = database.sensorDataDao()
    private val farmDao = database.farmDao()
    private val ownerDao = database.ownerDao()
    private val siloDao = database.siloDao() // Initialize SiloDao

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    // --- Device-related methods (existing) ---
    fun getDevicesByFarmIdLocally(farmId: UUID): Flow<List<Device>> {
        return deviceDao.getDevicesByFarmId(farmId).map { offlineDevices ->
            offlineDevices.map { it.toDevice() }
        }
    }

    suspend fun getDeviceByIdLocally(deviceId: UUID): Device? {
        return deviceDao.getDeviceById(deviceId)?.toDevice()
    }

    suspend fun getDeviceByDeviceIDLocally(deviceID: String): Device? {
        return deviceDao.getDeviceByDeviceID(deviceID)?.toDevice()
    }

    suspend fun insertDeviceLocally(device: OfflineDevice) {
        deviceDao.insertDevice(device)
    }

    suspend fun insertDevicesLocally(devices: List<OfflineDevice>) {
        deviceDao.insertDevices(devices)
    }

    suspend fun updateDeviceLocally(device: OfflineDevice) {
        deviceDao.updateDevice(device)
    }

    suspend fun deleteDeviceLocally(deviceId: UUID) {
        deviceDao.deleteDeviceById(deviceId)
    }

    // --- Sensor Data methods (existing) ---
    fun getSensorDataByDeviceIdLocally(deviceId: UUID): Flow<List<OfflineSensorData>> {
        return sensorDataDao.getSensorDataByDeviceId(deviceId)
    }

    suspend fun insertSensorDataLocally(sensorData: OfflineSensorData) {
        sensorDataDao.insertSensorData(sensorData)
    }

    suspend fun insertSensorDataListLocally(sensorDataList: List<OfflineSensorData>) {
        sensorDataDao.insertSensorDataList(sensorDataList)
    }

    // NEW: Method to clean old sensor data
    suspend fun cleanOldSensorData(daysAgo: Int = 30) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val cutoffDate = calendar.time
        sensorDataDao.deleteOldSensorData(cutoffDate)
    }

    // --- Owner-related methods (existing) ---
    suspend fun getOwnerByIdLocally(ownerId: UUID): OfflineOwner? {
        return ownerDao.getOwnerById(ownerId)
    }

    suspend fun insertOwnerLocally(owner: OfflineOwner) {
        ownerDao.insertOwner(owner)
    }

    // --- Farm-related methods (existing) ---
    suspend fun getFarmByIdLocally(farmId: UUID): OfflineFarm? {
        return farmDao.getFarmById(farmId)
    }

    suspend fun insertFarmLocally(farm: OfflineFarm) {
        farmDao.insertFarm(farm)
    }

    suspend fun insertFarmsLocally(farms: List<OfflineFarm>) {
        farmDao.insertFarms(farms)
    }

    suspend fun deleteFarmLocally(farmId: UUID) {
        farmDao.deleteFarm(farmDao.getFarmById(farmId)!!)
    }

    fun getFarmsByOwnerIdLocally(ownerId: UUID): Flow<List<it.cynomys.cfmandroid.farm.Farm>> {
        return farmDao.getFarmsByOwnerId(ownerId).map { offlineFarms ->
            offlineFarms.map { it.toFarm() }
        }
    }

    // --- Silo-related methods ---
    fun getSilosByFarmIdLocally(farmId: UUID): Flow<List<Silo>> {
        return siloDao.getSilosByFarmId(farmId).map { offlineSilos ->
            offlineSilos.map { it.toSilo() }
        }
    }

    suspend fun getSiloByIdLocally(siloId: UUID): Silo? {
        return siloDao.getSiloById(siloId)?.toSilo()
    }

    // The parameters here should be it.cynomys.cfmandroid.database.OfflineSilo
    // The import at the top `import it.cynomys.cfmandroid.database.OfflineSilo` handles this.
    suspend fun insertSiloLocally(silo: OfflineSilo) { // Corrected type to database.OfflineSilo
        siloDao.insertSilo(silo)
    }

    suspend fun insertSilosLocally(silos: List<OfflineSilo>) { // Corrected type to List<database.OfflineSilo>
        siloDao.insertSilos(silos)
    }

    suspend fun updateSiloLocally(silo: OfflineSilo) { // Corrected type to database.OfflineSilo
        siloDao.updateSilo(silo)
    }

    suspend fun deleteSiloLocally(siloId: UUID) {
        siloDao.deleteSiloById(siloId)
    }
}

/**
 * Extension functions to convert between online and offline entities
 * IMPORTANT: Ensure Device data class includes ownerId, farmId, and penId
 * based on your OfflineDevice entity.
 */
fun it.cynomys.cfmandroid.device.Device.toOfflineDevice(ownerId: UUID, farmId: UUID, penId: UUID): OfflineDevice {
    return OfflineDevice(
        id = this.id,
        ownerId = ownerId,
        farmId = farmId,
        penId = penId,
        deviceID = this.deviceID,
        displayName = this.displayName,
        predictions = this.predictions,
        indexes = this.indexes,
        lastSyncTime = Date() // Set current sync time
    )
}

fun OfflineDevice.toDevice(): it.cynomys.cfmandroid.device.Device {
    return it.cynomys.cfmandroid.device.Device(
        id = this.id,
        deviceID = this.deviceID,
        displayName = this.displayName,
        predictions = this.predictions,
        indexes = this.indexes,
        ownerId = this.ownerId,
        farmId = this.farmId,
        penId = this.penId
    )
}

// Extension function to convert Farm to OfflineFarm
fun Farm.toOfflineFarm(): OfflineFarm {
    return OfflineFarm(
        id = this.id ?: UUID.randomUUID(), // Provide a default if id is nullable
        ownerId = this.ownerId,
        name = this.name,
        address = this.address,
        coordinateX = this.coordinateX,
        coordinateY = this.coordinateY,
        area = this.area,
        species = this.species,
        lastSyncTime = Date()
    )
}

// Extension function to convert OfflineFarm to Farm
fun OfflineFarm.toFarm(): Farm {
    return Farm(
        id = this.id,
        ownerId = this.ownerId,
        name = this.name,
        coordinateX = this.coordinateX,
        coordinateY = this.coordinateY,
        address = this.address,
        area = this.area,
        species = this.species
    )
}

// Extension function to convert Silo to OfflineSilo
// This function takes it.cynomys.cfmandroid.silo.Silo and returns it.cynomys.cfmandroid.database.OfflineSilo
fun Silo.toOfflineSilo(ownerId: UUID, farmId: UUID, penId: UUID): it.cynomys.cfmandroid.database.OfflineSilo {
    return it.cynomys.cfmandroid.database.OfflineSilo( // Explicitly specify the package for OfflineSilo
        id = this.id ?: UUID.randomUUID(),
        farmId = farmId,
        name = this.displayName, // Assuming displayName maps to name in OfflineSilo
        capacity = this.silosHeight * this.silosDiameter, // Placeholder conversion, adjust as needed
        fillLevel = 0.0, // Default fill level, adjust as needed
        lastSyncTime = Date()
    )
}

// Extension function to convert OfflineSilo to Silo
// This function takes it.cynomys.cfmandroid.database.OfflineSilo and returns it.cynomys.cfmandroid.silo.Silo
fun it.cynomys.cfmandroid.database.OfflineSilo.toSilo(): Silo {
    return Silo(
        id = this.id,
        silosID = this.id.toString(), // Assuming silosID is String and can be derived from UUID
        displayName = this.name,
        silosHeight = this.capacity, // Placeholder, adjust based on your model
        silosDiameter = 0.0, // Placeholder
        coneHeight = 0.0, // Placeholder
        bottomDiameter = 0.0, // Placeholder
        shape = SiloShape.FULL_CYLINDRICAL, // Placeholder, actual shape from DB if stored
        penId = null, // Placeholder
        farmId = this.farmId,
        ownerId = null, // Placeholder
        model = SiloModel("", ""), // Placeholder
        material_name = "", // Placeholder
        material_density = 0.0,
        lastSyncTime = Date()
    )
}