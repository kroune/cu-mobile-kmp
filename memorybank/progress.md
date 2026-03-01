# CuMobile KMP — Implementation Progress

## Last Updated: 2026-02-23 (Code quality refactoring complete)

## Completed Phases

### Phase 0: Project Setup & Fixes ✅

**Bugs Fixed:**
- `AndroidManifest.xml` — added `android:name=".AndroidApplication"` so Koin initializes on startup
- Renamed all `kmptemplate` references to `cumobile` in:
  - `settings.gradle.kts` (rootProject.name)
  - `androidApp/build.gradle.kts` (applicationId, namespace)
  - `shared/build.gradle.kts` (namespace)
- Fixed test package from `com.kmptemplate.app` to `io.github.kroune.cumobile`

**Infrastructure Created:**

| Layer | Files Created |
|-------|---------------|
| **Data/Network** | `HttpClientFactory.kt`, `ApiService.kt` |
| **Data/Local** | `DataStoreFactory.kt`, `DataStorePath.android.kt`, `DataStorePath.ios.kt`, `AuthLocalDataSource.kt` |
| **Data/Repository** | `AuthRepositoryImpl.kt` |
| **Domain/Repository** | `AuthRepository.kt`, `ProfileRepository.kt`, `TaskRepository.kt`, `CourseRepository.kt`, `ContentRepository.kt`, `NotificationRepository.kt`, `PerformanceRepository.kt` |
| **Presentation** | `RootComponent.kt`, `DefaultRootComponent.kt`, `RootScreen.kt`, `LoginComponent.kt`, `DefaultLoginComponent.kt`, `LoginScreen.kt`, `MainComponent.kt`, `DefaultMainComponent.kt`, `MainScreen.kt` |
| **DI** | `AndroidKoin.kt`, `IosKoin.kt` (updated `Koin.kt`) |
| **Test** | `SmokeTest.kt` |

**Architecture Details:**
- Base URL: `https://my.centraluniversity.ru/api/`
- Package: `io.github.kroune.cumobile`
- Compose Multiplatform 1.11.0-alpha02, Kotlin 2.3.10
- Dependencies: Ktor, kotlinx-serialization, Decompose 3.4.0, Koin, DataStore Preferences

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew :shared:ktlintCheck :androidApp:ktlintCheck`)
- ✅ App runs on Android emulator (Login ↔ Main navigation working)

---

### Phase 1: Authentication (Days 2–3) ✅

**Files Created:**

| File | Description |
|------|-------------|
| `presentation/auth/webview/WebViewLoginComponent.kt` | MVI interface: State (isLoading, error), Intent (CookieCaptured, BackClicked) |
| `presentation/auth/webview/DefaultWebViewLoginComponent.kt` | Implementation: captures cookie → saves → validates → navigates |
| `presentation/auth/webview/WebViewLoginScreen.kt` | Shared UI: TopAppBar + PlatformWebView + loading overlay |
| `presentation/auth/webview/PlatformWebView.kt` | expect composable for platform WebView |
| `androidMain/.../PlatformWebView.android.kt` | Android actual: `android.webkit.WebView` with cookie interception |
| `iosMain/.../PlatformWebView.ios.kt` | iOS actual: `WKWebView` with `WKNavigationDelegate` cookie capture |

**Files Modified:**

| File | Changes |
|------|---------|
| `RootComponent.kt` | Added `WebViewLoginChild` to sealed `Child` hierarchy |
| `DefaultRootComponent.kt` | Added `AuthRepository` param, startup auth check, WebViewLogin navigation, logout with cookie clearing, lifecycle-scoped coroutine, `@OptIn(DelicateDecomposeApi::class)` |
| `RootScreen.kt` | Added `WebViewLoginChild` rendering case |
| `DefaultLoginComponent.kt` | Changed `onLoginSuccess` → `onNavigateToWebView` |
| `androidMain/di/AndroidKoin.kt` | Added `createRootComponent()` factory that resolves `AuthRepository` from Koin |
| `iosMain/MainViewController.kt` | Added `createRootComponent()` factory |
| `MainActivity.kt` | Uses `createRootComponent()` factory |
| `iOSApp.swift` | Uses `MainViewControllerKt.createRootComponent()` |

**Auth Flow:**
1. App starts → `DefaultRootComponent.checkSavedAuth()` validates saved cookie
2. If valid → navigate to `MainChild`
3. If invalid → stay at `LoginChild` → user taps login → `WebViewLoginChild`
4. WebView loads `https://my.centraluniversity.ru`
5. On each `onPageFinished` / `shouldOverrideUrlLoading`, check cookies for `bff.cookie`
6. Once captured → save via `AuthRepository` → validate via `GET /hub/students/me` → navigate to `MainChild`

