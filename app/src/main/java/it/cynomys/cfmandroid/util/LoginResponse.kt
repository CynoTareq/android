package it.cynomys.cfmandroid.util

import com.google.gson.annotations.SerializedName
import java.util.Date

data class LoginResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("birthday") val birthday: Date,
    @SerializedName("settings") val settings: Settings
)

data class Settings(
    @SerializedName("language") val language: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("hasAverage") val hasAverage: Boolean
)