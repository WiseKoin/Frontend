# WiseKoin Android Frontend Audit

Audit date: 2026-10-14 (updated 2026-10-15)  
Frontend: `D:\AndroidStudioProjects\WiseKoin`  
Backend reference: `D:\Projects\WiseKoin`

## 1. Executive summary

The Android frontend is **not production-ready**. The principal happy-path screens exist and several calls match the Spring Boot API. WK-A01, WK-A02, and WK-A04 through WK-A12 have been remediated in code: release networking is environment-backed, sessions rotate safely, reporting reads complete period data, submissions are single-flight, failures are mapped to safe recoverable UI states, Compose state collection is lifecycle-aware, requested wallet/debt/account-security flows are implemented end to end, and network access now sits behind constructor-injected repositories composed at the activity boundary. Remaining blockers concern residual product-scope gaps, release configuration, accessibility/localization, and broader device/integration coverage.

The largest confirmed blockers are:

- A real production value for the required `WISEKOIN_API_BASE_URL` repository secret still must be configured before the release workflow can run.
- Critical network-backed ViewModels now use injected repository interfaces and a single explicit composition root; the remaining architectural work is broader instrumented workflow coverage rather than hidden network dependencies.
- Frontend unit coverage now protects authentication/session behavior, reporting calculations, submission gating, and network-error mapping, but there are still no instrumented UI or end-to-end tests.

No Critical backend defect was found within the integration scope. The backend has the missing refresh/logout and account/feature endpoints; most gaps are in the Android client. Runtime end-to-end behavior against a configured server, device accessibility, CameraX/MediaRecorder lifecycle behavior, release signing, and R8 behavior could not be fully verified from static code.

## 2. Feature matrix

| Feature | Frontend status | Backend support | Integration status | Outstanding work |
| --- | --- | --- | --- | --- |
| Register/login | Implemented | Complete | Full auth response is stored as an encrypted access/refresh pair; failures are converted to safe authentication/network messages | Improve field-level validation |
| Session restore/logout | Implemented | Complete | Startup resolves the stored session before authenticated UI is shown; protected 401 responses rotate once and terminal failures clear credentials and navigate to Login | Device-test process recreation and offline retry behavior |
| User profile and account security | Mostly implemented | Complete | GET/PUT profile, persisted base-currency editing, OTP-gated password change, and OTP-gated account deletion are wired; success revokes the local session | Email editing remains absent; avatar is local-only; configure and smoke-test SMTP |
| Wallet list/create/details/delete | Mostly implemented | Complete | Summary/create contracts match; Overview wallet taps open an information modal with a guarded delete action and refreshed server state | Wallet rename/update UI remains absent |
| Categories | Partial | Complete | List/create match; loading failures expose safe retry state | No update/delete |
| Manual income/expense | Implemented with risks | Complete | Create contract matches, a retained ViewModel serializes submissions, and failures are safely mapped | Refresh destination screens |
| Wallet transfer | Missing | Complete | No `destinationWalletId` in client DTO/UI | Add transfer model and source/destination flow if backend capability is in product scope |
| Transaction history/filtering | Implemented | Complete | Expenses requests every server page for the selected server-side date range and parses ISO `LocalDateTime` values | Consider incremental Paging 3 for very large all-time histories |
| Monthly statistics | Implemented | Complete | Overview and Expenses render distinct income/expense series; period reports include goal funding and use complete queried data | Device/API fixture verification |
| Receipt scan | Partial | Complete | Multipart field and DTO match; confirmation is guarded and extraction failures are safe/recoverable | Auth gate and removal of dead save overloads |
| Voice transaction | Partial | Complete | Multipart field and DTO match | Auth gate, robust category mapping, amount precision, retry and lifecycle tests |
| Goals | Mostly implemented | Complete | Create/list and transaction-linked funding are accepted by backend and guarded against repeat submission | Auth gate, update/delete absent, stronger refresh/error state |
| Debts | Implemented | Complete | Create/list/repay/edit/delete are connected; editing validates against the repaid amount and right-swipe reveals an accessible destructive action | Device/API gesture and rollback smoke testing |
| OTP verification | Implemented | Complete | Purpose-bound OTP request/verify flows use expiring, one-time verification tokens; the backend enforces resend cooldowns, hourly send limits, and failed-attempt limits | Configure a production SMTP provider and run delivery/device tests |
| Recurring transactions | Missing | Complete | No model, service, ViewModel, screen, or route | Implement only if included in release product scope |
| Forgot password | Implemented | Implemented | Generic recovery request, OTP verify, and one-time password reset contract | Configure SMTP and complete device/API smoke testing |
| Bank notifications | Implemented | Uses existing transaction create endpoint | Opt-in Android listener, on-device parsing, encrypted account-scoped review queue, explicit wallet/category confirmation | Expand real-bank fixtures and device/OEM coverage |
| Help Center | Placeholder | No relevant backend endpoint | Toast-only control | Remove from release or implement an explicit external flow |
| Offline/cache/sync | Missing | N/A | Network-only | Define offline behavior; at minimum show connection-specific errors and retries |

