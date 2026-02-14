package com.example.studentexpensetracker

data class Expense(
    val id: Int = -1,
    val title: String,
    val amount: Double,
    val date: String,
    val category: String,
    val description: String = "",
    val paymentMode: String = "",
    val type: String = "Expense", // "Income" or "Expense"
    val time: String = "" // Added in DB version 4
)
