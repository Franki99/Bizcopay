package com.bizcopay.app.ui.payer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
fun PayerProfileScreen(rootNavController: NavController, viewModel: PayerViewModel) {
    val tokens by viewModel.tokens.collectAsState()
    val registrationState by viewModel.registrationState.collectAsState()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val name  = tokenManager.getName() ?: "User"
    val email = tokenManager.getEmail() ?: ""

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BizcoBackground)
            .padding(horizontal = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
            }
        }

        item {
            Text(
                "My NFC Devices",
                color = BizcoTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        tokens.forEach { token ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BizcoCard)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📱", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                token.label ?: token.uid,
                                color = BizcoTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "UID: ${token.uid}",
                                color = BizcoTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = { viewModel.deactivateToken(token.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = BizcoError)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            NfcRegistrationSection(
                registrationState = registrationState,
                tokens = emptyList(),
                onStart = { viewModel.startNfcRegistration() },
                onCancel = { viewModel.cancelNfcRegistration() },
                onRegister = { uid, label -> viewModel.registerToken(uid, label) },
                onDone = { viewModel.resetRegistration() },
                onDelete = { }
            )
        }

        item {
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
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
}
