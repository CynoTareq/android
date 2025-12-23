package it.cynomys.cfmandroid

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
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
import it.cynomys.cfmandroid.auth.signup.SignupScreen
import it.cynomys.cfmandroid.camera.AddCameraScreen
import it.cynomys.cfmandroid.camera.CameraListView
import it.cynomys.cfmandroid.camera.CameraLiveScreen
import it.cynomys.cfmandroid.camera.CameraViewModel
import it.cynomys.cfmandroid.camera.CameraViewModelFactory
import it.cynomys.cfmandroid.camera.EditCameraScreen
import it.cynomys.cfmandroid.device.DeviceFormView
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
import it.cynomys.cfmandroid.silo.SiloFormView
import it.cynomys.cfmandroid.silo.SiloView
import it.cynomys.cfmandroid.silo.SiloViewModel
import it.cynomys.cfmandroid.silo.SiloViewModelFactory
import it.cynomys.cfmandroid.silo.SilosScreen
import it.cynomys.cfmandroid.ui.theme.CFMAndroidTheme
import it.cynomys.cfmandroid.util.NetworkService
import it.cynomys.cfmandroid.weather.WeatherScreen
import it.cynomys.cfmandroid.weather.WeatherViewModel
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

                val siloViewModel: SiloViewModel = viewModel(
                    factory = SiloViewModelFactory(applicationContext)
                )
                val profileViewModel: ProfileViewModel = viewModel()

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
                        // AUTH NAVHOST - for login/signup flow
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Login.route
                        ) {
                            composable(Screen.Login.route) {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    navController = navController
                                )
                            }
                            composable(Screen.Signup.route) {
                                SignupScreen(
                                    viewModel = authViewModel,
                                    navController = navController
                                )
                            }
                        }
                    } else {
                        // MAIN APP NAVHOST - for authenticated users
                        val ownerId = userSession?.id

                        NavHost(
                            navController = navController,
                            startDestination = Screen.FarmList.route
                        ) {
                            composable(Screen.Login.route) {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    navController = navController
                                )
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
                                        onFarmSelected = { farmId ->
                                            navController.navigate(
                                                Screen.FarmDetail.createRoute(
                                                    farmId.toString()
                                                )
                                            )
                                        },
                                        onProfileSelected = {
                                            navController.navigate(
                                                Screen.Profile.createRoute(
                                                    ownerId.toString()
                                                )
                                            )
                                        },
                                        onEditFarm = { farmId -> navController.navigate("edit_farm/$farmId") }
                                    )
                                } else {
                                    Text("Owner ID not available. Please log in again.")
                                }
                            }

                            composable(
                                route = Screen.FarmDetail.route,
                                arguments = listOf(navArgument("farmId") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                val farmIdString = backStackEntry.arguments?.getString("farmId")
                                val farmId = farmIdString?.let { UUID.fromString(it) }
                                if (farmId != null) {
                                    FarmDetailMenuScreen(
                                        farmId = farmId,
                                        navController = navController,
                                        onDeviceSelected = {
                                            if (ownerId != null) {
                                                navController.navigate(
                                                    Screen.DeviceList.createRoute(
                                                        ownerId.toString(),
                                                        farmId.toString()
                                                    )
                                                )
                                            } else {
                                                // Handle error or navigate to login, or ensure ownerId is always available here.
                                            }
                                        },
                                        onWeatherSelected = {
                                            navController.navigate(
                                                Screen.Weather.createRoute(
                                                    farmId.toString()
                                                )
                                            )
                                        },
                                        onSilosSelected = {
                                            navController.navigate(
                                                Screen.Silos.createRoute(
                                                    farmId.toString()
                                                )
                                            )
                                        },
                                        onWebcamsSelected = {
                                            navController.navigate(
                                                Screen.Webcams.createRoute(
                                                    farmId.toString()
                                                )
                                            )
                                        },
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
                                arguments = listOf(navArgument("farmId") {
                                    type = NavType.StringType
                                })
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

                            // Weather Screen
                            composable(
                                route = Screen.Weather.route,
                                arguments = listOf(navArgument("farmId") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                val farmIdString = backStackEntry.arguments?.getString("farmId")

                                if (farmIdString != null) {
                                    val weatherViewModel: WeatherViewModel = viewModel(
                                        key = farmIdString,
                                        factory = object : ViewModelProvider.Factory {
                                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                                                    @Suppress("UNCHECKED_CAST")
                                                    return WeatherViewModel(
                                                        farmId = farmIdString,
                                                        networkService = NetworkService()
                                                    ) as T
                                                }
                                                throw IllegalArgumentException("Unknown ViewModel class")
                                            }
                                        }
                                    )

                                    WeatherScreen(
                                        viewModel = weatherViewModel,
                                        onBack = { navController.popBackStack() }
                                    )
                                } else {
                                    Text("Farm ID not available for weather screen.")
                                }
                            }

                            // Nested navigation for devices
                            navigation(
                                startDestination = Screen.DeviceList.createRoute(
                                    "{ownerId}",
                                    "{farmId}"
                                ),
                                route = Screen.Devices.createRoute("{farmId}")
                            ) {
                                composable(
                                    route = Screen.DeviceList.createRoute("{ownerId}", "{farmId}"),
                                    arguments = listOf(
                                        navArgument("ownerId") { type = NavType.StringType },
                                        navArgument("farmId") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val currentOwnerIdString =
                                        backStackEntry.arguments?.getString("ownerId")
                                    val currentOwnerId =
                                        currentOwnerIdString?.let { UUID.fromString(it) }
                                    val currentFarmIdString =
                                        backStackEntry.arguments?.getString("farmId")
                                    val currentFarmId =
                                        currentFarmIdString?.let { UUID.fromString(it) }

                                    if (currentOwnerId != null && currentFarmId != null) {
                                        DeviceListView(
                                            viewModel = deviceViewModel,
                                            ownerId = currentOwnerId,
                                            farmId = currentFarmId,
                                            navController = navController,
                                            onBack = { navController.popBackStack() },
                                            onAddDevice = { farmIdParam ->
                                                navController.navigate(
                                                    Screen.DeviceAdd.createRoute(
                                                        farmIdParam.toString()
                                                    )
                                                )
                                            },
                                            // UPDATED: Handle the Edit Navigation here
                                            onEditDevice = { deviceToEdit ->
                                                deviceToEdit.id.let { devId ->
                                                    navController.navigate(
                                                        Screen.DeviceEdit.createRoute(
                                                            devId.toString(),
                                                            currentFarmId.toString()
                                                        )
                                                    )
                                                }
                                            },
                                            onDeviceSelected = { device, farmIdParam ->
                                                navController.navigate(
                                                    Screen.DeviceDetail.createRoute(
                                                        device.deviceID,
                                                        farmIdParam.toString()
                                                    )
                                                )
                                            }
                                        )
                                    } else {
                                        Text("Invalid owner or farm ID for device list.")
                                    }
                                }



                                composable(
                                    route = Screen.DeviceAdd.createRoute("{farmId}"),
                                    arguments = listOf(navArgument("farmId") {
                                        type = NavType.StringType
                                    })
                                ) { backStackEntry ->
                                    val currentFarmIdString =
                                        backStackEntry.arguments?.getString("farmId")
                                    val currentFarmId =
                                        currentFarmIdString?.let { UUID.fromString(it) }

                                    if (currentFarmId != null && ownerId != null) {
                                        val farms by farmViewModel.farms.collectAsState()
                                        val currentFarm = farms.find { it.id == currentFarmId }

                                        if (currentFarm != null) {
                                            DeviceFormView(
                                                viewModel = deviceViewModel,
                                                navController = navController,
                                                ownerId = ownerId,
                                                farmId = currentFarmId,
                                                ownerEmail = userSession!!.email,
                                                farmSpecies = currentFarm.species,
                                                existingDevice = null // Add mode
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    CircularProgressIndicator()
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("Loading farm details...")
                                                }
                                            }
                                            LaunchedEffect(currentFarmId) {
                                                if (currentFarm == null) {
                                                    farmViewModel.getFarms(ownerId)
                                                }
                                            }
                                        }
                                    } else {
                                        Text("Farm ID or Owner ID not available for adding device.")
                                    }
                                }


                                composable(
                                    route = Screen.DeviceEdit.createRoute("{deviceId}", "{farmId}"),
                                    arguments = listOf(
                                        navArgument("deviceId") { type = NavType.StringType },
                                        navArgument("farmId") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val deviceIdString =
                                        backStackEntry.arguments?.getString("deviceId")
                                    val deviceId = deviceIdString?.let { UUID.fromString(it) }
                                    val currentFarmIdString =
                                        backStackEntry.arguments?.getString("farmId")
                                    val currentFarmId =
                                        currentFarmIdString?.let { UUID.fromString(it) }

                                    val devices by deviceViewModel.devices.collectAsState()
                                    val deviceToEdit = devices.find { it.id == deviceId }
                                    val farms by farmViewModel.farms.collectAsState()
                                    val currentFarm = farms.find { it.id == currentFarmId }

                                    if (deviceId != null && currentFarmId != null && ownerId != null && deviceToEdit != null && currentFarm != null) {
                                        DeviceFormView(
                                            viewModel = deviceViewModel,
                                            navController = navController,
                                            ownerId = ownerId,
                                            farmId = currentFarmId,
                                            ownerEmail = userSession!!.email,
                                            farmSpecies = currentFarm.species,
                                            existingDevice = deviceToEdit // Edit mode - pass the existing device
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator()
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Loading device details...")
                                            }
                                        }
                                        // Ensure data is loaded
                                        LaunchedEffect(currentFarmId, ownerId) {
                                            if (currentFarm == null && ownerId != null) {
                                                farmViewModel.getFarms(ownerId)
                                            }
                                            if (deviceToEdit == null && currentFarmId != null && ownerId != null) {
                                                deviceViewModel.getDevices(ownerId, currentFarmId)
                                            }
                                        }
                                    }
                                }


                                composable(
                                    route = Screen.DeviceDetail.createRoute(
                                        "{deviceId}",
                                        "{farmId}"
                                    ),
                                    arguments = listOf(
                                        navArgument("deviceId") { type = NavType.StringType },
                                        navArgument("farmId") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val deviceIdString =
                                        backStackEntry.arguments?.getString("deviceId")
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
                                    route = Screen.CameraLive.route,
                                    arguments = listOf(navArgument("cameraUrl") {
                                        type = NavType.StringType
                                    })
                                ) { backStackEntry ->
                                    val encodedUrl =
                                        backStackEntry.arguments?.getString("cameraUrl")
                                    if (encodedUrl != null) {
                                        val decodedUrl = URLDecoder.decode(
                                            encodedUrl,
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        CameraLiveScreen(
                                            cameraUrl = decodedUrl,
                                            navController = navController
                                        )
                                    } else {
                                        Text("Camera URL not provided.")
                                    }
                                }
                                composable(
                                    route = Screen.CameraAdd.route, // This is "camera_add/{farmId}"
                                    arguments = listOf(navArgument("farmId") {
                                        type = NavType.StringType
                                    })
                                ) { backStackEntry ->
                                    val currentFarmIdString =
                                        backStackEntry.arguments?.getString("farmId")
                                    val currentFarmId =
                                        currentFarmIdString?.let { UUID.fromString(it) }
                                    // Assuming you have a CameraAddView Composable to navigate to.
                                    // If you don't, you need to create one, similar to DeviceAddView.
                                    if (currentFarmId != null && ownerId != null) {

                                        AddCameraScreen(
                                            viewModel = cameraViewModel,
                                            farmId = currentFarmId,
                                            ownerId = ownerId,
                                            navController = navController
                                        )

                                    } else {
                                        Text("Farm ID or Owner ID not available for adding camera.")
                                    }
                                }

                                composable(
                                    route = Screen.Webcams.route,
                                    arguments = listOf(navArgument("farmId") {
                                        type = NavType.StringType
                                    })
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
                                                navController.navigate(
                                                    Screen.CameraAdd.createRoute(
                                                        farmIdParam.toString()
                                                    )
                                                )
                                            },
                                            onCameraSelected = { cameraUrl ->
                                                navController.navigate(
                                                    Screen.CameraLive.createRoute(
                                                        Uri.encode(cameraUrl)
                                                    )
                                                )
                                            }
                                        )
                                    } else {
                                        Text("Invalid farm ID or owner ID for webcams.")
                                    }
                                }


                                composable(
                                    route = Screen.CameraEdit.route, // This is "camera_edit/{cameraId}"
                                    arguments = listOf(navArgument("cameraId") {
                                        type = NavType.StringType
                                    })
                                ) { backStackEntry ->
                                    val cameraIdString =
                                        backStackEntry.arguments?.getString("cameraId")
                                    val cameraId = cameraIdString?.let { UUID.fromString(it) }

                                    // Get the current list of cameras from the ViewModel state
                                    val cameras by cameraViewModel.cameras.collectAsState()

                                    // Find the camera to edit using the ID
                                    val cameraToEdit = cameras.find { it.id == cameraId }

                                    if (cameraId != null && cameraToEdit != null) {
                                        // Use the newly provided EditCameraScreen
                                        EditCameraScreen(
                                            viewModel = cameraViewModel,
                                            camera = cameraToEdit, // Pass the found Camera object
                                            navController = navController
                                        )
                                    } else {
                                        Text("Camera not found or ID not available for editing.")
                                    }
                                }

                                // Silos
                                composable(
                                    route = Screen.Silos.route,
                                    arguments = listOf(
                                        navArgument("farmId") { type = NavType.StringType },
                                        navArgument("penId") {
                                            type = NavType.StringType
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { backStackEntry ->
                                    val farmIdString = backStackEntry.arguments?.getString("farmId")
                                    val penIdString = backStackEntry.arguments?.getString("penId")
                                    val ownerId =
                                        authViewModel.userSession.collectAsState().value?.id

                                    Log.d(
                                        "MainActivity",
                                        "Navigating to SilosScreen: farmId=$farmIdString, penId=$penIdString, ownerId=$ownerId"
                                    )

                                    if (farmIdString != null && ownerId != null) {
                                        SilosScreen(
                                            farmId = farmIdString,
                                            ownerId = ownerId,
                                            penId = penIdString,
                                            navController = navController,
                                            onBack = { navController.popBackStack() }
                                        )
                                    } else {
                                        Text("Error: Required IDs (Farm or Owner) not available for Silos Screen.")
                                    }


                                }




                                // Inside your NavHost
                                composable(
                                    route = "silo_add/{farmId}/{ownerId}/{penId}?siloId={siloId}",
                                    arguments = listOf(
                                        navArgument("farmId") { type = NavType.StringType },
                                        navArgument("ownerId") { type = NavType.StringType },
                                        navArgument("penId") { type = NavType.StringType; nullable = true },
                                        navArgument("siloId") { type = NavType.StringType; nullable = true }
                                    )
                                ) { backStackEntry ->
                                    val farmId = backStackEntry.arguments?.getString("farmId")?.let { UUID.fromString(it) }
                                    val ownerId = backStackEntry.arguments?.getString("ownerId")?.let { UUID.fromString(it) }
                                    val penIdStr = backStackEntry.arguments?.getString("penId")
                                    val siloId = backStackEntry.arguments?.getString("siloId")

                                    // Handle "null" string from navigation safely
                                    val penId = if (penIdStr == null || penIdStr == "null") null else UUID.fromString(penIdStr)

                                    // Observe the selected silo from the ViewModel
                                    val siloToEdit by siloViewModel.selectedSilo.collectAsState()

                                    // If siloId is provided, trigger a fetch in the ViewModel
                                    LaunchedEffect(siloId) {
                                        if (!siloId.isNullOrEmpty()) {
                                            siloViewModel.getSiloById(UUID.fromString(siloId))
                                        }
                                    }

                                    // Determine if we are still loading the silo for edit mode
                                    val isLoadingSilo = !siloId.isNullOrEmpty() && siloToEdit == null

                                    if (isLoadingSilo) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
                                    } else if (farmId != null && ownerId != null) {
                                        SiloFormView(
                                            viewModel = siloViewModel,
                                            navController = navController,
                                            ownerId = ownerId,
                                            ownerEmail = userSession!!.email, // Ensure you pass the user's email here
                                            farmId = farmId,
                                            penId = penId,
                                            siloToEdit = siloToEdit // This will be null for Add, and populated for Edit
                                        )
                                    }
                                }


                                composable(Screen.SiloView.route) { backStackEntry ->
                                    val siloId = backStackEntry.arguments?.getString("siloId") ?: ""
                                    val farmId = backStackEntry.arguments?.getString("farmId") ?: ""
                                    val ownerId = backStackEntry.arguments?.getString("ownerId") ?: ""
                                    val penId = backStackEntry.arguments?.getString("penId")

                                    SiloView(
                                        siloId = siloId,
                                        farmId = farmId,
                                        ownerId = ownerId,
                                        penId = penId,
                                        navController = navController,
                                        siloViewModel = siloViewModel,
                                        sensorDataViewModel = sensorDataViewModel,
                                        authViewModel = authViewModel
                                    )
                                }


                                composable(Screen.QRScanner.route) {
                                    QRScannerScreen(
                                        navController = navController,
                                        onScanned = { scannedCode ->
                                            Log.d(
                                                "MainActivity",
                                                "onScanned callback received: $scannedCode"
                                            )
                                            navController.previousBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("scannedDeviceId", scannedCode)
                                            Log.d(
                                                "MainActivity",
                                                "savedStateHandle set for scannedDeviceId: $scannedCode"
                                            )
                                        }
                                    )
                                }


                                // Profile Screen
                                composable(
                                    route = Screen.Profile.route,
                                    arguments = listOf(navArgument("ownerId") {
                                        type = NavType.StringType
                                    })
                                ) { backStackEntry ->
                                    val currentOwnerIdString =
                                        backStackEntry.arguments?.getString("ownerId")
                                    val currentOwnerId =
                                        currentOwnerIdString?.let { UUID.fromString(it) }

                                    if (currentOwnerId != null) {
                                        ProfileScreen(
                                            ownerId = currentOwnerId,
                                            onNavigateBack = { navController.popBackStack() },
                                            // MODIFIED: Passing the existing authViewModel instance
                                            authViewModel = authViewModel,
                                            profileViewModel = profileViewModel
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
        }}

    fun setAppLocale(localeCode:String){
        val locals = LocaleListCompat.forLanguageTags(localeCode)
        AppCompatDelegate.setApplicationLocales(locals)
    }

        override fun onResume() {
            super.onResume()
            authViewModel.validateSession()
        }
    }
