package it.cynomys.cfmandroid.silo


import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import it.cynomys.cfmandroid.Screen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilosScreen(
    farmId: String?, // Farm ID as a string from navigation
    ownerId: UUID, // Owner ID as UUID
    penId: String?, // Pen ID as a string from navigation, now optional
    navController: NavController,
    onBack: () -> Unit // Callback for back navigation
) {
    // Obtain context within the @Composable scope
    val context = LocalContext.current

    // Obtain the ViewModel instance using the custom factory
    val siloViewModel: SiloViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SiloViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    // Pass the context obtained from LocalContext.current
                    return SiloViewModel(context) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    // Parse UUIDs safely outside the conditional rendering logic, using remember
    val farmUuid: UUID? = remember(farmId) {
        try {
            farmId?.let { UUID.fromString(it) }
        } catch (e: IllegalArgumentException) {
            null
        }
    }
    val penUuid: UUID? = remember(penId) {
        try {
            penId?.let { UUID.fromString(it) }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    // Trigger data loading when the screen is composed or farmId/penId changes
    LaunchedEffect(farmUuid, penUuid) {
        Log.d("SilosScreen", "This is silos screen +++++++++++++++++++++++++++++++++++++")
        if (farmUuid != null) {
            siloViewModel.getSilos(ownerId, farmId.let { UUID.fromString(it) }) // Pass nullable penId
        } else {
            // Handle error or display a message if farmId is missing
            siloViewModel.error
        }
    }

    // Collect states from ViewModel
    val isLoading by siloViewModel.isLoading.collectAsState()
    val error by siloViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Silos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // The arguments to SilosScreen are: farmId: String?, ownerId: UUID, penId: String?

                    if (farmId != null) {

                        val currentFarmId = farmId
                        val currentOwnerId = ownerId.toString()

                        // CRITICAL FIX: Check if the String? argument (penId) is the literal string "null" (case-insensitive).
                        // If it is, or if it's truly null/empty, set the value to Kotlin null.
                        // Otherwise, use the string value.
                        val sanitizedPenId = if (penId.isNullOrEmpty() || penId.equals("null", ignoreCase = true)) {
                            null // Pass a true Kotlin null
                        } else {
                            penId // Pass the valid ID string
                        }

                        IconButton(onClick = {
                            // Navigation call uses the sanitized value
                            navController.navigate(
                                Screen.SiloAdd.createRoute(
                                    farmId = currentFarmId,
                                    ownerId = currentOwnerId,
                                    penId = sanitizedPenId // Will be Kotlin null or a UUID string
                                )
                            )
                            Log.d("SilosScreen", "Add Silo clicked for farm: $currentFarmId, pen: $sanitizedPenId")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add new silo"
                            )
                        }
                    }
                } // <--- End of actions block
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(text = "Error: $error")
            } else if (farmUuid == null) { // Only check for farmUuid as penId can be null
                // Display error message if Farm ID is invalid
                Text("Error: Invalid Farm ID format.")
            } else {
                // Display the SiloListView, passing null for penId if it's not present
                SiloListView(
                    viewModel = siloViewModel,
                    ownerId = ownerId,
                    farmId = farmUuid,
                    penId = penUuid, // Pass nullable penId to SiloListView
                    navController = navController,
                    onBack = onBack,
                    onAddSilo = { currentFarmId, currentPenId ->
                        // Navigate to the add silo screen, passing farmId, ownerId, and penId
                        // Ensure currentPenId is not null, as SiloAdd route requires it.
                        // If you want to add a silo without a pen, you'll need a different route/screen.
                        if (currentPenId != null) {
                            // navController.navigate(Screen.SiloAdd.createRoute(currentFarmId.toString(), ownerId.toString(), currentPenId.toString()))
                        } else {
                            // Handle case where penId is null but SiloAdd requires it
                            //  siloViewModel.setError("Cannot add silo without a specific pen. Pen ID is missing.")
                        }
                    },
                    onSiloSelected = { silo -> // Changed to only accept 'silo'
                        // Navigate to the silo detail screen, passing silo ID, farm ID, owner ID, and pen ID
                        // Ensure required IDs are not null before navigating

                        val siloId = silo.id?.toString()
                        val currentFarmId = silo.farmId?.toString()
                        val currentOwnerId = silo.ownerId?.toString() ?: ownerId.toString()
                        val currentPenId = silo.penId?.toString()  ?: "null"// This can be null

                        if (siloId != null && currentFarmId != null && currentOwnerId != null && currentPenId != null) {

                            navController.navigate(Screen.SiloView.createRoute(siloId, currentFarmId, currentOwnerId, currentPenId))
                        } else {
                            // Handle cases where required IDs are null for navigation
                            //     siloViewModel.setError("Cannot navigate to silo detail: missing required IDs for silo: ${silo.displayName}")
                        }
                    },
                    onEditSilo = { silo ->
                        // Navigate to edit silo screen
                        // You'll need to add a Screen.SiloEdit route or reuse Screen.SiloAdd with an edit parameter

                        val siloId = silo.id?.toString()
                        val currentFarmId = silo.farmId?.toString()
                        val currentOwnerId = silo.ownerId?.toString() ?: ownerId.toString()
                        val currentPenId = silo.penId?.toString() ?: "null"

                        if (siloId != null && currentFarmId != null && currentOwnerId != null) {
                            // Option 1: If you have a separate SiloEdit screen route
                            // navController.navigate(Screen.SiloEdit.createRoute(siloId, currentFarmId, currentOwnerId, currentPenId))

                            // Option 2: Reuse SiloAdd route with siloId parameter to indicate edit mode
                            // You'll need to modify Screen.SiloAdd to accept an optional siloId parameter
                            navController.navigate("silo_add/$currentFarmId/$currentOwnerId/$currentPenId?siloId=$siloId")

                            Log.d("SilosScreen", "Edit Silo clicked for silo: ${silo.displayName}")
                        } else {
                            Log.e("SilosScreen",
                                "Cannot navigate to edit silo: missing required IDs silo id $siloId ,currentfarm id $currentFarmId ,currentownerid :$currentOwnerId ,currentpenid : $currentPenId"
                            )
                        }
                    }
                )
            }
        }
    }
}