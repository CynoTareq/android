// OwnerDto.kt
package it.cynomys.cfmandroid.model

import java.util.Date
import java.util.UUID

data class OwnerDto(
    val id: UUID,
    val name: String,
    val email: String,
    val password: String,
    val birthday: Date?,
    val settings: Settings,
    val isFreeUser: Boolean,
    val parentOwnerId: UUID?
)

data class Settings(
    val language: String?,
    val currency: String?,
    val unit: String?,
    val hasAverage: Boolean
)