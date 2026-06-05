package com.bizcopay.app.data.local

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

enum class NfcDeviceType { CARD, RING, BRACELET }

data class NfcDeviceInfo(
    val type: NfcDeviceType,
    val registeredAt: Long,
    val customLabel: String? = null,
)

object NfcDeviceTypeStore {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (!::prefs.isInitialized)
            prefs = context.getSharedPreferences("nfc_device_types", Context.MODE_PRIVATE)
    }

    fun save(uid: String, type: NfcDeviceType) {
        if (!::prefs.isInitialized) return
        val existing = try { JSONObject(prefs.getString(uid, "{}") ?: "{}") } catch (_: Exception) { JSONObject() }
        existing.put("type", type.name)
        if (!existing.has("ts")) existing.put("ts", System.currentTimeMillis())
        prefs.edit().putString(uid, existing.toString()).apply()
    }

    fun updateLabel(uid: String, label: String) {
        if (!::prefs.isInitialized) return
        val existing = try { JSONObject(prefs.getString(uid, "{}") ?: "{}") } catch (_: Exception) { JSONObject() }
        existing.put("customLabel", label)
        prefs.edit().putString(uid, existing.toString()).apply()
    }

    fun getInfo(uid: String): NfcDeviceInfo {
        if (!::prefs.isInitialized) return NfcDeviceInfo(NfcDeviceType.CARD, System.currentTimeMillis())
        return try {
            val raw = prefs.getString(uid, null) ?: return NfcDeviceInfo(NfcDeviceType.CARD, System.currentTimeMillis())
            val j = JSONObject(raw)
            NfcDeviceInfo(
                type = NfcDeviceType.valueOf(j.optString("type", "CARD")),
                registeredAt = j.optLong("ts", System.currentTimeMillis()),
                customLabel = if (j.has("customLabel")) j.getString("customLabel") else null,
            )
        } catch (_: Exception) {
            NfcDeviceInfo(NfcDeviceType.CARD, System.currentTimeMillis())
        }
    }

    fun remove(uid: String) {
        if (::prefs.isInitialized) prefs.edit().remove(uid).apply()
    }
}
