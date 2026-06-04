package com.bizcopay.app.ui.payer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizcopay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayerInsightsScreen(viewModel: PayerViewModel) {
    val analytics by viewModel.analytics.collectAsState()
    val analyticsLoaded by viewModel.analyticsLoaded.collectAsState()
    var selectedPeriod by remember { mutableStateOf("year") }

    LaunchedEffect(Unit) { viewModel.loadAnalytics(selectedPeriod) }

    Box(modifier = Modifier.fillMaxSize().background(BizcoBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            item {
                Text(
                    "Spending Insights",
                    color = BizcoTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("week" to "Week", "month" to "Month", "year" to "Year").forEach { (period, label) ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = {
                                selectedPeriod = period
                                viewModel.loadAnalytics(period)
                            },
                            label = { Text(label, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BizcoBlue,
                                selectedLabelColor = Color.White,
                                containerColor = BizcoSurface,
                                labelColor = BizcoTextSecondary
                            )
                        )
                    }
                }
            }

            if (!analyticsLoaded) {
                item {
                    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BizcoOrange)
                    }
                }
            } else if (analytics == null) {
                item {
                    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊", fontSize = 48.sp)
                            Spacer(Modifier.height(16.dp))
                            Text("No insights yet", color = BizcoTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Complete transactions to see spending analytics",
                                color = BizcoTextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 40.dp)
                            )
                        }
                    }
                }
            } else {
                val data = analytics!!

                // Summary card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BizcoCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Total Spent", color = BizcoTextSecondary, fontSize = 13.sp)
                            Text(
                                "RWF ${"%,.0f".format(data.totalSpent)}",
                                color = BizcoTextPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "This month: RWF ${"%,.0f".format(data.thisMonth)}",
                                color = BizcoOrange,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // Donut chart
                if (data.byCategory.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = BizcoCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "By Category",
                                    color = BizcoTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(20.dp))
                                val chartData = data.byCategory.map { it.category to it.amount.toFloat() }
                                val colors = data.byCategory.map { categoryColor(it.category) }
                                DonutChart(
                                    data = chartData,
                                    colors = colors,
                                    centerText = "RWF\n${"%,.0f".format(data.totalSpent)}",
                                    modifier = Modifier.size(200.dp)
                                )
                                Spacer(Modifier.height(20.dp))
                                data.byCategory.forEach { cat ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(categoryColor(cat.category))
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "${categoryEmoji(cat.category)} ${cat.category}",
                                            color = BizcoTextPrimary,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "RWF ${"%,.0f".format(cat.amount)}",
                                            color = BizcoTextSecondary,
                                            fontSize = 13.sp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val pct = if (data.totalSpent > 0)
                                            (cat.amount / data.totalSpent * 100).toInt() else 0
                                        Text(
                                            "$pct%",
                                            color = BizcoOrange,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
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
                                    "Monthly Spending",
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
                                        .height(160.dp)
                                )
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
fun DonutChart(
    data: List<Pair<String, Float>>,
    colors: List<Color>,
    centerText: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val total = data.sumOf { it.second.toDouble() }.toFloat()
            if (total == 0f) return@Canvas
            val strokeWidth = size.minDimension * 0.18f
            val arcSize = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - arcSize) / 2f,
                (size.height - arcSize) / 2f
            )
            var startAngle = -90f
            data.forEachIndexed { i, (_, value) ->
                val sweep = (value / total) * 360f
                drawArc(
                    color = colors[i % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep - 3f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            centerText.split("\n").forEachIndexed { i, line ->
                Text(
                    line,
                    color = BizcoTextPrimary,
                    fontSize = if (i == 0) 11.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BizcoBarChart(data: List<Pair<String, Float>>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val maxVal = data.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1f
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val n = data.size
            val barW = size.width / (n * 2f)
            data.forEachIndexed { i, (_, v) ->
                val barH = (v / maxVal) * (size.height - 8f)
                val x = i * (barW + barW) + barW / 2f
                drawRoundRect(
                    color = BizcoOrange,
                    topLeft = Offset(x, size.height - barH),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach { (label, _) ->
                Text(label, color = BizcoTextSecondary, fontSize = 10.sp)
            }
        }
    }
}