## 3. Prioritized findings

### WK-A01 — Resolved (was Critical) — Release networking is environment-specific and HTTPS-only

- **Location:** `app/build.gradle.kts`; `data/remote/RetrofitClient.kt`; `app/src/main/AndroidManifest.xml`; `app/src/debug/AndroidManifest.xml`; `.github/workflows/build-release.yml`.
- **Resolution evidence:** `BuildConfig.API_BASE_URL` is defined per build type. Debug defaults to `http://10.0.2.2:8080/` and owns the only cleartext network-security override. Main/release explicitly sets `usesCleartextTraffic=false`. Release tasks fail configuration unless `WISEKOIN_API_BASE_URL` is supplied as an HTTPS URL ending in `/`; the release workflow requires and injects that secret.
- **Problem and impact addressed:** Emulator-only configuration can no longer silently ship in a release artifact, and release cleartext is disabled.
- **Backend dependency:** The backend expects an externally deployed base URL; its README still contains a placeholder production URL, so the final production host is not verifiable from code.
- **Remaining operational step:** Configure the real HTTPS URL as the `WISEKOIN_API_BASE_URL` repository secret.
- **Verification:** A release invocation without the variable failed as designed. `assembleRelease bundleRelease` succeeded with `https://api.example.test/`, the generated release `BuildConfig` contained that value, and merged release manifests contained `usesCleartextTraffic=false`.
- **Classification:** Resolved in code; production hostname configuration remains external and unverified.

### WK-A02 — Resolved (was High) — Rotating refresh sessions and server logout are implemented

- **Location:** `data/model/AuthDto.kt`; `data/remote/AuthApiService.kt`; `data/remote/RetrofitClient.kt`, `RefreshTokenAuthenticator`; `data/local/TokenManager.kt`; `viewmodel/AuthViewModel.kt`; `navigation/WiseKoinNavigation.kt`.
- **Resolution evidence:** The complete auth response and refresh request are modeled. Login/register atomically persist both rotated tokens. A separate unauthenticated auth client calls `/refresh`; the authenticated client uses a synchronized authenticator, reuses a token already rotated by another request, retries at most once, and clears the session on terminal refresh failure. Logout calls `/logout` with the refresh token and always clears local state afterward.
- **Security evidence:** Token pairs are AES-GCM encrypted with a non-exportable Android Keystore key, committed together, and excluded from cloud backup/device transfer.
- **Backend dependency:** `AuthController` and rotating `RefreshTokenService` are already present.
- **Verification:** `RefreshTokenAuthenticatorTest` covers rotation, reuse after concurrent refresh, and terminal failure cleanup; all three tests pass. Debug compile/lint/assembly and release compile/assembly also pass.
- **Classification:** Resolved. App-wide terminal-session navigation is now completed under WK-A04.

### WK-A03 — Resolved (was Low) — HTTP logs omit authentication bodies and secrets

- **Location:** `data/remote/RetrofitClient.kt`, `HttpLoggingPolicyTest.kt`; `data/local/TokenManager.kt`; backup/data-extraction rules.
- **Resolution evidence:** Debug networking now uses `BASIC`, which logs request/response lines without headers or bodies; release logging remains `NONE`. Authorization and Cookie headers are explicitly redacted as defense in depth. Login, registration, refresh-token, and provider error bodies therefore cannot be copied into Logcat by the configured logger.
- **Storage evidence:** Token pairs remain Android-Keystore encrypted and excluded from cloud backup/device transfer.
- **Verification:** `HttpLoggingPolicyTest` locks debug to `BASIC` and release to `NONE`; the debug unit suite passes.
- **Classification:** Resolved in code.

