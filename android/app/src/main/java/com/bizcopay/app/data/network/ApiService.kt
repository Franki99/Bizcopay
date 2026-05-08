package com.bizcopay.app.data.network

import com.bizcopay.app.data.network.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/otp/send")
    suspend fun sendOtp(@Body body: SendOtpRequest): Response<MessageResponse>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/auth/reset-pin")
    suspend fun resetPin(@Body body: ResetPinRequest): Response<MessageResponse>

    @GET("api/users/me")
    suspend fun getMe(): Response<AuthResponse>

    @GET("api/wallets/me")
    suspend fun getWallet(): Response<WalletResponse>

    @POST("api/transactions")
    suspend fun createTransaction(@Body body: CreateTransactionRequest): Response<TransactionResponse>

    @POST("api/transactions/{id}/nfc")
    suspend fun resolveNfc(@Path("id") id: String, @Body body: NfcTapRequest): Response<TransactionStatusResponse>

    @POST("api/transactions/{id}/approve")
    suspend fun approveWithPin(@Path("id") id: String, @Body body: ApprovePinRequest): Response<TransactionStatusResponse>

    @GET("api/transactions")
    suspend fun getTransactions(): Response<List<TransactionResponse>>
}