**Discoveries:**
- `koin-core` declared as `implementation` in shared module — used factory functions in platform source sets
- `Icons.AutoMirrored.Filled.ArrowBack` not available — used `TextButton` with "← Назад"
- Android `CookieManager.setAcceptThirdPartyCookies()` requires `WebView` instance, not `CookieManager`
- `navigation.push()` requires `@OptIn(DelicateDecomposeApi::class)`

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew :shared:ktlintCheck :androidApp:ktlintCheck`)
- ✅ Emulator test passed — login and logout flow working

---

### Phase 2: Data Models (Day 3) ✅

**Files Created** (all in `shared/src/commonMain/kotlin/io/github/kroune/cumobile/data/model/`):

| File | Key Classes |
|------|------------|
| `StudentProfile.kt` | `StudentProfile`, `EmailInfo`, `PhoneInfo` |
| `StudentLmsProfile.kt` | `StudentLmsProfile` |
| `Course.kt` | `Course` |
| `CourseOverview.kt` | `CourseOverview`, `CourseTheme`, `Longread`, `ThemeExercise`, `ExerciseActivity` |
| `StudentTask.kt` | `StudentTask`, `TaskExercise`, `TaskCourse` |
| `TaskDetails.kt` | `TaskDetails`, `TaskDetailsExercise`, `TaskDetailsSolution`, `TaskDetailsStudent` |
| `TaskEvent.kt` | `TaskEvent`, `TaskEventContent`, `TaskEventScore`, `TaskEventEstimation`, `TaskEventSolution`, `TaskEventActor`, `TaskEventActorName`, `TaskEventTask`, `TaskEventEstimationActivity` |
| `TaskComment.kt` | `TaskComment`, `CommentSender` |
| `LongreadMaterial.kt` | `LongreadMaterial`, `LongreadMaterialContent`, `MaterialAttachment`, `MaterialEstimation`, `MaterialEstimationActivity` |
| `NotificationModels.kt` | `NotificationItem`, `NotificationLink`, `NotificationRequest`, `NotificationPaging`, `NotificationFilter` |
| `Performance.kt` | `StudentPerformanceResponse`, `StudentPerformanceCourse`, `CourseExercisesResponse`, `CourseExercise`, `CourseExerciseActivity`, `CourseExerciseTheme`, `CourseStudentPerformanceResponse`, `TaskScore`, `TaskScoreActivity` |
| `Gradebook.kt` | `GradebookResponse`, `GradebookSemester`, `GradebookGrade` |
| `UploadLinkData.kt` | `UploadLinkData` |

**Total**: 13 files, ~37 `@Serializable` data classes.

**Design decisions**:
- `String` for enum-like fields (safer for unknown API values)
- `String?` for ISO 8601 dates (no kotlinx-datetime dependency)
- `JsonElement?` for polymorphic JSON fields (`viewContent`, `lateDays`)
- `MaterialAttachment` defined in `LongreadMaterial.kt`, reused across models

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew :shared:ktlintCheck`)

---

### Phase 3: API Service & Repositories (Days 4–5) ✅

**Files Created:**

| File | Description |
|------|-------------|
| `data/repository/ProfileRepositoryImpl.kt` | Profile & avatar operations via ApiService |
| `data/repository/TaskRepositoryImpl.kt` | Tasks CRUD, submissions, comments, late days |
| `data/repository/CourseRepositoryImpl.kt` | Courses list & overview |
| `data/repository/ContentRepositoryImpl.kt` | Longreads, materials, file links |
| `data/repository/NotificationRepositoryImpl.kt` | In-app notifications |
| `data/repository/PerformanceRepositoryImpl.kt` | Performance & gradebook |

**Files Modified:**

| File | Changes |
|------|---------|
| `data/network/ApiService.kt` | Expanded from 2 methods to 25 endpoint methods covering all API calls |
| `data/model/NotificationModels.kt` | Changed `NotificationFilter.category` from `String` to `Int`; added `NotificationRequest.create()` factory |
| `domain/repository/ProfileRepository.kt` | Defined 4 methods: fetchProfile, fetchLmsProfile, fetchAvatar, deleteAvatar |
| `domain/repository/TaskRepository.kt` | Defined 9 methods: fetchTasks, fetchTaskDetails, fetchTaskEvents, fetchTaskComments, startTask, submitTask, prolongLateDays, cancelLateDays, createComment |
| `domain/repository/CourseRepository.kt` | Defined 2 methods: fetchCourses, fetchCourseOverview |
| `domain/repository/ContentRepository.kt` | Defined 4 methods: fetchLongreadMaterials, fetchMaterial, getDownloadLink, getUploadLink |
| `domain/repository/NotificationRepository.kt` | Defined 1 method: fetchNotifications |
| `domain/repository/PerformanceRepository.kt` | Defined 4 methods: fetchPerformance, fetchCourseExercises, fetchCoursePerformance, fetchGradebook |
| `di/Koin.kt` | Added 6 new repository bindings to repositoryModule |

