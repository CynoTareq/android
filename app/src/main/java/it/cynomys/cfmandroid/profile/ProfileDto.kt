package it.cynomys.cfmandroid.profile
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

fun ProfileDto.toOwner(): Owner {
    return Owner(
        id = id,
        name = name,
        email = email,
        password = password,
        birthday = birthday,
        settings = Settings(
            language = language,
            currency = currency,
            unit = unit,
            hasAverage = hasAverage
        ),
        isFreeUser = isFreeUser,
        parentOwnerId = parentOwnerId,
        roleId = roleId
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
