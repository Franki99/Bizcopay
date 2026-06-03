package com.bizcopay.app.data.network.models

data class CreateTransactionRequest(val amount: Double, val description: String?)
data class NfcTapRequest(val nfcUid: String)
data class ApprovePinRequest(val pin: String)
data class CategoryRequest(val category: String)

data class TransactionUser(val id: String, val name: String)

data class TransactionResponse(
    val id: String,
    val amount: String,
    val currency: String = "RWF",
    val status: String,
    val riskScore: String,
    val merchantId: String,
    val payerId: String?,
    val description: String?,
    val category: String?,
    val failureReason: String?,
    val createdAt: String,
    val merchant: TransactionUser?,
    val payer: TransactionUser?,
)

data class TransactionStatusResponse(val status: String, val riskScore: String?)
