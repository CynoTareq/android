// Screen.kt
package it.cynomys.cfmandroid

import java.util.UUID

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Success : Screen("success")
    object FarmList : Screen("farm_list")
    object FarmDetail : Screen("farm_detail/{farmId}") {
        fun createRoute(farmId: String) = "farm_detail/$farmId"
    }
    /*
    object Devices : Screen("farm_detail/{farmId}/devices") {
        fun createRoute(farmId: String) = "farm_detail/$farmId/devices"
    }

     */
    object Weather : Screen("farm_detail/{farmId}/weather") {
        fun createRoute(farmId: String) = "farm_detail/$farmId/weather"
    }
    object Silos : Screen("farm_detail/{farmId}/silos") {
        fun createRoute(farmId: String) = "farm_detail/$farmId/silos"
    }
    object Webcams : Screen("farm_detail/{farmId}/webcams") {
        fun createRoute(farmId: String) = "farm_detail/$farmId/webcams"
    }
    object AddFarm : Screen("add_farm")
    object EditFarm : Screen("edit_farm/{farmId}") {
        fun createRoute(farmId: String) = "edit_farm/$farmId"
    }
    object CameraLiveView : Screen("camera_live/{cameraId}") {
        fun createRoute(cameraId: String) = "camera_live/$cameraId"
    }
    object DeviceList : Screen("device_list/{ownerId}/{farmId}") {
        fun createRoute(ownerId: UUID, farmId: UUID) = "device_list/$ownerId/$farmId"
    }

    object DeviceDetail : Screen("device_detail/{deviceId}") {
        fun createRoute(deviceId: String) = "device_detail/$deviceId"
    }

    object DeviceEdit : Screen("edit_device/{deviceId}") {
        fun createRoute(deviceId: UUID) = "edit_device/$deviceId"
    }

    object DeviceAdd : Screen("add_device/{ownerId}/{farmId}") {
        fun createRoute(ownerId: UUID, farmId: UUID) = "add_device/$ownerId/$farmId"
    }
}