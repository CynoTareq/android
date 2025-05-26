// Owner.kt
package it.cynomys.cfmandroid.model

import java.util.*

data class Owner(
    val id: UUID?,
    val name: String,
    val email: String,
    val password: String,
    val birthday: Date?,
    val settings: Settings,
    val isFreeUser: Boolean,
    val parentOwnerId: UUID?
)
