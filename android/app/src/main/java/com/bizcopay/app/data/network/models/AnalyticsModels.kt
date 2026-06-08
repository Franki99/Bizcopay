package com.bizcopay.app.data.network.models

data class CategorySpending(val category: String, val amount: Double, val count: Int)
data class ChartPoint(val label: String, val amount: Double, val count: Int = 0)

data class PayerAnalyticsResponse(
    val totalSpent: Double,
    val thisMonth: Double,
    val byCategory: List<CategorySpending>,
    val chartPoints: List<ChartPoint>,
)

data class MerchantAnalyticsResponse(
    val totalRevenue: Double,
    val thisMonth: Double,
    val transactionCount: Int,
    val chartPoints: List<ChartPoint>,
)
