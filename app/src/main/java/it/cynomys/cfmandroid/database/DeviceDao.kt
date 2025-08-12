// DAOs
package it.cynomys.cfmandroid.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

@Dao
interface DeviceDao {
    @Query("SELECT * FROM offline_devices WHERE farmId = :farmId")
    fun getDevicesByFarmId(farmId: UUID): Flow<List<OfflineDevice>>

    @Query("SELECT * FROM offline_devices WHERE id = :deviceId")
    suspend fun getDeviceById(deviceId: UUID): OfflineDevice?

    @Query("SELECT * FROM offline_devices WHERE deviceID = :deviceID")
    suspend fun getDeviceByDeviceID(deviceID: String): OfflineDevice?

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Use REPLACE to update existing devices
    suspend fun insertDevice(device: OfflineDevice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<OfflineDevice>)

    @Update
    suspend fun updateDevice(device: OfflineDevice) // This might become redundant if insertDevice handles updates

    @Delete
    suspend fun deleteDevice(device: OfflineDevice)

    @Query("DELETE FROM offline_devices WHERE id = :deviceId")
    suspend fun deleteDeviceById(deviceId: UUID) // Add or ensure this exists

    @Query("SELECT * FROM offline_devices")
    suspend fun getAllDevices(): List<OfflineDevice>
}

@Dao
interface SensorDataDao {
    @Query("SELECT * FROM offline_sensor_data WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun getSensorDataByDeviceId(deviceId: UUID): Flow<List<OfflineSensorData>>

    @Query("SELECT * FROM offline_sensor_data WHERE deviceId = :deviceId AND sensorName = :sensorName ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestSensorData(deviceId: UUID, sensorName: String, limit: Int): List<OfflineSensorData>

    @Query("SELECT DISTINCT sensorName FROM offline_sensor_data WHERE deviceId = :deviceId")
    suspend fun getSensorNames(deviceId: UUID): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorData(sensorData: OfflineSensorData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorDataList(sensorDataList: List<OfflineSensorData>)

    @Query("DELETE FROM offline_sensor_data WHERE deviceId = :deviceId")
    suspend fun deleteSensorDataByDeviceId(deviceId: UUID)

    @Query("DELETE FROM offline_sensor_data WHERE timestamp < :cutoffDate")
    suspend fun deleteOldSensorData(cutoffDate: Date)
}

@Dao
interface FarmDao {
    @Query("SELECT * FROM offline_farms WHERE ownerId = :ownerId")
    fun getFarmsByOwnerId(ownerId: UUID): Flow<List<OfflineFarm>>

    @Query("SELECT * FROM offline_farms WHERE id = :farmId")
    suspend fun getFarmById(farmId: UUID): OfflineFarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: OfflineFarm)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarms(farms: List<OfflineFarm>)

    @Update
    suspend fun updateFarm(farm: OfflineFarm)

    @Delete
    suspend fun deleteFarm(farm: OfflineFarm)
    // ADD THIS NEW METHOD
    @Query("DELETE FROM offline_farms WHERE id = :farmId")
    suspend fun deleteFarmById(farmId: UUID)
}

@Dao
interface OwnerDao {
    @Query("SELECT * FROM offline_owners WHERE id = :ownerId")
    suspend fun getOwnerById(ownerId: UUID): OfflineOwner?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwner(owner: OfflineOwner)

    @Update
    suspend fun updateOwner(owner: OfflineOwner)

    @Delete
    suspend fun deleteOwner(owner: OfflineOwner)
}