package com.bizcopay.app.ui.payer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.data.network.ApiClient
import com.bizcopay.app.data.network.models.CategoryRequest
import com.bizcopay.app.data.network.models.NfcTokenResponse
import com.bizcopay.app.data.network.models.PayerAnalyticsResponse
import com.bizcopay.app.data.network.models.RegisterNfcTokenRequest
import com.bizcopay.app.data.network.models.TransactionResponse
import com.bizcopay.app.data.network.models.WalletResponse
import com.bizcopay.app.data.nfc.NfcEventBus
import com.bizcopay.app.data.socket.SocketManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PayerState {
    object Idle : PayerState()
    data class Approved(val amount: String) : PayerState()
    data class Failed(val reason: String) : PayerState()
    data class Error(val message: String) : PayerState()
}

sealed class NfcRegistrationState {
    object Idle : NfcRegistrationState()
    object ReadingNfc : NfcRegistrationState()
    data class Captured(val uid: String) : NfcRegistrationState()
    object Registering : NfcRegistrationState()
    data class Success(val label: String) : NfcRegistrationState()
    data class Error(val message: String) : NfcRegistrationState()
}

class PayerViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val api = ApiClient.create(tokenManager)
    private var socketManager: SocketManager? = null
    private var nfcReadJob: Job? = null

    private val _state = MutableStateFlow<PayerState>(PayerState.Idle)
    val state: StateFlow<PayerState> = _state

    private val _wallet = MutableStateFlow<WalletResponse?>(null)
    val wallet: StateFlow<WalletResponse?> = _wallet

    private val _registrationState = MutableStateFlow<NfcRegistrationState>(NfcRegistrationState.Idle)
    val registrationState: StateFlow<NfcRegistrationState> = _registrationState

    private val _tokens = MutableStateFlow<List<NfcTokenResponse>>(emptyList())
    val tokens: StateFlow<List<NfcTokenResponse>> = _tokens

    private val _transactions = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val transactions: StateFlow<List<TransactionResponse>> = _transactions

    private val _analytics = MutableStateFlow<PayerAnalyticsResponse?>(null)
    val analytics: StateFlow<PayerAnalyticsResponse?> = _analytics

    init {
        loadWallet()
        loadTokens()
        loadTransactions()
        loadAnalytics()
        connectSocket()
    }

    private fun loadWallet() {
        viewModelScope.launch {
            try {
                val response = api.getWallet()
                if (response.isSuccessful) _wallet.value = response.body()
            } catch (_: Exception) {}
        }
    }

    fun loadTokens() {
        viewModelScope.launch {
            try {
                val response = api.getMyTokens()
                if (response.isSuccessful) _tokens.value = response.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun deactivateToken(tokenId: String) {
        viewModelScope.launch {
            try {
                val response = api.deactivateNfcToken(tokenId)
                if (response.isSuccessful) {
                    _tokens.value = _tokens.value.filter { it.id != tokenId }
                }
            } catch (_: Exception) {}
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            try {
                val r = api.getMyTransactions()
                if (r.isSuccessful) _transactions.value = r.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            try {
                val r = api.getPayerAnalytics()
                if (r.isSuccessful) _analytics.value = r.body()
            } catch (_: Exception) {}
        }
    }

    fun categorizeTransaction(transactionId: String, category: String) {
        viewModelScope.launch {
            try {
                val r = api.categorizeTransaction(transactionId, CategoryRequest(category))
                if (r.isSuccessful) loadTransactions()
            } catch (_: Exception) {}
        }
    }

    private fun connectSocket() {
        val token = tokenManager.getToken() ?: return
        socketManager = SocketManager(token).also { mgr ->
            mgr.connect()
            mgr.onPaymentApproved { data ->
                _state.value = PayerState.Approved(data.optString("amount", "?"))
                loadWallet()
                loadTransactions()
            }
            mgr.onPaymentFailed { data ->
                _state.value = PayerState.Failed(data.optString("reason", "Unknown reason"))
            }
        }
    }

    fun reset() { _state.value = PayerState.Idle }

    // ── NFC Token Registration ────────────────────────────────────────────────

    fun startNfcRegistration() {
        _registrationState.value = NfcRegistrationState.ReadingNfc
        nfcReadJob = viewModelScope.launch {
            NfcEventBus.uid.collect { uid ->
                _registrationState.value = NfcRegistrationState.Captured(uid)
                nfcReadJob?.cancel()
            }
        }
    }

    fun cancelNfcRegistration() {
        nfcReadJob?.cancel()
        _registrationState.value = NfcRegistrationState.Idle
    }

    fun registerToken(uid: String, label: String) {
        viewModelScope.launch {
            _registrationState.value = NfcRegistrationState.Registering
            try {
                val response = api.registerNfcToken(
                    RegisterNfcTokenRequest(uid = uid, label = label.ifBlank { null })
                )
                if (response.isSuccessful) {
                    _registrationState.value = NfcRegistrationState.Success(
                        label.ifBlank { uid }
                    )
                    loadTokens()
                } else {
                    val code = response.code()
                    val msg = if (code == 409) "This token is already registered"
                              else "Registration failed"
                    _registrationState.value = NfcRegistrationState.Error(msg)
                }
            } catch (e: Exception) {
                _registrationState.value = NfcRegistrationState.Error("Cannot reach server")
            }
        }
    }

    fun resetRegistration() {
        nfcReadJob?.cancel()
        _registrationState.value = NfcRegistrationState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        socketManager?.disconnect()
        nfcReadJob?.cancel()
    }
}
