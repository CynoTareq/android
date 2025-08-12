package it.cynomys.cfmandroid

sealed class Screen(val route: String) {
    object Login : Screen("login")
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