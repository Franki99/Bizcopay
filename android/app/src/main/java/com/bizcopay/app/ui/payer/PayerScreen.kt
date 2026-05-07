package com.bizcopay.app.ui.payer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.ui.navigation.Screen

@Composable
fun PayerScreen(navController: NavController, viewModel: PayerViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    fun logout() {
        TokenManager(context).clear()
        navController.navigate(Screen.Login.route) { popUpTo(0) }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("My Wallet") },
                actions = {
                    IconButton(onClick = { logout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            wallet?.let {
                Text("${it.currency} ${it.balance}", style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(40.dp))

            when (val s = state) {
                is PayerState.Idle -> {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                    Spacer(Modifier.height(12.dp))
                    Text("Listening for payment requests...", style = MaterialTheme.typography.bodyMedium)
                }

                is PayerState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Verifying PIN...")
                }

                is PayerState.PinRequired -> {
                    var pin by remember { mutableStateOf("") }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Payment Request", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(12.dp))
                            Text("From: ${s.payment.merchantName}")
                            Text("Amount: $${s.payment.amount}", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = pin,
                                onValueChange = { if (it.length <= 4) pin = it },
                                label = { Text("Enter your PIN") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { viewModel.reset() }, modifier = Modifier.weight(1f)) {
                                    Text("Decline")
                                }
                                Button(
                                    onClick = { viewModel.submitPin(s.payment.transactionId, pin) },
                                    enabled = pin.length == 4,
                                    modifier = Modifier.weight(1f)
                                ) { Text("Approve") }
                            }
                        }
                    }
                }

                is PayerState.Approved -> {
                    Text("Payment Approved", style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("$${s.amount} deducted", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { viewModel.reset() }) { Text("Done") }
                }

                is PayerState.Failed -> {
                    Text("Payment Failed", style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(s.reason, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { viewModel.reset() }) { Text("OK") }
                }

                is PayerState.Error -> {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.reset() }) { Text("OK") }
                }
            }
        }
    }
}
