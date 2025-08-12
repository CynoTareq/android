// OwnerDto.kt
package it.cynomys.cfmandroid.auth

import java.util.Date
import java.util.UUID

data class OwnerDto(
    val id: UUID,
    val name: String,
    val email: String,
    val password: String,
    val birthday: Date?,
    val settings: SettingsDto,
    val isFreeUser: Boolean,
    val parentOwnerId: UUID?
)

data class SettingsDto(
    val language: String?,
    val currency: String?,
    val unit: String?,
    val hasAverage: Boolean
)