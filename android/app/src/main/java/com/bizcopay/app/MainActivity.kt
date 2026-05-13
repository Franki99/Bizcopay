package com.bizcopay.app

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.data.nfc.NfcEventBus
import com.bizcopay.app.ui.navigation.NavGraph
import com.bizcopay.app.ui.navigation.Screen
import com.bizcopay.app.ui.theme.BizcopayTheme

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcPendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        val tokenManager = TokenManager(this)
        val startDestination = when {
            !tokenManager.isLoggedIn() -> Screen.Login.route
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
