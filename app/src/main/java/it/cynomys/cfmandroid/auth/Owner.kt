package it.cynomys.cfmandroid.auth

import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

// --- ENUM DEFINITIONS ---
enum class LanguageEnum(val value: String, val label: String) {
    ENGLISH("en", "English"),
    ITALIAN("it", "Italian"),
    FRENCH("fr", "French"),
    SPANISH("es", "Spanish"),
    CATALAN("ca", "Catalan");

    companion object {
        fun fromValue(value: String): LanguageEnum? = entries.find { it.value == value }
    }
}

enum class CurrencyEnum(val value: String, val label: String) {
    EUR("EUR", "EUR - Euro"),
    USD("USD", "USD - US Dollar"),
    GBP("GBP", "GBP - British Pound"),
    CAD("CAD", "CAD - Canadian Dollar"),
    AUD("AUD", "AUD - Australian Dollar"),
    JPY("JPY", "JPY - Japanese Yen");

    companion object {
        fun fromValue(value: String): CurrencyEnum? = entries.find { it.value == value }
    }
}

enum class UnitEnum(val value: String, val label: String) {
    METRIC("metric", "Metric"),
    IMPERIAL("imperial", "Imperial");

    companion object {
        fun fromValue(value: String): UnitEnum? = entries.find { it.value == value }
    }
}
// ----------------------------

@Serializable
data class Owner(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID?,
    val name: String,
    val email: String,
    val password: String? = null,
    @Serializable(with = DateSerializer::class)
    val birthday: Date? = null,
    val settings: Settings? = null,
    val isFreeUser: Boolean,
    @Serializable(with = UUIDSerializer::class)
    val parentOwnerId: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val roleIds: UUID? = null
)

@Serializable
data class Settings(
    val language: String,
    val currency: String,
    val unit: String,
    val hasAverage: Boolean
)