package com.bizcopay.app.data.local

import android.content.Context

class NotificationPreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("bizco_notif_prefs", Context.MODE_PRIVATE)

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(v) { prefs.edit().putBoolean(KEY_ENABLED, v).apply() }

    var topUpEnabled: Boolean
        get() = prefs.getBoolean(KEY_TOP_UP, true)
        set(v) { prefs.edit().putBoolean(KEY_TOP_UP, v).apply() }

    var paymentEnabled: Boolean
        get() = prefs.getBoolean(KEY_PAYMENT, true)
        set(v) { prefs.edit().putBoolean(KEY_PAYMENT, v).apply() }

    companion object {
        private const val KEY_ENABLED  = "notif_enabled"
        private const val KEY_TOP_UP   = "notif_top_up"
        private const val KEY_PAYMENT  = "notif_payment"
    }
}
