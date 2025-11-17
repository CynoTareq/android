package it.cynomys.cfmandroid

sealed class Screen(val route: String) {
    object LanguageSelection : Screen("language_selection")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Success : Screen("success")
    object FarmList : Screen("farm_list")
    object FarmDetail : Screen("farm_detail/{farmId}") {
        fun createRoute(farmId: String) = "farm_detail/$farmId"
    }

    object Devices : Screen("farm_detail/{farmId}/devices") {
        fun createRoute(farmId: String) = "farm_detail/$farmId/devices"
    }

    object Weather : Screen("farm_detail/{farmId}/weather") {
        fun createRoute(farmId: String) = "farm_detail/$farmId/weather"
    }
    // Changed Silos route to make penId optional
    object Silos : Screen("farm_detail/{farmId}/silos?penId={penId}") {
        fun createRoute(farmId: String, penId: String? = null): String {
            return if (penId != null) {
                "farm_detail/$farmId/silos?penId=$penId"
            } else {
                "farm_detail/$farmId/silos"
            }
        }
    }

    // In Screen.kt, add the following:

    // Silo screens - Detail
    object SiloView : Screen("silo_detail/{siloId}/{farmId}/{ownerId}/{penId}") {
        // Includes all four IDs in the path. penId is optional in the data model (Silo.kt).
        fun createRoute(siloId: String, farmId: String, ownerId: String, penId: String?): String {
            // Use "null" as a placeholder string if penId is null to ensure a complete path is passed.
            return "silo_detail/$siloId/$farmId/$ownerId/${penId ?: "null"}"
        }
    }

    // Silo screens - Edit
    object SiloEdit : Screen("silo_edit/{siloId}/{farmId}/{ownerId}/{penId}") {
        fun createRoute(siloId: String, farmId: String, ownerId: String, penId: String?): String {
            return "silo_edit/$siloId/$farmId/$ownerId/${penId ?: "null"}"
        }
    }



    // NEW: Silo screens - Add (MODIFIED to use penId as a required path parameter placeholder)
    object SiloAdd : Screen("silo_add/{farmId}/{ownerId}/{penId}") { // <-- Route changed: back to path parameter
        fun createRoute(farmId: String, ownerId: String, penId: String? = null): String {
            // CRITICAL: Always provide a value for {penId} in the path, using "null" as placeholder
            val penIdPlaceholder = penId ?: "null"
            return "silo_add/$farmId/$ownerId/$penIdPlaceholder" // Example: silo_add/.../.../null
        }
    }



    object Webcams : Screen("farm_detail/{farmId}/webcams") {
        fun createRoute(farmId: String) = "farm_detail/$farmId/webcams"
    }
    object AddFarm : Screen("add_farm")
    object EditFarm : Screen("edit_farm/{farmId}") {
        fun createRoute(farmId: String) = "edit_farm/$farmId"
    }

    object DeviceList : Screen("device_list") {
        fun createRoute(ownerId: String, farmId: String) = "device_list/$ownerId/$farmId"
    }

    // Corrected: Added {farmId} to DeviceDetail route
    object DeviceDetail : Screen("device_detail/{deviceId}/{farmId}") {
        fun createRoute(deviceId: String, farmId: String) = "device_detail/$deviceId/$farmId"
    }

    object DeviceEdit : Screen("edit_device/{deviceId}/{farmId}") {
        fun createRoute(deviceId: String, farmId: String) = "edit_device/$deviceId/$farmId"
    }

    object DeviceAdd : Screen("add_device") {
        fun createRoute(farmId: String) = "add_device/$farmId"
    }

    object DeviceView : Screen("device_view/{deviceId}/{farmId}") {
        fun createRoute(deviceId: String, farmId: String) = "device_view/$deviceId/$farmId"
    }

    // Camera screens
    object CameraAdd : Screen("camera_add/{farmId}") {
        fun createRoute(farmId: String) = "camera_add/$farmId"
    }

    object CameraList : Screen("camera_list/{farmId}") {
        fun createRoute(farmId: String) = "camera_list/$farmId"
    }

    object CameraLive : Screen("camera_live/{cameraUrl}") {
        fun createRoute(cameraUrl: String) = "camera_live/$cameraUrl"
    }

    object CameraEdit : Screen("camera_edit/{cameraId}") {
        fun createRoute(cameraId: String) = "camera_edit/$cameraId"
    }
    // Profile Screen
    object Profile : Screen("profile/{ownerId}") {
        fun createRoute(ownerId: String) = "profile/$ownerId"
    }

    // QR Scanner
    object QRScanner : Screen("qr_scanner")
}