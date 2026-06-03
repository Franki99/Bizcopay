package com.bizcopay.app.data.network.models

data class CategorySpending(val category: String, val amount: Double, val count: Int)
data class MonthlyAmount(val month: String, val amount: Double, val count: Int = 0)

data class PayerAnalyticsResponse(
    val totalSpent: Double,
    val thisMonth: Double,
    val byCategory: List<CategorySpending>,
    val byMonth: List<MonthlyAmount>,
)

data class MerchantAnalyticsResponse(
    val totalRevenue: Double,
    val thisMonth: Double,
    val transactionCount: Int,
    val byMonth: List<MonthlyAmount>,
)