**ApiService endpoints (25 total):**
- Auth: validateAuth
- Profile: fetchProfile, fetchProfileRaw, fetchAvatar, deleteAvatar, fetchLmsProfile
- Tasks: fetchTasks, fetchTaskDetails, fetchTaskEvents, fetchTaskComments, startTask, submitTask, prolongLateDays, cancelLateDays, createComment
- Courses: fetchCourses, fetchCourseOverview
- Content: fetchLongreadMaterials, fetchMaterial, getDownloadLink, getUploadLink
- Notifications: fetchNotifications
- Performance: fetchPerformance, fetchCourseExercises, fetchCoursePerformance, fetchGradebook

**Architecture pattern:**
- Every repository impl takes `AuthLocalDataSource` + `ApiService` via constructor injection
- Cookie retrieval via `authLocal.cookieFlow.first()` — returns null early if not authenticated
- All ApiService methods accept `cookie: String` as first parameter; repositories hide this detail from domain layer
- ApiService methods return `null` / `false` / empty list on failure, never throw

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew :shared:ktlintCheck`)

---

### Phase 4: Home Screen & Navigation (Days 6–8) ✅

**Files Created:**

| File | Description |
|------|-------------|
| `presentation/home/HomeComponent.kt` | MVI interface: State (tasks, courses, profileInitials, lateDaysBalance), Intent (OpenTask, OpenCourse, Refresh), computed deadlineTasks/activeCourses |
| `presentation/home/DefaultHomeComponent.kt` | Loads tasks/courses/profile in coroutineScope, delegates navigation via callbacks |
| `presentation/home/HomeScreen.kt` | Deadlines section (horizontal LazyRow) + Courses section (2-column LazyVerticalGrid) |
| `presentation/tasks/TasksComponent.kt` | Placeholder MVI interface (stub for Phase 5) |
| `presentation/tasks/DefaultTasksComponent.kt` | Placeholder implementation |
| `presentation/tasks/TasksScreen.kt` | Placeholder screen |
| `presentation/courses/CoursesComponent.kt` | Placeholder MVI interface (stub for Phase 6) |
| `presentation/courses/DefaultCoursesComponent.kt` | Placeholder implementation |
| `presentation/courses/CoursesScreen.kt` | Placeholder screen |
| `presentation/files/FilesComponent.kt` | Placeholder MVI interface (stub for Phase 10) |
| `presentation/files/DefaultFilesComponent.kt` | Placeholder implementation |
| `presentation/files/FilesScreen.kt` | Placeholder screen |
| `presentation/common/Theme.kt` | AppColors (dark theme), taskStateLabel/Color, courseCategoryLabel/Color |
| `presentation/common/TopBar.kt` | Top bar with title, late days balance, notification bell (emoji), avatar circle |
| `presentation/common/TaskCard.kt` | DeadlineTaskCard, StatusBadge, DeadlineDateRow |
| `presentation/common/CourseCard.kt` | CourseCard with category chip |

**Files Modified:**

| File | Changes |
|------|---------|
| `presentation/main/MainComponent.kt` | Rewritten: tabPages (ChildPages), detailStack (ChildStack), TabChild/DetailChild sealed classes |
| `presentation/main/DefaultMainComponent.kt` | Rewritten: ChildPages for tabs, ChildStack for details, requires TaskRepository/CourseRepository/ProfileRepository |
| `presentation/main/MainScreen.kt` | Rewritten: Scaffold with TopBar + tab content + BottomNavBar (Unicode emoji icons) + DetailOverlay |
| `presentation/root/DefaultRootComponent.kt` | Updated constructor: accepts TaskRepository/CourseRepository/ProfileRepository, passes to DefaultMainComponent |
| `androidMain/di/AndroidKoin.kt` | Updated createRootComponent to resolve 4 repositories from Koin |
| `iosMain/MainViewController.kt` | Updated createRootComponent to resolve 4 repositories from Koin |

**Navigation architecture:**
- `ChildPages` for bottom nav tabs (preserves state on tab switch)
- `ChildStack` for detail navigation overlay (CourseDetail, Longread, Profile, Notifications, CoursePerformance)
- Material Icons Extended unavailable for Compose Multiplatform 1.11.0-alpha02 — using Unicode emoji for nav icons and notification bell

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew :shared:ktlintCheck`)

---

## Remaining Phases

### Phase 5: Tasks Tab (Days 9–10) ✅

**Files Created:**

| File | Description |
|------|-------------|
| `presentation/tasks/TaskListItem.kt` | Task card: exercise name, course, status badge, deadline, late days info |

**Files Rewritten:**

