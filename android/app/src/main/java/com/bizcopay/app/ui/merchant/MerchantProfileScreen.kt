package com.bizcopay.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.ui.navigation.Screen
import com.bizcopay.app.ui.theme.*

@Composable
fun MerchantProfileScreen(rootNavController: NavController, viewModel: MerchantViewModel) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val name  = tokenManager.getName() ?: "Merchant"
    val email = tokenManager.getEmail() ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BizcoBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BizcoBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name.take(1).uppercase(),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(name, color = BizcoTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(email, color = BizcoTextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.wrapContentWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BizcoOrange.copy(alpha = 0.15f))
        ) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("MERCHANT", color = BizcoOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                viewModel.reset()
                tokenManager.clear()
                rootNavController.navigate(Screen.Login.route) { popUpTo(0) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BizcoError.copy(alpha = 0.15f))
        ) { Text("Logout", color = BizcoError, fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(32.dp))
    }
}
