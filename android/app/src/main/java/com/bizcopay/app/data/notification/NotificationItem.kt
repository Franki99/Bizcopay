package com.bizcopay.app.data.notification

import org.json.JSONObject

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: String,   // "payment" | "wallet"
)

fun NotificationItem.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("body", body)
    put("timestamp", timestamp)
    put("isRead", isRead)
    put("type", type)
}

fun JSONObject.toNotificationItem() = NotificationItem(
    id        = getString("id"),
    title     = getString("title"),
    body      = getString("body"),
    timestamp = getLong("timestamp"),
    isRead    = getBoolean("isRead"),
    type      = getString("type"),
)
