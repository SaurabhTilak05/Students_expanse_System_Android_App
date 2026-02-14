package com.example.studentexpensetracker

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "expense_tracker.db"
        private const val DATABASE_VERSION = 3 // Incremented for type column
        const val TABLE_EXPENSES = "expenses"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_DATE = "date"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_PAYMENT_MODE = "payment_mode"
        const val COLUMN_TYPE = "type" // Income or Expense
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_EXPENSES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_AMOUNT + " REAL,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_PAYMENT_MODE + " TEXT,"
                + COLUMN_TYPE + " TEXT DEFAULT 'Expense'" + ")")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COLUMN_DESCRIPTION TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COLUMN_PAYMENT_MODE TEXT DEFAULT ''")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_EXPENSES ADD COLUMN $COLUMN_TYPE TEXT DEFAULT 'Expense'")
        }
    }

    fun addExpense(expense: Expense): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TITLE, expense.title)
        values.put(COLUMN_AMOUNT, expense.amount)
        values.put(COLUMN_DATE, expense.date)
        values.put(COLUMN_CATEGORY, expense.category)
        values.put(COLUMN_DESCRIPTION, expense.description)
        values.put(COLUMN_PAYMENT_MODE, expense.paymentMode)
        values.put(COLUMN_TYPE, expense.type)
        val id = db.insert(TABLE_EXPENSES, null, values)
        db.close()
        return id
    }

    fun updateExpense(expense: Expense): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TITLE, expense.title)
        values.put(COLUMN_AMOUNT, expense.amount)
        values.put(COLUMN_DATE, expense.date)
        values.put(COLUMN_CATEGORY, expense.category)
        values.put(COLUMN_DESCRIPTION, expense.description)
        values.put(COLUMN_PAYMENT_MODE, expense.paymentMode)
        values.put(COLUMN_TYPE, expense.type)
        
        val success = db.update(TABLE_EXPENSES, values, "$COLUMN_ID = ?", arrayOf(expense.id.toString()))
        db.close()
        return success
    }

    fun getAllExpenses(): List<Expense> {
        val expenses = ArrayList<Expense>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSES ORDER BY $COLUMN_DATE DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val expense = Expense(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    paymentMode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_MODE)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)) ?: "Expense"
                )
                expenses.add(expense)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return expenses
    }
    
    fun getExpensesByMonth(year: Int, month: Int): List<Expense> {
        val expenses = ArrayList<Expense>()
        val db = this.readableDatabase
        // Format month to ensure two digits (01, 02, etc.)
        val monthStr = String.format("%02d", month)
        val dateFilter = "$year-$monthStr-%"
        
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EXPENSES WHERE $COLUMN_DATE LIKE ? ORDER BY $COLUMN_DATE DESC", arrayOf(dateFilter))
        
        if (cursor.moveToFirst()) {
            do {
                val expense = Expense(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    paymentMode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAYMENT_MODE)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)) ?: "Expense"
                )
                expenses.add(expense)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return expenses
    }

    fun getTotalExpense(year: Int = -1, month: Int = -1): Double {
        var total = 0.0
        val db = this.readableDatabase
        val cursor = if (year != -1 && month != -1) {
            val monthStr = String.format("%02d", month)
            val dateFilter = "$year-$monthStr-%"
            db.rawQuery("SELECT SUM($COLUMN_AMOUNT) FROM $TABLE_EXPENSES WHERE $COLUMN_DATE LIKE ? AND $COLUMN_TYPE = 'Expense'", arrayOf(dateFilter))
        } else {
             db.rawQuery("SELECT SUM($COLUMN_AMOUNT) FROM $TABLE_EXPENSES WHERE $COLUMN_TYPE = 'Expense'", null)
        }
        
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return total
    }

    fun getTotalIncome(year: Int = -1, month: Int = -1): Double {
        var total = 0.0
        val db = this.readableDatabase
        val cursor = if (year != -1 && month != -1) {
            val monthStr = String.format("%02d", month)
            val dateFilter = "$year-$monthStr-%"
            db.rawQuery("SELECT SUM($COLUMN_AMOUNT) FROM $TABLE_EXPENSES WHERE $COLUMN_DATE LIKE ? AND $COLUMN_TYPE = 'Income'", arrayOf(dateFilter))
        } else {
             db.rawQuery("SELECT SUM($COLUMN_AMOUNT) FROM $TABLE_EXPENSES WHERE $COLUMN_TYPE = 'Income'", null)
        }
        
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return total
    }

    fun deleteExpense(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_EXPENSES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }
}
