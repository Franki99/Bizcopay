package com.bizcopay.app.ui.splash

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bizcopay.app.ui.navigation.Screen
import com.bizcopay.app.ui.theme.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreen(navController: NavController) {
    var page by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BizcoBlue, BizcoBlueDark, BizcoBackground)))
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            },
            label = "splash_page"
        ) { currentPage ->
            when (currentPage) {
                0 -> SplashPage1 { page = 1 }
                else -> SplashPage2 { navController.navigate(Screen.Login.route) { popUpTo(0) } }
            }
        }

        // Dots indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(2) { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == page) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (i == page) BizcoOrange else BizcoTextMuted)
                )
            }
        }
    }
}

@Composable
private fun SplashPage1(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(BizcoOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("📡", fontSize = 48.sp)
        }
        Spacer(Modifier.height(48.dp))
        Text(
            "Bizcopay",
            color = BizcoTextPrimary,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Seamless contactless payments\nat your fingertips",
            color = BizcoTextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(64.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BizcoOrange)
        ) { Text("Next", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}

@Composable
private fun SplashPage2(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(BizcoBlue.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text("💳", fontSize = 48.sp)
        }
        Spacer(Modifier.height(48.dp))
        Text(
            "Smart & Secure",
            color = BizcoTextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        FeatureRow("🔒", "AI-powered fraud detection")
        Spacer(Modifier.height(12.dp))
        FeatureRow("📊", "Real-time spending insights")
        Spacer(Modifier.height(12.dp))
        FeatureRow("📱", "Tap & pay with NFC ring or card")
        Spacer(Modifier.height(64.dp))
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BizcoOrange)
        ) { Text("Get Started", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}

@Composable
private fun FeatureRow(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BizcoCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(Modifier.width(16.dp))
        Text(text, color = BizcoTextPrimary, fontSize = 15.sp)
    }
}
