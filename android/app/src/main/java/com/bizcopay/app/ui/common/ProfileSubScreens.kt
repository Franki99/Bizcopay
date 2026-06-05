package com.bizcopay.app.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizcopay.app.data.local.NotificationPreferencesManager
import com.bizcopay.app.ui.auth.AuthViewModel
import com.bizcopay.app.ui.theme.*

// ── Sub-screen header ─────────────────────────────────────────────────────────

@Composable
fun ProfileSubScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 24.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = BizcoTextPrimary)
        }
        Text(title, color = BizcoTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Shared menu row ───────────────────────────────────────────────────────────

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String = "",
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = BizcoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle.isNotEmpty())
                Text(subtitle, color = BizcoTextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = BizcoTextMuted, modifier = Modifier.size(20.dp))
    }
}

// ── Preferences sub-screen ────────────────────────────────────────────────────

@Composable
fun ProfilePreferencesScreen(
    notifPrefs: NotificationPreferencesManager,
    onBack: () -> Unit,
    paymentTitle: String = "Payments",
    paymentSubtitle: String = "When a payment is made or fails",
) {
    var notifEnabled   by remember { mutableStateOf(notifPrefs.notificationsEnabled) }
    var topUpEnabled   by remember { mutableStateOf(notifPrefs.topUpEnabled) }
    var paymentEnabled by remember { mutableStateOf(notifPrefs.paymentEnabled) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BizcoBackground)
    ) {
        item { ProfileSubScreenHeader("Preferences", onBack) }

        item {
            Text(
                "Notifications",
                color = BizcoTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BizcoCard)
            ) {
                Column {
                    PrefToggleRow(
                        icon = Icons.Rounded.NotificationsNone,
                        iconTint = BizcoTextSecondary,
                        title = "Enable Notifications",
                        subtitle = "Push alerts for account activity",
                        checked = notifEnabled,
                        onCheckedChange = { notifEnabled = it; notifPrefs.notificationsEnabled = it }
                    )
                    AnimatedVisibility(visible = notifEnabled) {
                        Column {
                            Divider(color = BizcoBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                            PrefToggleRow(
                                icon = Icons.Rounded.AccountBalanceWallet,
                                iconTint = BizcoGreen,
                                title = "Account Top-up",
                                subtitle = "When funds are added to your wallet",
                                checked = topUpEnabled,
                                onCheckedChange = { topUpEnabled = it; notifPrefs.topUpEnabled = it }
                            )
                            Divider(color = BizcoBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                            PrefToggleRow(
                                icon = Icons.Rounded.Payment,
                                iconTint = BizcoBlue,
                                title = paymentTitle,
                                subtitle = paymentSubtitle,
                                checked = paymentEnabled,
                                onCheckedChange = { paymentEnabled = it; notifPrefs.paymentEnabled = it }
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun PrefToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
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

// ── Account & Security sub-screen ─────────────────────────────────────────────

@Composable
fun ProfileAccountSecurityScreen(
    onBack: () -> Unit,
    deviceSection: @Composable () -> Unit = {},
) {
    val authViewModel: AuthViewModel = viewModel()
    val changePinState by authViewModel.changePinState.collectAsState()
    var showChangePinDialog by remember { mutableStateOf(false) }

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
    ) {
        item { ProfileSubScreenHeader("Account & Security", onBack) }

        item {
            Text(
                "Security",
                color = BizcoTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BizcoCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            authViewModel.resetChangePinState()
                            showChangePinDialog = true
                        }
                        .padding(horizontal = 20.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BizcoBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = BizcoBlue, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Change PIN", color = BizcoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text("Update your 4-digit security PIN", color = BizcoTextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = BizcoTextMuted, modifier = Modifier.size(20.dp))
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            deviceSection()
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    if (showChangePinDialog) {
        ProfileChangePinDialog(
            changePinState = changePinState,
            onConfirm = { current, newPin -> authViewModel.changePin(current, newPin) },
            onDismiss = { showChangePinDialog = false; authViewModel.resetChangePinState() }
        )
    }
}

@Composable
fun ProfileChangePinDialog(
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
        title = { Text("Change PIN", color = BizcoTextPrimary, fontWeight = FontWeight.Bold) },
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
                        focusedBorderColor = BizcoBlue, unfocusedBorderColor = BizcoBorder,
                        focusedTextColor = BizcoTextPrimary, unfocusedTextColor = BizcoTextPrimary,
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
                        focusedBorderColor = BizcoBlue, unfocusedBorderColor = BizcoBorder,
                        focusedTextColor = BizcoTextPrimary, unfocusedTextColor = BizcoTextPrimary,
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
            TextButton(onClick = { onConfirm(currentPin, newPin) }, enabled = canConfirm) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BizcoBlue, strokeWidth = 2.dp)
                else Text("Confirm", color = if (canConfirm) BizcoBlue else BizcoTextMuted, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isLoading) onDismiss() }) {
                Text("Cancel", color = BizcoTextSecondary)
            }
        }
    )
}

// ── Get in Touch sub-screen ───────────────────────────────────────────────────

@Composable
fun ProfileGetInTouchScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    fun openEmail(subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@bizcopay.rw")).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Send Email").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BizcoBackground)
    ) {
        item { ProfileSubScreenHeader("Get in Touch", onBack) }

        item {
            Text(
                "Support",
                color = BizcoTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BizcoCard)
            ) {
                Column {
                    ContactRow(
                        icon = Icons.Rounded.ConfirmationNumber,
                        iconTint = BizcoBlue,
                        title = "Raise a Ticket",
                        subtitle = "Submit a support request",
                        onClick = { openEmail("Support Ticket - Bizcopay") }
                    )
                    Divider(color = BizcoBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                    ContactRow(
                        icon = Icons.Rounded.BugReport,
                        iconTint = BizcoError,
                        title = "Report an Issue",
                        subtitle = "Let us know about a bug or problem",
                        onClick = { openEmail("Issue Report - Bizcopay") }
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text(
                "Contact",
                color = BizcoTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BizcoCard)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BizcoGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Email, contentDescription = null, tint = BizcoGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Email Us", color = BizcoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text("support@bizcopay.rw", color = BizcoBlue, fontSize = 13.sp)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = BizcoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = BizcoTextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = BizcoTextMuted, modifier = Modifier.size(20.dp))
    }
}

// ── FAQs sub-screen ───────────────────────────────────────────────────────────

private val faqItems = listOf(
    "How do I add funds to my wallet?" to
        "To top up your wallet, contact your Bizcopay administrator or use the designated top-up channel provided by your institution. Funds are credited instantly.",
    "How does NFC payment work?" to
        "Register your NFC card, ring, or tag in the Devices section. At checkout, tap your registered NFC device on the merchant's terminal to pay instantly.",
    "What happens if a payment fails?" to
        "If a payment fails, no funds are deducted. You will receive a notification with the reason. Common causes include insufficient balance, expired session, or a fraud flag.",
    "What is the transaction time limit?" to
        "A payment request expires after 5 minutes. If you do not tap your NFC device within that window, the transaction is cancelled automatically.",
    "How do I register an NFC card or tag?" to
        "Go to Profile → Account & Security → My Devices, then tap \"Register New Device\". Follow the on-screen prompts and tap your NFC card to the back of your phone.",
    "Is my PIN stored securely?" to
        "Yes. Your PIN is never stored in plain text. It is hashed using bcrypt before being saved, and it is never transmitted in responses.",
    "How do I change my PIN?" to
        "Go to Profile → Account & Security → Change PIN. Enter your current PIN and your new 4-digit PIN to confirm.",
    "Who do I contact if I am charged incorrectly?" to
        "Go to Profile → Get in Touch and raise a support ticket. Include the transaction date, amount, and merchant name so our team can investigate quickly."
)

@Composable
fun ProfileFAQsScreen(onBack: () -> Unit) {
    val expanded = remember { mutableStateMapOf<Int, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BizcoBackground)
    ) {
        item { ProfileSubScreenHeader("FAQs", onBack) }

        item {
            Text(
                "Frequently Asked Questions",
                color = BizcoTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BizcoCard)
            ) {
                Column {
                    faqItems.forEachIndexed { idx, (question, answer) ->
                        if (idx > 0) Divider(color = BizcoBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 20.dp))
                        FaqRow(
                            question = question,
                            answer = answer,
                            isExpanded = expanded[idx] == true,
                            onToggle = { expanded[idx] = !(expanded[idx] ?: false) }
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun FaqRow(question: String, answer: String, isExpanded: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                question,
                color = BizcoTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = BizcoTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            Text(
                answer,
                color = BizcoTextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}
