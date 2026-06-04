package com.bizcopay.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bizcopay.app.ui.navigation.Screen
import com.bizcopay.app.ui.theme.*

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("PAYER") }
    var otpCode by remember { mutableStateOf("") }

    val otpSent = state is AuthState.RegisterOtpSent

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            val route = if ((state as AuthState.Success).role == "MERCHANT") Screen.Merchant.route
                        else Screen.Payer.route
            navController.navigate(route) { popUpTo(0) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BizcoBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.radialGradient(listOf(BizcoBlue, BizcoBlueDark)),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) { Text("ðŸ“¡", fontSize = 28.sp) }
            Spacer(Modifier.height(20.dp))
            Text(
                if (!otpSent) "Create Account" else "Verify Email",
                color = BizcoTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (!otpSent) "Join Bizcopay today"
                else "A 6-digit code was sent to\n${(state as? AuthState.RegisterOtpSent)?.email ?: email}",
                color = BizcoTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(32.dp))

            if (!otpSent) {
                BizcoTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name"
                )
                Spacer(Modifier.height(14.dp))
                BizcoTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    keyboardType = KeyboardType.Email
                )
                Spacer(Modifier.height(14.dp))
                BizcoTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    label = "4-Digit PIN",
                    keyboardType = KeyboardType.NumberPassword,
                    isPassword = true
                )
                Spacer(Modifier.height(20.dp))

                Text("Account Type", color = BizcoTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("PAYER", "MERCHANT").forEach { role ->
                        val selected = selectedRole == role
                        Button(
                            onClick = { selectedRole = role },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) BizcoBlue else BizcoCard
                            )
                        ) {
                            Text(
                                role,
                                color = if (selected) Color.White else BizcoTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            } else {
                BizcoTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    label = "Verification Code",
                    keyboardType = KeyboardType.Number
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { viewModel.sendRegistrationOtp(email.trim()) }) {
                    Text("Resend code", color = BizcoBlue, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (state is AuthState.Error) {
                Text(
                    (state as AuthState.Error).message,
                    color = BizcoError,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    if (!otpSent) viewModel.sendRegistrationOtp(email.trim())
                    else viewModel.register(name.trim(), email.trim(), pin, selectedRole, otpCode.trim())
                },
                enabled = state !is AuthState.Loading &&
                    if (!otpSent) name.isNotBlank() && email.isNotBlank() && pin.length == 4
                    else otpCode.length == 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
            ) {
                if (state is AuthState.Loading)
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(
                    if (!otpSent) "Send Verification Code" else "Create Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", color = BizcoTextSecondary, fontSize = 14.sp)
                TextButton(
                    onClick = { navController.popBackStack() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Sign In", color = BizcoBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

