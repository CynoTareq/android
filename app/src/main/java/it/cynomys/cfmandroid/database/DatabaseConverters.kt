// DatabaseConverters.kt
package it.cynomys.cfmandroid.database

import androidx.room.TypeConverter
import it.cynomys.cfmandroid.farm.Species
import java.util.Date
import java.util.UUID

class DatabaseConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuidString: String?): UUID? {
        return try {
            UUID.fromString(uuidString)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    // NEW: Type Converters for Species enum
    @TypeConverter
    fun fromSpecies(value: Species?): String? {
        return value?.name
    }

    @TypeConverter
    fun toSpecies(value: String?): Species? {
        return value?.let { Species.valueOf(it) }
    }
}