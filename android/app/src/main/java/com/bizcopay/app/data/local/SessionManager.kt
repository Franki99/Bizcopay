package com.bizcopay.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    private val _expired = MutableStateFlow(false)
    val expired: StateFlow<Boolean> = _expired

    fun triggerExpiry() { _expired.value = true }
    fun resetExpiry()   { _expired.value = false }
}
