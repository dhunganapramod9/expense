package com.example.expensetracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.math.BigDecimal
import java.util.UUID

class ExpenseViewModel : ViewModel() {
    var expenseList: List<ExpenseItem> by mutableStateOf(emptyList())
        private set
    var inputName: String by mutableStateOf("")
        private set
    var inputAmount: String by mutableStateOf("")
        private set
    var nameError: String? by mutableStateOf(null)
        private set
    var amountError: String? by mutableStateOf(null)
        private set

    // One source of truth: the total always reflects the current list.
    val totalSpent: BigDecimal
        get() = expenseList.fold(BigDecimal.ZERO) { total, expense -> total + expense.amount }

    fun onNameChange(value: String) {
        inputName = value
        nameError = null
    }

    fun onAmountChange(value: String) {
        inputAmount = value
        amountError = null
    }

    fun addExpense() {
        val name = inputName.trim()
        val amountText = inputAmount.trim()
        val amount = amountText.toBigDecimalOrNull()
        nameError = if (name.isEmpty()) "Enter an expense name." else null
        amountError = when {
            amountText.isEmpty() -> "Enter an expense amount."
            amount == null -> "Enter a valid number, such as 12.50."
            amount <= BigDecimal.ZERO -> "Amount must be greater than zero."
            !amountText.matches(Regex("(?:[0-9]+(?:\\.[0-9]{0,2})?|\\.[0-9]{1,2})")) ->
                "Use a decimal amount with at most two decimal places."
            else -> null
        }
        if (nameError != null || amountError != null || amount == null) return
        expenseList = expenseList + ExpenseItem(UUID.randomUUID().toString(), name, amount)
        inputName = ""
        inputAmount = ""
    }

    fun deleteExpense(id: String) {
        expenseList = expenseList.filterNot { it.id == id }
    }
}
