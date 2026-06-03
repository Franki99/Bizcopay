package com.bizcopay.app.ui.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizcopay.app.ui.payer.BizcoBarChart
import com.bizcopay.app.ui.theme.*

@Composable
fun MerchantAnalyticsScreen(viewModel: MerchantViewModel) {
    val analytics by viewModel.analytics.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAnalytics() }

    Box(modifier = Modifier.fillMaxSize().background(BizcoBackground)) {
        if (analytics == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BizcoBlue)
            }
        } else {
            val data = analytics!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    Text(
                        "Analytics",
                        color = BizcoTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }

                // Summary cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            "Total Revenue",
                            "RWF ${"%,.0f".format(data.totalRevenue)}",
                            Modifier.weight(1f)
                        )
                        StatCard(
                            "This Month",
                            "RWF ${"%,.0f".format(data.thisMonth)}",
                            Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    StatCard(
                        "Total Sales",
                        "${data.transactionCount} transactions",
                        Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Monthly bar chart
                if (data.byMonth.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = BizcoCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    "Revenue by Month",
                                    color = BizcoTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(16.dp))
                                val barData = data.byMonth.map { it.month.takeLast(5) to it.amount.toFloat() }
                                BizcoBarChart(
                                    data = barData,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = BizcoCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    "Monthly Breakdown",
                                    color = BizcoTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(12.dp))
                                data.byMonth.reversed().forEach { m ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            m.month,
                                            color = BizcoTextSecondary,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "${m.count} sales",
                                            color = BizcoTextMuted,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "RWF ${"%,.0f".format(m.amount)}",
                                            color = BizcoTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    HorizontalDivider(color = BizcoBorder, thickness = 0.5.dp)
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BizcoCard)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = BizcoTextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = BizcoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