### WK-A04 — Resolved (was High) — Invalid/expired sessions are resolved and routed safely

- **Location:** `viewmodel/AuthViewModel.kt`; `data/remote/RetrofitClient.kt`, `RefreshTokenAuthenticator`; `navigation/WiseKoinNavigation.kt`.
- **Resolution evidence:** Authentication now has explicit RESOLVING, AUTHENTICATED, GUEST, and ERROR states. A stored session is verified through `/api/users/me` before authenticated navigation renders, transient resolution failures show a retry state, and terminal refresh/repeated-401 failures clear credentials and notify the app. Navigation observes the expiry version, clears the protected back stack, and opens Login. Guest Overview remains explicit; protected bottom navigation and Overview transaction/history actions are gated.
- **Backend dependency:** Protected endpoints return 401 through Spring Security and the existing rotating refresh endpoint remains the source of truth.
- **Verification:** `RefreshTokenAuthenticatorTest` now also asserts one terminal-expiry notification after refresh failure; debug compilation and unit tests pass.
- **Classification:** Resolved in code; process-recreation/offline/device navigation behavior still requires device testing.

### WK-A05 — Resolved (was High) — Debt repayment uses owned selections and is cancellable

- **Location:** `ui/screens/DebtManagementScreen.kt`; `viewmodel/DebtViewModel.kt`; `navigation/WiseKoinNavigation.kt`.
- **Resolution evidence:** Opening a repayment loads all owned wallets and the debt-compatible owned category type (LEND or BORROW). The modal requires explicit wallet/category selections, validates positive amount and remaining debt, exposes loading and safe failure states, blocks duplicate submission, and calls `DebtViewModel.repayDebt`. Backdrop dismissal and Cancel both close the modal when no request is running. No database IDs or fake wallet names remain in the repayment flow.
- **Backend dependency:** `/api/debts/{id}/repay` continues enforcing wallet/category ownership and applies INCOME for LEND or EXPENSE for BORROW.
- **Verification:** Debug compilation and unit tests pass. Live LEND/BORROW repayment and cancellation still require device/API smoke testing.
- **Classification:** Resolved in code.

### WK-A06 — Resolved (was High) — Expenses history and period charts use complete server data

- **Location:** `data/remote/TransactionApiService.kt`; `viewmodel/WalletViewModel.kt`, `loadTransactionReport`; `viewmodel/TransactionReporting.kt`; `ui/screens/ExpensesScreen.kt`; `ui/components/SharedComponents.kt`.
- **Resolution evidence:** Each selected period is translated to an exact server `startDate`/`endDate` range, and the ViewModel follows every returned page (100 records per request) before publishing one report result. Week/month/year charts are calculated from that complete result; All Time loads every page and labels only the actual first/last transaction years. Both income and red expense columns render for every granularity, and goal-linked transfers count as expenses.
- **Date correctness:** Backend timestamps are parsed with `DateTimeFormatter.ISO_LOCAL_DATE_TIME`, including fractional seconds. A superseded period request is cancelled and prevented from overwriting the newest selection.
- **Verification:** `TransactionReportingTest` deterministically covers fractional timestamps, Monday–Sunday ranges, income plus goal-inclusive expense totals, and actual all-time year bounds. Debug unit tests pass.
- **Classification:** Resolved in code; a large live account should still be profiled to decide whether incremental Paging 3 is desirable for UX/performance.

### WK-A07 — Resolved (was High) — Money-changing requests are single-flight in the client

- **Location:** `viewmodel/TransactionEntryViewModel.kt`; `AddTransactionScreen.kt`; `ReceiptViewModel.kt`; `VoiceViewModel.kt`; `GoalViewModel.kt`; `DebtViewModel.kt`; `WalletViewModel.kt`; `AuthViewModel.kt`; their submit buttons.
- **Resolution evidence:** Manual transaction, debt, and Add Transaction wallet creation now run through a retained ViewModel with an atomic single-submission gate. Receipt and voice confirmations use the same single-flight pattern. Goal creation/funding, debt repayment/creation, Overview wallet creation, login, and registration reject a second invocation while active. Corresponding buttons are disabled and show progress where the flow exposes a submit control.
- **Rotation/recomposition behavior:** Submission state is owned by destination/shared ViewModels rather than local button coroutines, so recomposition and configuration changes cannot enable a parallel POST; completion produces one success/error callback.
- **Backend dependency:** No backend change was required to close the confirmed client race. The backend still has no idempotency-key contract, so independent transport-level retries are not claimed to be deduplicated by the server.
- **Verification:** `SubmissionGateTest` verifies that an overlapping intent is rejected and that the gate reopens after completion.
- **Classification:** Resolved for the audited duplicate UI-intent defect.

