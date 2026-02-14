package com.example.studentexpensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var tvTotalExpense: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        val rvExpenses = findViewById<RecyclerView>(R.id.rvExpenses)
        rvExpenses.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter(emptyList())
        rvExpenses.adapter = expenseAdapter
        
        val itemTouchHelperCallback = object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT or androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val expense = expenseAdapter.getExpenseAt(position)
                dbHelper.deleteExpense(expense.id)
                expenseAdapter.removeItem(position)
                loadData() // Refresh total
                android.widget.Toast.makeText(this@MainActivity, "Expense Deleted", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvExpenses)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val expenses = dbHelper.getAllExpenses()
        expenseAdapter.updateData(expenses)
        
        val total = dbHelper.getTotalExpense()
        tvTotalExpense.text = String.format("$%.2f", total)
    }
}
