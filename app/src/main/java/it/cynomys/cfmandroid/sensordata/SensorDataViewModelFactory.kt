package it.cynomys.cfmandroid.sensordata

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SensorDataViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensorDataViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}