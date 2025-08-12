// SiloDao.kt
package it.cynomys.cfmandroid.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SiloDao {
    @Query("SELECT * FROM offline_silos WHERE farmId = :farmId")
    fun getSilosByFarmId(farmId: UUID): Flow<List<OfflineSilo>>

    @Query("SELECT * FROM offline_silos WHERE id = :siloId")
    suspend fun getSiloById(siloId: UUID): OfflineSilo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSilo(silo: OfflineSilo) // Correct: no explicit package here

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSilos(silos: List<OfflineSilo>) // Correct: no explicit package here

    @Update
    suspend fun updateSilo(silo: OfflineSilo) // Correct: no explicit package here

    @Delete
    suspend fun deleteSilo(silo: OfflineSilo)

    @Query("DELETE FROM offline_silos WHERE id = :siloId")
    suspend fun deleteSiloById(siloId: UUID)
}