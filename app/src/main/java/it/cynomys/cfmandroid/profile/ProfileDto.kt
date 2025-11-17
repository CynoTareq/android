package it.cynomys.cfmandroid.profile
import it.cynomys.cfmandroid.auth.DateOnlySerializer
import it.cynomys.cfmandroid.auth.DateSerializer
import it.cynomys.cfmandroid.auth.Owner
import it.cynomys.cfmandroid.auth.Settings
import it.cynomys.cfmandroid.auth.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class ProfileDto(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String,
    val email: String,
    val password: String? = null,
    @Serializable(with = DateSerializer::class)
    val birthday: Date? = null,
    val language: String? = null,
    val currency: String? = null,
    val unit: String? = null,
    val hasAverage: Boolean = false,
    val isFreeUser: Boolean = true,
    @Serializable(with = UUIDSerializer::class)
    val parentOwnerId: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val roleId: UUID? = null
)

// DTO for the PUT /api/owner/{ownerId}/settings request/response
@Serializable
data class ProfileUpdateDto(
    // FIX: Use String type for manual formatting and parsing
    val birthday: String? = null,
    val language: String? = null,
    val currency: String? = null,
    val unit: String? = null,
    val hasAverage: Boolean = false,
    val email: String,
    val name: String
)

// Conversion from the full DTO (used in GET) to the Domain Model (Owner)
fun ProfileDto.toOwner(): Owner {
    return Owner(
        id = id,
        name = name,
        email = email,
        password = password,
        birthday = birthday,
        settings = Settings(
            language = language ?: "en",
            currency = currency ?: "USD",
            unit = unit ?: "metric",
            hasAverage = hasAverage
        ),
        isFreeUser = isFreeUser,
        parentOwnerId = parentOwnerId,
        roleId = roleId
    )
}

// Conversion from Domain Model (Owner) to the update DTO (for PUT /settings)
fun Owner.toProfileUpdateDto(): ProfileUpdateDto {
    return ProfileUpdateDto(
        // FIX: Format the Date to the required "yyyy-MM-dd" String for the API request
        birthday = birthday?.let { DateOnlySerializer.formatDate(it) },
        language = settings?.language,
        currency = settings?.currency,
        unit = settings?.unit,
        hasAverage = settings?.hasAverage ?: false,
        email = email,
        name = name
    )
}

// Conversion from the update DTO (from PUT response) to the Domain Model (Owner)
fun ProfileUpdateDto.toOwner(existingOwner: Owner): Owner {
    return existingOwner.copy(
        name = name,
        email = email,
        // FIX: Parse the "yyyy-MM-dd" String from the API response back to a Date for the Owner model
        birthday = birthday?.let { DateOnlySerializer.parseDate(it) },
        settings = existingOwner.settings?.copy(
            language = language ?: existingOwner.settings.language,
            currency = currency ?: existingOwner.settings.currency,
            unit = unit ?: existingOwner.settings.unit,
            hasAverage = hasAverage
        ) ?: Settings(
            language = language ?: "en",
            currency = currency ?: "USD",
            unit = unit ?: "metric",
            hasAverage = hasAverage
        )
    )
}

fun Owner.toProfileDto(): ProfileDto {
    return ProfileDto(
        id = id,
        name = name,
        email = email,
        password = password,
        birthday = birthday,
        language = settings?.language,
        currency = settings?.currency,
        unit = settings?.unit,
        hasAverage = settings?.hasAverage ?: false,
        isFreeUser = isFreeUser,
        parentOwnerId = parentOwnerId,
        roleId = roleId
    )
}