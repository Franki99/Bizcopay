package com.bizcopay.app.data.network.models

data class RegisterRequest(val name: String, val email: String, val pin: String, val role: String, val otpCode: String)
data class LoginRequest(val email: String, val pin: String)
data class SendOtpRequest(val email: String, val purpose: String)
data class ResetPinRequest(val email: String, val otpCode: String, val newPin: String)
data class ChangePinRequest(val currentPin: String, val newPin: String)
data class SendChangeEmailOtpRequest(val newEmail: String)
data class ChangeEmailRequest(val newEmail: String, val otpCode: String)
data class AuthResponse(val user: UserDto, val token: String)
data class MessageResponse(val message: String)
data class UserDto(val id: String, val name: String, val email: String, val role: String, val isActive: Boolean)
