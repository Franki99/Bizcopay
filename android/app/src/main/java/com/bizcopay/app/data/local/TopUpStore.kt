package com.bizcopay.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class TopUpRecord(
    val id: String,
    val amount: String,
    val balanceAfter: String,
    val timestamp: Long,
)

object TopUpStore {
    private lateinit var prefs: SharedPreferences

    private val _topUps = MutableStateFlow<List<TopUpRecord>>(emptyList())
    val topUps: StateFlow<List<TopUpRecord>> = _topUps.asStateFlow()

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.getSharedPreferences("bizco_topup_store", Context.MODE_PRIVATE)
            _topUps.value = loadFromPrefs()
        }
    }

    fun add(amount: String, balanceAfter: String) {
        val record = TopUpRecord(
            id = UUID.randomUUID().toString(),
            amount = amount,
            balanceAfter = balanceAfter,
            timestamp = System.currentTimeMillis(),
        )
        val updated = (listOf(record) + _topUps.value).take(100)
        _topUps.value = updated
        if (::prefs.isInitialized) saveToPrefs(updated)
    }

    fun clearAll() {
        _topUps.value = emptyList()
        if (::prefs.isInitialized) saveToPrefs(emptyList())
    }

    private fun saveToPrefs(items: List<TopUpRecord>) {
        val arr = JSONArray()
        items.forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id); put("amount", r.amount)
                put("balanceAfter", r.balanceAfter); put("timestamp", r.timestamp)
            })
        }
        prefs.edit().putString("topups", arr.toString()).apply()
    }

    private fun loadFromPrefs(): List<TopUpRecord> {
        return try {
            val json = prefs.getString("topups", null) ?: return emptyList()
            val arr = JSONArray(json)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                TopUpRecord(o.getString("id"), o.getString("amount"), o.getString("balanceAfter"), o.getLong("timestamp"))
            }
        } catch (_: Exception) { emptyList() }
    }
}
