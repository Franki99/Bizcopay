package com.bizcopay.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun ResetPinScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    val otpSent = state is AuthState.ResetOtpSent

    LaunchedEffect(state) {
        if (state is AuthState.PinResetSuccess) {
            navController.popBackStack()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reset PIN", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            if (!otpSent) "Enter your email to receive a verification code."
            else "Enter the code sent to ${(state as AuthState.ResetOtpSent).email}.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !otpSent,
            modifier = Modifier.fillMaxWidth()
        )

        if (otpSent) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = otpCode, onValueChange = { if (it.length <= 6) otpCode = it },
                label = { Text("Verification Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = newPin, onValueChange = { if (it.length <= 4) newPin = it },
                label = { Text("New PIN (4 digits)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = { viewModel.sendResetOtp(email.trim()) }) {
                Text("Resend code")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state is AuthState.Error) {
            Text((state as AuthState.Error).message, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = {
                if (!otpSent) viewModel.sendResetOtp(email.trim())
                else viewModel.resetPin(email.trim(), otpCode.trim(), newPin)
            },
            enabled = state !is AuthState.Loading &&
                if (!otpSent) email.isNotBlank()
                else otpCode.length == 6 && newPin.length == 4,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state is AuthState.Loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text(if (!otpSent) "Send Code" else "Reset PIN")
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { navController.popBackStack(); viewModel.resetState() }) {
            Text("Back to Sign In")
        }
    }
}
