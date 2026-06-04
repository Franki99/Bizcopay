package com.bizcopay.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bizcopay.app.data.nfc.NfcEventBus
import com.bizcopay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantTerminalScreen(navController: NavController, viewModel: MerchantViewModel) {
    val state by viewModel.state.collectAsState()
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BizcoBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Payment Terminal",
                        color = BizcoTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BizcoBackground,
                    titleContentColor = BizcoTextPrimary,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val s = state) {
                is MerchantState.Idle -> {
                    Text(
                        "New Payment",
                        color = BizcoTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Enter the amount to charge",
                        color = BizcoTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(32.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (RWF)", color = BizcoTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
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
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (optional)", color = BizcoTextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
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
                    Spacer(Modifier.height(28.dp))
                    Button(
                        onClick = {
                            viewModel.createTransaction(
                                amount.toDoubleOrNull() ?: 0.0,
                                description
                            )
                        },
                        enabled = amount.toDoubleOrNull() != null && amount.toDouble() > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
                    ) {
                        Text(
                            "Request Payment",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                is MerchantState.Loading -> {
                    CircularProgressIndicator(color = BizcoBlue)
                    Spacer(Modifier.height(12.dp))
                    Text("Processing...", color = BizcoTextSecondary)
                }

                is MerchantState.WaitingForNfc -> {
                    LaunchedEffect(s.transactionId) {
                        NfcEventBus.uid.collect { uid ->
                            viewModel.onNfcRead(s.transactionId, uid)
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BizcoCard)
                    ) {
                        Column(
                            Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("ðŸ“¡", fontSize = 48.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "RWF ${s.amount}",
                                color = BizcoTextPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Ask customer to tap their NFC ring, card, or bracelet",
                                color = BizcoTextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(24.dp))
                            CircularProgressIndicator(color = BizcoBlue)
                            Spacer(Modifier.height(24.dp))
                            OutlinedButton(
                                onClick = { viewModel.onNfcRead(s.transactionId, "SIMULATED-UID-001") },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Simulate NFC Tap (no device)", color = BizcoTextSecondary) }
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.reset() }) {
                                Text("Cancel", color = BizcoBlue)
                            }
                        }
                    }
                }

                is MerchantState.WaitingForPin -> {
                    var pin by remember { mutableStateOf("") }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BizcoCard)
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Text(
                                "PIN Required",
                                color = BizcoTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Amount: RWF ${s.amount}",
                                color = BizcoBlue,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Hand the device to the customer to enter their PIN",
                                color = BizcoTextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(20.dp))
                            OutlinedTextField(
                                value = pin,
                                onValueChange = { if (it.length <= 4) pin = it },
                                label = { Text("Customer PIN", color = BizcoTextMuted) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
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
                            Spacer(Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.reset() },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Cancel", color = BizcoTextSecondary) }
                                Button(
                                    onClick = { viewModel.submitPin(s.transactionId, pin) },
                                    enabled = pin.length == 4,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
                                ) { Text("Confirm", fontWeight = FontWeight.SemiBold) }
                            }
                        }
                    }
                }

                is MerchantState.Approved -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BizcoCard)
                    ) {
                        Column(
                            Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("âœ“", fontSize = 56.sp, color = BizcoSuccess)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Payment Approved",
                                color = BizcoTextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "RWF ${s.amount} received",
                                color = BizcoSuccess,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = { viewModel.reset() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
                            ) { Text("New Payment", fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                is MerchantState.Failed -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BizcoCard)
                    ) {
                        Column(
                            Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("âœ•", fontSize = 56.sp, color = BizcoError)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Payment Failed",
                                color = BizcoTextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(s.reason, color = BizcoTextSecondary, fontSize = 14.sp)
                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = { viewModel.reset() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
                            ) { Text("Try Again", fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                is MerchantState.Error -> {
                    Text(s.message, color = BizcoError, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.reset() },
                        colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
                    ) { Text("Retry") }
                }
            }
        }
    }
}

