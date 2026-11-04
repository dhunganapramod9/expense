package com.example.expensetracker

import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test

class ExpenseViewModelTest {
    private fun add(vm: ExpenseViewModel, name: String, amount: String) {
        vm.onNameChange(name)
        vm.onAmountChange(amount)
        vm.addExpense()
    }

    @Test fun startsEmpty() {
        val vm = ExpenseViewModel()
        assertTrue(vm.expenseList.isEmpty())
        assertEquals("", vm.inputName)
        assertEquals("", vm.inputAmount)
        assertEquals(0, BigDecimal.ZERO.compareTo(vm.totalSpent))
    }

    @Test fun addsTrimmedNameAndClearsInputs() {
        val vm = ExpenseViewModel()
        add(vm, " Lunch ", " 12.50 ")
        assertEquals("Lunch", vm.expenseList.single().name)
        assertEquals(BigDecimal("12.50"), vm.expenseList.single().amount)
        assertEquals("", vm.inputName)
        assertEquals("", vm.inputAmount)
        assertNull(vm.nameError)
        assertNull(vm.amountError)
    }

    @Test fun totalsDecimalAmountsExactly() {
        val vm = ExpenseViewModel()
        add(vm, "First", "0.10")
        add(vm, "Second", ".20")
        assertEquals(BigDecimal("0.30"), vm.totalSpent)
    }

    @Test fun rejectsBlankNamesWithoutLosingInput() {
        val vm = ExpenseViewModel()
        add(vm, "   ", "10.00")
        assertTrue(vm.expenseList.isEmpty())
        assertNotNull(vm.nameError)
        assertEquals("10.00", vm.inputAmount)
    }

    @Test fun rejectsInvalidAmountsWithoutChangingExistingExpenses() {
        val vm = ExpenseViewModel()
        add(vm, "Valid", "5.00")
        for (amount in listOf("", " ", "abc", "NaN", "Infinity", "-1", "0", "0.00", "1.234", "1e3", "1,50")) {
            add(vm, "Invalid", amount)
            assertEquals("Rejected: $amount", 1, vm.expenseList.size)
            assertNotNull("Rejected: $amount", vm.amountError)
            assertEquals(amount, vm.inputAmount)
            assertEquals(BigDecimal("5.00"), vm.totalSpent)
        }
    }

    @Test fun correctsErrorsAndAddsSuccessfully() {
        val vm = ExpenseViewModel()
        vm.addExpense()
        assertNotNull(vm.nameError)
        assertNotNull(vm.amountError)
        vm.onNameChange("Bus")
        vm.onAmountChange("2")
        assertNull(vm.nameError)
        assertNull(vm.amountError)
        vm.addExpense()
        assertEquals(1, vm.expenseList.size)
    }

    @Test fun duplicateExpensesHaveUniqueKeysAndDeleteIndependently() {
        val vm = ExpenseViewModel()
        add(vm, "Coffee", "3.00")
        add(vm, "Coffee", "3.00")
        val firstId = vm.expenseList.first().id
        val secondId = vm.expenseList.last().id
        assertNotEquals(firstId, secondId)
        vm.deleteExpense(firstId)
        assertEquals(secondId, vm.expenseList.single().id)
        assertEquals(BigDecimal("3.00"), vm.totalSpent)
        vm.deleteExpense(secondId)
        assertTrue(vm.expenseList.isEmpty())
        assertEquals(0, BigDecimal.ZERO.compareTo(vm.totalSpent))
    }

    @Test fun unknownDeleteIsHarmlessAndLeavesDraftAlone() {
        val vm = ExpenseViewModel()
        add(vm, "Lunch", "10")
        vm.onNameChange("Draft")
        vm.onAmountChange("4")
        vm.deleteExpense("missing")
        assertEquals(1, vm.expenseList.size)
        assertEquals("Draft", vm.inputName)
        assertEquals("4", vm.inputAmount)
    }
}
