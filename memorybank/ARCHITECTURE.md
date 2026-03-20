# CuMobile KMP — Architecture Reference

> Primary reference for AI sessions. Read this before any other file.
> Last updated: 2026-03-18

---

## Project Overview

KMP (Kotlin Multiplatform) rewrite of Flutter LMS app for Central University (ЦУ).
- Flutter reference: `cu-3rd-party/lms-mobile` (DeepWiki indexed), installed at `/home/olowo/StudioProjects/lms-mobile`
- Target platforms: Android + iOS (test on Android emulator, Linux host)
- Goal: maximum shared code in `shared/` module

**Android applicationId**: `com.thirdparty.cumobile` (matches Flutter app for seamless update)
**iOS bundleId**: `ru.spacedreamer.centraluniversity` (matches Flutter app)
**Kotlin package**: `io.github.kroune.cumobile` (internal source code package, unchanged)
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
- `XxxScreen.kt` — Compose UI consuming state + dispatching intents + collecting effects

### One-shot Effects
- Components that perform mutations expose `val effects: Flow<Effect>` for one-shot events (error messages, toasts)
- Implemented via `Channel<Effect>(Channel.BUFFERED)` + `receiveAsFlow()` in Default components
- UI collects effects in `LaunchedEffect(Unit)` into local `mutableStateOf` for display
- `ActionErrorBar` composable (in `CommonStates.kt`) shows transient error messages with dismiss button
- Currently used in: `LongreadComponent`, `FilesComponent`, `ProfileComponent`
- Pattern: `_effects.trySend(Effect.ShowError("message"))` in component, collected in screen composable

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
- Log mutation failures at `warn` level: `logger.warn { "Failed to ..." }`
- Never silently swallow exceptions
- Components with loggers: `DefaultHomeComponent`, `DefaultFilesComponent`, `DefaultLongreadComponent`, `DefaultProfileComponent`

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
5. Captured → save via `AuthRepository` → validate via `GET /student-hub/students/me` → `MainChild`
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

### Theme System (dark + light)
- `AppColorScheme` data class in `Theme.kt`: all colors (theme-varying + semantic)
- `DarkAppColors` / `LightAppColors` instances; semantic colors (task states, grades, categories) are identical in both
- `LocalAppColors` — `staticCompositionLocalOf`; `AppTheme.colors` — `@Composable @ReadOnlyComposable` accessor
- `CuMobileTheme(darkTheme)` — wraps `CompositionLocalProvider` + `MaterialTheme`; used in `App()` and `@Preview` functions
- Dark accent: `#00E676`; Light accent: `#007B32`
- All screens use `AppTheme.colors.xxx` (camelCase), never hardcoded colors
- Functions `taskStateColor()`, `courseCategoryColor()`, `gradeColor()` are `@Composable` (read from `AppTheme.colors`)

### Icons
- Material Icons Extended **is available** (`androidx.compose.material:material-icons-extended:1.11.0-alpha02`) but **not currently declared** in `shared/build.gradle.kts`
- Add to `commonMain.dependencies` when needed for richer icons
- Bottom nav currently uses Unicode emoji (can be migrated to Material icons after adding the dependency)