### WK-A08 — Resolved (was Medium) — Wallet creation response contract is correctly typed

- **Location:** `data/remote/WalletApiService.kt`, `createWallet`; `viewmodel/WalletViewModel.kt`, `createWallet`.
- **Resolution evidence:** Android now declares the backend's single `WalletResponseDto` for POST `/api/wallets`. Overview wallet creation no longer interprets that response as a summary and refreshes the authoritative summary once after success.
- **Additional cleanup:** The Add Transaction dialog now consumes the returned `WalletResponseDto` directly for selection; its follow-up summary refresh is only used to refresh the option list.
- **Classification:** Resolved.

### WK-A09 — Resolved (was Medium) — Network failures are mapped to safe recoverable UI errors

- **Location:** `data/remote/NetworkErrorMapper.kt`; network-backed ViewModels; `ui/components/SharedComponents.kt`, `NetworkErrorBanner`; Overview, Expenses, Debt, Add Transaction, receipt, voice, auth, goal, and profile flows.
- **Resolution evidence:** A single mapper now distinguishes offline, timeout, 400/422 validation, 401, 403, 404, 409, 429, 5xx, insufficient balance, and unknown failures. It parses and retains `Retry-After`, emits only stable user-safe copy, and never returns raw exception, SQL, or provider response text. Network cancellations propagate instead of becoming false user errors.
- **Recovery evidence:** Loading failures expose retry actions in the principal list/report/selection flows; form submissions return to an enabled state for correction or retry. Direct wallet calls and empty network catches were removed from Add Transaction and Confirm Receipt. Fake “Main Cash”, receipt merchant/category, and voice-category substitutions were removed, and exception objects from receipt/voice providers are no longer logged.
- **Backend dependency:** The mapper tolerates the backend’s structured application errors, rate-limit envelope/headers, and plain-string receipt/voice failures without exposing their bodies.
- **Verification:** `NetworkErrorMapperTest` covers 5xx detail redaction, 429 retry-delay extraction, and recoverable timeouts. Static search finds no production `localizedMessage`, raw camera exception message, screen-owned `RetrofitClient`, or empty network catch.
- **Classification:** Resolved in code; copy localization remains tracked under WK-A15.

### WK-A10 — Resolved (was Medium) — Loads and StateFlow collection are lifecycle-aware

- **Location:** all Compose screens/navigation collecting ViewModel state; `WalletViewModel.refreshAll`; Overview, Expenses, Confirm Receipt, Add Transaction, and voice load effects.
- **Resolution evidence:** All UI `StateFlow` values use `collectAsStateWithLifecycle`, so off-screen collectors suspend. Duplicate initial effects were consolidated and keyed to authentication/period/type inputs. Voice no longer requests categories twice or loads unrelated transaction/statistics data merely to populate wallets.
- **Request evidence:** `WalletViewModel` rejects overlapping refreshes and fetches wallets, recent transactions, and statistics concurrently. Report requests cancel superseded periods and ignore stale results; category requests cancel superseded types; wallet, goal, and debt loaders reject overlapping calls.
- **Backend dependency:** No backend change was required; the fixes reduce unnecessary use of the existing rate-limit budget.
- **Verification:** Static search finds no remaining `collectAsState(` usage or screen-owned Retrofit calls. Debug unit tests, lint, and assembly cover compilation of the lifecycle-aware paths.
- **Classification:** Resolved in code; radio/network profiling still requires a device.

### WK-A11 — Resolved (was Medium) — Requested wallet, debt, and account-security management is implemented

