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

| Library                          | Version        | Use                                   |
|----------------------------------|----------------|---------------------------------------|
| Compose Multiplatform            | 1.11.0-alpha02 | UI (shared)                           |
| Decompose                        | 3.5.0          | Navigation (ChildPages + ChildStack + ChildItems) |
| Koin                             | —              | DI                                    |
| Ktor                             | —              | HTTP client                           |
| kotlinx-serialization            | —              | JSON                                  |
| DataStore Preferences            | —              | Key-value local persistence           |
| Room                             | 2.8.4          | Structured local persistence (SQLite) |
| essenty-lifecycle-coroutines     | 2.5.0          | Auto-cancelling coroutine scopes      |
| kotlin-logging (io.github.oshai) | 8.0.01         | Structured logging in catch blocks    |
| kotlinx-datetime                 | —              | Date/time formatting and parsing      |
| Ksoup (fleeksoft)                | 0.2.6          | HTML parsing (KMP Jsoup port)         |
| ComposeMediaPlayer               | 0.8.7          | Video/audio playback (KMP)            |

---

## Module Structure

```
CuMobile/
├── androidApp/                    # Android shell (Kotlin)
│   └── src/main/
│       ├── AndroidManifest.xml    # android:name=".AndroidApplication"
│       ├── MainActivity.kt        # Uses createRootComponent() from DI
│       └── AndroidApplication.kt  # Koin init
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
        │       ├── auth/          # LoginComponent, LoginScreen
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
- `XxxScreen.kt` — Compose UI consuming state + dispatching intents + collecting effects, it can be split into multiple files depending on the complexity

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
  lifecycle managed by `ChildItemsLifecycleController`. Experimental API (`@ExperimentalDecomposeApi`).
  Uses `MaterialConfig` (serializable sealed interface) as key, `LongreadItem` (sealed wrapper) as child.
  `CodingMaterialComponent` has full MVI; simple materials (Markdown, File, Image, etc.) are lightweight wrappers.
  - **material as constructor-val**: `LongreadMaterial` is immutable data fixed at creation — it stays as a
    constructor `val` property, NOT in component State. Putting it in State would bloat every `state.copy()`.
  - **ExternalUpdate channel**: Parent broadcasts events (e.g. search query changes) to children via
    `MutableSharedFlow<ExternalUpdate>(replay=1)`. Children collect and store in their own state.
- `navigation.push()` requires `@OptIn(DelicateDecomposeApi::class)`

### DI (Koin)

- `di/Koin.kt` — common modules (network, local, repository)
- `androidMain/di/AndroidKoin.kt` — Android-specific bindings + `createRootComponent()` factory
- `iosMain/di/IosKoin.kt` — iOS-specific bindings
- `iosMain/MainViewController.kt` — `createRootComponent()` for iOS

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

- Use `coroutineScope()` from `essenty-lifecycle-coroutines` in all components
- Do NOT manually create `CoroutineScope` + `onDestroy` cleanup

### Formatting Utilities

- All date/time/size formatting in `presentation/common/FormatUtils.kt`
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
- Pattern: `XxxScreen(component)` doesn't have any ui logic, it simply delegates to `XxxScreenContent(state, onIntent, ...)` which is
  `internal`; previews call the content function with mock state
- Common components (TopBar, TaskCard, etc.) have previews wrapping in `CuMobileTheme` +
  `Box(background)`
- Import: `import androidx.compose.ui.tooling.preview.Preview`

### Detekt

- Run `./gradlew detektAll` — fix violations, never suppress
- 6 "compiler errors" in detektMainAndroid are false positives (cross-module resolution)
