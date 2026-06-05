package com.bizcopay.app.ui.common

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizcopay.app.data.notification.NotificationItem
import com.bizcopay.app.data.notification.NotificationStore
import com.bizcopay.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(onDismiss: () -> Unit) {
    val notifications by NotificationStore.notifications.collectAsState()
    var selectedNotif by remember { mutableStateOf<NotificationItem?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BizcoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notifications", color = BizcoTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (notifications.isNotEmpty()) {
                    IconButton(onClick = { NotificationStore.clearAll() }) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear all", tint = BizcoTextSecondary)
                    }
                }
            }

            Divider(color = BizcoBorder, thickness = 0.5.dp)

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.NotificationsNone, contentDescription = null, tint = BizcoTextMuted, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No notifications yet", color = BizcoTextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Payment and wallet alerts will appear here",
                            color = BizcoTextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 520.dp)) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationRow(notif, onClick = { selectedNotif = notif })
                        Divider(color = BizcoBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 72.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    selectedNotif?.let { notif ->
        NotificationDetailDialog(notif = notif, onDismiss = { selectedNotif = null })
    }
}

@Composable
private fun NotificationRow(item: NotificationItem, onClick: () -> Unit) {
    val isPayment = item.type == "payment"
    val iconColor = if (isPayment) BizcoBlue else BizcoGreen
    val icon = if (isPayment) Icons.Rounded.Payment else Icons.Rounded.AccountBalanceWallet

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!item.isRead) BizcoBlue.copy(alpha = 0.04f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title,
                    color = BizcoTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (!item.isRead) FontWeight.SemiBold else FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (!item.isRead) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BizcoBlue))
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                item.body,
                color = BizcoTextSecondary,
                fontSize = 13.sp,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Text(relativeTime(item.timestamp), color = BizcoTextMuted, fontSize = 11.sp)
        }

        Spacer(Modifier.width(8.dp))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = BizcoTextMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun NotificationDetailDialog(notif: NotificationItem, onDismiss: () -> Unit) {
    val isPayment = notif.type == "payment"
    val iconColor = if (isPayment) BizcoBlue else BizcoGreen
    val icon = if (isPayment) Icons.Rounded.Payment else Icons.Rounded.AccountBalanceWallet

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BizcoCard,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
        },
        title = {
            Text(notif.title, color = BizcoTextPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    notif.body,
                    color = BizcoTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    absoluteTime(notif.timestamp),
                    color = BizcoTextMuted,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = BizcoBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

private fun relativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < TimeUnit.MINUTES.toMillis(1)  -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1)    -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} min ago"
        diff < TimeUnit.HOURS.toMillis(24)   -> "${TimeUnit.MILLISECONDS.toHours(diff)} hr ago"
        diff < TimeUnit.DAYS.toMillis(7)     -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun absoluteTime(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))
