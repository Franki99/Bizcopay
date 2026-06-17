package com.bizcopay.app.ui.merchant

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bizcopay.app.data.local.NotificationPreferencesManager
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.ui.common.*
import com.bizcopay.app.ui.navigation.Screen
import com.bizcopay.app.ui.theme.*

private enum class MerchantProfileDest { Main, Preferences, AccountSecurity, GetInTouch, FAQs }

@Composable
fun MerchantProfileScreen(rootNavController: NavController, viewModel: MerchantViewModel) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val notifPrefs = remember { NotificationPreferencesManager(context) }

    var dest by remember { mutableStateOf(MerchantProfileDest.Main) }

    when (dest) {
        MerchantProfileDest.Preferences ->
            ProfilePreferencesScreen(
                notifPrefs = notifPrefs,
                onBack = { dest = MerchantProfileDest.Main },
                paymentTitle = "Payments Received",
                paymentSubtitle = "When a customer pays you"
            )
        MerchantProfileDest.AccountSecurity ->
            ProfileAccountSecurityScreen(
                onBack = { dest = MerchantProfileDest.Main }
            )
        MerchantProfileDest.GetInTouch ->
            ProfileGetInTouchScreen(onBack = { dest = MerchantProfileDest.Main })
        MerchantProfileDest.FAQs ->
            ProfileFAQsScreen(onBack = { dest = MerchantProfileDest.Main })
        MerchantProfileDest.Main ->
            MerchantProfileMain(
                tokenManager = tokenManager,
                onPreferences = { dest = MerchantProfileDest.Preferences },
                onAccountSecurity = { dest = MerchantProfileDest.AccountSecurity },
                onGetInTouch = { dest = MerchantProfileDest.GetInTouch },
                onFAQs = { dest = MerchantProfileDest.FAQs },
                onLogout = {
                    viewModel.reset()
                    tokenManager.clearAuth()
                    rootNavController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
    }
}

@Composable
private fun MerchantProfileMain(
    tokenManager: TokenManager,
    onPreferences: () -> Unit,
    onAccountSecurity: () -> Unit,
    onGetInTouch: () -> Unit,
    onFAQs: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val name = tokenManager.getName() ?: "Merchant"
    val email = tokenManager.getEmail() ?: ""
    var profilePicUri by remember { mutableStateOf(tokenManager.getProfilePicUri()) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { /* some providers don't support persistable grants */ }
            tokenManager.saveProfilePicUri(it.toString())
            profilePicUri = it.toString()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BizcoBackground)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
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
                            Text(name.take(1).uppercase(), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Tap to change photo", color = BizcoTextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(10.dp))
                Text(name, color = BizcoTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(email, color = BizcoTextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BizcoBlue.copy(alpha = 0.15f))
                ) {
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("MERCHANT", color = BizcoBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                    ProfileMenuItem(
                        icon = Icons.Rounded.Tune,
                        iconTint = BizcoBlue,
                        title = "Preferences",
                        subtitle = "Notifications and display settings",
                        onClick = onPreferences
                    )
                    Divider(color = BizcoBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                    ProfileMenuItem(
                        icon = Icons.Rounded.Shield,
                        iconTint = Color(0xFF6366F1),
                        title = "Account & Security",
                        subtitle = "PIN management",
                        onClick = onAccountSecurity
                    )
                    Divider(color = BizcoBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                    ProfileMenuItem(
                        icon = Icons.Rounded.HeadsetMic,
                        iconTint = Color(0xFF14B8A6),
                        title = "Get in Touch",
                        subtitle = "Support tickets and contact",
                        onClick = onGetInTouch
                    )
                    Divider(color = BizcoBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                    ProfileMenuItem(
                        icon = Icons.Rounded.HelpOutline,
                        iconTint = Color(0xFFF59E0B),
                        title = "FAQs",
                        subtitle = "Common questions answered",
                        onClick = onFAQs
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
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
                        .clickable(onClick = onLogout)
                        .padding(horizontal = 20.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BizcoError.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null, tint = BizcoError, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("Logout", color = BizcoError, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