- **Location:** Android `OverviewScreen.kt`, `DebtManagementScreen.kt`, `AccountSecurityScreens.kt`, `ProfileScreen.kt`, `WiseKoinNavigation.kt`; wallet/debt/account-security DTOs, API services, repositories, and ViewModels. Backend `UserController`, `UserService`, `module/user/otp`, `GlobalExceptionHandler`, and `V7__add_user_otp_challenges.sql`.
- **Resolution evidence:** Tapping a wallet opens a details modal with a two-step, in-flight-guarded delete action. Debts can be edited and right-swiped to reveal a red, accessible trash action; successful mutations update authoritative UI state. The profile security entry opens dedicated password-change and account-deletion pages, with the destructive account action immediately below account activity. Both sensitive operations require the current password and a verified, purpose-bound OTP before the backend changes credentials or deletes owned data.
- **Security and reliability:** The backend resolves the authenticated user rather than accepting a user ID, locks OTP state during mutations, stores only BCrypt OTP hashes and SHA-256 verification-token hashes, applies expiry/cooldown/hourly-send/attempt limits, consumes verification tokens once, revokes refresh sessions after password change, and cascades or explicitly removes owned data during account deletion. Android uses injectable repositories, single-flight submissions, lifecycle-aware state, safe error mapping, countdown/resend UI, password confirmation, and explicit destructive confirmation.
- **Operational dependency:** SMTP delivery is environment-backed and intentionally fails with a safe service-unavailable response when not configured. Production mail credentials must be supplied outside source control.
- **Verification:** Android unit tests, lint, and debug assembly pass. Backend OTP/service tests cover hashing, expiration, resend cooldown, hourly send limits, failed-attempt lockout, purpose-bound one-time tokens, password change, session revocation, and account cleanup. Live SMTP delivery and device/API gesture flows remain external smoke tests.
- **Classification:** Resolved in code for the five requested WK-A11 capabilities.

### WK-A12 — Resolved (was Medium) — Network boundaries are injectable and dead/mock production paths are removed

- **Location:** `di/AppContainer.kt`; `di/WiseKoinViewModelFactory.kt`; `data/remote/RetrofitClient.kt`; all files under `data/repository`; network-backed ViewModels; `WiseKoinNavigation.kt`; split `*Screen.kt`/`*Components.kt` files; `SharedComponents.kt`; `ReceiptViewModel.kt`.
- **Resolution evidence:** `RetrofitClient` is now an instance owned only by the activity composition root. Its typed API interfaces are constructor-injected into network repositories, and repository interfaces plus the encrypted `SessionTokenStore` are constructor-injected into every network-backed ViewModel through one lifecycle-aware factory. Composables receive ViewModels explicitly and can no longer fall back to unsafe no-argument construction. A lifecycle listener bridges authenticator expiry events without making the client or repositories global.
- **Dead/mock cleanup:** The mutable `sampleTransactions` production list, its implicit UI default, the local-only receipt save overload, the hardcoded wallet/category-ID receipt path, the fake social-login state mutation, the unused `UserViewModel`, and a dead commented error interceptor were removed. Receipt/voice/transaction paths now have one authoritative repository-backed save route.
- **Component separation:** Add Transaction, Confirm Receipt, Debt Management, and Profile orchestration were separated from stateless components. Their screen files are now approximately 278–520 lines instead of 647–1,363 lines, while UI behavior and state ownership remain unchanged.
- **Testability evidence:** `NetworkWalletRepository` is exercised with a fake injected API, and the session-expiry bridge is covered independently. Static search finds no `RetrofitClient` reference outside `data/remote` and `di/AppContainer`, no ViewModel-owned API client, no screen `viewModel()` fallback, and none of the removed mock/hardcoded paths.
- **Backend dependency:** None beyond the unchanged API contracts.
- **Verification:** Twenty frontend unit tests pass, including the new repository-delegation and session-listener tests. Kotlin compilation, lint, and debug assembly pass.
- **Classification:** Resolved in code.

### WK-A13 — Partially resolved (Medium) — Automated coverage remains incomplete

- **Location:** `app/src/test`; `app/src/androidTest` remains absent.
- **Evidence:** Unit tests now cover token refresh/session state, reporting/date calculations, submission gating, and safe error mapping. Compose navigation, permissions, media capture, and complete financial workflows remain untested.
- **Problem and impact:** The added unit tests protect the remediated regressions, but navigation, device lifecycle, permissions, media capture, and complete API workflows can still regress without detection before release.
- **Backend dependency:** Contract tests should use the checked backend DTO/API behavior as fixtures, not a live production server.
- **Recommended fix:** Add repository/ViewModel unit tests with fakes or MockWebServer, serialization contract tests, refresh-concurrency tests, and Compose navigation tests for auth and all money-changing flows. Gate CI on unit tests and lint.
- **Classification:** Unit-test foundation added; instrumented and broader workflow coverage remain open.

