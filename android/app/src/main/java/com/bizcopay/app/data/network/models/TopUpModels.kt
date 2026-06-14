package com.bizcopay.app.data.network.models

data class TopUpRequestBody(
    val amount: Double,
    val note: String? = null,
)

data class TopUpRequestResponse(
    val id: String,
    val amount: String,
    val note: String?,
    val status: String,
    val createdAt: String,
)
