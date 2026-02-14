package com.example.studentexpensetracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var expenseId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        dbHelper = DatabaseHelper(this)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val spinnerCategory = findViewById<android.widget.Spinner>(R.id.spinnerCategory)
        val etDate = findViewById<EditText>(R.id.etDate)
        val etTime = findViewById<EditText>(R.id.etTime)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val spinnerPaymentMode = findViewById<android.widget.Spinner>(R.id.spinnerPaymentMode)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val rgTransactionType = findViewById<RadioGroup>(R.id.rgTransactionType)
        val rbExpense = findViewById<RadioButton>(R.id.rbExpense)
        val rbIncome = findViewById<RadioButton>(R.id.rbIncome)

        // Setup Categories
        val categories = arrayOf("Food", "Travel", "Shopping", "Bills", "Rent", "Others")
        val categoryAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        // Setup Payment Modes
        val paymentModes = arrayOf("Cash", "Card", "Online", "PhonePe", "Google Pay")
        val paymentAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentModes)
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPaymentMode.adapter = paymentAdapter

        // Check if editing
        if (intent.hasExtra("EXPENSE_ID")) {
            expenseId = intent.getIntExtra("EXPENSE_ID", -1)
            etTitle.setText(intent.getStringExtra("TITLE"))
            etAmount.setText(intent.getDoubleExtra("AMOUNT", 0.0).toString())
            etDate.setText(intent.getStringExtra("DATE"))
            etTime.setText(intent.getStringExtra("TIME") ?: "")
            etDescription.setText(intent.getStringExtra("DESCRIPTION"))
            
            val category = intent.getStringExtra("CATEGORY")
            if (category != null) {
                val spinnerPosition = categoryAdapter.getPosition(category)
                spinnerCategory.setSelection(spinnerPosition)
            }

            val paymentMode = intent.getStringExtra("PAYMENT_MODE")
            if (paymentMode != null) {
                val spinnerPosition = paymentAdapter.getPosition(paymentMode)
                spinnerPaymentMode.setSelection(spinnerPosition)
            }

            val type = intent.getStringExtra("TYPE") ?: "Expense"
            if (type == "Income") {
                rbIncome.isChecked = true
            } else {
                rbExpense.isChecked = true
            }

            btnSave.text = if (type == "Income") "Update Income" else "Update Expense"
            title = if (type == "Income") "Update Income" else "Update Expense"
        } else {
            // Set current date as default for new entry
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            etDate.setText(currentDate)
            
            val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            etTime.setText(currentTime)
        }
        
        // Date Picker Dialog
        etDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val datePickerDialog = android.app.DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                etDate.setText(selectedDate)
            }, year, month, day)
            
            datePickerDialog.show()
        }

        // Time Picker Dialog
        etTime.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)

            val timePickerDialog = android.app.TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val calendarTime = java.util.Calendar.getInstance()
                calendarTime.set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
                calendarTime.set(java.util.Calendar.MINUTE, selectedMinute)
                val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendarTime.time)
                etTime.setText(formattedTime)
            }, hour, minute, false)
            
            timePickerDialog.show()
        }

        // Update button text based on transaction type
        rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbIncome -> {
                    btnSave.text = if (expenseId != -1) "Update Income" else "Save Income"
                }
                R.id.rbExpense -> {
                    btnSave.text = if (expenseId != -1) "Update Expense" else "Save Expense"
                }
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val amountStr = etAmount.text.toString()
            val category = spinnerCategory.selectedItem.toString()
            val date = etDate.text.toString()
            val time = etTime.text.toString()
            val description = etDescription.text.toString()
            val paymentMode = spinnerPaymentMode.selectedItem.toString()

            if (title.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                id = expenseId,
                title = title,
                amount = amount,
                category = category,
                date = date,
                time = time,
                description = description,
                paymentMode = paymentMode,
                type = if (rbIncome.isChecked) "Income" else "Expense"
            )

            if (expenseId != -1) {
                val success = dbHelper.updateExpense(expense)
                if (success > 0) {
                    val message = if (rbIncome.isChecked) "Income Updated" else "Expense Updated"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val message = if (rbIncome.isChecked) "Error updating income" else "Error updating expense"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                // Add
                val id = dbHelper.addExpense(expense)
                if (id > -1) {
                    val message = if (rbIncome.isChecked) "Income Added" else "Expense Added"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val message = if (rbIncome.isChecked) "Error adding income" else "Error adding expense"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
