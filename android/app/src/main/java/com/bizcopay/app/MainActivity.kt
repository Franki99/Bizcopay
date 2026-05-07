package com.bizcopay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.ui.navigation.NavGraph
import com.bizcopay.app.ui.navigation.Screen
import com.bizcopay.app.ui.theme.BizcopayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}
