# WiseKoin Android Contributor Guide

## Scope and stack

- This repository is the Kotlin Android client. The reference Spring Boot API lives in the separate `D:\Projects\WiseKoin` repository; do not mix commits or paths between the two repositories.
- Use the checked-in Gradle Wrapper and version catalog. The app is a single `app` module using Kotlin, Jetpack Compose/Material 3, Navigation Compose, ViewModel/StateFlow, Retrofit/OkHttp/Gson, CameraX, and minSdk 24.
- Keep the package root `com.wisekoin`. Place API DTOs in `data/model`, Retrofit declarations in `data/remote`, persistence in `data/local`, screen state/business orchestration in ViewModels, reusable UI in `ui/components`, screens in `ui/screens`, and routes in `navigation`.

## Architecture and state

- Composables render state and emit user intents. Do not add direct Retrofit calls, persistence, or business rules to a Composable; put them behind a repository and ViewModel. When touching an existing direct call, move the affected flow toward that boundary rather than expanding the pattern.
- Expose immutable UI state through `StateFlow`. Model loading, content, empty, recoverable error, and submission states explicitly. Prevent duplicate submissions and stale responses, and propagate coroutine cancellation instead of catching it as a generic failure.
- Collect flows with lifecycle awareness (`collectAsStateWithLifecycle`) and key effects to the actual inputs. Avoid duplicate `LaunchedEffect` loads and avoid creating multiple ViewModels for state that a navigation flow must share.
- Preserve user input across configuration/process recreation where appropriate with `rememberSaveable` or `SavedStateHandle`. Keep Android `Context`, camera, recorder, streams, and temporary files out of long-lived state; release resources in lifecycle callbacks and cleanup paths.
- Use dependency injection or constructor-injected interfaces for repositories and network/storage clients. New code must be testable without the global `RetrofitClient` singleton.

## Backend contract integration

- Treat the backend controllers and DTOs as the source of truth. Before changing an API call, compare its path, HTTP method, query/part names, enum spelling, nullability, date format, request body, response envelope, and error status with `D:\Projects\WiseKoin\src\main\java\com\wisekoin\backend\module`.
- Financial values use `BigDecimal`. Do not hardcode database IDs or infer a selected wallet/category from display text. Use server IDs returned for the authenticated user.
- All protected calls require a valid access token. Implement the backend's rotating refresh-token contract with a single-flight authenticator, atomically replace both tokens, retry a request at most once, and clear the session on unrecoverable 401 responses.
- Keep authentication tokens in Keystore-backed encrypted storage and exclude them from backup/device transfer. Never log authorization headers, credentials, full response bodies, receipt images, or voice recordings.
- Convert backend failures into stable domain/UI errors. Preserve structured validation messages where safe; do not show raw exception strings or provider/server internals to users.

## UI, accessibility, and resources

- Use Material components and the existing theme. Extract reusable sections before screens become monolithic, and keep previews/data fixtures outside production state.
- Put user-visible text in string resources. Supply meaningful semantics/content descriptions for interactive icons, maintain at least 48 dp touch targets, support screen readers, dynamic font sizes, keyboard/IME actions, and narrow screens.
- Every network-backed screen must provide loading, content, empty, error, and retry behavior. Disable or make submissions idempotent while in flight. Do not silently substitute fake data after failures.
- Handle runtime permission denial and “don’t ask again” states with a recoverable route to system settings. Camera/audio features must stop and release resources when leaving the screen.

## Build and release configuration

- Do not hardcode API hosts in Kotlin. Define environment-specific HTTPS base URLs through build types/product flavors or injected Gradle properties, with local cleartext permitted only by a debug network security configuration.
- Release builds must disable BODY logging, disallow broad cleartext traffic, use an explicit signing setup supplied outside version control, and enable/test R8 resource/code shrinking with appropriate rules.
- Never commit `local.properties`, signing keys, service account files, API keys, tokens, generated APK/AAB files, or machine-specific paths. Avoid duplicate dependency declarations; prefer `gradle/libs.versions.toml`.

## Tests and verification

- Add unit tests for ViewModels, repositories, DTO serialization, error mapping, refresh concurrency, amount/date parsing, and filters. Add Compose/navigation tests for critical login, transaction, receipt/voice, goal, and debt workflows.
- Use fake repositories or MockWebServer for deterministic tests. Do not call production services from automated tests.
- From the repository root, run the narrowest relevant task while iterating, then:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

For release-affecting changes also run `.\gradlew.bat assembleRelease` and inspect signing/minification/network behavior. Instrumented tests require an emulator or device and use `.\gradlew.bat connectedDebugAndroidTest`.

## Change discipline

- Preserve unrelated working-tree changes. Keep contract DTO/service changes, ViewModel behavior, UI states, tests, and resource updates together.
- Do not treat a successful build as end-to-end verification. Trace each changed path from UI to ViewModel/repository/client and the matching backend endpoint, then state what was and was not exercised on a device.
