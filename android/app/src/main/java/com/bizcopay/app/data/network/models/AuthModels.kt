package com.bizcopay.app.data.network.models

data class RegisterRequest(val name: String, val email: String, val pin: String, val role: String)
data class LoginRequest(val email: String, val pin: String)
data class AuthResponse(val user: UserDto, val token: String)
data class UserDto(val id: String, val name: String, val email: String, val role: String, val isActive: Boolean)