| File | Changes |
|------|---------|
| `presentation/tasks/TasksComponent.kt` | Full MVI interface: State (allTasks, loading, error, segment, statusFilter, courseFilter, searchQuery), Intent (SelectSegment, FilterByStatus, FilterByCourse, Search, OpenTask, Refresh), plus helper functions (ACTIVE_STATES, ARCHIVE_STATES, normalizeTaskState, effectiveTaskState) |
| `presentation/tasks/DefaultTasksComponent.kt` | Loads tasks via TaskRepository, dispatches intents, filtering/sorting helpers (filteredTasks, availableCourses, availableStatuses, taskComparator) |
| `presentation/tasks/TasksScreen.kt` | Full UI: segment control (Active/Archive with counts), search field, status filter chips, course filter chips, reset button, task list (LazyColumn), loading/error/empty states |

**Files Modified:**

| File | Changes |
|------|---------|
| `presentation/common/TaskCard.kt` | Refactored StatusBadge to be public with (label, color) params; made formatDeadline and isOverdue public; added taskStateBadgeLabel helper |
| `presentation/main/DefaultMainComponent.kt` | Updated createTabChild to pass TaskRepository + onOpenTask callback to DefaultTasksComponent |

**Design details:**
- Active segment: backlog, inProgress, hasSolution, revision, rework, review
- Archive segment: evaluated, failed, rejected
- State normalization: rework→revision, rejected→failed (matching Flutter)
- Virtual "hasSolution" state: inProgress + submitAt != null
- Sorting: evaluated/failed/rejected/review go to bottom; rest by deadline ascending (null last)
- Filter chips are toggleable (tap again to deselect)

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew :shared:ktlintCheck`)

---

### Phase 6: Courses Tab (Days 11–12) ✅

**Files Created:**

| File | Description |
|------|-------------|
| `presentation/courses/detail/CourseDetailComponent.kt` | MVI interface: State (courseId, overview, loading, error, searchQuery, expandedThemeIds), Intent (Search, ToggleTheme, OpenLongread, Back, Refresh), filteredThemes() helper |
| `presentation/courses/detail/DefaultCourseDetailComponent.kt` | Loads CourseOverview, manages theme expansion/search |
| `presentation/courses/detail/CourseDetailScreen.kt` | Full UI: top bar, search, expandable theme cards, longread rows with exercises and deadlines |

**Files Rewritten:**

| File | Changes |
|------|---------|
| `presentation/courses/CoursesComponent.kt` | Full MVI with 3 segments (Courses/Grade Sheet/Record Book), activeCourses()/archivedCourses() helpers |
| `presentation/courses/DefaultCoursesComponent.kt` | Loads courses, performance, gradebook from repositories |
| `presentation/courses/CoursesScreen.kt` | Segment control, course list tiles, archived section, grade sheet tiles (color-coded grades), gradebook by semester with grade rows |

**Files Modified:**

| File | Changes |
|------|---------|
| `presentation/main/MainComponent.kt` | Added CourseDetailComponent, navigateToCoursePerformance(), changed CourseDetailChild to hold component, added CoursePerformanceChild |
| `presentation/main/DefaultMainComponent.kt` | Added PerformanceRepository param, wired repos into DefaultCoursesComponent, creates DefaultCourseDetailComponent |
| `presentation/main/MainScreen.kt` | Renders CourseDetailScreen, added CoursePerformanceChild placeholder |
| `presentation/root/DefaultRootComponent.kt` | Added performanceRepository, passes to DefaultMainComponent |
| `androidMain/di/AndroidKoin.kt` | Added PerformanceRepository resolution |
| `iosMain/MainViewController.kt` | Added PerformanceRepository resolution |

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew :shared:ktlintCheck :androidApp:ktlintCheck`)

---

### Phase 7: Longread/Material Viewer (Days 13–16) ✅

**Files Created:**

| File | Description |
|------|-------------|
| `presentation/longread/LongreadComponent.kt` | MVI interface: State (materials, taskDetails, taskEvents, taskComments, activeTaskId, selectedTaskTab, solutionUrl, commentText, isSubmitting), Intent sealed interface with 11 actions |
| `presentation/longread/DefaultLongreadComponent.kt` | Implementation: loadMaterials, loadTaskDetailsForCodingMaterials, selectTask/startTask/submitSolution/createComment/prolongLateDays/cancelLateDays/downloadFile |
| `presentation/longread/LongreadScreen.kt` | Main layout: LongreadTopBar, MaterialList (LazyColumn), MarkdownCard, FileCard, QuestionsCard, stripHtmlTags(), formatFileSize() |
| `presentation/longread/LongreadTaskSection.kt` | CodingMaterialCard, TaskHeader, TaskManagementSection, StartTaskButton, TabSelector, SolutionTab, ScoreDisplay, LateDaysInfo, canSubmitSolution() |
| `presentation/longread/LongreadTaskInfo.kt` | CommentsTab, CommentCard, InfoTab, TaskInfoSummary, InfoRow, EventCard, eventTypeLabel(), eventTypeColor(), formatDateTime() |

