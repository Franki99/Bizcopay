package com.bizcopay.app.ui.payer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizcopay.app.data.local.NfcDeviceInfo
import com.bizcopay.app.data.local.NfcDeviceType
import com.bizcopay.app.data.local.NfcDeviceTypeStore
import com.bizcopay.app.data.network.models.NfcTokenResponse
import com.bizcopay.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class RegStep { None, TypePick, Scanning, Captured, Registering }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayerNfcDevicesScreen(viewModel: PayerViewModel, onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val tokens by viewModel.tokens.collectAsState()
    val registrationState by viewModel.registrationState.collectAsState()

    var regStep by remember { mutableStateOf(RegStep.None) }
    var selectedType by remember { mutableStateOf(NfcDeviceType.CARD) }
    var capturedUid by remember { mutableStateOf("") }
    var selectedDevice by remember { mutableStateOf<NfcTokenResponse?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { NfcDeviceTypeStore.init(context) }

    LaunchedEffect(registrationState) {
        when (val rs = registrationState) {
            is NfcRegistrationState.Captured -> {
                capturedUid = rs.uid
                regStep = RegStep.Captured
            }
            is NfcRegistrationState.Registering -> regStep = RegStep.Registering
            is NfcRegistrationState.Success -> {
                NfcDeviceTypeStore.save(capturedUid, selectedType)
                refreshKey++
                regStep = RegStep.None
                viewModel.resetRegistration()
            }
            is NfcRegistrationState.Error -> regStep = RegStep.None
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BizcoBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (onBack != null) 4.dp else 24.dp,
                            end = 24.dp,
                            top = 12.dp,
                            bottom = 8.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = BizcoTextPrimary)
                        }
                    }
                    Text("NFC Devices", color = BizcoTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    if (tokens.isNotEmpty()) {
                        Text(
                            "${tokens.size} device${if (tokens.size != 1) "s" else ""}",
                            color = BizcoTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (tokens.isEmpty()) {
                item { NfcEmptyState() }
            } else {
                items(tokens, key = { it.id }) { token ->
                    val info = remember(token.uid, refreshKey) { NfcDeviceTypeStore.getInfo(token.uid) }
                    DeviceCard(
                        token = token,
                        info = info,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        onClick = { selectedDevice = token }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { regStep = RegStep.TypePick },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = BizcoBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Register device", modifier = Modifier.size(28.dp))
        }
    }

    // Registration flow sheets
    when (regStep) {
        RegStep.TypePick -> TypePickerSheet(
            onSelect = { type ->
                selectedType = type
                regStep = RegStep.Scanning
                viewModel.startNfcRegistration()
            },
            onDismiss = { regStep = RegStep.None }
        )
        RegStep.Scanning -> ScanningSheet(
            onCancel = {
                viewModel.cancelNfcRegistration()
                regStep = RegStep.None
            }
        )
        RegStep.Captured -> LabelEntrySheet(
            uid = capturedUid,
            type = selectedType,
            onConfirm = { label -> viewModel.registerToken(capturedUid, label) },
            onCancel = {
                viewModel.cancelNfcRegistration()
                regStep = RegStep.None
            }
        )
        RegStep.Registering -> RegisteringSheet()
        RegStep.None -> {}
    }

    // Device detail sheet
    selectedDevice?.let { token ->
        val currentInfo = remember(token.uid, refreshKey) { NfcDeviceTypeStore.getInfo(token.uid) }
        DeviceDetailSheet(
            token = token,
            info = currentInfo,
            onDismiss = { selectedDevice = null },
            onDelete = {
                viewModel.deactivateToken(token.id)
                NfcDeviceTypeStore.remove(token.uid)
                selectedDevice = null
            },
            onRename = { label ->
                NfcDeviceTypeStore.updateLabel(token.uid, label)
                refreshKey++
            }
        )
    }
}

// ── Visual device card ────────────────────────────────────────────────────────

@Composable
private fun DeviceCard(
    token: NfcTokenResponse,
    info: NfcDeviceInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val displayName = info.customLabel ?: token.label ?: "My Device"
    val date = if (info.registeredAt > 0)
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(info.registeredAt))
    else "—"

    val gradient = deviceGradient(info.type)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(185.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .clickable(onClick = onClick)
    ) {
        // Subtle shine overlay in top-left
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(300f, 300f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeviceDecoration(info.type)
                Text(
                    "BIZCOPAY",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                "••••  •••  ${token.uid.takeLast(4).uppercase()}",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "DEVICE NAME",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(displayName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "REGISTERED",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(date, color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DeviceDecoration(type: NfcDeviceType) {
    when (type) {
        NfcDeviceType.CARD ->
            Icon(
                Icons.Rounded.Nfc,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(30.dp)
            )
        NfcDeviceType.RING ->
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .border(5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                )
            }
        NfcDeviceType.BRACELET ->
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(20.dp)
                        .border(4.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                )
            }
    }
}

private fun deviceGradient(type: NfcDeviceType): Brush = when (type) {
    NfcDeviceType.CARD ->
        Brush.linearGradient(listOf(Color(0xFF1A4B8A), Color(0xFF0A1628)))
    NfcDeviceType.RING ->
        Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF2E1065)))
    NfcDeviceType.BRACELET ->
        Brush.linearGradient(listOf(Color(0xFF0891B2), Color(0xFF083344)))
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun NfcEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(BizcoBlue.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Nfc,
                    contentDescription = null,
                    tint = BizcoBlue,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "No devices registered",
                color = BizcoTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Tap + to register your NFC card, ring, or bracelet",
                color = BizcoTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Type picker sheet ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypePickerSheet(
    onSelect: (NfcDeviceType) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BizcoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                "Choose Device Type",
                color = BizcoTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text("What type of NFC device are you registering?", color = BizcoTextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NfcDeviceType.entries.forEach { type ->
                    TypeOptionCard(
                        type = type,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(type) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TypeOptionCard(type: NfcDeviceType, modifier: Modifier, onClick: () -> Unit) {
    val label = when (type) {
        NfcDeviceType.CARD -> "Card"
        NfcDeviceType.RING -> "Ring"
        NfcDeviceType.BRACELET -> "Bracelet"
    }

    Box(
        modifier = modifier
            .aspectRatio(0.62f)
            .clip(RoundedCornerShape(16.dp))
            .background(deviceGradient(type))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Shine overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(200f, 200f)
                    )
                )
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(vertical = 20.dp)
        ) {
            Spacer(Modifier.weight(1f))
            DeviceDecoration(type)
            Spacer(Modifier.height(16.dp))
            Text(
                label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

// ── Scanning sheet ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanningSheet(onCancel: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseOut), RepeatMode.Restart),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseOut), RepeatMode.Restart),
        label = "alpha"
    )

    ModalBottomSheet(
        onDismissRequest = onCancel,
        containerColor = BizcoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                // Pulse ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(BizcoBlue.copy(alpha = pulseAlpha * 0.4f))
                )
                // Static inner circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BizcoBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Nfc, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("Ready to Scan", color = BizcoTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Hold your NFC card, ring, or bracelet\nagainst the back of your phone",
                color = BizcoTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))
            TextButton(onClick = onCancel) {
                Text("Cancel", color = BizcoError, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Label entry sheet ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabelEntrySheet(
    uid: String,
    type: NfcDeviceType,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var label by remember { mutableStateOf(defaultLabel(type)) }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        containerColor = BizcoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(deviceGradient(type)),
                    contentAlignment = Alignment.Center
                ) { DeviceDecoration(type) }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Device Detected!", color = BizcoTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("UID: ${uid.takeLast(8).uppercase()}", color = BizcoTextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Give it a name", color = BizcoTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                placeholder = { Text("e.g. My Visa Card", color = BizcoTextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BizcoBlue,
                    unfocusedBorderColor = BizcoBorder,
                    focusedTextColor = BizcoTextPrimary,
                    unfocusedTextColor = BizcoTextPrimary,
                    cursorColor = BizcoBlue
                )
            )

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) { Text("Cancel", color = BizcoTextSecondary) }
                Button(
                    onClick = { onConfirm(label.trim().ifBlank { defaultLabel(type) }) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
                ) { Text("Register", fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Registering overlay ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisteringSheet() {
    ModalBottomSheet(
        onDismissRequest = {},
        containerColor = BizcoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = BizcoBlue, modifier = Modifier.size(44.dp), strokeWidth = 3.dp)
            Spacer(Modifier.height(16.dp))
            Text("Registering device…", color = BizcoTextSecondary, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Device detail sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceDetailSheet(
    token: NfcTokenResponse,
    info: NfcDeviceInfo,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
) {
    val displayName = info.customLabel ?: token.label ?: "My Device"
    var labelEdit by remember(displayName) { mutableStateOf(displayName) }
    var isEditing by remember { mutableStateOf(false) }
    val date = if (info.registeredAt > 0)
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(info.registeredAt))
    else "—"
    val typeLabel = when (info.type) {
        NfcDeviceType.CARD -> "NFC Card"
        NfcDeviceType.RING -> "NFC Ring"
        NfcDeviceType.BRACELET -> "NFC Bracelet"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BizcoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            // Mini device card preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(deviceGradient(info.type))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                                start = Offset(0f, 0f),
                                end = Offset(300f, 300f)
                            )
                        )
                )
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        DeviceDecoration(info.type)
                        Text("BIZCOPAY", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "••••  •••  ${token.uid.takeLast(4).uppercase()}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(displayName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Details
            DetailInfoRow(icon = Icons.Rounded.Badge, label = "Device Type", value = typeLabel)
            DetailInfoRow(icon = Icons.Rounded.Fingerprint, label = "UID", value = token.uid.uppercase())
            DetailInfoRow(icon = Icons.Rounded.CalendarToday, label = "Registered", value = date)

            Spacer(Modifier.height(16.dp))
            Divider(color = BizcoBorder, thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            // Rename
            if (isEditing) {
                OutlinedTextField(
                    value = labelEdit,
                    onValueChange = { labelEdit = it },
                    label = { Text("Device name", color = BizcoTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BizcoBlue,
                        unfocusedBorderColor = BizcoBorder,
                        focusedTextColor = BizcoTextPrimary,
                        unfocusedTextColor = BizcoTextPrimary,
                        cursorColor = BizcoBlue
                    )
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { isEditing = false; labelEdit = displayName },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancel", color = BizcoTextSecondary) }
                    Button(
                        onClick = { onRename(labelEdit.trim()); isEditing = false },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
                    ) { Text("Save", fontWeight = FontWeight.SemiBold) }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = BizcoBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rename", color = BizcoBlue)
                    }
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BizcoError.copy(alpha = 0.12f))
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = BizcoError, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Remove", color = BizcoError, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BizcoTextMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = BizcoTextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = BizcoTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun defaultLabel(type: NfcDeviceType) = when (type) {
    NfcDeviceType.CARD -> "My NFC Card"
    NfcDeviceType.RING -> "My NFC Ring"
    NfcDeviceType.BRACELET -> "My NFC Bracelet"
}
