package com.bizcopay.app.ui.payer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizcopay.app.data.local.StatementHelper
import com.bizcopay.app.data.local.TopUpRecord
import com.bizcopay.app.data.local.TopUpStore
import com.bizcopay.app.data.network.models.TransactionResponse
import com.bizcopay.app.ui.common.PeriodPickerSheet
import com.bizcopay.app.ui.common.StatementPeriod
import com.bizcopay.app.ui.common.filterByPeriod
import com.bizcopay.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayerHistoryScreen(viewModel: PayerViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val wallet       by viewModel.wallet.collectAsState()
    val topUps       by TopUpStore.topUps.collectAsState()
    var selected     by remember { mutableStateOf<TransactionResponse?>(null) }
    var tab          by remember { mutableStateOf(0) }     // 0=Payments 1=Top-ups
    var showPeriod   by remember { mutableStateOf(false) }

    val context      = LocalContext.current
    val tokenManager = remember { com.bizcopay.app.data.local.TokenManager(context) }
    val ownerName    = remember { tokenManager.getName() ?: "" }
    val ownerEmail   = remember { tokenManager.getEmail() ?: "" }
    val walletId     = wallet?.id ?: ""

    val grouped = transactions.groupBy { it.createdAt.take(10) }

    Box(modifier = Modifier.fillMaxSize().background(BizcoBackground)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // Header row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("History", color = BizcoTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    if (transactions.isNotEmpty()) {
                        IconButton(onClick = { showPeriod = true }) {
                            Icon(Icons.Rounded.FileDownload, contentDescription = "Export statement", tint = BizcoBlue)
                        }
                    }
                }
            }

            // Tab selector
            item {
                PillTabRow(
                    tabs = listOf("Payments", "Top-ups"),
                    selected = tab,
                    onSelect = { tab = it },
                    modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 4.dp)
                )
            }

            // Content for selected tab
            if (tab == 0) {
                // ── Payments tab ──────────────────────────────────────────────
                if (transactions.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                            Text("No transactions yet", color = BizcoTextMuted, fontSize = 15.sp)
                        }
                    }
                } else {
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
                }
            } else {
                // ── Top-ups tab ───────────────────────────────────────────────
                if (topUps.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Rounded.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = BizcoTextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text("No top-ups yet", color = BizcoTextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Credits added to your wallet will appear here",
                                    color = BizcoTextMuted,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(topUps, key = { it.id }) { record ->
                        TopUpListItem(
                            record = record,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        selected?.let { tx ->
            TransactionDetailSheet(
                tx = tx,
                onDismiss = { selected = null },
                onCategorize = { cat -> viewModel.categorizeTransaction(tx.id, cat); selected = null }
            )
        }
    }

    // Period picker — shown before export
    if (showPeriod) {
        PeriodPickerSheet(
            onDismiss = { showPeriod = false },
            onSelect = { period ->
                showPeriod = false
                StatementHelper.exportAndShare(
                    context = context,
                    transactions = transactions.filterByPeriod(period),
                    role = "PAYER",
                    ownerName = ownerName,
                    ownerEmail = ownerEmail,
                    walletId = walletId,
                )
            }
        )
    }
}


// ── Pill tab selector ─────────────────────────────────────────────────────────

@Composable
fun PillTabRow(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BizcoCard)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            tabs.forEachIndexed { i, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (i == selected) BizcoBlue else Color.Transparent)
                        .clickable { onSelect(i) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (i == selected) Color.White else BizcoTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (i == selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ── Top-up list item ──────────────────────────────────────────────────────────

@Composable
fun TopUpListItem(record: TopUpRecord, modifier: Modifier = Modifier) {
    val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(record.timestamp))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BizcoCard)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BizcoGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = BizcoGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Wallet Top-up", color = BizcoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(date, color = BizcoTextSecondary, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "+ RWF ${"%,.0f".format(record.amount.toDoubleOrNull() ?: 0.0)}",
                    color = BizcoGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (record.balanceAfter.isNotBlank()) {
                    Text(
                        "Bal: ${"%,.0f".format(record.balanceAfter.toDoubleOrNull() ?: 0.0)}",
                        color = BizcoTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ── Helpers (unchanged) ───────────────────────────────────────────────────────

private fun formatDateHeader(date: String): String {
    val parts = date.split("-")
    if (parts.size != 3) return date
    val cal = Calendar.getInstance()
    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
    val todayCal = Calendar.getInstance()
    val yesterdayCal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -1) }
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
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(categoryColor(tx.category).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text(categoryEmoji(tx.category), fontSize = 20.sp) }

            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.merchant?.name ?: "Unknown merchant", color = BizcoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(tx.createdAt.take(16).replace("T", " "), color = BizcoTextSecondary, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "- RWF ${"%,.0f".format(tx.amount.toDouble())}",
                    color = if (tx.status == "APPROVED") BizcoError else BizcoTextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (tx.category != null) Text(tx.category, color = BizcoTextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun TransactionDetailSheet(tx: TransactionResponse, onDismiss: () -> Unit, onCategorize: (String) -> Unit) {
    val categories = listOf("FOOD","TRANSPORT","SHOPPING","ENTERTAINMENT","HEALTH","EDUCATION","BILLS","OTHER")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = BizcoCard)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                        .background(BizcoBorder).align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(20.dp))
                Text("- RWF ${"%,.0f".format(tx.amount.toDouble())}", color = BizcoError, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(tx.merchant?.name ?: "Unknown", color = BizcoTextSecondary, fontSize = 16.sp)
                Spacer(Modifier.height(20.dp))
                DetailRow("Date & Time", tx.createdAt.take(16).replace("T", " "))
                DetailRow("Status", tx.status)
                DetailRow("Category", tx.category ?: "Uncategorized")
                if (tx.description != null) DetailRow("Description", tx.description)
                Spacer(Modifier.height(20.dp))
                Text("Set Category", color = BizcoTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                categories.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { cat ->
                            CategoryChip(label = cat, selected = tx.category == cat, color = categoryColor(cat), modifier = Modifier.weight(1f), onClick = { onCategorize(cat) })
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
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = BizcoTextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = BizcoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
    Divider(color = BizcoBorder, thickness = 0.5.dp)
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = 0.3f) else BizcoSurface)
            .clickable { onClick() }.padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) { Text(categoryEmoji(label), fontSize = 16.sp) }
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
