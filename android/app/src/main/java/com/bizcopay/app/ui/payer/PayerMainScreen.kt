package com.bizcopay.app.ui.payer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bizcopay.app.ui.theme.*

@Composable
fun PayerMainScreen(rootNavController: NavController, viewModel: PayerViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        "Home" to Icons.Rounded.Home,
        "History" to Icons.Rounded.History,
        "Insights" to Icons.Rounded.PieChart,
        "Profile" to Icons.Rounded.Person,
    )

    Scaffold(
        containerColor = BizcoBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(BizcoCard)
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { i, (label, icon) ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedTab = i }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (i == selectedTab) BizcoBlue else BizcoSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (i == selectedTab) Color.White else BizcoTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                label,
                                color = if (i == selectedTab) BizcoBlue else BizcoTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (i == selectedTab) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> PayerHomeScreen(rootNavController, viewModel, onGoToHistory = { selectedTab = 1 })
                1 -> PayerHistoryScreen(viewModel)
                2 -> PayerInsightsScreen(viewModel)
                3 -> PayerProfileScreen(rootNavController, viewModel)
            }
        }
    }
}
