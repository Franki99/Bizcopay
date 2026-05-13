package com.bizcopay.app.data.network.models

data class RegisterNfcTokenRequest(val uid: String, val label: String?)
data class NfcTokenResponse(val id: String, val uid: String, val label: String?, val isActive: Boolean)