### Previews
- Every `@Composable` screen function must have a `@Preview` companion (dark + light)
- Pattern: `XxxScreen(component)` delegates to `XxxScreenContent(state, onIntent, ...)` which is `internal`; previews call the content function with mock state
- Common components (TopBar, TaskCard, etc.) have previews wrapping in `CuMobileTheme` + `Box(background)`
- Import: `import androidx.compose.ui.tooling.preview.Preview`

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
- Content search in longreads: `LongreadComponent.State` has `isSearchVisible`, `searchQuery`, `searchMatchCount`, `currentMatchIndex`; `handleSearchIntent()` in DefaultLongreadComponent; `SearchBar`/`SearchInput`/`SearchNavigation`/`highlightMatches()` in LongreadScreen
- In-app update checker: `UpdateChecker` (data/network/) checks GitHub releases API; `UpdateInfo`/`GithubRelease` (data/model/ReleaseInfo.kt); `MainComponent.updateInfo` + `dismissUpdate()`; `UpdateDialog` in MainScreen; `UpdateCheckerTest` unit tests
- API endpoint centralization: all endpoint paths in `ApiEndpoints.kt` (data/network/); `hub/` → `student-hub/` migration
- Edge-to-edge: `WindowInsets.statusBars` on main Column, `WindowInsets.navigationBars` on BottomNavBar, statusBars on DetailOverlay
- MaterialTheme: `App()` wraps `RootScreen` in `MaterialTheme` with `isSystemInDarkTheme()` (dark/light color schemes in `Main.kt`)
- Notification links: in-app longread navigation + external URLs via `LaunchedEffect` / `uriHandler`; `DefaultNotificationsComponent.onOpenLongread(longreadId, courseId, themeId)` wired from `DefaultMainComponent`; regex extracts all 3 IDs; `externalLinkToOpen` state field for external links
- Debug logging: auth flow (cookie capture, validation, API response) logged via kotlin-logging
- Bug fixes (2026-03-19):
  - `isOverdue()` implemented with kotlinx-datetime (was always returning false)
  - CoursePerformanceScreen uses `DetailTopBar` instead of wrong `TopBar`
  - TasksScreen preserves existing data during pull-to-refresh
  - CoursesScreen shows `EmptyContent` when no courses
  - Files tab: `ConfirmDeleteDialog` for "Delete All" action
  - Profile screen: calendar URL configuration section (`CalendarSection` composable, `CalendarRepository` injected into `DefaultProfileComponent`)
  - Home screen: "Подключить в настройках профиля" is now a clickable link navigating to Profile (via `OpenProfile` intent)
  - CoursesListContent refactored into smaller functions (`EditModeToggle`, `ActiveCourseItem`, `swapIds`)
  - File opening: `FileOpener` interface in commonMain; `AndroidFileOpener` (FileProvider + Intent.ACTION_VIEW) in androidMain; `IosFileOpener` stub in iosMain; registered in Koin platform modules; wired through `MainDependencies.fileOpener`; FileProvider configured in AndroidManifest.xml with `file_paths.xml`
  - Notification links: relative paths (e.g. `/schedule`) now expanded with `https://my.centraluniversity.ru` base URL

- File upload system (2026-03-19):
  - `PickedFile` data class (data/model/): name, bytes, contentType, size
  - `PendingAttachment` + `UploadStatus` (data/model/): tracks upload lifecycle (Uploading/Uploaded/Failed)
  - `ContentApiService.uploadFileToUrl()`: PUT raw bytes to presigned URL using `ByteArrayContent`
  - `ContentRepository.uploadFile()`: orchestrates getUploadLink → uploadFileToUrl → returns `MaterialAttachment`
  - `FilePicker` interface + `rememberFilePicker()` expect/actual Composable:
    - Android: `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument())` + ContentResolver
    - iOS: `UIDocumentPickerViewController` + `NSData.dataWithContentsOfURL` + security-scoped resource access
  - `LongreadComponent.State`: `pendingSolutionAttachments`, `pendingCommentAttachments`
  - New intents: `PickSolutionAttachment`, `RemoveSolutionAttachment`, `PickCommentAttachment`, `RemoveCommentAttachment`
  - `handleAttachmentIntent()` + `handleTaskActionIntent()` extracted from `onIntent` to stay under cyclomatic complexity limit
  - `SolutionTab`: `AttachButton`, `PendingAttachmentsList`, `PendingAttachmentChip` composables; submit blocked while uploads in progress
  - `CommentsTab`: same attach button + pending chips; send blocked while uploads in progress
  - `CodingMaterialCard` creates `rememberFilePicker` launchers for solution and comment pickers
  - `material-icons-extended:1.7.3` added for `AttachFile` and `Close` icons
