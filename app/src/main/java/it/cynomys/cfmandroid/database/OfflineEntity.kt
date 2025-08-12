// OfflineEntities.kt
package it.cynomys.cfmandroid.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import it.cynomys.cfmandroid.farm.Species
import java.util.Date
import java.util.UUID

@Entity(tableName = "offline_owners")
data class OfflineOwner(
    @PrimaryKey val id: UUID,
    val name: String,
    val email: String,
    val lastSyncTime: Date = Date()
)

@Entity(
    tableName = "offline_farms",
    foreignKeys = [
        ForeignKey(
            entity = OfflineOwner::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId")]
)
data class OfflineFarm(
    @PrimaryKey val id: UUID,
    val ownerId: UUID,
    val name: String,
    val coordinateX: Double,
    val coordinateY: Double,
    val address: String,
    val area: Double,
    val species: Species,
    val lastSyncTime: Date = Date()
)

@Entity(
    tableName = "offline_devices",
    foreignKeys = [
        ForeignKey(
            entity = OfflineFarm::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("farmId"), Index("deviceID")]
)
data class OfflineDevice(
    @PrimaryKey val id: UUID,
    val ownerId: UUID, // <--- ADD THIS LINE
    val farmId: UUID,
    val penId: UUID,   // <--- ADD THIS LINE
    val deviceID: String,
    val displayName: String,
    val predictions: Boolean,
    val indexes: String,
    val lastSyncTime: Date = Date()
)

@Entity(
    tableName = "offline_sensor_data",
    foreignKeys = [
        ForeignKey(
            entity = OfflineDevice::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deviceId"), Index("timestamp")]
)
data class OfflineSensorData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: UUID,
    val sensorName: String,
    val value: Double,
    val unit: String?,
    val timestamp: Date,
    val lastSyncTime: Date = Date()
)


@Entity(
    tableName = "offline_silos",
    foreignKeys = [
        ForeignKey(
            entity = OfflineFarm::class,
            parentColumns = ["id"],
            childColumns = ["farmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("farmId")]
)
data class OfflineSilo(
    @PrimaryKey val id: UUID,
    val farmId: UUID,
    val name: String,
    val capacity: Double,
    val fillLevel: Double,
    val lastSyncTime: Date = Date()
)