**Files Modified:**

| File | Changes |
|------|---------|
| `presentation/main/MainComponent.kt` | Added `LongreadChild(component: LongreadComponent)` to DetailChild, added `navigateToLongread()`, import |
| `presentation/main/DefaultMainComponent.kt` | Added `contentRepository` param, `Longread` DetailConfig, wired `onOpenLongread` in CourseDetail, creates `DefaultLongreadComponent`, `navigateToLongread()` implementation |
| `presentation/main/MainScreen.kt` | Added `LongreadChild` rendering calling `LongreadScreen`, import |
| `presentation/root/DefaultRootComponent.kt` | Added `contentRepository` param, passes to `DefaultMainComponent` |
| `androidMain/di/AndroidKoin.kt` | Resolves `ContentRepository` from Koin, passes to `DefaultRootComponent` |
| `iosMain/MainViewController.kt` | Resolves `ContentRepository` from Koin, passes to `DefaultRootComponent` |

**Material types handled:**
- `markdown` — HTML content rendered via `stripHtmlTags()` (basic text extraction; richer rendering deferred to Phase 11)
- `file` — Download card with filename, file size, download button
- `coding` — Full task management: start task, submit solution URL, view score, late days info, 3-tab layout (Solution/Comments/Info)
- `questions` — Placeholder card (not supported on mobile, per Flutter reference)

**Task management features:**
- Start task, submit solution URL
- Comments section with create new comment
- Events timeline with type-labeled cards
- Late days: prolong and cancel actions
- Score display with skill level

**Design decisions:**
- No Compose Multiplatform HTML/markdown renderer available — using basic `stripHtmlTags()` for Phase 7
- `String.format()` used in `formatFileSize()` — may need KMP-compatible alternative for iOS
- Task tabs: Solution / Comments / Info (matching Flutter)

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew :shared:ktlintCheck :androidApp:ktlintCheck`)

### Phase 8: Profile & Notifications (Days 17–18) ✅

**Files Created:**

| File | Description |
|------|-------------|
| `presentation/profile/ProfileComponent.kt` | MVI interface: State (profile, lmsProfile, avatarBytes, isLoading, error, isDeletingAvatar), computed initials/educationLevelLabel/otherEmails, Intent (Back, Refresh, DeleteAvatar, Logout), custom equals/hashCode for ByteArray |
| `presentation/profile/DefaultProfileComponent.kt` | Implementation: loads profile+lmsProfile+avatar, avatar deletion, logout delegation |
| `presentation/profile/ProfileScreen.kt` | UI: top bar with back+logout, avatar section (80dp circle, initials, delete button), name+course+education, info card (login, telegram, university email, masked other emails with type badges, masked phones with type badges), maskEmail()/maskPhone() helpers |
| `presentation/notifications/NotificationsComponent.kt` | MVI interface: State (educationNotifications, otherNotifications, isLoading, error, selectedTab), computed currentNotifications, Intent (Back, Refresh, SelectTab, OpenLink) |
| `presentation/notifications/DefaultNotificationsComponent.kt` | Implementation: loads both categories in parallel, sorts by date desc, longread deep-link detection via regex |
| `presentation/notifications/NotificationsScreen.kt` | UI: top bar, tab selector (Учеба/Другое chips), notification cards list with category icons (emoji), title, date (dd.MM.yyyy HH:mm), description, optional deep-link, formatNotificationDate() helper |

**Files Modified:**

| File | Changes |
|------|---------|
| `presentation/main/MainComponent.kt` | Changed `ProfileChild` and `NotificationsChild` from `data object` to `data class` wrapping `ProfileComponent`/`NotificationsComponent`, added imports |
| `presentation/main/DefaultMainComponent.kt` | Added `notificationRepository` param, creates `DefaultProfileComponent` (with `onLogout` delegation) and `DefaultNotificationsComponent`, added imports |
| `presentation/main/MainScreen.kt` | Replaced `DetailPlaceholder` for Profile with `ProfileScreen`, for Notifications with `NotificationsScreen`, added imports |
| `presentation/root/DefaultRootComponent.kt` | Added `notificationRepository` param, passes to `DefaultMainComponent`, added import |
| `androidMain/di/AndroidKoin.kt` | Resolves `NotificationRepository` from Koin, passes to `DefaultRootComponent`, added import |
| `iosMain/MainViewController.kt` | Resolves `NotificationRepository` from Koin, passes to `DefaultRootComponent`, added import |

**Profile features:**
- Full name, course + education level (translated: Bachelor→Бакалавриат, Master→Магистратура, Specialist→Специалитет)
- Avatar with initials placeholder (green on green-tinted circle), delete button
- Info card: login (timeLogin), telegram (@username), university email, other emails (masked: first 3 chars + domain), phones (masked: first 3 + last 2 chars), type badges
- Logout button (no confirmation dialog, matching Flutter)

**Notifications features:**
- Two tabs: "Учеба" (category=1) and "Другое" (category=2)
- Category-specific emoji icons: ServiceDesk→🎧, News→📰, Education→📚, default→🔔
- Date formatted as "dd.MM.yyyy HH:mm"
- Deep-link detection for longread URLs (regex: `my.centraluniversity.ru/.../longreads/(\d+)`)
- Tappable link with underline styling

**Design decisions:**
- Avatar upload not implemented (requires platform-specific image picker — deferred)
- Calendar integration not ported (not core LMS feature — deferred)
- Notification deep-linking triggers `onOpenLongread` callback (currently not wired through to navigation; full resolution requires courseId/themeId lookup — deferred)

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew :shared:ktlintCheck :androidApp:ktlintCheck`)

