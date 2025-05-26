// MainActivity.kt
package it.cynomys.cfmandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import it.cynomys.cfmandroid.model.Camera
import it.cynomys.cfmandroid.view.*
import it.cynomys.cfmandroid.viewmodel.AuthViewModel
import it.cynomys.cfmandroid.viewmodel.CameraViewModel
import it.cynomys.cfmandroid.viewmodel.DeviceViewModel
import it.cynomys.cfmandroid.viewmodel.FarmViewModel
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val authViewModel = remember { AuthViewModel() }
            val farmViewModel = remember { FarmViewModel() }
            val cameraViewModel = remember { CameraViewModel() }
            val deviceViewModel = remember { DeviceViewModel() }

            NavHost(
                navController = navController,
                startDestination = Screen.Login.route
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.FarmList.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.FarmList.route) {
                    val ownerId = authViewModel.userSession.value?.id
                    if (ownerId != null) {
                        FarmListScreen(
                            viewModel = farmViewModel,
                            ownerId = ownerId,
                            onAddFarm = { navController.navigate(Screen.AddFarm.route) },
                            onFarmSelected = { farmId ->
                                navController.navigate(Screen.FarmDetail.createRoute(farmId.toString()))
                            }
                        )
                    }
                }

                navigation(
                    route = Screen.FarmDetail.route,
                    startDestination = "${Screen.FarmDetail.route}/menu"
                ) {
                    composable("${Screen.FarmDetail.route}/menu") { backStackEntry ->
                        val farmId = backStackEntry.arguments?.getString("farmId")?.let { UUID.fromString(it) }
                        val ownerId = authViewModel.userSession.value?.id
                        if (farmId != null && ownerId != null) {
                            FarmDetailMenuScreen(
                                farmId = farmId,
                                onDeviceSelected = {
                                    navController.navigate(Screen.DeviceList.createRoute(ownerId,farmId))
                                },
                                onWeatherSelected = {
                                    navController.navigate(Screen.Weather.createRoute(farmId.toString()))
                                },
                                onSilosSelected = {
                                    navController.navigate(Screen.Silos.createRoute(farmId.toString()))
                                },
                                onWebcamsSelected = {
                                    navController.navigate(Screen.Webcams.createRoute(farmId.toString()))
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable(Screen.Webcams.route) { backStackEntry ->
                        val farmId = backStackEntry.arguments?.getString("farmId")?.let { UUID.fromString(it) }
                        val ownerId = authViewModel.userSession.value?.id
                        if (farmId != null && ownerId != null) {
                            CameraListView(
                                viewModel = cameraViewModel,
                                farmId = farmId,
                                ownerId = ownerId,
                                navController = navController,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // Add other sub-screens (Devices, Weather, Silos) here...
                }

                composable("add_camera/{farmId}") { backStackEntry ->
                    val farmId = backStackEntry.arguments?.getString("farmId")?.let { UUID.fromString(it) }
                    val ownerId = authViewModel.userSession.value?.id
                    if (farmId != null && ownerId != null) {
                        AddCameraScreen(
                            viewModel = cameraViewModel,
                            farmId = farmId,
                            ownerId = ownerId,
                            navController = navController
                        )
                    }
                }

                composable("edit_camera/{cameraId}") { backStackEntry ->
                    val cameraId = backStackEntry.arguments?.getString("cameraId")?.let { UUID.fromString(it) }
                    val camera = cameraViewModel.cameras.value.find { it.id == cameraId }
                    if (camera != null) {
                        EditCameraScreen(
                            viewModel = cameraViewModel,
                            camera = camera,
                            navController = navController
                        )
                    }
                }


                composable(Screen.CameraLiveView.route) { backStackEntry ->
                    val cameraId = backStackEntry.arguments?.getString("cameraId")
                    val camera = cameraViewModel.cameras.value.find { it.id?.toString() == cameraId }

                    if (camera != null) {
                        CameraLiveScreen(
                            cameraUrl = camera.cameraLink,
                            navController = navController
                        )
                    }
                }


                // Device List
                composable(Screen.DeviceList.route) { backStackEntry ->
                    val ownerId = backStackEntry.arguments?.getString("ownerId")?.let { UUID.fromString(it) }
                    val farmId = backStackEntry.arguments?.getString("farmId")?.let { UUID.fromString(it) }
                    if (ownerId != null && farmId != null) {
                        DeviceListView(
                            viewModel = deviceViewModel,
                            ownerId = ownerId,
                            farmId = farmId,
                            navController = navController
                        )
                    }
                }
//"device_detail/{deviceId}"
// Device Detail
                composable(Screen.DeviceDetail.route) { backStackEntry ->
                    val deviceIdString = backStackEntry.arguments?.getString("deviceId")
                    val deviceId = deviceIdString?.let { UUID.fromString(it) }

                    if (deviceId != null) {
                        DeviceDetailView(
                            deviceId = deviceId,
                            viewModel = deviceViewModel,
                            navController = navController
                        )
                    }
                }


// Edit Device
                composable(Screen.DeviceEdit.route) { backStackEntry ->
                    val deviceId = backStackEntry.arguments?.getString("deviceId")?.let { UUID.fromString(it) }
                    if (deviceId != null) {
                        DeviceEditView(
                            deviceId = deviceId,
                            viewModel = deviceViewModel,
                            navController = navController
                        )
                    }
                }

// Add Device
                composable(Screen.DeviceAdd.route) { backStackEntry ->
                    val ownerId = backStackEntry.arguments?.getString("ownerId")?.let { UUID.fromString(it) }
                    val farmId = backStackEntry.arguments?.getString("farmId")?.let { UUID.fromString(it) }
                    if (ownerId != null && farmId != null) {
                        DeviceAddView(
                            ownerId = ownerId,
                            farmId = farmId,
                            viewModel = deviceViewModel,
                            navController = navController
                        )
                    }
                }


                composable("qr_scan") {
                    QRScannerScreen(
                        navController = navController,
                        onScanned = { scannedCode ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("scannedDeviceId", scannedCode)
                        }
                    )
                }

            }
        }
    }
}