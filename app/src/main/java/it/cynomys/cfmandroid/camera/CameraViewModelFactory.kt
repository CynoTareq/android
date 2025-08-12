// CameraViewModelFactory.kt
package it.cynomys.cfmandroid.camera

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CameraViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel() as T // Corrected: Removed the 'context' argument
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}