---

### Phase 9: Performance & Gradebook (Days 19–20) ✅

**Files Created:**

| File | Description |
|------|-------------|
| `presentation/performance/CoursePerformanceComponent.kt` | MVI interface: State (courseId, courseName, totalGrade, exercises, activitySummaries, selectedTab, activityFilter), Intent (Back, Refresh, SelectTab, FilterByActivity), plus `ExerciseWithScore` and `ActivitySummary` data classes |
| `presentation/performance/DefaultCoursePerformanceComponent.kt` | Implementation: loads exercises + performance, joins with `joinExercisesWithScores()`, computes `buildActivitySummaries()` |
| `presentation/performance/CoursePerformanceScreen.kt` | Main screen: TopBar, TotalGradeCard, TabSelector, PerformanceContent, loading/error states, helper functions (formatScore, gradeColor, gradeDescription, scoreRatioColor) |
| `presentation/performance/ScoresTabContent.kt` | Scores tab: activity filter chips, exercise tiles with score badges |
| `presentation/performance/PerformanceTabContent.kt` | Performance tab: weighted activity summaries table (header, summary rows, total row) |

**Files Modified:**

| File | Changes |
|------|---------|
| `presentation/main/MainComponent.kt` | `CoursePerformanceChild` wraps `CoursePerformanceComponent`; `navigateToCoursePerformance` takes `courseId`, `courseName`, `totalGrade`; added import |
| `presentation/main/DefaultMainComponent.kt` | Creates `DefaultCoursePerformanceComponent` in `createDetailChild`; `DetailConfig.CoursePerformance` has 3 fields; updated callback signature; added import |
| `presentation/main/MainScreen.kt` | Renders `CoursePerformanceScreen` instead of `DetailPlaceholder`; added import |
| `presentation/courses/CoursesComponent.kt` | `OpenCoursePerformance` intent now has `courseId`, `courseName`, `totalGrade` |
| `presentation/courses/DefaultCoursesComponent.kt` | `onOpenCoursePerformance` callback signature updated to 3 params |
| `presentation/courses/CoursesScreen.kt` | Passes `courseName` and `totalGrade` in `OpenCoursePerformance` intent |

