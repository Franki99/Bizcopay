package com.bizcopay.app.data.network.models

data class CreateTransactionRequest(val amount: Double, val description: String?)
data class NfcTapRequest(val nfcUid: String)
data class ApprovePinRequest(val pin: String)

data class TransactionResponse(
    val id: String,
    val amount: String,
    val status: String,
    val riskScore: String,
    val merchantId: String,
    val payerId: String?,
    val description: String?
)

data class TransactionStatusResponse(val status: String, val riskScore: String?)
