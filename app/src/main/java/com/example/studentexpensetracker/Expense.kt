package com.example.studentexpensetracker

data class Expense(
    val id: Int = -1,
    val title: String,
    val amount: Double,
    val date: String,
    val category: String
)
