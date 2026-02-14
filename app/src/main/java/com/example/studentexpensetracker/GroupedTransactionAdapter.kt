package com.example.studentexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupedTransactionAdapter(
    private var groups: List<TransactionGroup>,
    private val onEditClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TRANSACTION = 1
    }

    // Flattened list of items to display (headers + expanded transactions)
    private var displayItems = mutableListOf<DisplayItem>()

    init {
        updateDisplayItems()
    }

    sealed class DisplayItem {
        data class Header(val group: TransactionGroup, val groupIndex: Int) : DisplayItem()
        data class Transaction(val expense: Expense, val groupIndex: Int) : DisplayItem()
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is DisplayItem.Header -> VIEW_TYPE_HEADER
            is DisplayItem.Transaction -> VIEW_TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction_group, parent, false)
                GroupHeaderViewHolder(view)
            }
            VIEW_TYPE_TRANSACTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_expense, parent, false)
                TransactionViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is DisplayItem.Header -> {
                (holder as GroupHeaderViewHolder).bind(item.group, item.groupIndex)
            }
            is DisplayItem.Transaction -> {
                (holder as TransactionViewHolder).bind(item.expense)
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size

    inner class GroupHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGroupDate: TextView = itemView.findViewById(R.id.tvGroupDate)
        private val tvTransactionCount: TextView = itemView.findViewById(R.id.tvTransactionCount)
        private val tvTotalExpense: TextView = itemView.findViewById(R.id.tvTotalExpense)
        private val tvTotalIncome: TextView = itemView.findViewById(R.id.tvTotalIncome)
        private val ivExpandIcon: ImageView = itemView.findViewById(R.id.ivExpandIcon)

        fun bind(group: TransactionGroup, groupIndex: Int) {
            tvGroupDate.text = "📅 ${group.displayDate}"
            tvTransactionCount.text = group.transactionCount.toString()
            tvTotalExpense.text = String.format("%.2f", group.totalExpense)
            tvTotalIncome.text = String.format("%.2f", group.totalIncome)

            // Update expand icon
            val iconRes = if (group.isExpanded) {
                R.drawable.ic_expand_less
            } else {
                R.drawable.ic_expand_more
            }
            ivExpandIcon.setImageResource(iconRes)

            // Handle click to expand/collapse
            itemView.setOnClickListener {
                toggleGroup(groupIndex)
            }
        }
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvPaymentMode: TextView = itemView.findViewById(R.id.tvPaymentMode)
        private val cvCategoryIcon: androidx.cardview.widget.CardView = itemView.findViewById(R.id.cvCategoryIcon)
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        private val btnEdit: android.widget.ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: android.widget.ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(expense: Expense) {
            tvTitle.text = expense.title
            tvAmount.text = String.format("%.2f", expense.amount)

            // Set amount color based on transaction type
            val context = itemView.context
            if (expense.type == "Income") {
                tvAmount.setTextColor(context.getColor(R.color.success))
            } else {
                tvAmount.setTextColor(context.getColor(R.color.error))
            }

            tvCategory.text = expense.category
            tvDate.text = expense.date
            tvPaymentMode.text = expense.paymentMode
            
            // Set Category Icon and Theme Color
            setCategoryIcon(expense.category)

            // Set Payment Mode Tag Color
            if (expense.paymentMode.equals("Cash", ignoreCase = true)) {
                tvPaymentMode.background.setTint(android.graphics.Color.parseColor("#DBEAFE"))
                tvPaymentMode.setTextColor(android.graphics.Color.parseColor("#1D4ED8"))
            } else {
                tvPaymentMode.background.setTint(android.graphics.Color.parseColor("#D1FAE5"))
                tvPaymentMode.setTextColor(android.graphics.Color.parseColor("#047857"))
            }

            tvPaymentMode.visibility = View.VISIBLE

            btnEdit.setOnClickListener {
                onEditClick(expense)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(expense)
            }
        }

        private fun setCategoryIcon(category: String) {
            val context = itemView.context
            val (iconRes, bgColor, iconColor) = when (category) {
                "Food" -> Triple(R.drawable.ic_food, R.color.cat_food_bg, R.color.cat_food_icon)
                "Travel" -> Triple(R.drawable.ic_travel, R.color.cat_travel_bg, R.color.cat_travel_icon)
                "Shopping" -> Triple(R.drawable.ic_shopping, R.color.cat_shopping_bg, R.color.cat_shopping_icon)
                "Bills" -> Triple(R.drawable.ic_bills, R.color.cat_bills_bg, R.color.cat_bills_icon)
                "Rent" -> Triple(R.drawable.ic_rent, R.color.cat_rent_bg, R.color.cat_rent_icon)
                else -> Triple(R.drawable.ic_others, R.color.cat_others_bg, R.color.cat_others_icon)
            }

            ivCategoryIcon.setImageResource(iconRes)
            ivCategoryIcon.imageTintList = android.content.res.ColorStateList.valueOf(context.getColor(iconColor))
            cvCategoryIcon.setCardBackgroundColor(context.getColor(bgColor))
        }
    }

    private fun toggleGroup(groupIndex: Int) {
        val group = groups[groupIndex]
        group.isExpanded = !group.isExpanded

        // Find the position of the header in displayItems
        val headerPosition = displayItems.indexOfFirst {
            it is DisplayItem.Header && it.groupIndex == groupIndex
        }

        if (headerPosition == -1) return

        if (group.isExpanded) {
            // Insert transactions after the header
            val transactionsToInsert = group.transactions.mapIndexed { index, expense ->
                DisplayItem.Transaction(expense, groupIndex)
            }
            displayItems.addAll(headerPosition + 1, transactionsToInsert)
            notifyItemChanged(headerPosition) // Update header icon
            notifyItemRangeInserted(headerPosition + 1, transactionsToInsert.size)
        } else {
            // Remove transactions
            val transactionCount = group.transactions.size
            repeat(transactionCount) {
                displayItems.removeAt(headerPosition + 1)
            }
            notifyItemChanged(headerPosition) // Update header icon
            notifyItemRangeRemoved(headerPosition + 1, transactionCount)
        }
    }

    private fun updateDisplayItems() {
        displayItems.clear()
        groups.forEachIndexed { groupIndex, group ->
            // Add header
            displayItems.add(DisplayItem.Header(group, groupIndex))
            // Add transactions if expanded
            if (group.isExpanded) {
                group.transactions.forEach { expense ->
                    displayItems.add(DisplayItem.Transaction(expense, groupIndex))
                }
            }
        }
    }

    fun updateData(newGroups: List<TransactionGroup>) {
        groups = newGroups
        updateDisplayItems()
        notifyDataSetChanged()
    }
}
