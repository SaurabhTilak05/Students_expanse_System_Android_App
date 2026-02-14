package com.example.studentexpensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var groupedAdapter: GroupedTransactionAdapter
    private lateinit var tvTotalExpense: TextView
    private lateinit var btnThemeToggle: ImageButton
    private var selectedYear = -1
    private var selectedMonth = -1

    companion object {
        private const val PREFS_NAME = "ThemePrefs"
        private const val KEY_THEME = "theme_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before super.onCreate
        applySavedTheme()
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        btnThemeToggle = findViewById(R.id.btnThemeToggle)
        
        val spinnerMonth = findViewById<android.widget.Spinner>(R.id.spinnerMonth)
        val spinnerYear = findViewById<android.widget.Spinner>(R.id.spinnerYear)
        val btnResetFilter = findViewById<android.widget.Button>(R.id.btnResetFilter)

        // Setup Month Spinner
        val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val monthAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter

        // Setup Year Spinner
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val years = ArrayList<String>()
        for (i in 0 until 5) {
            years.add((currentYear - i).toString())
        }
        val yearAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter

        // Set default selection to current date
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        spinnerMonth.setSelection(currentMonth)
        spinnerYear.setSelection(0) // Current year is at index 0

        val onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedMonth = spinnerMonth.selectedItemPosition + 1 // Months are 1-12
                selectedYear = spinnerYear.selectedItem.toString().toInt()
                loadData()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        spinnerMonth.onItemSelectedListener = onItemSelectedListener
        spinnerYear.onItemSelectedListener = onItemSelectedListener

        btnResetFilter.setOnClickListener {
            selectedMonth = -1
            selectedYear = -1
            // Optional: reset spinners visually or just load all
            loadData()
            android.widget.Toast.makeText(this, "Showing All Expenses", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Theme Toggle Button
        updateThemeIcon()
        btnThemeToggle.setOnClickListener {
            toggleTheme()
        }

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        val rvExpenses = findViewById<RecyclerView>(R.id.rvExpenses)
        rvExpenses.layoutManager = LinearLayoutManager(this)
        
        groupedAdapter = GroupedTransactionAdapter(emptyList(), 
            onEditClick = { expense ->
                val intent = Intent(this, AddExpenseActivity::class.java)
                intent.putExtra("EXPENSE_ID", expense.id)
                intent.putExtra("TITLE", expense.title)
                intent.putExtra("AMOUNT", expense.amount)
                intent.putExtra("CATEGORY", expense.category)
                intent.putExtra("DATE", expense.date)
                intent.putExtra("TIME", expense.time)
                intent.putExtra("DESCRIPTION", expense.description)
                intent.putExtra("PAYMENT_MODE", expense.paymentMode)
                intent.putExtra("TYPE", expense.type)
                startActivity(intent)
            },
            onDeleteClick = { expense ->
                // Show Confirmation Dialog
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Yes") { _, _ ->
                        val result = dbHelper.deleteExpense(expense.id)
                        if (result) {
                            loadData() // Refresh list and total
                            android.widget.Toast.makeText(this, "Expense Deleted", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(this, "Error Deleting Expense", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )
        rvExpenses.adapter = groupedAdapter

        // Footer - About App
        val tvFooterAppInfo = findViewById<TextView>(R.id.tvFooterAppInfo)
        tvFooterAppInfo.setOnClickListener {
            showAboutBottomSheet()
        }
    }

    private fun showAboutBottomSheet() {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_about_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        val btnClose = view.findViewById<android.widget.ImageButton>(R.id.btnClose)
        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val expenses = if (selectedYear != -1 && selectedMonth != -1) {
            dbHelper.getExpensesByMonth(selectedYear, selectedMonth)
        } else {
            dbHelper.getAllExpenses()
        }
        
        // Group transactions by date
        val groupedTransactions = TransactionGroupHelper.groupTransactionsByDate(expenses)
        groupedAdapter.updateData(groupedTransactions)
        
        val totalExpense = dbHelper.getTotalExpense(selectedYear, selectedMonth)
        val totalIncome = dbHelper.getTotalIncome(selectedYear, selectedMonth)
        
        // Remove $ sign - display amount only
        tvTotalExpense.text = String.format("%.2f", totalExpense)
        
        // Calculate progress based on income
        val progress = if (totalIncome > 0) ((totalExpense / totalIncome) * 100).toInt() else 0
        
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)
        progressBar.progress = progress.coerceIn(0, 100)
        
        // Update Income and Expense displays without $ sign
        val tvIncomeSummary = findViewById<TextView>(R.id.tvIncomeSummary)
        tvIncomeSummary?.text = String.format("%.2f", totalIncome)
        
        val tvExpenseSummary = findViewById<TextView>(R.id.tvExpenseSummary)
        tvExpenseSummary.text = String.format("%.2f", totalExpense)
    }

    private fun applySavedTheme() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun toggleTheme() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val newMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        
        // Save theme preference
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        sharedPreferences.edit().putInt(KEY_THEME, newMode).apply()
        
        // Apply theme
        AppCompatDelegate.setDefaultNightMode(newMode)
    }

    private fun updateThemeIcon() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val iconRes = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            R.drawable.ic_light_mode
        } else {
            R.drawable.ic_dark_mode
        }
        btnThemeToggle.setImageResource(iconRes)
    }
}
