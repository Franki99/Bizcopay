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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bizcopay.app.ui.navigation.Screen
import com.bizcopay.app.ui.theme.*

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var pin   by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            val route = if ((state as AuthState.Success).role == "MERCHANT") Screen.Merchant.route else Screen.Payer.route
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
                    .size(72.dp)
                    .background(
                        Brush.radialGradient(listOf(BizcoBlue, BizcoBlueDark)),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) { Text("ðŸ“¡", fontSize = 32.sp) }
            Spacer(Modifier.height(24.dp))
            Text("Welcome Back!", color = BizcoTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Sign in to your Bizcopay account",
                color = BizcoTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))

            BizcoTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(16.dp))
            BizcoTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = "4-Digit PIN",
                keyboardType = KeyboardType.NumberPassword,
                isPassword = true
            )
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { navController.navigate(Screen.ResetPin.route) }) {
                    Text("Forgot PIN?", color = BizcoBlue, fontSize = 13.sp)
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
                onClick = { viewModel.login(email.trim(), pin) },
                enabled = state !is AuthState.Loading && email.isNotBlank() && pin.length == 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
            ) {
                if (state is AuthState.Loading)
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                Text("Don't have an account? ", color = BizcoTextSecondary, fontSize = 14.sp)
                TextButton(
                    onClick = { navController.navigate(Screen.Register.route) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Register", color = BizcoBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun BizcoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = BizcoTextMuted) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor    = BizcoBlue,
            unfocusedBorderColor  = BizcoBorder,
            focusedContainerColor    = BizcoCard,
            unfocusedContainerColor  = BizcoCard,
            focusedTextColor      = BizcoTextPrimary,
            unfocusedTextColor    = BizcoTextPrimary,
            cursorColor           = BizcoBlue,
        )
    )
}

