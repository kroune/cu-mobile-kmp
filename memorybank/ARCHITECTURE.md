# CuMobile KMP — Architecture Reference

## Project Overview

KMP (Kotlin Multiplatform) rewrite of Flutter LMS app (`/home/olowo/StudioProjects/lms-mobile`) for
Central University (ЦУ). Flutter LMS app was created based on the website. We have done our own
reverse of the web, it can be found at `web-reverse`, it contains analysis of the api, models, how
web functions, etc.

- Target platforms: Android + iOS (test on Android emulator, Linux host)
- Maximum shared code in `shared/` module

**Android applicationId**: `com.thirdparty.cumobile` (matches Flutter app for seamless update),
debug postfix if build is a debug one
**iOS bundleId**: `ru.spacedreamer.centraluniversity` (matches Flutter app)
**Kotlin package**: `io.github.kroune.cumobile` (internal source code package, unchanged)
**Base URL**: `https://my.centraluniversity.ru/api/`
**Compose Multiplatform**: 1.11.0-alpha02
**Kotlin**: 2.3.10

---

## Tech Stack

| Library                          | Version        | Use                                               |
|----------------------------------|----------------|---------------------------------------------------|
| Compose Multiplatform            | 1.11.0-alpha02 | UI (shared)                                       |
| Decompose                        | 3.5.0          | Navigation (ChildPages + ChildStack + ChildItems) |
| Koin                             | —              | DI                                                |
| Ktor                             | —              | HTTP client                                       |
| kotlinx-serialization            | —              | JSON                                              |
| DataStore Preferences            | —              | Key-value local persistence                       |
| Room                             | 2.8.4          | Structured local persistence (SQLite)             |
| essenty-lifecycle-coroutines     | 2.5.0          | Auto-cancelling coroutine scopes                  |
| kotlin-logging (io.github.oshai) | 8.0.01         | Structured logging in catch blocks                |
| kotlinx-datetime                 | —              | Date/time formatting and parsing                  |
| Ksoup (fleeksoft)                | 0.2.6          | HTML parsing (KMP Jsoup port)                     |
| ComposeMediaPlayer               | 0.8.7          | Video/audio playback (KMP)                        |
| play-services-auth-api-phone     | 18.2.0         | SMS User Consent API (Android only)               |

---

## Module Structure

```
CuMobile/
├── androidApp/                    # Android shell (Kotlin)
│   └── src/main/
│       ├── AndroidManifest.xml    # android:name=".AndroidApplication"
│       ├── MainActivity.kt        # Uses createRootComponent() from DI; wraps App in testTagsAsResourceId
│       └── AndroidApplication.kt  # Koin init
│   └── src/release/generated/baselineProfiles/  # Committed baseline-prof.txt (consumed by R8 at release time)
├── baselineprofile/               # com.android.test module, androidx.baselineprofile generator
│   └── src/main/kotlin/           # Startup/UnauthUi/LoggedInTour generators (UiAutomator)
├── baseline-profile-tags/         # KMP library, just const val testTag strings (shared contract)
├── iosApp/                        # iOS shell (Swift)
│   └── iOSApp.swift               # Uses MainViewControllerKt.createRootComponent()
└── shared/
    └── src/
        ├── commonMain/kotlin/io/github/kroune/cumobile/
        │   ├── data/
        │   │   ├── local/         # DataStore, AuthLocalDataSource, FileStorage, DownloadedFileInfo
        │   │   │   └── db/        # Room: AppDatabase, Entity, DAO, platform builders, migration
        │   │   ├── model/         # 13 files, ~37 @Serializable DTOs
        │   │   ├── network/       # HttpClientFactory, ApiService (25 endpoints)
        │   │   └── repository/    # 8 impls + CookieAwareRepository base class
        │   ├── di/                # Koin.kt (8 repository bindings)
        │   ├── domain/
        │   │   └── repository/    # 8 repository interfaces
        │   └── presentation/
        │       ├── auth/          # LoginComponent, LoginScreen, LoginStepContent
        │       │   ├── sms/       # SmsCodeObserver (expect) — OTP autofill
        │       │   └── webview/   # WebViewLoginComponent, PlatformWebView (expect/actual)
        │       ├── common/        # Theme, TopBar, TaskCard, CourseCard, FormatUtils
        │       ├── courses/       # CoursesComponent + detail/CourseDetailComponent
        │       ├── files/         # FilesComponent (full file manager)
        │       ├── home/          # HomeComponent (deadlines + schedule + courses)
        │       ├── longread/      # LongreadComponent (ChildItems), Screen, SearchHandler
        │       │   ├── component/ # MaterialConfig, LongreadItem, CodingMaterialComponent, simple material components
        │       │   ├── htmlrender/ # HTML→Compose rendering (Ksoup parser → HtmlBlock → composables)
        │       │   └── ui/        # LongreadScreen, CodingMaterialCardContent, CommentsTab, InfoTab, SolutionTab, LateDaysSection
        │       ├── main/          # MainComponent (ChildPages tabs + ChildStack details)
        │       ├── notifications/ # NotificationsComponent
        │       ├── performance/   # CoursePerformanceComponent (2 tabs)
        │       ├── profile/       # ProfileComponent
        │       ├── root/          # RootComponent, RootScreen
        │       ├── scanner/       # ScannerComponent (document scanner + PDF generation)
        │       └── tasks/         # TasksComponent (full MVI with filtering)
        ├── androidMain/           # AndroidKoin, DataStorePath, PlatformWebView.android.kt, AndroidFileStorage
        └── iosMain/               # IosKoin, DataStorePath, PlatformWebView.ios.kt, IosFileStorage, MainViewController
```

