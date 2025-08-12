package it.cynomys.cfmandroid.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import it.cynomys.cfmandroid.repository.OfflineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = OfflineRepository(applicationContext)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Clean old data first
                repository.cleanOldSensorData() // Calling the newly added method
                // Add your sync logic here
                // For example, sync pending data to server
                // syncPendingData()

                Result.success()
            } catch (exception: Exception) {
                Result.retry()
            }
        }
    }
}