### WK-A14 — Resolved (was Medium) — Release builds are shrunk, externally signed, and verified

- **Location:** `app/build.gradle.kts`; `app/proguard-rules.pro`; `gradle/libs.versions.toml`; `.github/workflows/build-release.yml`; `README.md`.
- **Resolution evidence:** Release builds enable R8 and resource shrinking. Gson DTO metadata and Retrofit annotations have focused keep rules. Coil has one version-catalog declaration. Gradle consumes an all-or-nothing external `WISEKOIN_RELEASE_*` signing configuration; the workflow materializes the secret keystore only in the runner's temporary directory, runs tests/lint plus APK/AAB builds, verifies both Gradle-signed artifacts, publishes only those verified copies, and removes the temporary keystore.
- **Backend dependency:** A real HTTPS production endpoint and production signing secrets still have to be configured operationally.
- **Verification:** An unsigned local R8 build completed with an injected HTTPS test origin. A second build using an ephemeral external keystore ran `validateSigningRelease`; `apksigner` verified the resulting minified APK with one v2 signer.
- **Classification:** Resolved in code and build automation; production secrets remain an operational prerequisite.

### WK-A15 — Resolved in code (was Low) — UI copy and custom controls expose localization/accessibility contracts

- **Location:** `res/values/strings.xml`; shared components; authentication, receipt, voice, transaction, goal, debt, profile, reporting, and camera screens.
- **Resolution evidence:** Static user-facing prose is resource-backed and English is the consistent base locale. Dynamic duplicate messages use plurals. Interactive custom surfaces now declare Button, Tab, Checkbox, or RadioButton roles as appropriate; selected bottom tabs expose selected semantics. Standalone action images have resource-backed descriptions, while icons paired with an announcing parent/text remain intentionally decorative. Small custom controls use Material minimum-interactive sizing or explicit 48 dp bounds. Chart weekday/month labels now follow the device locale, and narrow/high-font balance cards already switch to a stacked layout.
- **Verification:** Static search finds no remaining hardcoded prose in `Text(...)`; remaining literals are runtime values such as transcript, monetary progress, and punctuation. Debug compilation and unit tests pass.
- **Limit:** TalkBack, accessibility-scanner, large-font, and narrow-device checks still require emulator/device execution before production approval.
- **Classification:** Resolved in code; device-level accessibility smoke testing remains a release-validation task.

### WK-A16 — Resolved (was Low) — Money precision and local-time semantics are explicit

- **Location:** `util/MoneyFormatter.kt`; `util/TransactionTime.kt`; receipt/voice DTOs and ViewModels; monetary entry/reporting screens; `README.md`.
- **Resolution evidence:** Receipt, voice, transaction, wallet, goal, and debt amounts use `BigDecimal` end to end. One formatter/parser applies the device locale, ISO 4217 fraction digits capped at the backend-supported four places, and `HALF_EVEN`; VND is zero-scale while fractional currencies retain their supported precision. Voice/receipt inputs use decimal keyboards, parsing no longer strips punctuation, and transaction lists no longer truncate with `toLong()`.
- **Time contract:** `transactionDate` is documented as an ISO-8601 user-local wall time without an offset, matching the backend DTO. Fallback timestamps use the device system zone, while receipt/voice extraction sends the same IANA zone separately.
- **Verification:** Unit tests cover US and Vietnamese separators, unsupported trailing input, VND scaling/rounding, fractional formatting, and deterministic zone-aware fallback timestamps.
- **Classification:** Resolved in code and documented against the current backend contract.

### WK-A17 — Medium — Remaining advertised/backend-supported product scope is incomplete (partially resolved)

