package com.bizcopay.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class TokenManager(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs = EncryptedSharedPreferences.create(
        "bizcopay_secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun saveUserId(id: String) = prefs.edit().putString(KEY_USER_ID, id).apply()
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun saveRole(role: String) = prefs.edit().putString(KEY_ROLE, role).apply()
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)
    fun saveName(name: String) = prefs.edit().putString(KEY_NAME, name).apply()
    fun getName(): String? = prefs.getString(KEY_NAME, null)
    fun isLoggedIn() = getToken() != null
    fun clear() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ROLE = "user_role"
        private const val KEY_NAME = "user_name"
    }
}
