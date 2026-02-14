package com.example.studentexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(private var expenseList: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.tvTitle.text = expense.title
        holder.tvAmount.text = String.format("$%.2f", expense.amount)
        holder.tvCategory.text = expense.category
        holder.tvDate.text = expense.date
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }
    
    fun updateData(newExpenses: List<Expense>) {
        expenseList = newExpenses
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        val list = expenseList.toMutableList()
        list.removeAt(position)
        expenseList = list
        notifyItemRemoved(position)
    }

    fun getExpenseAt(position: Int): Expense {
        return expenseList[position]
    }
}
