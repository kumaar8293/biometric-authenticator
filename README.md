# Biometric Authentication

A demonstration Android application showcasing biometric authentication using Android's BiometricPrompt API with Jetpack Compose.

## Description

This project provides a clean, production-ready implementation of biometric authentication on Android. It handles hardware availability checks, enrollment status verification, and provides a reactive UI that responds to authentication results using Kotlin Flow and Channels.

## Features

- Biometric authentication using Android's BiometricPrompt API
- Support for fingerprint, face recognition, and device credentials (PIN/pattern/password)
- Automatic detection of hardware availability and enrollment status
- Guided enrollment flow that directs users to device settings when needed
- Reactive UI using Jetpack Compose and Kotlin Flow
- Error handling for all authentication failure scenarios
- Android 11+ support for device credential fallback

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Biometric Library**: androidx.biometric (1.2.0-alpha05)
- **Coroutines**: Kotlin Coroutines with Flow and Channel
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)
- **Build System**: Gradle with Kotlin DSL

## Architecture

The application follows a simple, layered architecture:

```
UI Layer (MainActivity)
    ↓
BioMetricPromptManager
    ↓
BiometricPrompt (Android Framework)
```

### Component Responsibilities

- **MainActivity**: Extends `FragmentActivity` to provide FragmentManager support required by BiometricPrompt. Manages UI state and handles authentication result display. Automatically navigates users to enrollment settings when biometrics are not configured.

- **BioMetricPromptManager**: Encapsulates biometric authentication logic. Checks hardware availability and enrollment status before showing prompts. Uses Kotlin Channel to emit authentication results as a Flow, enabling reactive state management in Compose.

- **BiometricPrompt**: Android framework component that displays the system authentication dialog and handles user interaction. Requires FragmentActivity because it uses FragmentManager internally to present the dialog.

### Data Flow

1. User triggers authentication via button click
2. MainActivity calls `BioMetricPromptManager.showBioMetricPrompt()`
3. Manager checks availability using `BiometricManager.canAuthenticate()`
4. If available, Manager creates and shows BiometricPrompt
5. Authentication results flow through Channel → Flow → Compose State
6. UI recomposes to display the result

## How Biometric Authentication Works

Biometric authentication on Android involves two distinct phases:

1. **Availability Check**: Before showing the prompt, the app verifies:
   - Hardware exists and is functional
   - At least one biometric or credential is enrolled
   - No system-level errors prevent authentication

2. **Authentication**: During user interaction:
   - System displays the authentication dialog
   - User provides biometric input or credential
   - System verifies the input against enrolled data
   - Callback methods notify the app of success, failure, or error

The distinction is important: availability is checked programmatically, while authentication requires user interaction through the system dialog.

## Setup & Requirements

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API level 24 or higher
- Gradle 8.0 or higher

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd biometric-auth
   ```

2. Open the project in Android Studio

3. Sync Gradle files to download dependencies

4. Build the project:
   ```bash
   ./gradlew build
   ```

## How to Run

### On Emulator

1. Create an Android Virtual Device (AVD) with API level 24 or higher
2. Ensure the emulator has a fingerprint sensor configured:
   - Open AVD Manager
   - Edit your emulator
   - Go to "Show Advanced Settings"
   - Enable "Fingerprint" under "Extended controls"
3. Enroll a fingerprint in the emulator:
   - Settings → Security → Fingerprint
   - Follow the enrollment process
4. Run the app from Android Studio or via command line:
   ```bash
   ./gradlew installDebug
   ```

### On Real Device

1. Connect your Android device via USB
2. Enable USB debugging in Developer Options
3. Ensure your device has at least one biometric enrolled:
   - Settings → Security → Biometric settings
   - Enroll fingerprint, face, or ensure PIN/pattern is set
4. Run the app:
   ```bash
   ./gradlew installDebug
   ```

### Testing Authentication

1. Launch the app
2. Tap the "Authenticate" button
3. Use your enrolled biometric or device credential
4. Observe the result message displayed below the button

## Common Issues & Solutions

### Issue: "Hardware unavailable" error

**Solution**: The device doesn't have biometric hardware or it's disabled. This is expected on devices without fingerprint sensors or face recognition cameras.

### Issue: "Authentication Not Set" error

**Solution**: No biometrics or credentials are enrolled on the device. On Android 11+, the app will automatically open enrollment settings. On older versions, manually enroll via Settings → Security.

### Issue: BiometricPrompt not showing

**Possible causes**:
- Activity doesn't extend FragmentActivity (must extend FragmentActivity, not ComponentActivity)
- Availability check failed but wasn't handled
- App is in background when prompt should appear

**Solution**: Ensure MainActivity extends FragmentActivity and check logs for availability errors.

### Issue: "Feature Not Available" error

**Solution**: The device doesn't support biometric authentication. This is a hardware limitation and cannot be resolved in software.

### Issue: Prompt appears but authentication fails immediately

**Possible causes**:
- Biometric data changed (e.g., fingerprint re-enrolled)
- Hardware malfunction
- Too many failed attempts

**Solution**: Re-enroll biometrics in device settings or use device credential (PIN/pattern/password) if available.

### Issue: Build errors related to Compose

**Solution**: Ensure you're using a compatible version of Android Studio and that all dependencies are synced. Try:
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```