- **Location:** `SavingsGoalsScreen.kt`; `AddTransactionComponents.kt`; `ConfirmReceiptComponents.kt`; `ProfileScreen.kt`; `BankNotificationsScreen.kt`; `PasswordRecoveryScreens.kt`; backend goal/auth/OTP services; API service interfaces; no recurring-transaction feature files.
- **Resolved in this pass:** Goals can be swiped right to reveal deletion with a confirmation dialog; the backend atomically returns every contribution to its original wallet and removes goal-funding history before deleting the goal. Long-pressing a category in transaction-entry or receipt confirmation opens a modal that can rename it, change its icon, or delete it through the existing ownership-checked backend endpoints. The Profile tab now edits the persisted ISO base currency. Forgot-password now has a public, rate-limited recovery contract plus request, six-digit OTP verification, resend countdown, attempt/expiry enforcement, and new-password screens. Bank-notification tracking is now an opt-in local flow with finance-source filtering, de-identified parsing, encrypted account-scoped retention, and explicit transaction review.
- **Remaining evidence:** Recurring transactions, wallet-to-wallet transfers, transaction edit/delete, wallet update, and goal update remain absent from Android. Help Center remains a placeholder control.
- **Problem and impact:** The implemented subset now works end to end, but the remaining visible placeholders and backend-supported operations still leave an unclear release boundary.
- **Recommended fix:** Confirm the release scope, then implement or clearly disable the remaining capabilities and placeholder controls.
- **Classification:** Partially resolved; remaining implementation gaps are confirmed and the release requirement is unverified.

## 4. Integration assessment

### Contracts that align

