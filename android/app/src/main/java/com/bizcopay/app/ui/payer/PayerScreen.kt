package com.bizcopay.app.ui.payer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.bizcopay.app.data.network.models.NfcTokenResponse
import com.bizcopay.app.ui.navigation.Screen

@Composable
fun PayerScreen(navController: NavController, viewModel: PayerViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val registrationState by viewModel.registrationState.collectAsState()
    val tokens by viewModel.tokens.collectAsState()
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Wallet balance ────────────────────────────────────────────────
            wallet?.let {
                Text(
                    "${it.currency} ${it.balance}",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Available balance", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(40.dp))

            // ── Payment state ─────────────────────────────────────────────────
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
                                OutlinedButton(
                                    onClick = { viewModel.reset() },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Decline") }
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
                    Text(
                        "Payment Approved",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("$${s.amount} deducted", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { viewModel.reset() }) { Text("Done") }
                }

                is PayerState.Failed -> {
                    Text(
                        "Payment Failed",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
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

            // ── NFC Device Management (only visible when not handling a payment) ──
            if (state is PayerState.Idle || state is PayerState.Error) {
                Spacer(Modifier.height(40.dp))
                HorizontalDivider()
                Spacer(Modifier.height(24.dp))

                NfcRegistrationSection(
                    registrationState = registrationState,
                    tokens = tokens,
                    onStart = { viewModel.startNfcRegistration() },
                    onCancel = { viewModel.cancelNfcRegistration() },
                    onRegister = { uid, label -> viewModel.registerToken(uid, label) },
                    onDone = { viewModel.resetRegistration() },
                    onDelete = { tokenId -> viewModel.deactivateToken(tokenId) }
                )
            }
        }
    }
}

@Composable
private fun NfcRegistrationSection(
    registrationState: NfcRegistrationState,
    tokens: List<NfcTokenResponse>,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onRegister: (uid: String, label: String) -> Unit,
    onDone: () -> Unit,
    onDelete: (tokenId: String) -> Unit,
) {
    Text("My NFC Devices", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))

    // Registered token list
    tokens.forEach { token ->
        TokenRow(token = token, onDelete = { onDelete(token.id) })
        Spacer(Modifier.height(8.dp))
    }

    if (tokens.isNotEmpty()) Spacer(Modifier.height(8.dp))

    when (val rs = registrationState) {
        is NfcRegistrationState.Idle -> {
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                Text("Register NFC Ring / Card / Bracelet")
            }
        }

        is NfcRegistrationState.ReadingNfc -> {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tap your NFC ring, card, or bracelet\nagainst the back of this phone",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onCancel) { Text("Cancel") }
                }
            }
        }

        is NfcRegistrationState.Captured -> {
            var label by remember { mutableStateOf("") }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("NFC Device Detected", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "UID: ${rs.uid}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Label (e.g. My Ring, Work Card)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { onRegister(rs.uid, label) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Register") }
                    }
                }
            }
        }

        is NfcRegistrationState.Registering -> {
            CircularProgressIndicator()
            Spacer(Modifier.height(8.dp))
            Text("Registering device...")
        }

        is NfcRegistrationState.Success -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Registered successfully",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "\"${rs.label}\" is now linked to your wallet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onDone) { Text("Done") }
                }
            }
        }

        is NfcRegistrationState.Error -> {
            Text(rs.message, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun TokenRow(token: NfcTokenResponse, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    token.label ?: token.uid,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "UID: ${token.uid}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove device",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Remove device?") },
            text = {
                Text("\"${token.label ?: token.uid}\" will be unlinked from your wallet and can no longer be used for payments.")
            },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
