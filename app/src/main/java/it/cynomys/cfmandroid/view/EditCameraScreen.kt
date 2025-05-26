// EditCameraScreen.kt
package it.cynomys.cfmandroid.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.cynomys.cfmandroid.model.Camera
import it.cynomys.cfmandroid.model.CameraDto
import it.cynomys.cfmandroid.viewmodel.CameraViewModel
import java.util.UUID
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun EditCameraScreen(
    viewModel: CameraViewModel,
    camera: Camera,
    navController: NavController
) {
    var displayName by remember { mutableStateOf(camera.displayName) }
    var cameraLink by remember { mutableStateOf(camera.cameraLink) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Camera") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = cameraLink,
                onValueChange = { cameraLink = it },
                label = { Text("Camera Link") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.updateCamera(
                        camera.id!!,
                        CameraDto(cameraLink, displayName)
                    )
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = displayName.isNotBlank() && cameraLink.isNotBlank()
            ) {
                Text("Save Changes")
            }
        }
    }
}