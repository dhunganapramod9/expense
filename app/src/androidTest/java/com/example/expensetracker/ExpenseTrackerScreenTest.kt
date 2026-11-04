package com.example.expensetracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpenseTrackerScreenTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun addSurvivesRecreationAndDeleteReturnsToEmptyState() {
        compose.onNodeWithText("Expense Name").performScrollTo().performTextInput("Lunch")
        compose.onNodeWithText("Expense Amount").performScrollTo().performTextInput("12.50")
        compose.onNodeWithText("Add Expense").performScrollTo().performClick()
        compose.onNodeWithText("Lunch").performScrollTo().assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Lunch").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Delete Lunch").performScrollTo().performClick()
        compose.onNodeWithText("No expenses yet. Add your first expense above.")
            .performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("0 expenses recorded").performScrollTo().assertIsDisplayed()
    }

    @Test fun blankSubmissionShowsValidationMessages() {
        compose.onNodeWithText("Add Expense").performScrollTo().performClick()
        compose.onNodeWithText("Enter an expense name.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Enter an expense amount.").performScrollTo().assertIsDisplayed()
    }
}