---

## Architecture Patterns

### MVI (Model-View-Intent)

Every screen has:

- `XxxComponent.kt` — interface with `State`, `Intent`, `Effect`, and `stateFlow`/`effects`
- `DefaultXxxComponent.kt` — implementation
- `XxxScreen.kt` — Compose UI consuming state + dispatching intents + collecting effects, it can be
  split into multiple files depending on the complexity

### ContentState<T> — Progressive Loading

- `ContentState<T>` sealed interface in `presentation/common/ContentState.kt`: `Loading`,
  `Success<T>`, `Error`
- Replaces `isLoading: Boolean` + `error: String?` + plain data fields in component State
- Each data field in State wraps its content in `ContentState<T>` for per-section loading/error
  handling
- **Important content Error** → full-screen `ErrorContent`; **Minor content Error** →
  `ActionErrorBar`
- On refresh → reset all ContentState fields to `Loading` → skeletons show → content fills in
  progressively
- **No `isLoading`/`isRefreshing` booleans** — derived:
  `val isContentLoading get() = tasks.isLoading && courses.isLoading`
- Extensions: `dataOrNull`, `isLoading`, `isError`, `isSuccess`, `errorOrNull`
- **Parallel loading**: Default components launch separate coroutines per API call, each updates its
  own ContentState independently

### One-time Effects

- Components that perform mutations expose `val effects: Flow<Effect>` for one-shot events (error
  messages, toasts)
- Implemented via `Channel<Effect>(Channel.BUFFERED)` + `receiveAsFlow()` in Default components
- UI collects effects in `LaunchedEffect(Unit)` into local `mutableStateOf` for display
- `ActionErrorBar` composable (in `CommonStates.kt`) shows transient error messages with dismiss
  button
- Pattern: `_effects.trySend(Effect.ShowError("message"))` in component, collected in screen
  composable

### Navigation (Decompose)

- **ChildPages** — bottom nav tabs (preserves state on tab switch)
- **ChildStack** — detail navigation overlay (CourseDetail, Longread, Profile, Notifications,
  CoursePerformance)
- **ChildItems** — longread materials in LazyColumn. Each material is a component with automatic
  lifecycle managed by `ChildItemsLifecycleController`. Experimental API (
  `@ExperimentalDecomposeApi`).
  Uses `MaterialConfig` (serializable sealed interface) as key, `LongreadItem` (sealed wrapper) as
  child.
  `CodingMaterialComponent` has full MVI; simple materials (Markdown, File, Image, etc.) are
  lightweight wrappers.
    - **material as constructor-val**: `LongreadMaterial` is immutable data fixed at creation — it
      stays as a
      constructor `val` property, NOT in component State. Putting it in State would bloat every
      `state.copy()`.
    - **ExternalUpdate channel**: Parent broadcasts events (e.g. search query changes) to children
      via
      `MutableSharedFlow<ExternalUpdate>(replay=1)`. Children collect and store in their own state.
    - **Longread search pipeline**: `DefaultLongreadComponent` pre-extracts plain text from
      every material's HTML once on load (`buildPlainTextIndex` → `Map<String, String>`,
      on `dispatchers.default`). Keystrokes feed a `MutableStateFlow<String>`, debounced
      180 ms; downstream counts matches against the cached index on `dispatchers.default`
      and only then emits `ExternalUpdate.SearchQuery` to child material components.
      Avoids re-parsing HTML per keystroke per material. `LongreadSearchHandler` is a
      pure state mutator (toggle/query-text/match-count/nav); the component owns the
      pipeline and applies results back via `applyMatchCount(forQuery = ..., ...)`,
      ignoring stale results.
