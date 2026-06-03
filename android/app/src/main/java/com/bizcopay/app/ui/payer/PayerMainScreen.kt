package com.bizcopay.app.ui.payer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bizcopay.app.ui.theme.*

data class PayerTab(val label: String, val icon: ImageVector)

@Composable
fun PayerMainScreen(rootNavController: NavController, viewModel: PayerViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        PayerTab("Home",     Icons.Default.Home),
        PayerTab("History",  Icons.Default.DateRange),
        PayerTab("Insights", Icons.Default.BarChart),
        PayerTab("Profile",  Icons.Default.Person),
    )

    Scaffold(
        containerColor = BizcoBackground,
        bottomBar = {
            NavigationBar(
                containerColor = BizcoCard,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { i, tab ->
                    NavigationBarItem(
                        selected = i == selectedTab,
                        onClick  = { selectedTab = i },
                        icon     = { Icon(tab.icon, contentDescription = tab.label) },
                        label    = { Text(tab.label) },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = BizcoOrange,
                            selectedTextColor   = BizcoOrange,
                            unselectedIconColor = BizcoTextSecondary,
                            unselectedTextColor = BizcoTextSecondary,
                            indicatorColor      = BizcoSurface,
                        )
                    )
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