**Design details:**
- Two tabs: "Набранные баллы" (Scores) and "Успеваемость" (Performance)
- TotalGradeCard: color-coded grade box (green≥8, yellow≥6, orange≥4, red<4) with description (Отлично/Хорошо/Удовлетворительно/Неудовлетворительно)
- Scores tab: activity filter chips (horizontal scroll), exercise tiles showing theme/name/activity/score badge
- Performance tab: table with activity name, count, avg score, x, weight, =, total contribution, plus grand total row
- `ExerciseWithScore` joins `CourseExercise` with `TaskScore` by `exercise.id == task.exerciseId`
- `ActivitySummary` computed locally: group tasks by activity, compute avg score, total contribution = avgScore × weight
- File split: main screen (288 lines), scores tab (186 lines), performance tab (171 lines) — all under 400-line limit
- No new DI registrations needed — `PerformanceRepository` was already wired in Phase 6

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew :shared:ktlintCheck :androidApp:ktlintCheck`)

---

## Remaining Phases

### Phase 10: Files Tab (Day 21) ✅

**Files Created:**

| File | Description |
|------|-------------|
| `data/local/DownloadedFileInfo.kt` | Data class (name, path, sizeBytes, lastModifiedMillis) + computed `extension`, `formatSizeBytes()` helper |
| `data/local/FileStorage.kt` | Interface: `listFiles()`, `deleteFile()`, `deleteAllFiles()`, `saveFile()`, `fileExists()` |
| `domain/repository/FileRepository.kt` | Interface: `listDownloadedFiles()`, `deleteFile()`, `deleteAllFiles()`, `downloadAndSave()`, `fileExists()` |
| `data/repository/FileRepositoryImpl.kt` | Impl using `FileStorage` + `HttpClient` for downloads |
| `androidMain/data/local/AndroidFileStorage.kt` | Android impl: `java.io.File` with `context.filesDir/downloads` |
| `iosMain/data/local/IosFileStorage.kt` | iOS impl: `NSFileManager` + `NSDocumentDirectory/downloads` |

**Files Rewritten (from placeholder to full implementation):**

| File | Changes |
|------|---------|
| `presentation/files/FilesComponent.kt` | Full MVI: State (files, isLoading, error, selectedFiles, computed isSelecting/totalSizeBytes), Intent (Refresh, DeleteFile, DeleteAll, ToggleSelect, DeleteSelected, ClearSelection, OpenFile) |
| `presentation/files/DefaultFilesComponent.kt` | Full impl with FileRepository, lifecycle-aware loading, selection logic |
| `presentation/files/FilesScreen.kt` | Full Compose UI: file list with extension badges (text-based, no Icons), selection mode, delete actions, empty/loading/error states, manual date formatting (KMP-compatible) |

**Files Modified (DI wiring + download integration):**

| File | Changes |
|------|---------|
| `di/Koin.kt` | Added `FileRepository` binding in `repositoryModule` |
| `androidMain/di/AndroidKoin.kt` | Added `AndroidFileStorage` as `FileStorage`, `fileRepository` param to `createRootComponent()` |
| `iosMain/di/IosKoin.kt` | Added `IosFileStorage` as `FileStorage` |
| `iosMain/MainViewController.kt` | Added `fileRepository` param to `createRootComponent()` |
| `presentation/root/DefaultRootComponent.kt` | Added `fileRepository` param, passes to `DefaultMainComponent` |
| `presentation/main/DefaultMainComponent.kt` | Added `fileRepository` param, wired `onDownloadReady` to `fileRepository.downloadAndSave(url, filename)`, passes `fileRepository` to `DefaultFilesComponent` |
| `presentation/longread/DefaultLongreadComponent.kt` | Changed `onDownloadReady` from `(String) -> Unit` to `(String, String) -> Unit` (url + filename), added `buildLocalFilename()` helper |

**Design decisions:**
- No material-icons dependency — all icons use text alternatives ("×" for delete, "FILE" for file icon, extension text badges)
- `Dispatchers.Default` used instead of `Dispatchers.IO` for KMP compatibility
- Manual epoch-to-date conversion for `formatDate()` to avoid JVM-only APIs
- File download: gets pre-signed URL via API, then HTTP streams to local file via `readRawBytes()`
- Local filename format: `{base}_{version}.{ext}` with unsafe chars replaced by `_`

**Verification:**
- ✅ Build passes (`./gradlew :androidApp:assembleDebug`)
- ✅ ktlint passes (`./gradlew ktlintCheck`)

---

### Code Quality Refactoring (across multiple sessions) ✅

**Summary:** Production-quality code review and refactoring pass. Replaced lazy shortcuts (renamed exceptions to `ignored`, blanket `@Suppress`) with real fixes.

**Dependencies added:**
- `essenty-lifecycle-coroutines` v2.5.0 — auto-cancelling coroutine scopes from `ComponentContext`
- `kotlin-logging` (io.github.oshai) v8.0.01 — structured logging in catch blocks

**Refactoring completed:**

| Category | Changes |
|----------|---------|
| **Coroutine scopes** | Replaced manual `CoroutineScope(Dispatchers.Main + SupervisorJob())` + `onDestroy` cleanup in all 12 components with `coroutineScope()` from essenty-lifecycle-coroutines |
| **Logging** | Added `logger.error(e) { "..." }` to all catch blocks across ApiService.kt (26 blocks), FileRepositoryImpl.kt, DefaultFilesComponent.kt, DefaultHomeComponent.kt, AndroidFileStorage.kt, IosFileStorage.kt, FormatUtils.kt |
| **Cookie dedup** | Created `CookieAwareRepository` open base class with `withCookie`/`withCookieOrFalse` helpers. Refactored all 6 cookie-based repositories to extend it, eliminating duplicate `cookie()` methods |
| **Formatting dedup** | Created `FormatUtils.kt` consolidating 6 duplicate date/time/size formatting functions from 6 different files: `formatDeadline`, `formatDateTime`, `formatDateTimeFull`, `formatDeadlineShort`, `formatEpochDate`, `formatSizeBytes` |

**Files created:**
- `data/repository/CookieAwareRepository.kt` — base class for cookie-based repos
- `presentation/common/FormatUtils.kt` — consolidated formatting utilities

**Files modified (repositories):**
- `ProfileRepositoryImpl.kt`, `TaskRepositoryImpl.kt`, `CourseRepositoryImpl.kt`, `ContentRepositoryImpl.kt`, `NotificationRepositoryImpl.kt`, `PerformanceRepositoryImpl.kt` — all rewritten to extend CookieAwareRepository

**Files modified (formatting consumers):**
- `TaskCard.kt`, `LongreadTaskInfo.kt`, `NotificationsScreen.kt`, `CourseDetailScreen.kt`, `FilesScreen.kt`, `LongreadScreen.kt`, `DownloadedFileInfo.kt` — removed local formatting functions, import from FormatUtils

**ktlint rule discovered:**
- "Newline expected before expression body" — for functions with `= expr {`, the expression must start on a new line after `=`
- "Class body should not start with blank line" — no blank line after opening `{` of class body

**Verification:**
- ✅ `./gradlew ktlintCheck` — passes clean
- ✅ `./gradlew detektMainAndroid` — passes (6 "compiler errors" are false positives from cross-module resolution)
- ✅ `./gradlew :androidApp:assembleDebug` — passes clean

---

### Next steps

#### Deferred items
- [ ] Move model classes from `data/model/` to a standalone `model/` package (47 files affected, separate PR)
- [ ] Rename `*Response` types to non-API-specific names (part of model move)

#### Flutter comparison
- [ ] Compare KMP implementation with Flutter app (`/home/olowo/StudioProjects/lms-mobile`, indexed on deepwiki: `cu-3rd-party/lms-mobile`) for missing features/behavior differences

### Phase 11: Polish & Testing (Days 22–25) ✅
- ✅ Pull-to-refresh on all data screens (Home, Tasks, Courses, Notifications, Performance, Files)
- ✅ Dark theme refinement based on Flutter UI reference (fixed KMP string compatibility issues)
- ✅ Unit tests for `FormatUtils` and `Theme` logic (fixed `formatEpochDate` bug)

### Deferred Features Implementation ✅
- ✅ **Course Reordering**: Local persistence via DataStore, edit mode with Up/Down arrows in Courses tab
- ✅ **File Rename Templates**: DataStore storage and repository implemented; UI in File manager settings
- ✅ **Schedule / Calendar**: iCal integration with Yandex Calendar, daily view with date navigation on Home screen, cross-platform date handling via kotlinx-datetime

---

### Phase 12: kotlinx-datetime Migration ✅
- ✅ Refactored `FormatUtils.kt` to use `kotlinx-datetime` for all date/time formatting and parsing.
- ✅ Created `DateTimeProvider` in `commonMain` for consistent "today" and date-to-millis logic.
- ✅ Replaced manual epoch-to-date calculations with reliable library calls.
- ✅ Integrated `kotlinx-datetime` into `CalendarRepository` for iCal event filtering.

---

## Current Project State

```
CuMobile/
├── androidApp/              # Android shell
├── iosApp/                  # iOS shell (Swift)
├── shared/
│   └── src/
│       ├── commonMain/kotlin/io/github/kroune/cumobile/
│       │   ├── data/
│       │   │   ├── local/        # DataStore, AuthLocalDataSource, FileStorage, DownloadedFileInfo
│       │   │   ├── model/        # 13 model files, ~37 @Serializable DTOs
│       │   │   ├── network/      # HttpClientFactory, ApiService (25 endpoints)
│       │   │   └── repository/   # 8 repository impls (incl. CookieAwareRepository base class)
│       │   ├── di/               # Koin modules (8 repository bindings)
│       │   ├── domain/
│       │   │   └── repository/   # 8 repository interfaces (incl. FileRepository)
│       │   ├── presentation/
│       │   │   ├── auth/         # LoginComponent, LoginScreen
│       │   │   │   └── webview/  # WebViewLoginComponent, PlatformWebView
│       │   │   ├── common/       # Theme, TopBar, TaskCard, CourseCard, FormatUtils
│       │   │   ├── courses/      # CoursesComponent, CourseDetailComponent
│       │   │   ├── files/        # FilesComponent, FilesScreen (full file manager)
│       │   │   ├── home/         # HomeComponent, HomeScreen (deadlines + courses)
│       │   │   ├── longread/     # LongreadComponent, LongreadScreen, TaskSection, TaskInfo
│       │   │   ├── main/         # MainComponent (ChildPages + ChildStack), MainScreen
│       │   │   ├── performance/  # CoursePerformanceComponent, Screen, ScoresTab, PerformanceTab
│       │   │   ├── root/         # RootComponent, RootScreen
│       │   │   └── tasks/        # TasksComponent, TasksScreen, TaskListItem (full MVI)
│       │   └── Main.kt
│       ├── androidMain/          # AndroidKoin, DataStorePath, PlatformWebView, AndroidFileStorage
│       └── iosMain/              # IosKoin, DataStorePath, PlatformWebView, IosFileStorage, MainViewController
└── memorybank/
    ├── plan.md              # Full implementation plan
    ├── deferred-features.md
    └── progress.md         # This file
```