- Login/register request fields and paths.
- Category list/create path, `categoryType` query, and fields.
- Wallet summary GET.
- Paginated transaction envelope and `/api/transactions/stats?year=` implementation (the backend README's `/monthly` text is stale; code uses `/stats`, matching Android).
- Debt list/create/repay DTO field names, apart from invalid client-side ID selection.
- Goal list/create fields. Goal funding through `POST /api/transactions` with `type=TRANSFER` and `goalId` is accepted by `TransactionService`, even though `/api/goals/{id}/fund` is also available.
- Receipt and voice multipart part name `file` and extraction response fields.
- User GET/PUT profile fields used by the client; ignored `baseCurrency` is forward-compatible for Gson.

### Confirmed mismatches and omissions

- Authentication response/session lifecycle now matches backend rotation and logout contracts.
- Android transaction request omits `destinationWalletId`, so wallet transfers cannot be represented.
- Android transaction response omits destination wallet/goal data, preventing complete display of linked transfers.
- Backend recurring, wallet transfer, transaction edit/delete, wallet update, and goal update capabilities remain outside the current Android scope as detailed in WK-A17; category management, goal deletion/refund, base-currency editing, and password recovery are now integrated.
- Android safely maps backend error statuses and consumes rate-limit retry headers without exposing response bodies.

### Backend dependencies that block or constrain client work

- A real production HTTPS base URL must be provided.
- Password recovery now has an OTP-backed backend contract; production delivery still requires correctly configured SMTP settings.
- Safe automatic retry of financial POSTs would benefit from a backend idempotency-key contract.
- Currency precision and local-wall-time semantics are now documented and implemented jointly; changing to instant/offset storage would require a coordinated backend contract migration.
- Receipt/voice currently return some plain-string errors; a uniform error envelope would simplify safe client messaging, but it does not block basic integration.

## 5. Production readiness checklist

### Release blockers

- [x] Require an injected release HTTPS API origin and disallow release cleartext. The real repository secret still must be configured operationally.
- [x] Implement encrypted access/refresh storage, single-flight refresh rotation, logout revocation, and terminal refresh cleanup.
- [x] Disable sensitive HTTP bodies in every build and exclude encrypted token storage from backup/transfer.
- [x] Fix debt repayment category/wallet selection and dialog dismissal.
- [x] Make history/statistics accurate through server paging/filtering and robust date parsing.
- [x] Add in-flight protection to every money-changing action.
- [x] Correct the wallet POST response contract.
- [x] Implement wallet details/delete, debt edit/delete, and OTP-gated password/account deletion flows.
- [ ] Expand critical-path automated tests. The signed, R8-enabled release gate is complete.

### Required before a production candidate

- [x] Centralize safe network-error mapping and provide loading/empty/error/retry states for the audited flows.
- [x] Put network and token access behind constructor-injected repositories and remove production mock/dead paths.
- [x] Resolve authenticated vs guest navigation and gate protected screens/features.
- [ ] Confirm the remaining WK-A17 feature scope; implement or remove all visible placeholders.
- [x] Configure and exercise external signing, R8/resource shrinking, and release network/log policies.
- [ ] Test camera, microphone, permission denial, process recreation, rotation, and cancellation on supported API levels.
- [ ] Verify accessibility with TalkBack, large fonts, narrow screens, and touch-target checks.

### Optional improvements after blockers

- [ ] Add local caching/offline read behavior and a documented synchronization policy.
- [ ] Implement nonessential backend CRUD capabilities selected for the product roadmap.
- [ ] Add analytics/crash reporting with financial and authentication data redaction.
- [ ] Optimize images, Compose recomposition hot spots, and large-screen layouts based on profiling.

## 6. Action plan and acceptance criteria

1. **Completed — establish release configuration.** Debug/release URLs and network policies are separated; release requires an injected HTTPS origin and rejects cleartext.
2. **Completed — rebuild the backend session contract.** Full auth DTOs, encrypted no-backup storage, synchronized rotation, one retry, terminal cleanup, logout revocation, startup resolution, and app-wide expired-session navigation are implemented and unit-tested.
3. **Completed — stop data exposure.** HTTP bodies are omitted in debug and all logging is disabled in release; sensitive headers are redacted, and encrypted tokens remain excluded from backup/transfer.
4. **Completed in code — fix contract/workflow defects.** Wallet POST typing matches the backend, and debt repayment uses explicit owned wallet/category selections with working dismissal. Device/API smoke tests remain before release approval.
5. **Completed in code — guarantee single financial submissions.** Financial/authentication actions now reject overlapping intents and expose disabled/progress state; live slow-network rotation testing remains part of device verification.
6. **Completed in code — make reporting truthful.** Expenses follows all server pages for an exact period, uses ISO date parsing, includes goal funding, and derives accurate labels and income/expense charts. Live large-account profiling remains.
7. **Completed for WK-A09/WK-A10 — state/error behavior.** Typed safe error mapping, offline/timeout/rate-limit handling, retry surfaces, lifecycle-aware collection, cancellation propagation, and removal of fake fallbacks are implemented. The remaining dead/mock paths were removed while resolving WK-A12.
8. **Partially completed — close the product scope.** Wallet details/delete, debt edit/delete, category edit/delete, goal delete/refund, base-currency editing, password recovery/change, account deletion, OTP verification, and bank-notification review are complete. Decide the remaining WK-A17 recurring, transfer, transaction edit/delete, wallet update, goal update, and Help Center scope. Acceptance: every shipped control performs a documented flow or is removed/clearly disabled; the feature matrix is updated.
9. **Completed — establish injectable frontend boundaries.** A composition root owns API construction, repositories accept typed clients, ViewModels accept repository interfaces/token storage, oversized screens are separated from components, and dead/mock production paths are removed.
10. **Partially completed — expand quality gates.** CI now tests, lints, builds with R8/resource shrinking, signs through external secrets, verifies APK/AAB signatures, and publishes only verified artifacts. Compose/integration coverage and smoke tests on at least one min-SDK and one target-SDK emulator/device remain.

## 7. Verification performed and limits

- Static trace covered Android routes/screens, ViewModels, DTOs, Retrofit services, manifest, Gradle configuration, storage, camera/audio utilities, and matching Spring controllers/DTOs/services/security configuration.
- Thirty-six focused frontend unit tests now also lock HTTP logging to body-free/disabled levels, verify locale-aware US and Vietnamese monetary parsing plus ISO-currency rounding, and exercise deterministic user-local fallback timestamps; all pass.
- `gradlew testDebugUnitTest lintDebug assembleDebug` completed successfully after remediation.
- Release configuration correctly fails without `WISEKOIN_API_BASE_URL`. With a valid HTTPS test value, an R8/resource-shrunk `assembleRelease` completed successfully. A second build with an ephemeral external keystore passed `validateSigningRelease`, and `apksigner` verified one v2 signer; the real production origin and signing material remain repository/operations secrets.
- All 45 backend tests pass on Java 21 with an isolated H2 test profile; OTP, account-security, and internal-error redaction behavior is included in the suite. `mvnw spotless:check` also passes.
- Development and production Compose configurations resolve successfully with their example environment files.
- No emulator/device, configured SMTP provider, production URL, production signing credentials, or AI provider credentials were available for live end-to-end validation.

## 8. Final verdict

**Not production-ready, but WK-A01 through WK-A12 and WK-A14 through WK-A16 are resolved in code.** Expand WK-A13 workflow/device coverage, decide the remaining WK-A17 product scope, configure production URL/signing/SMTP secrets, and complete device-level/end-to-end verification before approval.
