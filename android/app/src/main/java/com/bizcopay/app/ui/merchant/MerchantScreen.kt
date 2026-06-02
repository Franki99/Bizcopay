package com.bizcopay.app.ui.merchant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.data.nfc.NfcEventBus
import com.bizcopay.app.ui.navigation.Screen

@Composable
fun MerchantScreen(navController: NavController, viewModel: MerchantViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    fun logout() {
        viewModel.reset()
        TokenManager(context).clear()
        navController.navigate(Screen.Login.route) { popUpTo(0) }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Merchant Terminal") },
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
            when (val s = state) {
                is MerchantState.Idle -> {
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it },
                        label = { Text("Amount (RWF)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Description (optional)") },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.createTransaction(amount.toDoubleOrNull() ?: 0.0, description) },
                        enabled = amount.toDoubleOrNull() != null && amount.toDouble() > 0,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Request Payment") }
                }

                is MerchantState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Processing...")
                }

                is MerchantState.WaitingForNfc -> {
                    // Collect real NFC taps from the activity via the event bus
                    LaunchedEffect(s.transactionId) {
                        NfcEventBus.uid.collect { uid ->
                            viewModel.onNfcRead(s.transactionId, uid)
                        }
                    }

                    Text("Ready — RWF ${s.amount}", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ask customer to tap their NFC ring, card, or bracelet",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator()
                    Spacer(Modifier.height(24.dp))
                    // Keep simulate button for testing without a physical NFC device
                    OutlinedButton(
                        onClick = { viewModel.onNfcRead(s.transactionId, "SIMULATED-UID-001") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Simulate NFC Tap (no device)") }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.reset() }) { Text("Cancel") }
                }

                is MerchantState.WaitingForPin -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Waiting for customer PIN approval...")
                }

                is MerchantState.Approved -> {
                    Text("Payment Approved", style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("RWF ${s.amount} received", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { viewModel.reset() }, modifier = Modifier.fillMaxWidth()) {
                        Text("New Payment")
                    }
                }

                is MerchantState.Failed -> {
                    Text("Payment Failed", style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(s.reason, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { viewModel.reset() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Try Again")
                    }
                }

                is MerchantState.Error -> {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.reset() }) { Text("Retry") }
                }
            }
        }
    }
}
