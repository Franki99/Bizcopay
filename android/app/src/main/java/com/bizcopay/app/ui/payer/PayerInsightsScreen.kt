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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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

    LaunchedEffect(selectedPeriod) { viewModel.loadAnalytics(selectedPeriod) }

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
                            onClick = { selectedPeriod = period },
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
                        CircularProgressIndicator(color = BizcoGreen)
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
                                color = BizcoGreen,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // Area chart
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BizcoCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            val chartTitle = when (selectedPeriod) {
                                "week" -> "Spending This Week"
                                "month" -> "Spending This Month"
                                else -> "Spending This Year"
                            }
                            Text(
                                chartTitle,
                                color = BizcoTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(16.dp))
                            val chartData = data.chartPoints.map { it.label to it.amount.toFloat() }
                            SmoothAreaChart(
                                data = chartData,
                                lineColor = BizcoGreen,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
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
                                            color = BizcoGreen,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
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
fun SmoothAreaChart(
    data: List<Pair<String, Float>>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    val allZero = data.all { it.second == 0f }

    if (allZero) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", color = BizcoTextSecondary, fontSize = 13.sp)
        }
        return
    }

    val n = data.size
    val step = when {
        n <= 7  -> 1
        n <= 14 -> 2
        else    -> n / 5
    }

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val w = size.width
            val h = size.height
            val topPad = 12f

            val maxVal = data.maxOf { it.second }.takeIf { it > 0f } ?: 1f

            // Grid lines
            listOf(1f / 3f, 2f / 3f, 1f).forEach { frac ->
                val y = topPad + (h - topPad) * frac
                drawLine(
                    color = Color.White.copy(alpha = 0.07f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }

            // Compute point positions
            val pts = data.mapIndexed { i, (_, v) ->
                val x = if (n == 1) w / 2f else i * (w / (n - 1).toFloat())
                val y = topPad + (1f - v / maxVal) * (h - topPad)
                Offset(x, y)
            }

            // Bezier line path
            val linePath = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val cx = (pts[i - 1].x + pts[i].x) / 2f
                    cubicTo(cx, pts[i - 1].y, cx, pts[i].y, pts[i].x, pts[i].y)
                }
            }

            // Gradient fill path (close area to bottom)
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(pts.last().x, h)
                lineTo(pts.first().x, h)
                close()
            }

            val gradient = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.40f), Color.Transparent),
                startY = topPad,
                endY = h
            )
            drawPath(fillPath, brush = gradient)
            drawPath(
                linePath,
                color = lineColor,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Peak dot
            val peakIdx = data.indexOfFirst { it.second == data.maxOf { p -> p.second } }
            val peakPt = pts[peakIdx]
            drawCircle(lineColor, radius = 5f, center = peakPt)
            drawCircle(Color.White, radius = 2.5f, center = peakPt)
        }

        // X-axis labels
        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            data.forEachIndexed { i, (label, _) ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (i % step == 0 || i == data.lastIndex) {
                        Text(label, color = BizcoTextSecondary, fontSize = 9.sp, textAlign = TextAlign.Center)
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
                    color = BizcoGreen,
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
