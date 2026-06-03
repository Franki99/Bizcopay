package com.bizcopay.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bizcopay.app.ui.auth.LoginScreen
import com.bizcopay.app.ui.auth.RegisterScreen
import com.bizcopay.app.ui.auth.ResetPinScreen
import com.bizcopay.app.ui.merchant.MerchantMainScreen
import com.bizcopay.app.ui.payer.PayerMainScreen
import com.bizcopay.app.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash    : Screen("splash")
    object Login     : Screen("login")
    object Register  : Screen("register")
    object ResetPin  : Screen("reset_pin")
    object Payer     : Screen("payer")
    object Merchant  : Screen("merchant")
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Splash.route)   { SplashScreen(navController) }
        composable(Screen.Login.route)    { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.ResetPin.route) { ResetPinScreen(navController) }
        composable(Screen.Payer.route)    { PayerMainScreen(navController) }
        composable(Screen.Merchant.route) { MerchantMainScreen(navController) }
    }
}
