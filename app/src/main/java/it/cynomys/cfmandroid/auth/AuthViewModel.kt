package it.cynomys.cfmandroid.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.cynomys.cfmandroid.auth.signup.SignupRequest
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.util.Date
import java.util.UUID

class AuthViewModel(private val context: Context? = null) : ViewModel() {

    private val networkService = NetworkService()
    private val prefs = context?.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_SESSION_KEY = "user_session"
        private const val IS_LOGGED_IN_KEY = "is_logged_in"
    }

    private val json = Json {
        serializersModule = SerializersModule {
            contextual(UUID::class, UUIDSerializer)
            contextual(Date::class, DateSerializer)
        }
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val _userSession = MutableStateFlow<Owner?>(null)
    val userSession: StateFlow<Owner?> = _userSession

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSessionRestored = MutableStateFlow(false)
    val isSessionRestored: StateFlow<Boolean> = _isSessionRestored

    private val _signupEvents = MutableSharedFlow<Boolean>(
        replay = 0,               // Do not replay past events to new collectors
        extraBufferCapacity = 1   // Allow one event to be sent before a collector is ready
    )
    val signupEvents: SharedFlow<Boolean> = _signupEvents.asSharedFlow()

    init {
        if (context != null) {
            restoreUserSession()
        } else {
            _isSessionRestored.value = true
        }
    }

    fun loginOwner(email: String, password: String) {
        val loginRequest = LoginRequest(email = email, password = password)

        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch {
            try {
                val result = networkService.post<LoginRequest, Owner>(
                    path = "api/owner/authenticate",
                    body = loginRequest,
                    responseType = Owner::class.java
                )
                when {
                    result.isSuccess -> {
                        val owner = result.getOrNull()
                        println("This is owner thats saved ------------------>")
                        println(owner.toString())
                        _userSession.value = owner

                        saveUserSession(owner)
                        Log.d("AuthViewModel", "User logged in: ${owner!!.email}, ID: ${owner.id}")
                        println("Success login owner: $owner")
                    }
                    else -> {
                        println("Error: ${result.exceptionOrNull()}")
                        _isError.value = true
                    }
                }
            } catch (e: Exception) {
                println("Error: $e")
                _isError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logoutOwner() {
        _userSession.value = null
        _isError.value = false
        clearUserSession()
    }

    fun isUserLoggedInInMemory(): Boolean {
        return _userSession.value != null
    }

    private fun saveUserSession(owner: Owner?) {
        prefs?.let { preferences ->
            val editor = preferences.edit()
            if (owner != null) {
                try {
                    val ownerJson = json.encodeToString(Owner.serializer(),owner)
                    println("Serialized owner JSON: $ownerJson")
                    editor.putString(USER_SESSION_KEY, ownerJson)
                    editor.putBoolean(IS_LOGGED_IN_KEY, true)
                } catch (e: Exception) {
                    println("Error saving user session: ${e.message}")
                    editor.putBoolean(IS_LOGGED_IN_KEY, true)
                }
            } else {
                editor.remove(USER_SESSION_KEY)
                editor.putBoolean(IS_LOGGED_IN_KEY, false)
            }
            editor.apply()
        }
    }

    private fun restoreUserSession() {
        viewModelScope.launch {
            prefs?.let { preferences ->
                try {
                    val isLoggedIn = preferences.getBoolean(IS_LOGGED_IN_KEY, false)
                    println("Checking if user is logged in: $isLoggedIn")

                    if (isLoggedIn) {
                        val ownerJson = preferences.getString(USER_SESSION_KEY, null)
                        println("Retrieved owner JSON: $ownerJson")

                        if (ownerJson != null) {
                            val owner = json.decodeFromString<Owner>(ownerJson)
                            _userSession.value = owner
                            println("Restored user session: ${owner.name} (${owner.email})")
                        } else {
                            println("No owner data found, clearing session")
                            clearUserSession()
                        }
                    }
                } catch (e: Exception) {
                    println("Error restoring user session: $e")
                    clearUserSession()
                } finally {
                    _isSessionRestored.value = true
                }
            } ?: run {
                _isSessionRestored.value = true
            }
        }
    }

    private fun clearUserSession() {
        prefs?.let { preferences ->
            val editor = preferences.edit()
            editor.remove(USER_SESSION_KEY)
            editor.putBoolean(IS_LOGGED_IN_KEY, false)
            editor.apply()
            println("Cleared user session from SharedPreferences")
        }
    }

    fun validateSession() {
        viewModelScope.launch {
            val currentUser = _userSession.value
            if (currentUser != null) {
                try {
                    println("Session is valid for user: ${currentUser.email}")
                } catch (e: Exception) {
                    println("Session validation failed: $e")
                    logoutOwner()
                }
            }
        }
    }

    // MODIFIED: To accept Settings and to only signal success, not log the user in.
    fun signupOwner(name: String, email: String, password: String, settings: Settings) {
        val signupRequest = SignupRequest(
            name = name,
            email = email,
            password = password,
            settings = settings
        )

        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch {
            try {
                val result = networkService.post<SignupRequest, Owner>(
                    path = "api/owner",
                    body = signupRequest,
                    responseType = Owner::class.java
                )
                when {
                    result.isSuccess -> {
                        // REMOVED: Automatic login logic
                        _signupEvents.emit(true)
                        println("Owner created successfully ------------------>")
                        println(result.getOrNull().toString())
                        Log.d("AuthViewModel", "User signed up successfully")
                    }
                    else -> {
                        println("Error: ${result.exceptionOrNull()}")
                        _isError.value = true
                    }
                }
            } catch (e: Exception) {
                println("Error during signup: $e")
                _isError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun updateLanguage(languageCode: String) {
        val updatedOwner = _userSession.value?.copy(
            settings = _userSession.value?.settings?.copy(
                language = languageCode
            )
        )

        _userSession.value = updatedOwner
        saveUserSession(updatedOwner)
    }


    fun updateSessionOwner(updatedOwner: Owner) {
        val currentOwner = _userSession.value

        val safeOwner = updatedOwner.copy(
            id = updatedOwner.id ?: currentOwner?.id
        )

        _userSession.value = safeOwner
        saveUserSession(safeOwner)
    }



}