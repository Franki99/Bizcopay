package com.bizcopay.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.bizcopay.app.MainActivity
import com.bizcopay.app.data.local.NotificationPreferencesManager
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {

    const val CHANNEL_PAYMENT = "bizco_payment"
    const val CHANNEL_WALLET  = "bizco_wallet"

    private val notifId = AtomicInteger(1000)

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_PAYMENT, "Payments", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Payment approved and failed alerts"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_WALLET, "Wallet", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Wallet top-up and balance updates"
            }
        )
    }

    fun showPaymentApproved(context: Context, amount: String) {
        val p = NotificationPreferencesManager(context)
        if (!p.notificationsEnabled || !p.paymentEnabled) return
        show(context, CHANNEL_PAYMENT, "Payment Successful", "RWF $amount was deducted from your wallet")
    }

    fun showPaymentFailed(context: Context, reason: String) {
        val p = NotificationPreferencesManager(context)
        if (!p.notificationsEnabled || !p.paymentEnabled) return
        show(context, CHANNEL_PAYMENT, "Payment Failed", reason)
    }

    fun showWalletToppedUp(context: Context, amount: String) {
        val p = NotificationPreferencesManager(context)
        if (!p.notificationsEnabled || !p.topUpEnabled) return
        show(context, CHANNEL_WALLET, "Wallet Topped Up", "RWF $amount has been added to your wallet")
    }

    fun showPaymentReceived(context: Context, amount: String) {
        val p = NotificationPreferencesManager(context)
        if (!p.notificationsEnabled || !p.paymentEnabled) return
        show(context, CHANNEL_PAYMENT, "Payment Received", "You received RWF $amount")
    }

    private fun show(context: Context, channel: String, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, notifId.get(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(notifId.getAndIncrement(), notif)
    }
}
