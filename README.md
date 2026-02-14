# Student Expense Tracker

A simple Android application to track student expenses.

## Prerequisites
- Android Studio Iguana or newer
- JDK 17 or newer

## How to Run

### Using Android Studio
1.  Open **Android Studio**.
2.  Select **Open** and navigate to this folder (`StudentExpenseTracker`).
3.  Wait for Gradle sync to complete.
4.  Connect an Android device or create an Emulator (AVD).
5.  Click the Green **Run** button (or press `Shift + F10`).

### Using Command Line
To build the APK:
```bash
./gradlew assembleDebug
```
The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

To install on a connected device:
```bash
./gradlew installDebug
```

## Features
- Add expenses with title, amount, category, and date.
- View list of expenses.
- See total expense amount.
- Delete expenses by swiping.
