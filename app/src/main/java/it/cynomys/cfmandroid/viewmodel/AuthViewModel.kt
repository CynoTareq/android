package it.cynomys.cfmandroid.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import it.cynomys.cfmandroid.model.Owner
import it.cynomys.cfmandroid.model.LoginRequest
import it.cynomys.cfmandroid.util.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel : ViewModel() {

    private val networkService = NetworkService()

    // UI state for user session
    private val _userSession = MutableStateFlow<Owner?>(null)
    val userSession: StateFlow<Owner?> = _userSession

    // UI state for error handling
    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError

    // Login Owner
    fun loginOwner(email: String, password: String) {
        val loginRequest = LoginRequest(email = email, password = password)

        viewModelScope.launch {
            try {
                val result = networkService.post<LoginRequest, Owner>(
                    path = "api/owner/authenticate",
                    body = loginRequest,
                    responseType = Owner::class.java
                )
                when {
                    result.isSuccess -> {
                        _userSession.value = result.getOrNull()
                        println("Success login owner: ${result.getOrNull()}")
                    }
                    else -> {
                        println("Error: ${result.exceptionOrNull()}")
                        _isError.value = true
                    }
                }
            } catch (e: Exception) {
                println("Error: $e")
                _isError.value = true
            }
        }
    }



    // Logout Owner
    fun logoutOwner() {
        _userSession.value = null
        _isError.value = false
    }
}