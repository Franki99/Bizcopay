package com.bizcopay.app

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.bizcopay.app.data.local.SessionManager
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.data.nfc.NfcEventBus
import com.bizcopay.app.data.notification.NotificationHelper
import com.bizcopay.app.ui.navigation.NavGraph
import com.bizcopay.app.ui.navigation.Screen
import com.bizcopay.app.ui.theme.BizcopayTheme

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcPendingIntent: PendingIntent
    private lateinit var tokenManager: TokenManager

    companion object {
        private const val SESSION_TIMEOUT_MS = 2 * 60 * 1000L  // 2 minutes
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(this)
        NotificationHelper.createChannels(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        val startDestination = when {
            !tokenManager.isLoggedIn() -> Screen.Splash.route
            tokenManager.getRole() == "MERCHANT" -> Screen.Merchant.route
            else -> Screen.Payer.route
        }

        setContent {
            BizcopayTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController, startDestination = startDestination)
            }
        }
    }

    // ── Session timeout ────────────────────────────────────────────────────────

    override fun onStop() {
        super.onStop()
        // Record when the app went to background (only while a user is logged in)
        if (tokenManager.isLoggedIn()) {
            tokenManager.saveBackgroundTime(System.currentTimeMillis())
        }
    }

    override fun onStart() {
        super.onStart()
        val bg = tokenManager.getBackgroundTime()
        if (bg > 0L && System.currentTimeMillis() - bg > SESSION_TIMEOUT_MS) {
            tokenManager.clearAuth()
            SessionManager.triggerExpiry()
        }
        tokenManager.clearBackgroundTime()
    }

    // ── NFC ────────────────────────────────────────────────────────────────────

    override fun onResume() {
        super.onResume()
        // Enable foreground dispatch so NFC taps go directly to this activity
        // while the app is in the foreground, bypassing the system NFC handler.
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        tag?.let {
            val uid = it.id.joinToString("") { byte -> "%02X".format(byte) }
            NfcEventBus.emit(uid)
        }
    }
}
