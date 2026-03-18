# CuMobile KMP — Architecture Reference

> Primary reference for AI sessions. Read this before any other file.
> Last updated: 2026-03-18

---

## Project Overview

KMP (Kotlin Multiplatform) rewrite of Flutter LMS app for Central University (ЦУ).
- Flutter reference: `cu-3rd-party/lms-mobile` (DeepWiki indexed), installed at `/home/olowo/StudioProjects/lms-mobile`
- Target platforms: Android + iOS (test on Android emulator, Linux host)
- Goal: maximum shared code in `shared/` module

**Package**: `io.github.kroune.cumobile`
**Base URL**: `https://my.centraluniversity.ru/api/`
**Compose Multiplatform**: 1.11.0-alpha02
**Kotlin**: 2.3.10

---

## Tech Stack

| Library | Version | Use |
|---------|---------|-----|
| Compose Multiplatform | 1.11.0-alpha02 | UI (shared) |
| Decompose | 3.4.0 | Navigation (ChildPages + ChildStack) |
| Koin | — | DI |
| Ktor | — | HTTP client |
| kotlinx-serialization | — | JSON |
| DataStore Preferences | — | Local persistence |
| essenty-lifecycle-coroutines | 2.5.0 | Auto-cancelling coroutine scopes |
| kotlin-logging (io.github.oshai) | 8.0.01 | Structured logging in catch blocks |
| kotlinx-datetime | — | Date/time formatting and parsing |

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
        │       ├── longread/      # LongreadComponent, Screen, TaskSection, TaskInfo
        │       ├── main/          # MainComponent (ChildPages tabs + ChildStack details)
        │       ├── notifications/ # NotificationsComponent
        │       ├── performance/   # CoursePerformanceComponent (2 tabs)
        │       ├── profile/       # ProfileComponent
        │       ├── root/          # RootComponent, RootScreen
        │       └── tasks/         # TasksComponent (full MVI with filtering)
        ├── androidMain/           # AndroidKoin, DataStorePath, PlatformWebView.android.kt, AndroidFileStorage
        └── iosMain/               # IosKoin, DataStorePath, PlatformWebView.ios.kt, IosFileStorage, MainViewController
```

---

## Architecture Patterns

### MVI (Model-View-Intent)
Every screen has:
- `XxxComponent.kt` — interface with `State`, `Intent`, and `stateFlow`
- `DefaultXxxComponent.kt` — implementation
- `XxxScreen.kt` — Compose UI consuming state + dispatching intents

### Navigation (Decompose)
- **ChildPages** — bottom nav tabs (preserves state on tab switch)
- **ChildStack** — detail navigation overlay (CourseDetail, Longread, Profile, Notifications, CoursePerformance)
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
- `ApiService` methods accept `cookie: String` as first param; repositories hide this from domain layer
- ApiService returns `null` / `false` / empty list on failure — **never throws**

### Logging
- Use `kotlin-logging`: `private val logger = KotlinLogging.logger {}`
- **Always** log errors in catch blocks: `logger.error(e) { "description" }`
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

1. App starts → `DefaultRootComponent.checkSavedAuth()` validates saved cookie
2. Valid → `MainChild`; Invalid → `LoginChild` → user taps login → `WebViewLoginChild`
3. WebView loads `https://my.centraluniversity.ru`
4. On each page load, check cookies for `bff.cookie`
5. Captured → save via `AuthRepository` → validate via `GET /hub/students/me` → `MainChild`
6. Logout → clear cookie → back to `LoginChild`

---

## ApiService Endpoints (25 total)

- **Auth**: `validateAuth`
- **Profile**: `fetchProfile`, `fetchProfileRaw`, `fetchAvatar`, `deleteAvatar`, `fetchLmsProfile`
- **Tasks**: `fetchTasks`, `fetchTaskDetails`, `fetchTaskEvents`, `fetchTaskComments`, `startTask`, `submitTask`, `prolongLateDays`, `cancelLateDays`, `createComment`
- **Courses**: `fetchCourses`, `fetchCourseOverview`
- **Content**: `fetchLongreadMaterials`, `fetchMaterial`, `getDownloadLink`, `getUploadLink`
- **Notifications**: `fetchNotifications`
- **Performance**: `fetchPerformance`, `fetchCourseExercises`, `fetchCoursePerformance`, `fetchGradebook`

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

