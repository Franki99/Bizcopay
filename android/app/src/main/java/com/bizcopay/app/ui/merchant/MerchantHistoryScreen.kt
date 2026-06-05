package com.bizcopay.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizcopay.app.data.local.StatementHelper
import com.bizcopay.app.ui.common.PeriodPickerSheet
import com.bizcopay.app.ui.common.filterByRange
import com.bizcopay.app.ui.theme.*

@Composable
fun MerchantHistoryScreen(viewModel: MerchantViewModel) {
    val history by viewModel.history.collectAsState()
    val wallet  by viewModel.wallet.collectAsState()
    var showPeriod by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tokenManager = remember { com.bizcopay.app.data.local.TokenManager(context) }
    val ownerName  = remember { tokenManager.getName() ?: "" }
    val ownerEmail = remember { tokenManager.getEmail() ?: "" }
    val walletId   = wallet?.id ?: ""

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    Box(modifier = Modifier.fillMaxSize().background(BizcoBackground)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sales History", color = BizcoTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { showPeriod = true }) {
                            Icon(Icons.Rounded.FileDownload, contentDescription = "Export statement", tint = BizcoBlue)
                        }
                    }
                }
            }

            if (history.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                        Text("No sales yet", color = BizcoTextMuted, fontSize = 15.sp)
                    }
                }
            }

            items(history) { tx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BizcoCard)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tx.payer?.name ?: "Customer", color = BizcoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(tx.createdAt.take(16).replace("T", " "), color = BizcoTextSecondary, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "+ RWF ${"%,.0f".format(tx.amount.toDouble())}",
                                color = BizcoSuccess, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                            )
                            val statusColor = if (tx.status == "APPROVED") BizcoSuccess else BizcoError
                            Text(tx.status, color = statusColor, fontSize = 11.sp)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showPeriod) {
        PeriodPickerSheet(
            onDismiss = { showPeriod = false },
            onSelect = { startMs, endMs ->
                showPeriod = false
                StatementHelper.exportAndShare(
                    context = context,
                    transactions = history.filterByRange(startMs, endMs),
                    role = "MERCHANT",
                    ownerName = ownerName,
                    ownerEmail = ownerEmail,
                    walletId = walletId,
                )
            }
        )
    }
}
