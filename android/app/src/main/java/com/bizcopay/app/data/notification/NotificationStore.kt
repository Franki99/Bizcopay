package com.bizcopay.app.data.notification

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.util.UUID

object NotificationStore {

    private lateinit var prefs: SharedPreferences
    private var currentUserId = ""

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    val unreadCount: Int get() = _notifications.value.count { !it.isRead }

    fun init(context: Context, userId: String) {
        if (!::prefs.isInitialized || currentUserId != userId) {
            currentUserId = userId
            prefs = context.getSharedPreferences("bizco_notif_$userId", Context.MODE_PRIVATE)
            _notifications.value = loadFromPrefs()
        }
    }

    fun add(title: String, body: String, type: String) {
        val item = NotificationItem(
            id        = UUID.randomUUID().toString(),
            title     = title,
            body      = body,
            timestamp = System.currentTimeMillis(),
            isRead    = false,
            type      = type,
        )
        val updated = (listOf(item) + _notifications.value).take(50)
        _notifications.value = updated
        if (::prefs.isInitialized) saveToPrefs(updated)
    }

    fun markAllRead() {
        val updated = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = updated
        if (::prefs.isInitialized) saveToPrefs(updated)
    }

    fun addIfAbsent(
        id: String,
        title: String,
        body: String,
        type: String,
        createdAt: String,
        onlyBefore: Long? = null,
    ) {
        val timestampMs = parseIso(createdAt) ?: System.currentTimeMillis()
        if (onlyBefore != null && timestampMs >= onlyBefore) return
        if (_notifications.value.any { it.id == id }) return
        val item = NotificationItem(
            id        = id,
            title     = title,
            body      = body,
            timestamp = timestampMs,
            isRead    = false,
            type      = type,
        )
        val updated = (_notifications.value + item)
            .sortedByDescending { it.timestamp }
            .take(50)
        _notifications.value = updated
        if (::prefs.isInitialized) saveToPrefs(updated)
    }

    fun clearAll() {
        _notifications.value = emptyList()
        if (::prefs.isInitialized) saveToPrefs(emptyList())
    }

    private fun saveToPrefs(items: List<NotificationItem>) {
        val arr = JSONArray()
        items.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("notifications", arr.toString()).apply()
    }

    private fun loadFromPrefs(): List<NotificationItem> {
        return try {
            val json = prefs.getString("notifications", null) ?: return emptyList()
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getJSONObject(it).toNotificationItem() }
        } catch (_: Exception) { emptyList() }
    }

    private fun parseIso(s: String): Long? = try {
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            .also { it.timeZone = java.util.TimeZone.getTimeZone("UTC") }
            .parse(s.take(19))?.time
    } catch (_: Exception) { null }
}
