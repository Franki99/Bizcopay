package com.bizcopay.app.ui.payer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bizcopay.app.data.local.TokenManager
import com.bizcopay.app.ui.theme.*

@Composable
fun PayerHomeScreen(
    rootNavController: NavController,
    viewModel: PayerViewModel,
    onGoToHistory: () -> Unit,
) {
    val state   by viewModel.state.collectAsState()
    val wallet  by viewModel.wallet.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val context = LocalContext.current
    val name    = remember { TokenManager(context).getName() ?: "User" }
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
                        Text("Good morning,", color = BizcoTextSecondary.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text(name, color = BizcoTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(28.dp))
                        Text("Active balance", color = BizcoTextSecondary.copy(alpha = 0.8f), fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        wallet?.let {
                            Text(
                                "${it.currency} ${"%,.0f".format(it.balance.toDouble())}",
                                color = BizcoTextPrimary,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            )
                        } ?: Text("—", color = BizcoTextPrimary, fontSize = 38.sp, fontWeight = FontWeight.Bold)
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
                        Text("View all", color = BizcoOrange, fontSize = 13.sp)
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
                    TransactionListItem(
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
                    Text("✓", fontSize = 48.sp, color = BizcoSuccess)
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
                    Text("✕", fontSize = 48.sp, color = BizcoError)
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
