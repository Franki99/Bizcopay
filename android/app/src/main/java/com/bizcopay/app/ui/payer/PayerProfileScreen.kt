package com.bizcopay.app.ui.payer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bizcopay.app.data.local.NotificationPreferencesManager
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.ui.auth.AuthViewModel
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

    val authViewModel: AuthViewModel = viewModel()
    val changePinState by authViewModel.changePinState.collectAsState()

    var showChangePinDialog by remember { mutableStateOf(false) }
    var profilePicUri by remember { mutableStateOf(tokenManager.getProfilePicUri()) }

    val notifPrefs = remember { NotificationPreferencesManager(context) }
    var notifEnabled  by remember { mutableStateOf(notifPrefs.notificationsEnabled) }
    var topUpEnabled  by remember { mutableStateOf(notifPrefs.topUpEnabled) }
    var paymentEnabled by remember { mutableStateOf(notifPrefs.paymentEnabled) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            tokenManager.saveProfilePicUri(it.toString())
            profilePicUri = it.toString()
        }
    }

    // Dismiss dialog on success
    LaunchedEffect(changePinState) {
        if (changePinState == "success") {
            showChangePinDialog = false
            authViewModel.resetChangePinState()
        }
    }

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
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePicUri != null) {
                        AsyncImage(
                            model = profilePicUri,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(BizcoBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                name.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Tap to change photo", color = BizcoTextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
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
                        Text("ðŸ“±", fontSize = 24.sp)
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

        // Preferences section
        item {
            Spacer(Modifier.height(20.dp))
            Text(
                "Preferences",
                color = BizcoTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BizcoCard)
            ) {
                Column {
                    // Master notifications toggle
                    NotifToggleRow(
                        icon = Icons.Rounded.NotificationsNone,
                        title = "Notifications",
                        subtitle = "Enable push notification alerts",
                        checked = notifEnabled,
                        onCheckedChange = {
                            notifEnabled = it
                            notifPrefs.notificationsEnabled = it
                        }
                    )
                    // Sub-toggles (visible only when master is on)
                    AnimatedVisibility(visible = notifEnabled) {
                        Column {
                            Divider(
                                color = BizcoBorder,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 56.dp)
                            )
                            NotifToggleRow(
                                icon = Icons.Rounded.AccountBalanceWallet,
                                title = "Account Top-up",
                                subtitle = "When funds are added to your wallet",
                                checked = topUpEnabled,
                                onCheckedChange = {
                                    topUpEnabled = it
                                    notifPrefs.topUpEnabled = it
                                },
                                iconTint = BizcoGreen
                            )
                            Divider(
                                color = BizcoBorder,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 56.dp)
                            )
                            NotifToggleRow(
                                icon = Icons.Rounded.Payment,
                                title = "Payments",
                                subtitle = "When a payment is made or fails",
                                checked = paymentEnabled,
                                onCheckedChange = {
                                    paymentEnabled = it
                                    notifPrefs.paymentEnabled = it
                                },
                                iconTint = BizcoBlue
                            )
                        }
                    }
                }
            }
        }

        // Change PIN section
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    authViewModel.resetChangePinState()
                    showChangePinDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BizcoCard)
            ) {
                Text("Change PIN", color = BizcoBlue, fontWeight = FontWeight.SemiBold)
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
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

    if (showChangePinDialog) {
        ChangePinDialog(
            changePinState = changePinState,
            onConfirm = { current, newPin -> authViewModel.changePin(current, newPin) },
            onDismiss = {
                showChangePinDialog = false
                authViewModel.resetChangePinState()
            }
        )
    }
}

@Composable
private fun NotifToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconTint: Color = BizcoTextSecondary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = BizcoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = BizcoTextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BizcoBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = BizcoBorder
            )
        )
    }
}

@Composable
private fun ChangePinDialog(
    changePinState: String?,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    val isLoading = changePinState == "loading"
    val isError = changePinState != null && changePinState != "loading" && changePinState != "success"
    val canConfirm = currentPin.length == 4 && newPin.length == 4 && !isLoading

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = BizcoCard,
        title = {
            Text("Change PIN", color = BizcoTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) currentPin = it },
                    label = { Text("Current PIN", color = BizcoTextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BizcoBlue,
                        unfocusedBorderColor = BizcoBorder,
                        focusedTextColor = BizcoTextPrimary,
                        unfocusedTextColor = BizcoTextPrimary,
                        cursorColor = BizcoBlue
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it },
                    label = { Text("New PIN", color = BizcoTextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BizcoBlue,
                        unfocusedBorderColor = BizcoBorder,
                        focusedTextColor = BizcoTextPrimary,
                        unfocusedTextColor = BizcoTextPrimary,
                        cursorColor = BizcoBlue
                    )
                )
                if (isError) {
                    Spacer(Modifier.height(8.dp))
                    Text(changePinState ?: "", color = BizcoError, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(currentPin, newPin) },
                enabled = canConfirm
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BizcoBlue, strokeWidth = 2.dp)
                } else {
                    Text("Confirm", color = if (canConfirm) BizcoBlue else BizcoTextMuted, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isLoading) onDismiss() }) {
                Text("Cancel", color = BizcoTextSecondary)
            }
        }
    )
}

