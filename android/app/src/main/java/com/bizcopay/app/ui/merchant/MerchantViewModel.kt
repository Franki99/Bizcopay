package com.bizcopay.app.ui.merchant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.data.network.ApiClient
import com.bizcopay.app.data.network.models.ApprovePinRequest
import com.bizcopay.app.data.network.models.CreateTransactionRequest
import com.bizcopay.app.data.network.models.MerchantAnalyticsResponse
import com.bizcopay.app.data.network.models.NfcTapRequest
import com.bizcopay.app.data.network.models.TransactionResponse
import com.bizcopay.app.data.socket.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MerchantState {
    object Idle : MerchantState()
    object Loading : MerchantState()
    data class WaitingForNfc(val transactionId: String, val amount: String) : MerchantState()
    data class WaitingForPin(val transactionId: String, val amount: String) : MerchantState()
    data class Approved(val amount: String) : MerchantState()
    data class Failed(val reason: String) : MerchantState()
    data class Error(val message: String) : MerchantState()
}

class MerchantViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val api = ApiClient.create(tokenManager)
    private var socketManager: SocketManager? = null

    private val _state = MutableStateFlow<MerchantState>(MerchantState.Idle)
    val state: StateFlow<MerchantState> = _state

    private val _history = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val history: StateFlow<List<TransactionResponse>> = _history

    private val _analytics = MutableStateFlow<MerchantAnalyticsResponse?>(null)
    val analytics: StateFlow<MerchantAnalyticsResponse?> = _analytics

    fun createTransaction(amount: Double, description: String) {
        viewModelScope.launch {
            _state.value = MerchantState.Loading
            try {
                val response = api.createTransaction(CreateTransactionRequest(amount, description.ifBlank { null }))
                if (response.isSuccessful) {
                    val tx = response.body()!!
                    connectSocket(tx.id)
                    _state.value = MerchantState.WaitingForNfc(tx.id, tx.amount)
                } else {
                    _state.value = MerchantState.Error("Failed to create transaction")
                }
            } catch (e: Exception) {
                _state.value = MerchantState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    // Called when merchant's phone reads the NFC UID.
    // On emulator: triggered by the "Simulate NFC Tap" button with a hardcoded UID.
    // On physical device: replace with real NFC UID from NfcAdapter.
    fun onNfcRead(transactionId: String, nfcUid: String) {
        viewModelScope.launch {
            _state.value = MerchantState.Loading
            try {
                val response = api.resolveNfc(transactionId, NfcTapRequest(nfcUid))
                if (!response.isSuccessful) {
                    _state.value = MerchantState.Error("NFC tap failed — token not registered?")
                }
                // Approved/failed/pending_pin result arrives via socket
            } catch (e: Exception) {
                _state.value = MerchantState.Error("Connection failed")
            }
        }
    }

    private fun connectSocket(transactionId: String) {
        val token = tokenManager.getToken() ?: return
        socketManager?.disconnect()
        socketManager = SocketManager(token).also { mgr ->
            mgr.connect()
            mgr.joinTransaction(transactionId)
            mgr.onPaymentApproved { data ->
                _state.value = MerchantState.Approved(data.optString("amount", "?"))
            }
            mgr.onPaymentFailed { data ->
                _state.value = MerchantState.Failed(data.optString("reason", "Unknown reason"))
            }
            mgr.onPaymentPendingPin { data ->
                _state.value = MerchantState.WaitingForPin(
                    transactionId = data.optString("transactionId"),
                    amount = data.optString("amount")
                )
            }
        }
    }

    fun submitPin(transactionId: String, pin: String) {
        viewModelScope.launch {
            _state.value = MerchantState.Loading
            try {
                val response = api.approveWithPin(transactionId, ApprovePinRequest(pin))
                if (!response.isSuccessful) {
                    _state.value = MerchantState.Error("Incorrect PIN")
                }
                // Approved/failed result arrives via socket
            } catch (e: Exception) {
                _state.value = MerchantState.Error("Connection failed")
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                val r = api.getMyTransactions()
                if (r.isSuccessful) _history.value = r.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            try {
                val r = api.getMerchantAnalytics()
                if (r.isSuccessful) _analytics.value = r.body()
            } catch (_: Exception) {}
        }
    }

    fun reset() {
        socketManager?.disconnect()
        socketManager = null
        _state.value = MerchantState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        socketManager?.disconnect()
    }
}