- Avatar upload (2026-03-19):
  - `ProfileApiService.uploadAvatar()`: POST multipart (`submitFormWithBinaryData`) to `student-hub/avatars/me`
  - `ProfileRepository.uploadAvatar(bytes, contentType): Boolean`
  - `ProfileComponent.Intent.UploadAvatar(PickedFile)` + `isUploadingAvatar` state
  - `AvatarSection` has green "+" button (bottom-right); reuses `FilePicker` expect/actual
  - After successful upload, fetches updated avatar bytes to refresh UI

- Document scanner (2026-03-20):
  - `ScannerComponent` interface with nested sealed intents: `Intent.Page` (Add/Remove/MoveUp/MoveDown), `Intent.Editor` (Open/UpdateRotation/Close), `Intent.Settings` (UpdateFileName/SetCompression), `Intent.SavePdf`
  - `DefaultScannerComponent`: page management, rotation editing, PDF generation + save via `FileStorage`
  - `ScannerScreen`: hero card, filename field, page list with thumbnails + reorder buttons, image editor overlay with rotation slider + quick buttons (-90°/0°/+90°), compression toggle, save button
  - `ImagePicker` expect/actual composable: Android `TakePicture` + `GetMultipleContents`; iOS `UIImagePickerController`
  - `ImageDecoder` expect/actual: Android `BitmapFactory.decodeByteArray().asImageBitmap()`; iOS `org.jetbrains.skia.Image.makeFromEncoded().toComposeImageBitmap()`
  - `PdfGenerator` interface (`data/local/`): `generatePdf(pages, compress): ByteArray?`
  - `AndroidPdfGenerator`: `android.graphics.pdf.PdfDocument`, rotation via `Matrix.postRotate()`, compression via `Bitmap.createScaledBitmap()` (max 1920px)
  - `IosPdfGenerator`: UIKit PDF context (`UIGraphicsBeginPDFContextToData`), rotation via `CGContextRotateCTM`
  - Navigation: `DetailConfig.Scanner` in `DefaultMainComponent`; `ScannerChild` in `MainComponent.DetailChild`; FAB in `FilesScreen`
  - DI: `PdfGenerator` + `FileStorage` added to `MainDependencies`; platform impls registered in `AndroidKoin`/`IosKoin`
  - FileProvider `cache-path` added to `file_paths.xml` for camera temp files
  - Unique filenames: `normalizeFileName()` strips invalid chars; `buildUniqueFilename()` appends `__dup{n}` if file exists

---

## Remaining Work

| Feature | Priority | Notes |
|---------|----------|-------|
| Late days dialog with stepper | **Done** | `ProlongLateDays(days: Int)` intent; stepper dialog; `formatDeadlinePlusDays()` in FormatUtils |
| File upload system | **Done** | expect/actual file picker, presigned URL upload, attach to solutions + comments |
| Content search in longreads | **Done** | Search bar + highlighting in MarkdownCard; prev/next match nav; `SearchBar`/`SearchInput`/`SearchNavigation` composables |
| Avatar upload | **Done** | `ProfileApiService.uploadAvatar()` multipart POST; reuses `FilePicker`; green "+" on avatar |
| In-app update checker | **Done** | `UpdateChecker` in `data/network/`, `UpdateInfo`/`GithubRelease` in `data/model/ReleaseInfo.kt`, dialog in `MainScreen` |
| Document scanner | **Done** | `ScannerComponent` MVI with nested `Intent.Page`/`Editor`/`Settings`; `ImagePicker`/`ImageDecoder` expect/actual; `PdfGenerator` interface + Android/iOS impls |
| Model package restructuring | Low | Move `data/model/` → `model/`, rename `*Response` types |

---

## Reference Files

- `memorybank/flutter-lms-research.md` — full API docs, all data models, feature details from Flutter app
- `memorybank/flutter-ui-reference.md` — complete UI reference (colors, layouts, platform differences)
- `memorybank/progress.md` — detailed historical log of all completed work
