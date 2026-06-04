package com.bizcopay.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.ui.theme.*
import java.util.Calendar

private fun merchantGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11  -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        else      -> "Good evening,"
    }
}

@Composable
fun MerchantHomeScreen(
    viewModel: MerchantViewModel,
    onGoToTerminal: () -> Unit,
    onGoToHistory: () -> Unit,
) {
    val history    by viewModel.history.collectAsState()
    val analytics  by viewModel.analytics.collectAsState()
    val context    = LocalContext.current
    val name       = remember { TokenManager(context).getName() ?: "Merchant" }
    val greeting   = remember { merchantGreeting() }
    val recentSales = history.take(5)

    Box(modifier = Modifier.fillMaxSize().background(BizcoBackground)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // ── Blue gradient header ──────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(BizcoBlue, BizcoBlueDark)))
                        .padding(horizontal = 24.dp, vertical = 36.dp)
                ) {
                    Column {
                        // Greeting row: avatar + name left, bell right
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
                                        .border(2.dp, BizcoGreen, CircleShape)
                                        .background(BizcoBlueDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        name.take(1).uppercase(),
                                        color = BizcoOnDark,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(greeting, color = BizcoOnDark.copy(alpha = 0.65f), fontSize = 13.sp)
                                    Text(name, color = BizcoOnDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
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

                        // Revenue card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Total Revenue",
                                    color = BizcoOnDark.copy(alpha = 0.65f),
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                analytics?.let {
                                    Text(
                                        "RWF ${"%,.0f".format(it.totalRevenue)}",
                                        color = BizcoOnDark,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-1).sp
                                    )
                                } ?: Text("—", color = BizcoOnDark, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Quick stats
                        analytics?.let { data ->
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MerchantStatChip(
                                    label = "This month",
                                    value = "RWF ${"%,.0f".format(data.thisMonth)}",
                                    modifier = Modifier.weight(1f)
                                )
                                MerchantStatChip(
                                    label = "Total sales",
                                    value = "${data.transactionCount} txns",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Open Terminal button ──────────────────────────────────────────
            item {
                Button(
                    onClick = onGoToTerminal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BizcoBlue)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Nfc,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Open Terminal", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ── Recent sales header ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Sales",
                        color = BizcoTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onGoToHistory) {
                        Text("View all", color = BizcoBlue, fontSize = 13.sp)
                    }
                }
            }

            if (recentSales.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No sales yet", color = BizcoTextMuted, fontSize = 14.sp)
                    }
                }
            } else {
                items(recentSales) { tx ->
                    RecentSaleItem(
                        tx = tx,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MerchantStatChip(label: String, value: String, modifier: Modifier = Modifier) {
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
private fun RecentSaleItem(
    tx: com.bizcopay.app.data.network.models.TransactionResponse,
    modifier: Modifier = Modifier,
) {
    val customerName = tx.payer?.name ?: "Customer"
    val initial = customerName.take(1).uppercase()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BizcoCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BizcoBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, color = BizcoBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(customerName, color = BizcoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(
                    tx.createdAt.take(16).replace("T", " "),
                    color = BizcoTextSecondary,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "+ RWF ${"%,.0f".format(tx.amount.toDouble())}",
                    color = if (tx.status == "APPROVED") BizcoSuccess else BizcoTextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val statusColor = if (tx.status == "APPROVED") BizcoSuccess else BizcoError
                Text(tx.status, color = statusColor, fontSize = 11.sp)
            }
        }
    }
}
