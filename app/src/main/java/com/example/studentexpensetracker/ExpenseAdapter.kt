package com.example.studentexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(
    private var expenseList: List<Expense>,
    private val onEditClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvPaymentMode: TextView = itemView.findViewById(R.id.tvPaymentMode)
        val btnEdit: android.widget.ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: android.widget.ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.tvTitle.text = expense.title
        
        // Remove $ sign and apply color based on type
        holder.tvAmount.text = String.format("%.2f", expense.amount)
        
        // Set amount color based on transaction type
        val context = holder.itemView.context
        if (expense.type == "Income") {
            holder.tvAmount.setTextColor(context.getColor(R.color.success))
        } else {
            holder.tvAmount.setTextColor(context.getColor(R.color.error))
        }
        
        holder.tvCategory.text = expense.category
        holder.tvDate.text = expense.date
        holder.tvPaymentMode.text = expense.paymentMode
        
        // Set Payment Mode Tag Color (Simple logic for demo)
        if (expense.paymentMode.equals("Cash", ignoreCase = true)) {
             holder.tvPaymentMode.background.setTint(android.graphics.Color.parseColor("#DBEAFE")) // Blue-ish
             holder.tvPaymentMode.setTextColor(android.graphics.Color.parseColor("#1D4ED8"))
        } else {
             holder.tvPaymentMode.background.setTint(android.graphics.Color.parseColor("#D1FAE5")) // Green-ish
             holder.tvPaymentMode.setTextColor(android.graphics.Color.parseColor("#047857"))
        }

        // Visibility logic removed as Payment Mode is now always part of the layout
        holder.tvPaymentMode.visibility = View.VISIBLE
        
        holder.btnEdit.setOnClickListener {
            onEditClick(expense)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(expense)
        }
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }
    
    fun updateData(newExpenses: List<Expense>) {
        expenseList = newExpenses
        notifyDataSetChanged()
    }

    fun getExpenseAt(position: Int): Expense {
        return expenseList[position]
    }
}