- `navigation.push()` requires `@OptIn(DelicateDecomposeApi::class)`

### DI (Koin)

- `di/Koin.kt` — common modules (core, network, data, repository)
- `androidMain/di/AndroidKoin.kt` — Android-specific bindings + `createRootComponent()` factory
- `iosMain/di/IosKoin.kt` — iOS-specific bindings
- `iosMain/MainViewController.kt` — `createRootComponent()` for iOS
- `coreModule` provides the singleton `AppDispatchers`

### Lazy Main Dependencies

- `DefaultRootComponent` takes `mainDependenciesFactory: () -> MainDependencies`
  (not an already-constructed bundle). Held behind `by lazy(mainDependenciesFactory)` so
  the 13 repositories / services are only resolved from Koin when the user lands on
  `Config.Main` for the first time. Unauthenticated launches (splash → Login) don't
  instantiate main-flow singletons.
- `createRootComponent()` in `Koin.kt` passes the lambda; the first `Config.Main`
  navigation triggers `koin.get<X>()` for each repository.

### Repositories

- All repository impls extend `CookieAwareRepository` (base class in `data/repository/`)
- Provides `withCookie {}` and `withCookieOrFalse {}` helpers
- Cookie retrieved via `authLocal.cookieFlow.first()`
- `ApiService` methods accept `cookie: String` as first param; repositories hide this from domain
  layer
- ApiService returns `null` / `false` / empty list on failure — **never throws**, always logs errors

### Logging

- Use `kotlin-logging`: `private val logger = KotlinLogging.logger {}`
- **Always** log errors in catch blocks: `logger.error(e) { "description" }`
- Log mutation failures at `warn` level: `logger.warn { "Failed to ..." }`
- Never silently swallow exceptions

### Coroutine Scopes

- Use `componentScope()` (in `presentation/common/ComponentScope.kt`) in every `Default*Component`:
  `private val scope = componentScope()`. Wraps the essenty
  `lifecycle.coroutineScope(Dispatchers.Main.immediate + SupervisorJob())` so it's not
  duplicated 18 times.
- Do NOT manually create `CoroutineScope` + `onDestroy` cleanup.

### Dispatcher Injection (AppDispatchers)

