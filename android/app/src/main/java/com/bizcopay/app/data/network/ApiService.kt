package com.bizcopay.app.data.network

import com.bizcopay.app.data.network.models.*
import okhttp3.ResponseBody
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

    @POST("api/auth/change-pin")
    suspend fun changePin(@Body body: ChangePinRequest): Response<MessageResponse>

    @POST("api/auth/send-change-email-otp")
    suspend fun sendChangeEmailOtp(@Body body: SendChangeEmailOtpRequest): Response<MessageResponse>

    @POST("api/auth/change-email")
    suspend fun changeEmail(@Body body: ChangeEmailRequest): Response<MessageResponse>

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

    @GET("api/transactions/my")
    suspend fun getMyTransactions(): Response<List<TransactionResponse>>

    @PATCH("api/transactions/{id}/category")
    suspend fun categorizeTransaction(@Path("id") id: String, @Body body: CategoryRequest): Response<TransactionResponse>

    @GET("api/transactions/my/export")
    suspend fun exportTransactions(): Response<ResponseBody>

    @GET("api/analytics/payer")
    suspend fun getPayerAnalytics(@Query("period") period: String = "year"): Response<PayerAnalyticsResponse>

    @GET("api/analytics/merchant")
    suspend fun getMerchantAnalytics(@Query("period") period: String = "year"): Response<MerchantAnalyticsResponse>

    @POST("api/nfc")
    suspend fun registerNfcToken(@Body body: RegisterNfcTokenRequest): Response<NfcTokenResponse>

    @GET("api/nfc")
    suspend fun getMyTokens(): Response<List<NfcTokenResponse>>

    @PATCH("api/nfc/{id}/deactivate")
    suspend fun deactivateNfcToken(@Path("id") id: String): Response<NfcTokenResponse>
}
