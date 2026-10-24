# WiseKoin Android App

Jetpack Compose mobile application for the WiseKoin financial management platform.

## Project Title

**WiseKoin** — Financial Tracking and Planning Mobile Application

## Demo Video

[Demo Video](#) <!-- Replace with actual link -->

## Tech Stack

- **Language:** Kotlin 2.1.0
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36
- **UI Framework:** Jetpack Compose (Compose BOM 2025.02.00)
- **Navigation:** Jetpack Navigation Compose 2.8.7
- **Architecture:** MVVM (Model-View-ViewModel)
- **Networking:** Retrofit 2.11.0 + OkHttp 4.12.0
- **Image Loading:** Coil 2.7.0
- **Camera:** CameraX 1.3.4
- **Local Storage:** Android Keystore-backed encrypted session storage (TokenManager)
- **Build Tool:** Gradle 9.2.1 (AGP 9.2.1)

## Screens

| Screen                 | Route                  | Description                                      |
|------------------------|------------------------|--------------------------------------------------|
| Login                  | `login`                | Email/password login                             |
| Register               | `register`             | New user registration                            |
| Overview               | `overview`             | Dashboard with wallet balance and recent activity|
| Expenses               | `expenses`             | Transaction list with filters and monthly stats  |
| Savings Goals          | `savings`              | List of savings goals with progress              |
| Create Goal            | `create_goal`          | Create a new savings goal                        |
| Add Funds              | `add_funds/{goalId}`   | Add funds to an existing goal                    |
| Debt Management        | `debt`                 | List of debts (lent/borrowed)                    |
| Add Transaction        | `add_transaction`      | Manual transaction entry                         |
| Scan Receipt           | `scan_receipt`         | Camera-based receipt photo capture               |
| Confirm Receipt        | `confirm_receipt`      | Review and confirm AI-extracted receipt data     |
| Voice Recording        | `voice_recording`      | Voice input for transaction entry                |
| Profile                | `profile`              | User profile and logout                          |

## API Integration

Debug builds default to `http://10.0.2.2:8080/` for the Android emulator. Override that value with the `WISEKOIN_DEBUG_API_BASE_URL` environment variable or Gradle property when using a physical device or another development backend.

Release builds require `WISEKOIN_API_BASE_URL` to be an HTTPS URL ending in `/`. Set it as an environment variable locally and as the `WISEKOIN_API_BASE_URL` repository secret for the release workflow. Release builds fail fast when it is absent or invalid.

Store-distributable builds are signed by Gradle only when all four external signing values are present. The release workflow requires them, materializes the keystore in the runner's temporary directory, builds with R8 and resource shrinking, verifies both signatures, and uploads only the verified artifacts. Keystores and passwords must never be committed.

```bash
# PowerShell example
$env:WISEKOIN_API_BASE_URL = "https://api.example.com/"
$env:WISEKOIN_RELEASE_STORE_FILE = "C:\secure\wisekoin-release.jks"
$env:WISEKOIN_RELEASE_STORE_PASSWORD = "..."
$env:WISEKOIN_RELEASE_KEY_ALIAS = "wisekoin"
$env:WISEKOIN_RELEASE_KEY_PASSWORD = "..."
./gradlew assembleRelease bundleRelease
```

Omit all four `WISEKOIN_RELEASE_*` values to produce an unsigned local R8 build for verification. Supplying only some signing values fails fast.

### API Services

- `AuthApiService` — login, register, refresh, logout
- `UserApiService` — get/update profile
- `WalletApiService` — list/create wallets
- `CategoryApiService` — CRUD categories
- `TransactionApiService` — CRUD transactions, monthly stats
- `DebtApiService` — CRUD debts, repayments
- `GoalApiService` — CRUD goals, fund goals
- `ReceiptApiService` — upload receipt image for AI extraction
- `RetrofitClient` — authenticated client with automatic single-retry token refresh

### Money and time contract

- API and UI-domain monetary values remain `BigDecimal`. Display and input use the device locale, ISO 4217 fraction digits (capped at the backend-supported four places), and `HALF_EVEN` rounding. VND therefore has zero fraction digits; currencies such as USD retain two.
- `transactionDate` is an ISO-8601 user-local wall time without an offset, matching the current backend DTO. Android uses the device system zone for fallback timestamps and sends that IANA zone separately with receipt/voice extraction. Neither side should reinterpret this field as UTC.

## Build and Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or open in Android Studio and click **Run**.

## Screenshots

<!-- Add screenshots here when available -->

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedDebugAndroidTest
```
