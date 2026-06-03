package com.bizcopay.app.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.data.network.ApiClient
import com.bizcopay.app.data.network.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
    data class RegisterOtpSent(val email: String) : AuthState()
    data class ResetOtpSent(val email: String) : AuthState()
    object PinResetSuccess : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val api = ApiClient.create()

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun resetState() { _state.value = AuthState.Idle }

    fun login(email: String, pin: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = api.login(LoginRequest(email, pin))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    saveSession(body.token, body.user.id, body.user.role, body.user.name, body.user.email)
                    _state.value = AuthState.Success(body.user.role)
                } else {
                    _state.value = AuthState.Error("Invalid email or PIN")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    fun sendRegistrationOtp(email: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = api.sendOtp(SendOtpRequest(email, "REGISTER"))
                if (response.isSuccessful) {
                    _state.value = AuthState.RegisterOtpSent(email)
                } else {
                    _state.value = AuthState.Error("Failed to send code. Check your email.")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    fun register(name: String, email: String, pin: String, role: String, otpCode: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = api.register(RegisterRequest(name, email, pin, role, otpCode))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    saveSession(body.token, body.user.id, body.user.role, body.user.name, body.user.email)
                    _state.value = AuthState.Success(body.user.role)
                } else {
                    _state.value = AuthState.Error("Invalid code or email already registered.")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    fun sendResetOtp(email: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = api.sendOtp(SendOtpRequest(email, "RESET_PIN"))
                if (response.isSuccessful) {
                    _state.value = AuthState.ResetOtpSent(email)
                } else {
                    _state.value = AuthState.Error("Failed to send code. Check your email.")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    fun resetPin(email: String, otpCode: String, newPin: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = api.resetPin(ResetPinRequest(email, otpCode, newPin))
                if (response.isSuccessful) {
                    _state.value = AuthState.PinResetSuccess
                } else {
                    _state.value = AuthState.Error("Invalid or expired code.")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    private fun saveSession(token: String, userId: String, role: String, name: String, email: String) {
        tokenManager.saveToken(token)
        tokenManager.saveUserId(userId)
        tokenManager.saveRole(role)
        tokenManager.saveName(name)
        tokenManager.saveEmail(email)
    }
}
