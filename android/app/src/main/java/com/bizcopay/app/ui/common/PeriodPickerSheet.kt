package com.bizcopay.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizcopay.app.data.network.models.TransactionResponse
import com.bizcopay.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

enum class StatementPeriod(val label: String, val days: Long?) {
    ALL("All time", null),
    DAYS_7("Last 7 days", 7),
    DAYS_30("Last 30 days", 30),
    MONTHS_3("Last 3 months", 90),
    MONTHS_6("Last 6 months", 180),
    YEAR("This year", 365),
}

fun List<TransactionResponse>.filterByPeriod(period: StatementPeriod): List<TransactionResponse> {
    val days = period.days ?: return this
    val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days)
    return filter { parseIsoDate(it.createdAt)?.let { t -> t >= cutoff } ?: true }
}

private fun parseIsoDate(s: String): Long? = try {
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        .also { it.timeZone = TimeZone.getTimeZone("UTC") }
        .parse(s.take(19))?.time
} catch (_: Exception) { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodPickerSheet(
    onDismiss: () -> Unit,
    onSelect: (StatementPeriod) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BizcoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text("Export Statement", color = BizcoTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Choose the period to include:", color = BizcoTextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))

            StatementPeriod.entries.forEach { period ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(period) }
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BizcoBlue.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (period == StatementPeriod.ALL) Icons.Rounded.AllInclusive else Icons.Rounded.DateRange,
                            contentDescription = null,
                            tint = BizcoBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(period.label, color = BizcoTextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = BizcoTextMuted, modifier = Modifier.size(18.dp))
                }
                Divider(color = BizcoBorder, thickness = 0.5.dp)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
