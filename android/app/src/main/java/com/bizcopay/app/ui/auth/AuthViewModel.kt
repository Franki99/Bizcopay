package com.bizcopay.app.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.data.network.ApiClient
import com.bizcopay.app.data.network.models.LoginRequest
import com.bizcopay.app.data.network.models.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val api = ApiClient.create()

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, pin: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = api.login(LoginRequest(email, pin))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    saveSession(body.token, body.user.id, body.user.role, body.user.name)
                    _state.value = AuthState.Success(body.user.role)
                } else {
                    _state.value = AuthState.Error("Invalid email or PIN")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    fun register(name: String, email: String, pin: String, role: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = api.register(RegisterRequest(name, email, pin, role))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    saveSession(body.token, body.user.id, body.user.role, body.user.name)
                    _state.value = AuthState.Success(body.user.role)
                } else {
                    _state.value = AuthState.Error("Registration failed. Email may already exist.")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    private fun saveSession(token: String, userId: String, role: String, name: String) {
        tokenManager.saveToken(token)
        tokenManager.saveUserId(userId)
        tokenManager.saveRole(role)
        tokenManager.saveName(name)
    }
}
