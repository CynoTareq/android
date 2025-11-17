package it.cynomys.cfmandroid.auth.signup

import it.cynomys.cfmandroid.auth.DateSerializer
import it.cynomys.cfmandroid.auth.Settings
import it.cynomys.cfmandroid.auth.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    @Serializable(with = DateSerializer::class)
    val birthday: Date? = null,
    // CHANGED: Made settings non-nullable, removing '?' and default object
    val settings: Settings,
    val isFreeUser: Boolean = true,
    @Serializable(with = UUIDSerializer::class)
    val parentOwnerId: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val roleId: UUID? = null
)