- `util/AppDispatchers.kt` — class with `io`, `default`, `main` `CoroutineDispatcher` fields.
  Registered as a Koin singleton in `coreModule`, injected into every class that needs to
  switch dispatchers (detekt's `InjectDispatcher` rule is enforced).
- Repositories that do network / DataStore / file I/O extend `CookieAwareRepository(authLocal, dispatchers)`
  which wraps `withCookie { }` / `withCookieOrFalse { }` in `withContext(dispatchers.io)` —
  callers can safely invoke from `Dispatchers.Main.immediate` without blocking the UI.
- `AuthRepositoryImpl`, `CalendarRepositoryImpl`, `FileRepositoryImpl` take `AppDispatchers`
  (or just the `io` dispatcher) directly.
- CPU-bound state computations in components (TasksComponent filter/sort, CoursePerformance
  joins+aggregates, Notifications sort, Longread match count) run on `dispatchers.default`
  via `withContext(dispatchers.default) { ... }`.
- Room's `queryDispatcher` already uses `Dispatchers.IO` internally — don't re-wrap.
- `MainDependencies.dispatchers` piped through `TabChildFactory` / `DetailChildFactory` so
  all tab and detail components receive the same `AppDispatchers` instance.
- `LongreadDependencies` also carries `dispatchers`.

### Lazy Tab Loading (ChildPages)

- `DefaultMainComponent` wires tabs via `ChildPages`. Non-active tabs sit in `Status.CREATED`,
  so their constructors (and `init {}` blocks) run at app start.
- **Do NOT call network-loading functions directly from `init {}`** in tab components —
  every tab would fetch at startup even though only one is visible.
- Gate the initial load on the lifecycle transition instead:
  ```kotlin
  init {
      lifecycle.doOnStart(isOneTime = true) {
          loadData()
      }
  }
  ```
- `doOnStart(isOneTime = true)` fires the block only on the first `CREATED → STARTED`
  transition — i.e. the first time the user opens that tab. Subsequent tab re-entries
  do not re-fetch (user refreshes manually via pull-to-refresh).
- Cheap local observers (DataStore flows, etc.) can stay in `init {}` directly —
  they don't hit the network.
- Detail-stack destinations (profile, longread, course detail, etc.) are naturally
  lazy — they're created only when navigated to — so `init { load() }` is fine there.

### Formatting Utilities

- All date/time/size formatting in `presentation/common/FormatUtils.kt`
- Deadline-specific helpers in `presentation/common/DeadlineFormat.kt`:
  `parseDeadlineInstant`, `isOverdue`, `formatDeadlineTime` ("HH:mm"),
  `formatDeadlineDayShortMonth` ("5 апр"). Reuse these — don't duplicate ISO parsing.
- Shared month names in FormatUtils: `russianMonthsShort` ("янв", "фев"),
  `russianMonthsFull` ("января", "февраля"). Import these; don't redeclare locally.
- Internal helper `parseIsoDateTime(iso)` handles the full suite (with offset, with Z,
  date-only → end-of-day). Use via the public `format*` / `parseDeadlineInstant` wrappers.
- Uses `kotlinx-datetime` — no JVM-only APIs
- `DateTimeProvider` in `commonMain` for "today" and date-to-millis

---

## Authentication Flow

1. App starts → Android native splash screen is shown by `androidx.core:core-splashscreen`
2. Root navigation starts in `SplashChild` while `DefaultRootComponent.checkSavedAuth()` does a fast
   local cookie check (`hasCookie()`)
3. Cookie exists → navigate from `SplashChild` to `MainChild`, validate in background
4. No cookie → navigate from `SplashChild` to `LoginChild`
5. Background validation fails → redirect to `LoginChild`
6. User taps login → `WebViewLoginChild` (or native auth flow)
7. WebView loads `https://my.centraluniversity.ru`
8. On each page load, check cookies for `bff.cookie`
9. Captured → save via `AuthRepository` → validate via `GET /student-hub/students/me` → `MainChild`
10. Logout → clear cookie → back to `LoginChild`
11. Android native splash: removed via `setKeepOnScreenCondition` — stays visible while active child
    is `SplashChild`

### Login Entry Points (LoginComponent)

`LoginComponent.AuthStep` has four branches:
- `Email` / `Password` / `Otp` — native Keycloak flow via `AuthApiService`
- `BffCookie` — direct paste of `bff.cookie` value. Skips Keycloak entirely,
  calls `authRepository.saveCookie()` + `validateCookie()`. Intended for
  testing and manual recovery, not hidden behind a flag.
- `FallbackToWebView` intent escapes to the full WebView login.

Errors are one-time events, not state: `LoginComponent.Effect.ShowError` is
emitted via a buffered Channel and collected in `LoginScreen` into a local
`mutableStateOf<String?>`, cleared on step change.

### SMS OTP Autofill

`presentation/auth/sms/SmsCodeObserver` is an `expect @Composable` that delivers
a detected OTP string to its callback.

- **Android**: uses Google's SMS User Consent API
  (`com.google.android.gms:play-services-auth-api-phone`). No runtime
  permissions; the system shows a one-tap consent dialog, then delivers the
  SMS body via an ActivityResult. The receiver is registered with
  `SmsRetriever.SEND_PERMISSION` via `ContextCompat.registerReceiver`
  (RECEIVER_EXPORTED on API 33+).
- **iOS**: no API to read SMS, so falls back to the pasteboard. Listens for
  `UIPasteboardChangedNotification` and `UIApplicationDidBecomeActiveNotification`
  and extracts a code from the clipboard string. Catches the "Copy code"
  banner + manual copies from Messages.

Usage:
- Native OTP step (`OtpStepContent`): calls `SmsCodeObserver` — on code, fills
  `otpCode` via intent and auto-submits.
- WebView login (`PlatformWebView.android.kt`): calls `SmsCodeObserver` — on
  code, writes to the Android clipboard. The keyboard's clipboard suggestion
  lets the user fill the WebView's own input. Clipboard is preferred over JS
  DOM injection — robust across site layout changes.

### Android WebView form autofill

`PlatformWebView.android.kt` sets `importantForAutofill = IMPORTANT_FOR_AUTOFILL_YES`
and `settings.saveFormData = true` so Google / password-manager autofill
services can offer saved credentials inside the auth WebView.

### Native form autofill

Native Keycloak fields in `LoginStepContent.kt` tag their `OutlinedTextField`s
via `Modifier.contentType(...)` from `androidx.compose.ui.autofill`:
- Email step → `ContentType.EmailAddress`
- Password step → `ContentType.Password`
- OTP step → `ContentType.SmsOtpCode`

This lets password managers (Google, 1Password, etc.) offer saved credentials
on both Android and iOS via the common Compose semantics API. `AuthTextField`
accepts an optional `contentType: ContentType?` parameter — pass it from each
step composable when adding new fields. Email and password are on separate
steps (AnimatedContent), so each is an independent autofill session.

---

## ApiService Endpoints (25 total)

- **Auth**: `validateAuth`
- **Profile**: `fetchProfile`, `fetchProfileRaw`, `fetchAvatar`, `deleteAvatar`, `fetchLmsProfile`
- **Tasks**: `fetchTasks`, `fetchTaskDetails`, `fetchTaskEvents`, `fetchTaskComments`, `startTask`,
  `submitTask`, `prolongLateDays`, `cancelLateDays`, `createComment`
- **Courses**: `fetchCourses`, `fetchCourseOverview`
- **Content**: `fetchLongreadMaterials`, `fetchMaterial`, `getDownloadLink`, `getUploadLink`
- **Notifications**: `fetchNotifications`
- **Performance**: `fetchPerformance`, `fetchCourseExercises`, `fetchCoursePerformance`,
  `fetchGradebook`

---

## Data Models (commonMain `data/model/`)

13 files, ~37 `@Serializable` data classes. Key types:

- `StudentProfile`, `StudentLmsProfile`
- `Course`, `CourseOverview`, `CourseTheme`, `Longread`, `ThemeExercise`
- `StudentTask`, `TaskDetails`, `TaskEvent`, `TaskComment`
- `LongreadMaterial`, `MaterialAttachment`
- `NotificationItem`, `NotificationLink`
- `StudentPerformanceResponse`, `CourseExercise`, `TaskScore`, `GradebookResponse`
- `UploadLinkData`
- `DownloadedFileInfo` (local, not serialized from API)

### Task State Machine

`backlog` → `inProgress` → `hasSolution` → `review` → `evaluated`/`revision`/`failed`

- `hasSolution` is virtual: inProgress + submitAt != null
- `rework` normalized to `revision`, `rejected` to `failed`

### Task Filtering (TasksComponent)

- Active: backlog, inProgress, hasSolution, revision, rework, review
- Archive: evaluated, failed, rejected
- Sort: evaluated/failed/review at bottom; rest by deadline ascending (null last)
- **State shape:** raw inputs (`segment`, `statusFilter`, `courseFilter`, `searchQuery`)
  in `State` alongside `content: ContentState<Content>` where `Content` holds the
  filtered/sorted lists + counts + available filter values. No more `isLoading`/`error`
  booleans and no `recomputeDerived()` extension.
- **Derivation:** `DefaultTasksComponent` keeps raw fields in a private `RawState` and
  launches `scheduleDerive()` after each mutation (and after `loadTasks`). Cancels any
  in-flight derivation (`deriveJob?.cancel()`) so only the latest snapshot reaches `state`.
  `buildTasksContent(...)` (in `TasksContentBuilder.kt`) runs on `dispatchers.default`,
  precomputes effective state per task once, and sweeps `allTasks` in a single pass to
  bucket active/archive + collect courses.

### Late Days System

- Max 7 days extension per task
- Cannot extend in: review, evaluated, revision, rework
- Cancel allowed if lateDays > 0 AND effectiveDeadline > 24h from now

### Course Reordering

- Custom order persisted in DataStore

---

## UI Conventions

### Theme System (dark + light)

- `AppColorScheme` data class in `Theme.kt`: all colors (theme-varying + semantic)
- `DarkAppColors` / `LightAppColors` instances; semantic colors (task states, grades, categories)
  are identical in both
- `LocalAppColors` — `staticCompositionLocalOf`; `AppTheme.colors` —
  `@Composable @ReadOnlyComposable` accessor
- `CuMobileTheme(darkTheme)` — wraps `CompositionLocalProvider` + `MaterialTheme`; used in `App()`
  and `@Preview` functions
- Dark accent: `#00E676`; Light accent: `#007B32`
- All screens use `AppTheme.colors.xxx` (camelCase), never hardcoded colors
- Functions `taskStateColor()`, `courseCategoryColor()`, `gradeColor()` are `@Composable` (read from
  `AppTheme.colors`)

### Icons

- Material Icons Extended used `androidx.compose.material:material-icons-extended:1.11.0-alpha02`

### Previews

- Every `@Composable` screen function must have a `@Preview` companion (dark + light)
- Pattern: `XxxScreen(component)` doesn't have any ui logic, it simply delegates to
  `XxxScreenContent(state, onIntent, ...)` which is
  `internal`; previews call the content function with mock state
- Common components (TopBar, TaskCard, etc.) have previews wrapping in `CuMobileTheme` +
  `Box(background)`
- Import: `import androidx.compose.ui.tooling.preview.Preview`
- Usually in a separate file `XxxScreenPreviews.kt` (detekt won't flag them for `MagicNumber`)

### Detekt

- Run `./gradlew detektAll` — fix violations, never suppress
- 6 "compiler errors" in detektMainAndroid are false positives (cross-module resolution)
- `MagicNumber` rule is configured with `ignoreAnnotated: ["Composable"]` — numbers inside
  `@Composable` functions (dp/sp/alpha literals, etc.) are exempt. Don't extract UI pixel
  values into `private const val` constants just to satisfy the rule; inline them.
  Still extract numbers when they carry non-obvious semantic meaning (e.g. `MillisPerHour`,
  thresholds like `UrgencyRedHours`) — that's about readability, not detekt.

---

## Baseline Profile (Android)

- Module layout: `:baselineprofile` (com.android.test + `androidx.baselineprofile`) produces
  profiles; `:baseline-profile-tags` holds the `testTag` constants shared between the generator
  and shared-UI — changing the UI does **not** invalidate the tags module's build cache.
- `androidApp` applies `androidx.baselineprofile` as consumer, adds `androidx.profileinstaller`
  (required for API 28-30 to apply profiles), and has `baselineProfile { saveInSrc = true; mergeIntoMain = true }`.
  MainActivity wraps `App(...)` in a `Box.semantics { testTagsAsResourceId = true }` so UiAutomator
  can query Compose tags via `By.res("...")`.
- testTag anchors (in `BaselineTestTags`): 4 bottom-nav tabs, 3 login/BffCookie anchors,
  first course card, first task card. Nothing else — keep the surface tiny.
- Three independent generator classes (order = most-resilient → most-complete):
  1. `StartupBaselineProfileGenerator` — cold start only. No testTag lookups → always passes.
  2. `UnauthUiBaselineProfileGenerator` — exercises the login screen composition (Email → BffCookie step).
  3. `LoggedInTourBaselineProfileGenerator` — gated on the `bffCookie` instrumentation arg
     (`Assume.assumeTrue`); performs full login + tab tour + one course detail.
- GMD: `pixel6Api31` (AOSP system image) defined in `:baselineprofile`. Reuses the
  locally-cached `android-31/default` image (no emulator download needed).
- Generation: `./gradlew :androidApp:generateBaselineProfile` (add
  `-Pandroid.testInstrumentationRunnerArguments.bffCookie=$BFF_COOKIE` for the logged-in tour).
  Output is committed at `androidApp/src/main/generated/baselineProfiles/baseline-prof.txt`
  (with `mergeIntoMain = true`) and automatically bundled by `assembleRelease`.
- Verification: `./scripts/verify-baseline-profile.sh <path-to-apk>` checks for
  `assets/dexopt/baseline.prof{,m}` in the APK/AAB.
- CI does not regenerate profiles (no emulator); the committed txt is consumed by every
  release build as source-of-truth.
