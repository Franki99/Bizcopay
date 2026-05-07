package com.bizcopay.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bizcopay.app.ui.auth.LoginScreen
import com.bizcopay.app.ui.auth.RegisterScreen
import com.bizcopay.app.ui.merchant.MerchantScreen
import com.bizcopay.app.ui.payer.PayerScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Merchant : Screen("merchant")
    object Payer : Screen("payer")
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Merchant.route) { MerchantScreen(navController) }
        composable(Screen.Payer.route) { PayerScreen(navController) }
    }
}
