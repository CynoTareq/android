// Camera.kt
package it.cynomys.cfmandroid.camera

import java.util.UUID

data class Camera(
    val id: UUID?,
    val cameraLink: String,
    val displayName: String
)

data class CameraDto(
    val cameraLink: String,
    val displayName: String
)