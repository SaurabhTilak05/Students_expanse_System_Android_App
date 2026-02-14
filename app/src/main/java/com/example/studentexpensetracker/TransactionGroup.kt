package com.example.studentexpensetracker

data class TransactionGroup(
    val date: String, // Original date in format "dd/MM/yyyy"
    val displayDate: String, // "Today", "Yesterday", or formatted date
    val transactions: List<Expense>,
    val totalExpense: Double,
    val totalIncome: Double,
    val transactionCount: Int,
    var isExpanded: Boolean = false
)
