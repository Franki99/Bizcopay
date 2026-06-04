package com.bizcopay.app.ui.payer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizcopay.app.data.local.StatementHelper
import com.bizcopay.app.data.network.models.TransactionResponse
import com.bizcopay.app.ui.theme.*
import java.util.Calendar

@Composable
fun PayerHistoryScreen(viewModel: PayerViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    var selected by remember { mutableStateOf<TransactionResponse?>(null) }
    val context = LocalContext.current
    val tokenManager = remember { com.bizcopay.app.data.local.TokenManager(context) }
    val ownerName  = remember { tokenManager.getName() ?: "" }
    val ownerEmail = remember { tokenManager.getEmail() ?: "" }

    // Group by date (first 10 chars of createdAt ISO string = YYYY-MM-DD)
    val grouped = transactions.groupBy { it.createdAt.take(10) }

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
                        "Transaction History",
                        color = BizcoTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (transactions.isNotEmpty()) {
                        IconButton(onClick = {
                            StatementHelper.exportAndShare(context, transactions, "PAYER", ownerName, ownerEmail)
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

            if (transactions.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions yet", color = BizcoTextMuted, fontSize = 15.sp)
                    }
                }
            }

            grouped.forEach { (date, txs) ->
                item {
                    Text(
                        formatDateHeader(date),
                        color = BizcoTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
                items(txs) { tx ->
                    TransactionListItem(
                        tx = tx,
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 4.dp)
                            .clickable { selected = tx }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        selected?.let { tx ->
            TransactionDetailSheet(
                tx = tx,
                onDismiss = { selected = null },
                onCategorize = { cat ->
                    viewModel.categorizeTransaction(tx.id, cat)
                    selected = null
                }
            )
        }
    }
}

private fun formatDateHeader(date: String): String {
    val parts = date.split("-")
    if (parts.size != 3) return date
    val cal = Calendar.getInstance()
    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
    val todayCal = Calendar.getInstance()
    val yesterdayCal = Calendar.getInstance()
    yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
    return when {
        cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR) -> "Today"
        cal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
        cal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
        else -> date
    }
}

@Composable
fun TransactionListItem(tx: TransactionResponse, modifier: Modifier = Modifier) {
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(categoryColor(tx.category).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text(categoryEmoji(tx.category), fontSize = 20.sp) }

            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.merchant?.name ?: "Unknown merchant",
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
fun TransactionDetailSheet(
    tx: TransactionResponse,
    onDismiss: () -> Unit,
    onCategorize: (String) -> Unit,
) {
    val categories = listOf("FOOD","TRANSPORT","SHOPPING","ENTERTAINMENT","HEALTH","EDUCATION","BILLS","OTHER")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = BizcoCard)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BizcoBorder)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(20.dp))

                Text(
                    "- RWF ${"%,.0f".format(tx.amount.toDouble())}",
                    color = BizcoError,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(tx.merchant?.name ?: "Unknown", color = BizcoTextSecondary, fontSize = 16.sp)
                Spacer(Modifier.height(20.dp))

                DetailRow("Date & Time", tx.createdAt.take(16).replace("T", " "))
                DetailRow("Status", tx.status)
                DetailRow("Category", tx.category ?: "Uncategorized")
                if (tx.description != null) DetailRow("Description", tx.description)

                Spacer(Modifier.height(20.dp))
                Text("Set Category", color = BizcoTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))

                val rows = categories.chunked(4)
                rows.forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { cat ->
                            CategoryChip(
                                label = cat,
                                selected = tx.category == cat,
                                color = categoryColor(cat),
                                modifier = Modifier.weight(1f),
                                onClick = { onCategorize(cat) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(label, color = BizcoTextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = BizcoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
    Divider(color = BizcoBorder, thickness = 0.5.dp)
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = 0.3f) else BizcoSurface)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(categoryEmoji(label), fontSize = 16.sp)
    }
}

fun categoryColor(category: String?): Color = when (category) {
    "FOOD"          -> CatFood
    "TRANSPORT"     -> CatTransport
    "SHOPPING"      -> CatShopping
    "ENTERTAINMENT" -> CatEntertainment
    "HEALTH"        -> CatHealth
    "EDUCATION"     -> CatEducation
    "BILLS"         -> CatBills
    else            -> CatOther
}

fun categoryEmoji(category: String?): String = when (category) {
    "FOOD"          -> "🍔"
    "TRANSPORT"     -> "🚌"
    "SHOPPING"      -> "🛍️"
    "ENTERTAINMENT" -> "🎬"
    "HEALTH"        -> "🏥"
    "EDUCATION"     -> "📚"
    "BILLS"         -> "💡"
    else            -> "💼"
}