Design decisions:
- `String` for enum-like fields (safer for unknown API values)
- `String?` for ISO 8601 dates (kotlinx-datetime used in FormatUtils)
- `JsonElement?` for polymorphic JSON fields

---

## Key Business Logic

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

### Schedule / Calendar
- ICS parser (pure Kotlin, shared) fetches from user-configured Yandex Calendar URL
- URL stored in DataStore (key: `ics_url`)
- RRULE expansion + EXDATE handling
- Home tab has daily view with date navigation
- URL configuration in Profile screen

### Course Reordering
- Custom order persisted in DataStore
- Edit mode with Up/Down arrows in Courses tab (no drag-and-drop yet)

---

## UI Conventions

### Colors (dark theme only)
- Primary accent: `#00E676` (green)
- Scaffold background: `#121212`
- Surface/cards: `#1E1E1E`
- Grade colors: `#00E676` ≥8, `#FFCA28` ≥6, `#FF9800` ≥4, `#EF5350` <4
- Theme, colors, task state labels: `presentation/common/Theme.kt`

### Icons
- Material Icons Extended **is available** (`androidx.compose.material:material-icons-extended:1.11.0-alpha02`) but **not currently declared** in `shared/build.gradle.kts`
- Add to `commonMain.dependencies` when needed for richer icons
- Bottom nav currently uses Unicode emoji (can be migrated to Material icons after adding the dependency)

### Previews
- Every `@Composable` screen function must have a `@Preview` companion

### Detekt
- Run `./gradlew detektAll` — fix violations, never suppress
- 6 "compiler errors" in detektMainAndroid are false positives (cross-module resolution)

### ktlint Rules Discovered
- "Newline expected before expression body" — for `= expr {`, expression starts on new line after `=`
- "Class body should not start with blank line"

---

## Completed Features (as of 2026-03-18)

All phases complete:
- Phase 0: Project setup, infra (auth, HTTP, DI, DataStore)
- Phase 1: Authentication (WebView cookie capture)
- Phase 2: Data models (13 files, 37 DTOs)
- Phase 3: ApiService (25 endpoints) + all repository impls
- Phase 4: Home screen + bottom nav + detail stack
- Phase 5: Tasks tab (full MVI, filtering, search)
- Phase 6: Courses tab (3 segments: Courses, Grade Sheet, Record Book)
- Phase 7: Longread / material viewer (markdown, file, coding, questions)
- Phase 8: Profile + Notifications screens
- Phase 9: Course Performance screen (Scores + Performance tabs)
- Phase 10: Files tab (full file manager, download, local storage)
- Code Quality: essenty-lifecycle-coroutines, kotlin-logging, CookieAwareRepository, FormatUtils
- Phase 11: Pull-to-refresh, dark theme polish, unit tests (FormatUtils, Theme)
- Deferred: Course reordering, File rename templates, Schedule/Calendar
- Phase 12: kotlinx-datetime migration

---

## Remaining Work

| Feature | Priority | Notes |
|---------|----------|-------|
| Late days dialog with stepper | High | Currently sends fixed request, needs stepper UI |
| File upload system | High | expect/actual file picker, presigned URL upload, progress |
| Content search in longreads | Medium | Case-insensitive, highlight, match navigation |
| Avatar upload | Medium | expect/actual image picker, POST multipart |
| In-app update checker | Low | GitHub releases API, version compare, update dialog |
| Document scanner | Low | High complexity, platform-specific (camera, PDF gen) |
| Model package restructuring | Low | Move `data/model/` → `model/`, rename `*Response` types |

---

## Reference Files

- `memorybank/flutter-lms-research.md` — full API docs, all data models, feature details from Flutter app
- `memorybank/flutter-ui-reference.md` — complete UI reference (colors, layouts, platform differences)
- `memorybank/progress.md` — detailed historical log of all completed work
