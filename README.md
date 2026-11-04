# Expense Tracker

Mobile App Development midterm project — Option 1: Expense & Budget Tracker.

A single-screen Android app built with Kotlin, Jetpack Compose, Material 3, and
MVVM. It records expenses and shows the total amount spent.

## Features

- Add an expense with a name and amount.
- View recorded expenses in a scrolling list of cards.
- Delete individual expenses and see the total update.
- Get validation messages for missing names, invalid numbers, and amounts that
  are zero, negative, or have more than two decimal places.
- See an empty-list message when no expenses are recorded.

Amounts accept a dot as the decimal separator, for example `12.50`. Totals use
`BigDecimal` for exact decimal arithmetic and display two decimal places using the
device locale. No particular currency is assumed.

## Project structure

The Kotlin source is in `app/src/main/java/com/example/expensetracker/`.

| File | Responsibility |
| --- | --- |
| `MainActivity.kt` | Hosts the Compose screen and obtains its ViewModel. |
| `ExpenseItem.kt` | Defines an expense's ID, name, and amount. |
| `ExpenseViewModel.kt` | Holds the list, input values, validation errors, and event methods. |
| `ui/ExpenseTrackerScreen.kt` | Displays state and sends user actions through callbacks. |
| `ui/theme/` | Contains the Material 3 theme, colors, and typography. |

The ViewModel uses individual `mutableStateOf` properties with private setters.
Adding or deleting replaces the list; the total is derived from that list.
The screen receives state and callbacks from `ExpenseTrackerRoute`, and
`LazyColumn` uses each expense's UUID as its stable key.

Expenses are kept in memory. The activity-scoped ViewModel retains state during
activity recreation, such as rotation, but does not save it after process death
or a fresh app session. Budget limits and persistent storage are outside the
current implementation.

## Build and run

For a missing `jlink.exe` error or Windows Gradle JVM setup, see
[Windows JDK setup](docs/WINDOWS_BUILD.md).

Open the project in Android Studio and select an emulator or Android device
running Android 7.0 (API 24) or later.

The project uses Java 25 for the Gradle daemon, the Gradle 9.6 wrapper, Android SDK
Platform 37, and Build Tools 36.0.0. Configure the SDK in Android Studio or an
ignored `local.properties` file. Keep the wrapper checksum and TLS verification
enabled.

```sh
bash gradlew :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Tests

```sh
bash gradlew :app:testDebugUnitTest :app:lintDebug
bash gradlew :app:assembleDebugAndroidTest
# Requires a running emulator or connected device:
bash gradlew :app:connectedDebugAndroidTest
```

Unit tests cover input validation, clearing inputs after addition, exact totals,
duplicate expenses with different IDs, and deletion back to an empty list.
Device tests cover adding and deleting, activity recreation, and validation messages.

The last verified development run built the debug app and instrumentation APK,
and passed all 9 local tests. Lint completed with 0 errors and 14 warnings,
including an obsolete lifecycle lint registry that skipped two custom checks.
Device tests were compiled but were not run in that environment.
