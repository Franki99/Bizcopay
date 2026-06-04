package com.bizcopay.app.ui.payer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizcopay.app.data.network.models.NfcTokenResponse
import com.bizcopay.app.ui.theme.*

@Composable
fun NfcRegistrationSection(
    registrationState: NfcRegistrationState,
    tokens: List<NfcTokenResponse>,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onRegister: (uid: String, label: String) -> Unit,
    onDone: () -> Unit,
    onDelete: (tokenId: String) -> Unit,
) {
    Text("Register New NFC Device", color = BizcoTextPrimary, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))

    when (val rs = registrationState) {
        is NfcRegistrationState.Idle -> {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
            ) {
                Text("Register NFC Ring / Card / Bracelet")
            }
        }

        is NfcRegistrationState.ReadingNfc -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BizcoSurface)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = BizcoBlue)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tap your NFC ring, card, or bracelet\nagainst the back of this phone",
                        color = BizcoTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = BizcoBlue)
                    }
                }
            }
        }

        is NfcRegistrationState.Captured -> {
            var label by remember { mutableStateOf("") }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BizcoSurface)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("NFC Device Detected", color = BizcoTextPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "UID: ${rs.uid}",
                        style = MaterialTheme.typography.bodySmall,
                        color = BizcoTextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Label (e.g. My Ring, Work Card)", color = BizcoTextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor    = BizcoBlue,
                            unfocusedBorderColor  = BizcoBorder,
                            focusedContainerColor    = BizcoCard,
                            unfocusedContainerColor  = BizcoCard,
                            focusedTextColor      = BizcoTextPrimary,
                            unfocusedTextColor    = BizcoTextPrimary,
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancel", color = BizcoTextSecondary) }
                        Button(
                            onClick = { onRegister(rs.uid, label) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
                        ) { Text("Register") }
                    }
                }
            }
        }

        is NfcRegistrationState.Registering -> {
            CircularProgressIndicator(color = BizcoBlue)
            Spacer(Modifier.height(8.dp))
            Text("Registering device...", color = BizcoTextSecondary)
        }

        is NfcRegistrationState.Success -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BizcoSuccess.copy(alpha = 0.15f))
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Registered successfully", color = BizcoSuccess, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "\"${rs.label}\" is now linked to your wallet",
                        color = BizcoTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onDone,
                        colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
                    ) { Text("Done") }
                }
            }
        }

        is NfcRegistrationState.Error -> {
            Text(rs.message, color = BizcoError, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
            ) { Text("Try Again") }
        }
    }
}

