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
import androidx.compose.material3.ExperimentalMaterial3Api
import com.bizcopay.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
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

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        if (!otpSent) {
            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pin, onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("PIN (4 digits)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Text("Account Type", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("PAYER", "MERCHANT").forEach { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        label = { Text(role) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        } else {
            Text(
                "A 6-digit code was sent to\n${(state as AuthState.RegisterOtpSent).email}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            OutlinedTextField(
                value = otpCode, onValueChange = { if (it.length <= 6) otpCode = it },
                label = { Text("Verification Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { viewModel.sendRegistrationOtp(email) }) {
                Text("Resend code")
            }
            Spacer(Modifier.height(16.dp))
        }

        if (state is AuthState.Error) {
            Text((state as AuthState.Error).message, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = {
                if (!otpSent) viewModel.sendRegistrationOtp(email.trim())
                else viewModel.register(name.trim(), email.trim(), pin, selectedRole, otpCode.trim())
            },
            enabled = state !is AuthState.Loading &&
                if (!otpSent) name.isNotBlank() && email.isNotBlank() && pin.length == 4
                else otpCode.length == 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state is AuthState.Loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text(if (!otpSent) "Send Verification Code" else "Create Account")
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Already have an account? Sign In")
        }
    }
}
