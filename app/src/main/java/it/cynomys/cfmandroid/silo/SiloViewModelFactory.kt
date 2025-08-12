package it.cynomys.cfmandroid.silo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Factory class for creating instances of SiloViewModel
class SiloViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `ViewModel` class.
     *
     * @param modelClass The `Class` of the `ViewModel` to create.
     * @param <T> The type of the `ViewModel`.
     * @return A new instance of the `ViewModel`.
     * @throws IllegalArgumentException If the `modelClass` is not assignable from `SiloViewModel`.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is assignable from SiloViewModel
        if (modelClass.isAssignableFrom(SiloViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Return a new instance of SiloViewModel, passing the application context
            return SiloViewModel(context) as T
        }
        // If the ViewModel class is not supported, throw an exception
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
