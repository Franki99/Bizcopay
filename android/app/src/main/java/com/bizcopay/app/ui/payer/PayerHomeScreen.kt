package com.bizcopay.app.ui.payer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.ui.theme.*
import java.util.Calendar

private fun timeBasedGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11  -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        else      -> "Good evening,"
    }
}

private fun merchantInitialColor(name: String): Color {
    val colors = listOf(
        Color(0xFF3B82F6), Color(0xFFA855F7), Color(0xFFEF4444),
        Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF6366F1),
        Color(0xFFF97316), Color(0xFF14B8A6)
    )
    return colors[(name.firstOrNull()?.code ?: 0) % colors.size]
}

@Composable
fun PayerHomeScreen(
    rootNavController: NavController,
    viewModel: PayerViewModel,
    onGoToHistory: () -> Unit,
) {
    val state        by viewModel.state.collectAsState()
    val wallet       by viewModel.wallet.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val analytics    by viewModel.analytics.collectAsState()
    val context = LocalContext.current
    val name    = remember { TokenManager(context).getName() ?: "User" }
    val greeting = remember { timeBasedGreeting() }
    val profilePicUri = remember { TokenManager(context).getProfilePicUri() }
    val recentTxs = transactions.take(3)

    Box(modifier = Modifier.fillMaxSize().background(BizcoBackground)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Gradient header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(BizcoBlue, BizcoBlueDark)))
                        .padding(horizontal = 24.dp, vertical = 36.dp)
                ) {
                    Column {
                        // Greeting row: avatar + name on left, bell on right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, BizcoGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (profilePicUri != null) {
                                        AsyncImage(
                                            model = profilePicUri,
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(BizcoBlueDark),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                name.take(1).uppercase(),
                                                color = BizcoOnDark,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(greeting, color = BizcoOnDark.copy(alpha = 0.65f), fontSize = 13.sp)
                                    Text(name, color = BizcoOnDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            // Notification bell
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = "Notifications",
                                    tint = BizcoOnDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Frosted balance card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Active Balance",
                                        color = BizcoOnDark.copy(alpha = 0.65f),
                                        fontSize = 13.sp
                                    )
                                    Text("ðŸ‘", fontSize = 16.sp)
                                }
                                Spacer(Modifier.height(6.dp))
                                wallet?.let {
                                    Text(
                                        "${it.currency} ${"%,.0f".format(it.balance.toDouble())}",
                                        color = BizcoOnDark,
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-1).sp
                                    )
                                } ?: Text("â€”", color = BizcoOnDark, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Quick stats row
                        analytics?.let { data ->
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatChip(
                                    label = "This month",
                                    value = "RWF ${"%,.0f".format(data.thisMonth)}",
                                    modifier = Modifier.weight(1f)
                                )
                                StatChip(
                                    label = "Total spent",
                                    value = "RWF ${"%,.0f".format(data.totalSpent)}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Recent transactions header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Transactions",
                        color = BizcoTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onGoToHistory) {
                        Text("View all", color = BizcoBlue, fontSize = 13.sp)
                    }
                }
            }

            if (recentTxs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("No transactions yet", color = BizcoTextMuted, fontSize = 14.sp) }
                }
            } else {
                items(recentTxs) { tx ->
                    HomeTransactionItem(
                        tx = tx,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        // Payment result overlay
        if (state is PayerState.Approved || state is PayerState.Failed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                PaymentResultCard(state = state, onDismiss = { viewModel.reset() })
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label, color = BizcoOnDark.copy(alpha = 0.65f), fontSize = 11.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = BizcoOnDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HomeTransactionItem(
    tx: com.bizcopay.app.data.network.models.TransactionResponse,
    modifier: Modifier = Modifier
) {
    val merchantName = tx.merchant?.name ?: "Unknown merchant"
    val initColor = merchantInitialColor(merchantName)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BizcoCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Merchant initial avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(initColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    merchantName.take(1).uppercase(),
                    color = initColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    merchantName,
                    color = BizcoTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    tx.createdAt.take(16).replace("T", " "),
                    color = BizcoTextSecondary,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "- RWF ${"%,.0f".format(tx.amount.toDouble())}",
                    color = if (tx.status == "APPROVED") BizcoError else BizcoTextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (tx.category != null) {
                    Text(tx.category, color = BizcoTextMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun PaymentResultCard(state: PayerState, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BizcoCard)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                is PayerState.Approved -> {
                    Text("âœ“", fontSize = 48.sp, color = BizcoSuccess)
                    Spacer(Modifier.height(12.dp))
                    Text("Payment Successful", color = BizcoTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "RWF ${state.amount} deducted from your wallet",
                        color = BizcoTextSecondary,
                        fontSize = 14.sp
                    )
                }
                is PayerState.Failed -> {
                    Text("âœ•", fontSize = 48.sp, color = BizcoError)
                    Spacer(Modifier.height(12.dp))
                    Text("Payment Failed", color = BizcoTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(state.reason, color = BizcoTextSecondary, fontSize = 14.sp)
                }
                else -> {}
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
            ) { Text("Done", fontWeight = FontWeight.SemiBold) }
        }
    }
}

