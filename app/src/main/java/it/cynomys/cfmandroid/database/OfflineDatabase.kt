package it.cynomys.cfmandroid.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        OfflineDevice::class,
        OfflineSensorData::class,
        OfflineFarm::class,
        OfflineOwner::class,
        OfflineSilo::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class OfflineDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun sensorDataDao(): SensorDataDao
    abstract fun farmDao(): FarmDao
    abstract fun ownerDao(): OwnerDao
    abstract fun siloDao(): SiloDao

    companion object {
        @Volatile
        private var INSTANCE: OfflineDatabase? = null

        fun getDatabase(context: Context): OfflineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDatabase::class.java,
                    "cfm_offline_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}