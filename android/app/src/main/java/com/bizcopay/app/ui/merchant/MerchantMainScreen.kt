package com.bizcopay.app.ui.merchant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bizcopay.app.ui.theme.*

data class MerchantTab(val label: String, val icon: ImageVector)

@Composable
fun MerchantMainScreen(rootNavController: NavController, viewModel: MerchantViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        MerchantTab("Terminal",  Icons.Default.TouchApp),
        MerchantTab("History",   Icons.Default.DateRange),
        MerchantTab("Analytics", Icons.Default.BarChart),
        MerchantTab("Profile",   Icons.Default.Person),
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
                0 -> MerchantTerminalScreen(rootNavController, viewModel)
                1 -> MerchantHistoryScreen(viewModel)
                2 -> MerchantAnalyticsScreen(viewModel)
                3 -> MerchantProfileScreen(rootNavController, viewModel)
            }
        }
    }
}
