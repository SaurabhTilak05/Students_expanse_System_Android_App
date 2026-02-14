package com.example.studentexpensetracker

import java.text.SimpleDateFormat
import java.util.*

object TransactionGroupHelper {
    
    /**
     * Groups a list of expenses by date and returns a list of TransactionGroup objects
     * sorted by date (most recent first)
     */
    fun groupTransactionsByDate(expenses: List<Expense>): List<TransactionGroup> {
        // Group expenses by date
        val groupedMap = expenses.groupBy { it.date }
        
        // Convert to TransactionGroup objects
        val groups = groupedMap.map { (date, transactions) ->
            val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
            val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
            
            TransactionGroup(
                date = date,
                displayDate = formatDateForDisplay(date),
                transactions = transactions,
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                transactionCount = transactions.size,
                isExpanded = false
            )
        }
        
        // Sort by date (most recent first)
        return groups.sortedByDescending { parseDateString(it.date) }
    }
    
    /**
     * Formats a date string to display as "Today", "Yesterday", or formatted date
     * Input format: "dd/MM/yyyy"
     */
    private fun formatDateForDisplay(dateString: String): String {
        val date = parseDateString(dateString) ?: return dateString
        
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time
        
        return when {
            isSameDay(date, today) -> "Today"
            isSameDay(date, yesterday) -> "Yesterday"
            else -> {
                // Format as "14 Feb 2026"
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                formatter.format(date)
            }
        }
    }
    
    /**
     * Parses a date string in format "yyyy-MM-dd" to Date object
     */
    private fun parseDateString(dateString: String): Date? {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Checks if two dates are on the same day
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
