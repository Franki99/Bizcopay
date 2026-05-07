package com.bizcopay.app.ui.payer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.data.network.ApiClient
import com.bizcopay.app.data.network.models.ApprovePinRequest
import com.bizcopay.app.data.network.models.WalletResponse
import com.bizcopay.app.data.socket.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PendingPayment(val transactionId: String, val amount: String, val merchantName: String)

sealed class PayerState {
    object Idle : PayerState()
    object Loading : PayerState()
    data class PinRequired(val payment: PendingPayment) : PayerState()
    data class Approved(val amount: String) : PayerState()
    data class Failed(val reason: String) : PayerState()
    data class Error(val message: String) : PayerState()
}

class PayerViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val api = ApiClient.create(tokenManager)
    private var socketManager: SocketManager? = null

    private val _state = MutableStateFlow<PayerState>(PayerState.Idle)
    val state: StateFlow<PayerState> = _state

    private val _wallet = MutableStateFlow<WalletResponse?>(null)
    val wallet: StateFlow<WalletResponse?> = _wallet

    init {
        loadWallet()
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

    private fun connectSocket() {
        val token = tokenManager.getToken() ?: return
        socketManager = SocketManager(token).also { mgr ->
            mgr.connect()
            mgr.onPaymentPendingPin { data ->
                _state.value = PayerState.PinRequired(
                    PendingPayment(
                        transactionId = data.optString("transactionId"),
                        amount = data.optString("amount"),
                        merchantName = data.optString("merchantName", "Merchant")
                    )
                )
            }
            mgr.onPaymentApproved { data ->
                _state.value = PayerState.Approved(data.optString("amount", "?"))
                loadWallet()
            }
            mgr.onPaymentFailed { data ->
                _state.value = PayerState.Failed(data.optString("reason", "Unknown reason"))
            }
        }
    }

    fun submitPin(transactionId: String, pin: String) {
        viewModelScope.launch {
            _state.value = PayerState.Loading
            try {
                val response = api.approveWithPin(transactionId, ApprovePinRequest(pin))
                if (!response.isSuccessful) {
                    _state.value = PayerState.Error("Invalid PIN")
                }
                // Success/failure comes via socket event
            } catch (e: Exception) {
                _state.value = PayerState.Error("Connection failed")
            }
        }
    }

    fun reset() { _state.value = PayerState.Idle }

    override fun onCleared() {
        super.onCleared()
        socketManager?.disconnect()
    }
}
