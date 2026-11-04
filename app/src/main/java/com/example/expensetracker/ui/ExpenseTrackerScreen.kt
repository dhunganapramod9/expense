package com.example.expensetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ExpenseItem
import com.example.expensetracker.ExpenseViewModel
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import java.math.BigDecimal
import java.text.NumberFormat

@Composable
fun ExpenseTrackerRoute(viewModel: ExpenseViewModel) {
    ExpenseTrackerScreen(
        expenses = viewModel.expenseList,
        inputName = viewModel.inputName,
        inputAmount = viewModel.inputAmount,
        totalSpent = viewModel.totalSpent,
        nameError = viewModel.nameError,
        amountError = viewModel.amountError,
        onNameChange = viewModel::onNameChange,
        onAmountChange = viewModel::onAmountChange,
        onAddExpense = viewModel::addExpense,
        onDeleteExpense = viewModel::deleteExpense
    )
}

@Composable
fun ExpenseTrackerScreen(
    expenses: List<ExpenseItem>,
    inputName: String,
    inputAmount: String,
    totalSpent: BigDecimal,
    nameError: String?,
    amountError: String?,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onAddExpense: () -> Unit,
    onDeleteExpense: (String) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        // One scroll container keeps the form usable with the keyboard or large text.
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "summary") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Expense Tracker", style = MaterialTheme.typography.headlineMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Total spent", style = MaterialTheme.typography.titleMedium)
                            Text(
                                formatAmount(totalSpent),
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.semantics {
                                    contentDescription = "Total spent ${formatAmount(totalSpent)}"
                                }
                            )
                            Text("${expenses.size} expenses recorded")
                        }
                    }
                }
            }
            item(key = "form") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add an expense", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = onNameChange,
                        label = { Text("Expense Name") },
                        placeholder = { Text("e.g. Lunch") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        isError = nameError != null,
                        supportingText = { nameError?.let { Text(it) } }
                    )
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = onAmountChange,
                        label = { Text("Expense Amount") },
                        placeholder = { Text("e.g. 12.50") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = amountError != null,
                        supportingText = { Text(amountError ?: "Positive amount; use a dot and up to 2 decimal places.") }
                    )
                    Button(onClick = onAddExpense, modifier = Modifier.fillMaxWidth()) {
                        Text("Add Expense")
                    }
                }
            }
            item(key = "list-heading") {
                Text("Recorded expenses", style = MaterialTheme.typography.titleLarge)
            }
            if (expenses.isEmpty()) {
                item(key = "empty") {
                    Card(Modifier.fillMaxWidth()) {
                        Text("No expenses yet. Add your first expense above.", Modifier.padding(20.dp))
                    }
                }
            }
            items(items = expenses, key = { it.id }) { expense ->
                ExpenseCard(expense = expense, onDelete = { onDeleteExpense(expense.id) })
            }
        }
    }
}

@Composable
private fun ExpenseCard(expense: ExpenseItem, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(expense.name, style = MaterialTheme.typography.titleMedium)
                Text(formatAmount(expense.amount), style = MaterialTheme.typography.bodyLarge)
            }
            TextButton(
                onClick = onDelete,
                modifier = Modifier.semantics { contentDescription = "Delete ${expense.name}" }
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// No currency was specified; show a locale-formatted decimal amount.
private fun formatAmount(amount: BigDecimal): String = NumberFormat.getNumberInstance().apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}.format(amount)

@Preview(showBackground = true)
@Composable
private fun ExpenseTrackerPreview() {
    ExpenseTrackerTheme {
        ExpenseTrackerScreen(
            expenses = listOf(ExpenseItem("preview-lunch", "Lunch", BigDecimal("12.50"))),
            inputName = "", inputAmount = "", totalSpent = BigDecimal("12.50"),
            nameError = null, amountError = null,
            onNameChange = {}, onAmountChange = {}, onAddExpense = {}, onDeleteExpense = {}
        )
    }
}
