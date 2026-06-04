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
import com.bizcopay.app.ui.theme.*

@Composable
fun MerchantHistoryScreen(viewModel: MerchantViewModel) {
    val history by viewModel.history.collectAsState()
    val context = LocalContext.current
    val tokenManager = remember { com.bizcopay.app.data.local.TokenManager(context) }
    val ownerName  = remember { tokenManager.getName() ?: "" }
    val ownerEmail = remember { tokenManager.getEmail() ?: "" }

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
                    Text(
                        "Sales History",
                        color = BizcoTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (history.isNotEmpty()) {
                        IconButton(onClick = {
                            StatementHelper.exportAndShare(context, history, "MERCHANT", ownerName, ownerEmail)
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.FileDownload,
                                contentDescription = "Download statement",
                                tint = BizcoBlue
                            )
                        }
                    }
                }
            }

            if (history.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                tx.payer?.name ?: "Customer",
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
                                "+ RWF ${"%,.0f".format(tx.amount.toDouble())}",
                                color = BizcoSuccess,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
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
}
