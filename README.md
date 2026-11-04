# Expense Tracker — Mobile App Development Midterm

A single-screen Kotlin app for Option 1: Expense & Budget Tracker. It continues the
original Android Compose project, retaining its Material 3 theme and Gradle setup.
The requested scope is recording expenses and showing total spending; no budget
limits, database, authentication, or external service have been added.

## What the app does

- Shows a total-spent summary Card above the expense form.
- Accepts Expense Name and Expense Amount, with an Add Expense button.
- Displays recorded expenses in a LazyColumn of Material 3 Cards, each with Delete.
- Clears both inputs after a successful addition and updates the total immediately.
- Shows inline errors for blank names/amounts, invalid numbers, negative or zero
  amounts, and values with more than two decimal places. Invalid submissions keep
  the inputs and existing expenses so the user can correct them.
- Accepts decimal input such as `12.50` or `.50`. Use a dot as the input separator;
  commas, currency symbols, and scientific notation are intentionally rejected.
- Shows an empty-list message and a zero total initially and after deleting all items.

Amounts use BigDecimal for exact decimal addition. Display uses the device locale
with two decimal places. No currency is assumed because the requirements do not
specify one. State survives activity recreation (such as rotation) through the
ViewModel, but is intentionally in memory: process death or a fresh launch resets it.

## Architecture / screencast explanation

- `ExpenseItem.kt`: immutable ID, name, and amount.
- `ExpenseViewModel.kt`: owns `expenseList: List<ExpenseItem>`, `inputName: String`,
  `inputAmount: String`, and validation errors. Public state has private setters.
  `onNameChange`, `onAmountChange`, `addExpense`, and `deleteExpense` handle events.
- `ui/ExpenseTrackerScreen.kt`: the route reads ViewModel state and passes values
  and callbacks to the screen. The screen and expense cards contain presentation
  logic only; they do not parse inputs or change the list themselves.
- `MainActivity.kt`: retains the original theme and edge-to-edge setup and obtains
  the ViewModel using `viewModel()`, so its lifetime follows the activity's store.

The list and fields use Compose `mutableStateOf`. Adding/deleting assigns a new
immutable list; Compose observes that change and recomposes the UI that reads it.
`totalSpent` is calculated from the list, rather than maintained as a second mutable
value that might become inconsistent. This also makes an empty list total zero.
State flows down and events flow up: the screen receives values and callbacks
(state hoisting), keeping it independent of a specific ViewModel instance.

`items(expenses, key = { it.id })` uses a UUID created once when adding an expense.
Identical names/amounts still have distinct keys. Deleting an earlier item does not
change the identity of later items, as index-based keys would. Keys preserve item
identity; observable state changes trigger recomposition. Static summary/form/list
heading items also have separate stable keys.

Material 3 components include Scaffold, Card, OutlinedTextField, Button, TextButton,
and themed typography/colors. A single scrolling container accommodates small
screens, large text, and the on-screen keyboard.

## Short technical reflection answers

The exact professor question wording was not supplied; these address the three
requested topics:

1. **Why this state design?** The ViewModel owns the list and both draft inputs,
   providing one source of truth across recompositions and activity recreation.
   Private setters keep all validation and changes behind event methods. The total
   is derived from the list, avoiding duplicate state.
2. **How do recomposition and LazyColumn keys work?** Compose tracks reads of
   observable state. Updating an input or replacing the list invalidates readers;
   the route supplies updated values to the UI. Stable IDs associate each row with
   its expense across insertion/deletion, even when names and amounts repeat.
3. **How independent is the ViewModel?** It does not know about an Activity,
   Context, widgets, navigation, or Composable functions. Its events can be called
   and tested without rendering a UI. It uses AndroidX ViewModel and Compose state,
   so it is independent of the screen, not completely framework-independent.

## Suggested screencast (about 3 minutes)

1. Launch: show the zero total and empty-list message.
2. Submit empty fields; show both inline validation errors.
3. Enter Lunch / 12.50 and add it; show the card, total, and cleared inputs.
4. Add Bus / 2.25; show total 14.75. Try `abc`, `-5`, and `1.234`; show errors
   without changing the list. Correct the input to add another expense.
5. Add two expenses with the same name; delete one to demonstrate separate IDs.
6. Delete every expense; show the empty state and total returning to zero.
7. Briefly show ViewModel state/events, the route's hoisted callbacks, and the
   LazyColumn key. Use the three reflection answers above as speaking notes.

## Build and verification

Prerequisites: Java 25 (the checked-in Gradle daemon toolchain), Android SDK Platform
37, Android Build Tools 36.0.0, and the checked-in Gradle 9.6 wrapper. Keep the
wrapper checksum and TLS verification enabled. Use `bash gradlew` because the
checked-in wrapper is not executable. Configure your SDK via `ANDROID_HOME` or an
ignored `local.properties` file.

```sh
bash gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
bash gradlew :app:assembleDebugAndroidTest
# With a connected device/emulator:
bash gradlew :app:connectedDebugAndroidTest
```

In the prepared cloud environment, source `/workspace/expense-environment.sh` and
use `expense_gradle` in place of `bash gradlew` for its proxy, CA, and cache settings.
The APK is `app/build/outputs/apk/debug/app-debug.apk`.

ViewModel unit tests cover initial state, addition/input clearing, decimal totals,
invalid submissions, error correction, distinct IDs, and deleting all expenses.
Device tests cover adding/deleting, activity recreation, and blank-input errors.
Building a test APK does not mean those device tests have executed.

### Verified in the development environment

- `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: succeeded.
- Local tests: 9 passed (8 ViewModel tests plus the original example), 0 failures,
  0 skipped.
- `:app:assembleDebugAndroidTest`: succeeded; device tests compiled, not executed.
- Lint: 0 errors, 14 warnings. These include existing resource/dependency warnings
  and an obsolete lifecycle lint registry, which causes its two custom lifecycle
  checks to be skipped. No assertions or lint failures were suppressed.
- `adb devices` found no connected device. Visual behavior, keyboard interaction,
  rotation, and instrumentation tests still need an emulator/device demonstration.
