// FarmViewModelFactory.kt
package it.cynomys.cfmandroid.farm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FarmViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FarmViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }// Add this method to your OfflineRepository class

}