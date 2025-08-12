package it.cynomys.cfmandroid

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import it.cynomys.cfmandroid.auth.AuthViewModel
import it.cynomys.cfmandroid.auth.AuthViewModelFactory
import it.cynomys.cfmandroid.auth.LoginScreen
import it.cynomys.cfmandroid.camera.CameraListView
import it.cynomys.cfmandroid.camera.CameraLiveScreen
import it.cynomys.cfmandroid.camera.CameraViewModel
import it.cynomys.cfmandroid.camera.CameraViewModelFactory
import it.cynomys.cfmandroid.device.DeviceAddView
import it.cynomys.cfmandroid.device.DeviceEditView
import it.cynomys.cfmandroid.device.DeviceListView
import it.cynomys.cfmandroid.device.DeviceView
import it.cynomys.cfmandroid.device.DeviceViewModel
import it.cynomys.cfmandroid.device.DeviceViewModelFactory
import it.cynomys.cfmandroid.device.QRScannerScreen
import it.cynomys.cfmandroid.farm.AddEditFarmScreen
import it.cynomys.cfmandroid.farm.FarmDetailMenuScreen
import it.cynomys.cfmandroid.farm.FarmListScreen
import it.cynomys.cfmandroid.farm.FarmViewModel
import it.cynomys.cfmandroid.farm.FarmViewModelFactory
import it.cynomys.cfmandroid.farm.toFarmDto
import it.cynomys.cfmandroid.profile.ProfileScreen
import it.cynomys.cfmandroid.profile.ProfileViewModel
import it.cynomys.cfmandroid.sensordata.SensorDataViewModel
import it.cynomys.cfmandroid.sensordata.SensorDataViewModelFactory
import it.cynomys.cfmandroid.silo.SiloViewModel
import it.cynomys.cfmandroid.silo.SiloViewModelFactory
import it.cynomys.cfmandroid.silo.SilosScreen
import it.cynomys.cfmandroid.ui.theme.CFMAndroidTheme
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(applicationContext)
        )[AuthViewModel::class.java]

        setContent {
            CFMAndroidTheme {
                val navController = rememberNavController()

                val userSession by authViewModel.userSession.collectAsState()
                val isSessionRestored by authViewModel.isSessionRestored.collectAsState()
                val isLoggedIn = userSession != null

                val farmViewModel: FarmViewModel = viewModel(
                    factory = FarmViewModelFactory(applicationContext)
                )
                val deviceViewModel: DeviceViewModel = viewModel(
                    factory = DeviceViewModelFactory(applicationContext)
                )
                val cameraViewModel: CameraViewModel = viewModel(
                    factory = CameraViewModelFactory(applicationContext)
                )
                val sensorDataViewModel: SensorDataViewModel = viewModel(
                    factory = SensorDataViewModelFactory(applicationContext)
                )

                val siloViewModel: SiloViewModel = viewModel( // Add this block
                    factory = SiloViewModelFactory(applicationContext)
                )
                val profileViewModel: ProfileViewModel = viewModel() // Initialize ProfileViewModel

                if (!isSessionRestored) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                        Text("Restoring session...")
                    }
                } else {
                    if (!isLoggedIn) {
                        LoginScreen(viewModel = authViewModel, navController = navController)
                    } else {
                        val ownerId = userSession?.id

                        NavHost(
                            navController = navController,
                            startDestination = Screen.FarmList.route
                        ) {
                            composable(Screen.Login.route) {
                                LoginScreen(viewModel = authViewModel, navController = navController)
                            }
                            composable(Screen.Success.route) {
                                LaunchedEffect(Unit) {
                                    navController.navigate(Screen.FarmList.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                                Text("Login Success")
                            }




                            composable(Screen.FarmList.route) {
                                if (ownerId != null) {
                                    FarmListScreen(
                                        viewModel = farmViewModel,
                                        ownerId = ownerId,
                                        navController = navController,
                                        onAddFarm = { navController.navigate(Screen.AddFarm.route) },
                                        onFarmSelected = { farmId -> navController.navigate(Screen.FarmDetail.createRoute(farmId.toString())) },
                                        onProfileSelected = { // Provide the onProfileSelected callback here
                                            navController.navigate(Screen.Profile.createRoute(ownerId.toString())) // Navigate to the profile screen
                                        }
                                    )
                                } else {
                                    Text("Owner ID not available. Please log in again.")
                                }
                            }


                            composable(
                                route = Screen.FarmDetail.route,
                                arguments = listOf(navArgument("farmId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val farmIdString = backStackEntry.arguments?.getString("farmId")
                                val farmId = farmIdString?.let { UUID.fromString(it) }
                                if (farmId != null) {
                                    FarmDetailMenuScreen(
                                        farmId = farmId,
                                        navController = navController,
                                        onDeviceSelected = {
                                            if (ownerId != null) {
                                                navController.navigate(Screen.DeviceList.createRoute(ownerId.toString(), farmId.toString()))
                                            } else {
                                                // Handle error or navigate to login, or ensure ownerId is always available here.
                                            }
                                        },
                                        onWeatherSelected = { navController.navigate(Screen.Weather.createRoute(farmId.toString())) },
                                        onSilosSelected = { navController.navigate(Screen.Silos.createRoute(farmId.toString())) },
                                        onWebcamsSelected = { navController.navigate(Screen.Webcams.createRoute(farmId.toString())) },
                                        onBack = { navController.popBackStack() }
                                    )
                                } else {
                                    Text("Farm ID not available.")
                                }
                            }
                            composable(Screen.AddFarm.route) {
                                if (ownerId != null) {
                                    AddEditFarmScreen(
                                        viewModel = farmViewModel,
                                        ownerId = ownerId,
                                        onBack = { navController.popBackStack() }
                                    )
                                } else {
                                    Text("Owner ID not available. Cannot add farm.")
                                }
                            }
                            composable(
                                route = Screen.EditFarm.route,
                                arguments = listOf(navArgument("farmId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val farmIdString = backStackEntry.arguments?.getString("farmId")
                                val farmId = farmIdString?.let { UUID.fromString(it) }
                                val farms by farmViewModel.farms.collectAsState()
                                val farmToEdit = farms.find { it.id == farmId }

                                if (ownerId != null && farmToEdit != null) {
                                    AddEditFarmScreen(
                                        viewModel = farmViewModel,
                                        ownerId = ownerId,
                                        initialFarm = farmToEdit.toFarmDto(),
                                        onBack = { navController.popBackStack() }
                                    )
                                } else {
                                    Text("Farm not found or owner ID not available.")
                                }
                            }

                            // Nested navigation for devices
                            navigation(
                                startDestination = Screen.DeviceList.createRoute("{ownerId}", "{farmId}"),
                                route = Screen.Devices.createRoute("{farmId}")
                            ) {
                                composable(
                                    route = Screen.DeviceList.createRoute("{ownerId}", "{farmId}"),
                                    arguments = listOf(
                                        navArgument("ownerId") { type = NavType.StringType },
                                        navArgument("farmId") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val currentOwnerIdString = backStackEntry.arguments?.getString("ownerId")
                                    val currentOwnerId = currentOwnerIdString?.let { UUID.fromString(it) }
                                    val currentFarmIdString = backStackEntry.arguments?.getString("farmId")
                                    val currentFarmId = currentFarmIdString?.let { UUID.fromString(it) }

                                    if (currentOwnerId != null && currentFarmId != null) {
                                        DeviceListView(
                                            viewModel = deviceViewModel,
                                            ownerId = currentOwnerId,
                                            farmId = currentFarmId,
                                            navController = navController,
                                            onBack = { navController.popBackStack() }, // Added onBack
                                            onAddDevice = { farmIdParam -> // Added onAddDevice
                                                navController.navigate(Screen.DeviceAdd.createRoute(farmIdParam.toString()))
                                            },
                                            onDeviceSelected = { device, farmIdParam -> // Added onDeviceSelected
                                                navController.navigate(Screen.DeviceDetail.createRoute(device.deviceID, farmIdParam.toString()))
                                            }
                                        )
                                    } else {
                                        Text("Invalid owner or farm ID for device list.")
                                    }
                                }
                                composable(
                                    route = Screen.DeviceAdd.createRoute("{farmId}"),
                                    arguments = listOf(navArgument("farmId") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val currentFarmIdString = backStackEntry.arguments?.getString("farmId")
                                    val currentFarmId = currentFarmIdString?.let { UUID.fromString(it) }
                                    if (currentFarmId != null && ownerId != null) {
                                        DeviceAddView(
                                            viewModel = deviceViewModel,
                                            farmId = currentFarmId,
                                            ownerId = ownerId,
                                            navController = navController,
                                        )
                                    } else {
                                        Text("Farm ID or Owner ID not available for adding device.")
                                    }
                                }





                                composable(
                                    route = Screen.DeviceDetail.createRoute("{deviceId}", "{farmId}"),
                                    arguments = listOf(
                                        navArgument("deviceId") { type = NavType.StringType },
                                        navArgument("farmId") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val deviceIdString = backStackEntry.arguments?.getString("deviceId")
                                    val deviceId = deviceIdString?.let { UUID.fromString(it) }
                                    val farmIdString = backStackEntry.arguments?.getString("farmId")
                                    val farmId = farmIdString?.let { UUID.fromString(it) }
                                    if (deviceId != null && ownerId != null && farmId != null) {
                                        DeviceView(
                                            deviceID = deviceId.toString(),
                                            farmId = farmId.toString(),
                                            navController = navController,
                                            sensorDataViewModel = sensorDataViewModel,
                                            ownerId = ownerId
                                        )
                                    } else {
                                        Text("Device ID, Farm ID or Owner ID not available for detail view.")
                                    }
                                }
                                composable(
                                    route = Screen.DeviceEdit.createRoute("{deviceId}", "{farmId}"),
                                    arguments = listOf(
                                        navArgument("deviceId") { type = NavType.StringType },
                                        navArgument("farmId") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val deviceIdString = backStackEntry.arguments?.getString("deviceId")
                                    val deviceId = deviceIdString?.let { UUID.fromString(it) }
                                    val currentFarmIdString = backStackEntry.arguments?.getString("farmId")
                                    val currentFarmId = currentFarmIdString?.let { UUID.fromString(it) }
                                    val devices by deviceViewModel.devices.collectAsState()
                                    val deviceToEdit = devices.find { it.id == deviceId }

                                    if (deviceId != null && currentFarmId != null && ownerId != null && deviceToEdit != null) {
                                        DeviceEditView(
                                            viewModel = deviceViewModel,
                                            ownerId = ownerId,
                                            farmId = currentFarmId,
                                            deviceId = deviceToEdit.id.toString(),
                                            navController = navController,
                                            onBack = { navController.popBackStack() }
                                        )
                                    } else {
                                        Text("Device not found or IDs not available for editing device.")
                                    }
                                }
                            }

                            // Camera screens
                            composable(
                                route = Screen.CameraLive.route, // Use the route directly
                                arguments = listOf(navArgument("cameraUrl") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val encodedUrl = backStackEntry.arguments?.getString("cameraUrl")
                                if (encodedUrl != null) {
                                    val decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
                                    CameraLiveScreen(cameraUrl = decodedUrl, navController = navController)
                                } else {
                                    Text("Camera URL not provided.")
                                }
                            }


                            composable(
                                route = Screen.Webcams.route,
                                arguments = listOf(navArgument("farmId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val farmIdString = backStackEntry.arguments?.getString("farmId")
                                val farmId = farmIdString?.let { UUID.fromString(it) }
                                if (farmId != null && ownerId != null) {
                                    CameraListView(
                                        viewModel = cameraViewModel,
                                        farmId = farmId,
                                        ownerId = ownerId,
                                        navController = navController,
                                        onBack = { navController.popBackStack() },
                                        onAddCamera = { farmIdParam ->
                                            navController.navigate(Screen.CameraAdd.createRoute(farmIdParam.toString()))
                                        },
                                        onCameraSelected = { cameraUrl ->
                                            navController.navigate(Screen.CameraLive.createRoute(Uri.encode(cameraUrl)))                                        }
                                    )
                                } else {
                                    Text("Invalid farm ID or owner ID for webcams.")
                                }
                            }




                            /*



                            composable(
                                route = Screen.CameraList.createRoute("{farmId}"),
                                arguments = listOf(navArgument("farmId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val farmIdString = backStackEntry.arguments?.getString("farmId")
                                val farmId = farmIdString?.let { UUID.fromString(it) }
                                if (farmId != null && ownerId != null) {
                                    CameraListView(
                                        viewModel = cameraViewModel,
                                        farmId = farmId,
                                        ownerId = ownerId,
                                        navController = navController,
                                        onBack = { navController.popBackStack() }, // Added onBack
                                        onAddCamera = { farmIdParam -> // Added onAddCamera
                                            navController.navigate(Screen.CameraAdd.createRoute(farmIdParam.toString()))
                                        },
                                        onCameraSelected = { cameraUrl -> // Added onCameraSelected
                                            navController.navigate(Screen.CameraLive.createRoute(cameraUrl))
                                        }
                                    )
                                } else {
                                    Text("Invalid farm ID or owner ID for camera list.")
                                }
                            }
                            composable(
                                route = Screen.CameraAdd.createRoute("{farmId}"),
                                arguments = listOf(navArgument("farmId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val farmIdString = backStackEntry.arguments?.getString("farmId")
                                val farmId = farmIdString?.let { UUID.fromString(it) }
                                if (farmId != null && ownerId != null) {
                                    AddCameraScreen(
                                        viewModel = cameraViewModel,
                                        farmId = farmId,
                                        ownerId = ownerId,
                                        navController = navController
                                    )
                                } else {
                                    Text("Farm ID or owner ID not available for adding camera.")
                                }
                            }

                          */



                            // Silos (Updated for optional penId)
                            composable(
                                route = Screen.Silos.route,
                                arguments = listOf(
                                    navArgument("farmId") { type = NavType.StringType },
                                    navArgument("penId") {
                                        type = NavType.StringType
                                        nullable = true // Mark penId as nullable
                                        defaultValue = null // Set default value to null
                                    }
                                )
                            ) { backStackEntry ->
                                val farmIdString = backStackEntry.arguments?.getString("farmId")
                                val penIdString = backStackEntry.arguments?.getString("penId") // This will be null if not provided
                                val ownerId = authViewModel.userSession.collectAsState().value?.id

                                Log.d("MainActivity", "Navigating to SilosScreen: farmId=$farmIdString, penId=$penIdString, ownerId=$ownerId")

                                if (farmIdString != null && ownerId != null) {
                                    SilosScreen(
                                        farmId = farmIdString,
                                        ownerId = ownerId,
                                        penId = penIdString, // Pass the potentially null penId
                                        navController = navController,
                                        onBack = { navController.popBackStack() }
                                    )
                                } else {
                                    Text("Error: Required IDs (Farm or Owner) not available for Silos Screen.")
                                }
                            }

                            composable(Screen.QRScanner.route) {
                                QRScannerScreen(

                                    navController = navController,
                                    onScanned = { scannedCode ->
                                        Log.d("MainActivity", "onScanned callback received: $scannedCode") // Add this line
                                        navController.previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("scannedDeviceId", scannedCode)
                                        Log.d("MainActivity", "savedStateHandle set for scannedDeviceId: $scannedCode") // Add this line
                                    }
                                )
                            }


                            // Profile Screen Composable
                            composable(
                                route = Screen.Profile.route,
                                arguments = listOf(navArgument("ownerId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val currentOwnerIdString = backStackEntry.arguments?.getString("ownerId")
                                val currentOwnerId = currentOwnerIdString?.let { UUID.fromString(it) }

                                if (currentOwnerId != null) {
                                    ProfileScreen(
                                        ownerId = currentOwnerId,
                                        onNavigateBack = { navController.popBackStack() },
                                        profileViewModel = profileViewModel // Pass the initialized profileViewModel
                                    )
                                } else {
                                    Text("Error: Owner ID not available for profile screen.")
                                }
                            }





                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        authViewModel.validateSession()
    }
}
