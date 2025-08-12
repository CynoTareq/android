// Owner.kt
package it.cynomys.cfmandroid.auth

import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class Owner(
    // Added @Serializable(with = UUIDSerializer::class) to ensure UUIDs are handled correctly during serialization/deserialization.
    @Serializable(with = UUIDSerializer::class)
    val id: UUID?,
    val name: String,
    val email: String,
    val password: String? = null, // Consider removing this for security if not strictly needed in client-side model
    @Serializable(with = DateSerializer::class) // Already correctly annotated
    val birthday: Date? = null,
    val settings: Settings? = null,
    val isFreeUser: Boolean,
    @Serializable(with = UUIDSerializer::class) // Added @Serializable for parentOwnerId
    val parentOwnerId: UUID? = null,
    // Added roleId as per your provided JSON response.
    // Ensure it's also serialized correctly if it's a UUID.
    @Serializable(with = UUIDSerializer::class)
    val roleId: UUID? = null
)

@Serializable
data class Settings(
    val language: String?,
    val currency: String?,
    val unit: String?,
    val hasAverage: Boolean
)
