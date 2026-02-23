# CuMobile KMP — Implementation Plan

## 1. Project Overview

**Goal**: Rewrite the Flutter LMS app ([cu-3rd-party/lms-mobile](https://github.com/cu-3rd-party/lms-mobile)) for Central University as a Kotlin Multiplatform (KMP) app supporting Android and iOS, with maximum shared code.

**Reference**: Flutter app at `/home/olowo/StudioProjects/lms-mobile`, indexed on DeepWiki as `cu-3rd-party/lms-mobile`.

**Base URL**: `https://my.centraluniversity.ru/api`

**Package**: `io.github.kroune.cumobile`

---

## 2. Current Project State

- KMP template scaffold with `shared` and `androidApp` modules
- Compose Multiplatform 1.11.0-alpha02, Kotlin 2.3.10
- Dependencies already declared: Ktor, kotlinx-serialization, Decompose, Koin, Compose, DataStore
- iOS wiring exists in Swift but references missing `RootComponent`
- Only code: `Text("Тест")` composable, empty Koin module
- **Critical bugs to fix first**:
  - `AndroidApplication` not declared in `AndroidManifest.xml` (`android:name` missing)
  - iOS Swift code references non-existent `RootComponent` class
  - Template names (`kmptemplate`) still in `settings.gradle.kts` and `androidApp/build.gradle.kts`

---

## 3. Technology Stack

| Concern               | Library                          |
|------------------------|----------------------------------|
| UI                     | Compose Multiplatform            |
| Navigation             | Decompose                        |
| DI                     | Koin                             |
| HTTP                   | Ktor                             |
| Serialization          | kotlinx-serialization            |
| Async                  | kotlinx-coroutines               |
| Local storage          | DataStore Preferences            |
| Architecture           | MVI + Clean Architecture         |
| Linting                | ktlint                           |

---

## 4. Architecture — MVI + Clean Architecture

### Layer diagram

```
┌───────────────────────────────────────────────┐
│                 Presentation                   │
│  Decompose Components (MVI: State/Intent/Label)│
│  Compose Screens & Widgets                     │
├───────────────────────────────────────────────┤
│                   Domain                       │
│  Use Cases (business logic)                    │
│  Repository Interfaces                         │
├───────────────────────────────────────────────┤
│                    Data                        │
│  Repository Implementations                    │
│  ApiService (Ktor)                             │
│  Local Storage (DataStore Preferences)         │
│  Data Models (@Serializable)                   │
└───────────────────────────────────────────────┘
```

### MVI Pattern per screen

Each Decompose component follows MVI:
- **Model (State)**: Immutable data class exposed as `Value<State>`
- **View**: Compose `@Composable` observing state via `subscribeAsState()`
- **Intent**: Sealed interface of user actions, dispatched to component

```kotlin
// Example pattern:
interface TasksComponent {
    val state: Value<State>
    fun onIntent(intent: Intent)

    data class State(
        val tasks: List<StudentTask> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val filter: TaskFilter = TaskFilter()
    )

    sealed interface Intent {
        data class FilterByStatus(val status: TaskStatus) : Intent
        data class Search(val query: String) : Intent
        data class OpenTask(val taskId: Int) : Intent
    }
}
```

### Directory structure

```
shared/src/commonMain/kotlin/io/github/kroune/cumobile/
├── data/
│   ├── model/           # @Serializable data classes (DTOs)
│   ├── network/         # Ktor HttpClient setup, ApiService
│   ├── local/           # DataStore Preferences wrapper
│   └── repository/      # Repository implementations
├── domain/
│   ├── model/           # Domain models (if differ from DTOs)
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Use case classes
├── presentation/
│   ├── root/            # RootComponent (auth routing)
│   ├── auth/            # Login components & screens
│   ├── main/            # MainComponent (bottom nav)
│   ├── home/            # Home tab components & screens
│   ├── tasks/           # Tasks tab components & screens
│   ├── courses/         # Courses tab components & screens
│   ├── files/           # Files tab components & screens
│   ├── longread/        # Longread viewer components & screens
│   ├── profile/         # Profile components & screens
│   ├── notifications/   # Notifications components & screens
│   ├── performance/     # Performance components & screens
│   └── common/          # Shared widgets, theme, colors
├── di/
│   └── Modules.kt       # Koin module definitions
└── util/                # Extensions, helpers, constants

shared/src/androidMain/  # Android Ktor engine, platform utils
shared/src/iosMain/      # iOS Ktor engine, platform utils

androidApp/              # Thin Android shell (MainActivity, Application)
iosApp/                  # Thin iOS shell (Swift wrappers)
```

---

## 5. Implementation Phases

### Phase 0: Project Setup & Fixes (Day 1)

1. Fix `AndroidManifest.xml` — add `android:name=".AndroidApplication"` to `<application>`
2. Rename template references (`kmptemplate` → `cumobile`) in:
   - `settings.gradle.kts` (rootProject.name)
   - `androidApp/build.gradle.kts` (applicationId, namespace)
   - `shared/build.gradle.kts` (namespace)
3. Add DataStore dependencies to `shared/build.gradle.kts` commonMain
4. Create full directory structure under `shared/src/commonMain/`
5. Set up Ktor `HttpClient` with kotlinx-serialization content negotiation
6. Set up Decompose `RootComponent` with empty navigation graph
7. Configure Koin modules skeleton
8. Verify the app builds and runs on Android emulator

### Phase 1: Authentication (Days 2–3)

**Screens**: LoginScreen, WebViewLoginScreen

**Data layer**:
- `AuthLocalDataSource` using DataStore Preferences for cookie storage
  - Key: `"bff_cookie"`, value: the `bff.cookie` string
  - DataStore is KMP-compatible, no need for expect/actual
- `AuthRepository` interface in domain, implementation in data

**Network layer**:
- Configure Ktor `HttpClient` to attach `Cookie: bff.cookie=<value>` header on all requests
- Handle 401 responses → clear cookie, emit auth-required event via `SharedFlow`
- Handle `Set-Cookie` response header for transparent cookie refresh

**WebView login** (platform-specific — the only expect/actual UI):
- Android: `android.webkit.WebView` composable loading `https://my.centraluniversity.ru`
- iOS: `WKWebView` via Compose UIKit interop
- Intercept `bff.cookie` from cookies after `/api/account/signin/callback`
- On success: store cookie via `AuthRepository`, validate by calling `GET /hub/students/me`

**MVI components**:
- `RootComponent`: manages auth state (`LoginChild` vs `MainChild`)
- `LoginComponent`: Intent = `LoginClicked`; navigates to WebView
- `WebViewLoginComponent`: Intent = `CookieCaptured(cookie)`; validates + navigates

### Phase 2: Data Models (Day 3)

All models as `@Serializable` data classes in `data/model/`:

1. `StudentProfile` — id, firstName, lastName, middleName, birthdate, telegram, emails, phones, course, gender, educationLevel, enrollmentPhase
2. `StudentLmsProfile` — id, firstName, lastName, universityEmail, lateDaysBalance, studyStartYear, studyLevel
3. `Course` — id, name, state, category, categoryCover, isArchived
4. `CourseOverview` — id, name, isArchived, themes: `List<CourseTheme>`
5. `CourseTheme` — id, name, order, state, longreads: `List<Longread>`
6. `Longread` — id, type, name, state, exercises: `List<ThemeExercise>`
7. `ThemeExercise` — id, name, maxScore, deadline, activity: `ExerciseActivity?`
8. `ExerciseActivity` — id, name, weight
9. `StudentTask` — id, state, score, deadline, submitAt, exercise: `TaskExercise`, course: `TaskCourse`, isLateDaysEnabled, lateDays
10. `TaskExercise` — id, name, type, maxScore, deadline
11. `TaskCourse` — id, name, isArchived
12. `TaskDetails` — id, score, extraScore, maxScore, scoreSkillLevel, state, solutionUrl, solutionAttachments, submitAt, isLateDaysEnabled, lateDays, lateDaysBalance, deadline
13. `TaskEvent` — id, occurredOn, type, actorEmail, actorName, content: `TaskEventContent`
14. `TaskEventContent` — state, score, estimation, attachments, solutionUrl, reviewerName, reviewersNames, lateDaysValue, etc.
15. `TaskComment` — id, content, authorName, authorEmail, createdAt, attachments
16. `LongreadMaterial` — id, discriminator, viewContent, filename, version, length, attachments, estimation, taskId
17. `MaterialAttachment` — name, filename, mediaType, length, version
18. `MaterialEstimation` — deadline, maxScore, activityName, activityWeight
19. `NotificationItem` — id, title, description, category, createdAt, link
20. `StudentPerformanceCourse` — id, name, total
21. `GradebookResponse` — semesters: `List<GradebookSemester>`
22. `GradebookSemester` — year, semesterNumber, grades: `List<GradebookGrade>`
23. `GradebookGrade` — subject, grade, normalizedGrade, assessmentType, subjectType

### Phase 3: API Service & Repositories (Days 4–5)

**ApiService** class in `data/network/`:

| Method | Endpoint | Returns |
|--------|----------|---------|
| `fetchProfile()` | `GET /hub/students/me` | `StudentProfile?` |
| `fetchAvatar()` | `GET /hub/avatars/me` | `ByteArray?` |
| `uploadAvatar(bytes)` | `POST /hub/avatars/me` | `Boolean` |
| `deleteAvatar()` | `DELETE /hub/avatars/me` | `Boolean` |
| `fetchLmsProfile()` | `GET /micro-lms/students/me` | `StudentLmsProfile?` |
| `fetchTasks(states)` | `GET /micro-lms/tasks/student?state=...` | `List<StudentTask>` |
| `fetchTaskDetails(id)` | `GET /micro-lms/tasks/{id}` | `TaskDetails?` |
| `fetchTaskEvents(id)` | `GET /micro-lms/tasks/{id}/events` | `List<TaskEvent>` |
| `fetchTaskComments(id)` | `GET /micro-lms/tasks/{id}/comments` | `List<TaskComment>` |
| `startTask(id)` | `PUT /micro-lms/tasks/{id}/start` | `Boolean` |
| `submitTask(id)` | `PUT /micro-lms/tasks/{id}/submit` | `Boolean` |
| `prolongLateDays(id)` | `PUT /micro-lms/tasks/{id}/late-days-prolong` | `Boolean` |
| `cancelLateDays(id)` | `PUT /micro-lms/tasks/{id}/late-days-cancel` | `Boolean` |
| `createComment(body)` | `POST /micro-lms/comments` | `Boolean` |
| `fetchCourses()` | `GET /micro-lms/courses/student?limit=10000` | `List<Course>` |
| `fetchCourseOverview(id)` | `GET /micro-lms/courses/{id}/overview` | `CourseOverview?` |
| `fetchCourseExercises(id)` | `GET /micro-lms/courses/{id}/exercises` | `List<CourseExercise>` |
| `fetchCoursePerformance(id)` | `GET /micro-lms/courses/{id}/student-performance` | Performance response |
| `fetchLongreadMaterials(id)` | `GET /micro-lms/longreads/{id}/materials?limit=10000` | `List<LongreadMaterial>` |
| `fetchMaterial(id)` | `GET /micro-lms/materials/{id}` | `LongreadMaterial?` |
| `getDownloadLink(filename, version)` | `GET /micro-lms/content/download-link?...` | `String?` |
| `getUploadLink(dir, filename, type)` | `GET /micro-lms/content/upload-link?...` | `String?` |
| `fetchPerformance()` | `GET /micro-lms/performance/student` | Performance response |
| `fetchGradebook()` | `GET /micro-lms/gradebook` | `GradebookResponse?` |
| `fetchNotifications(category)` | `POST /notification-hub/notifications/in-app` | `List<NotificationItem>` |

**Repositories** (interface in `domain/repository/`, impl in `data/repository/`):
- `AuthRepository` — cookie CRUD, auth state flow
- `ProfileRepository` — profile, avatar, LMS profile
- `TaskRepository` — tasks CRUD, task details, events, comments, submissions
- `CourseRepository` — courses, overview, exercises
- `ContentRepository` — longreads, materials, file links
- `NotificationRepository` — notifications by category
- `PerformanceRepository` — performance, gradebook

### Phase 4: Home Screen & Navigation (Days 6–8)

**Decompose navigation graph**:
```
RootComponent
├── LoginComponent
│   └── WebViewLoginComponent
└── MainComponent (BottomNav with ChildStack per tab)
    ├── HomeComponent (Tab 0 — "Главная")
    ├── TasksComponent (Tab 1 — "Задания")
    ├── CoursesComponent (Tab 2 — "Обучение")
    └── FilesComponent (Tab 3 — "Файлы")
```

**Detail navigation** (child stack pushed from tabs):
```
MainComponent detail stack:
├── CourseDetailComponent(courseId)
├── LongreadComponent(longreadId, courseId, themeId, ...)
├── ProfileComponent
├── NotificationsComponent
└── CoursePerformanceComponent(courseId)
```

**Screens**:
1. `HomeScreen` — Deadlines section (upcoming tasks by deadline), Courses section (top courses grid)
2. Bottom navigation — 4 tabs with Material3 `NavigationBar`
3. Top bar — Avatar button → Profile, Bell icon → Notifications

### Phase 5: Tasks Tab (Days 9–10)

MVI components:
- `TasksComponent.State` — tasks list, loading, error, filter (status, course, query)
- `TasksComponent.Intent` — FilterByStatus, FilterByCourse, Search, OpenTask, Refresh

Screen:
- Task cards with status-colored indicators
- Search bar + filter chips (status, course)
- Pull-to-refresh
- Tap task → navigate to LongreadComponent
- Task states: created, inProgress, submitted, rework, scored, cancelled

### Phase 6: Courses Tab (Days 11–12)

MVI components:
- `CoursesComponent.State` — courses, gradebook, performance, archived toggle
- `CoursesComponent.Intent` — OpenCourse, OpenPerformance, ToggleArchived, Refresh

Screens:
- `CoursesScreen` — course cards with category colors, archived section
- `CourseDetailScreen` — themes list, expandable longreads with exercises, search
- Tap longread/exercise → navigate to LongreadComponent

### Phase 7: Longread/Material Viewer (Days 13–16)

**Most complex screen** — core learning content viewer.

MVI components:
- `LongreadComponent.State` — materials, taskDetails, events, comments, loading states
- `LongreadComponent.Intent` — StartTask, SubmitTask, AddComment, DownloadFile, ProlongLateDays, CancelLateDays, Refresh

Screen sections:
- Material list with type-specific rendering (markdown, file, coding, questions)
- HTML/Markdown content display (evaluate Compose markdown lib vs platform WebView)
- File download with progress tracking
- Task management (start, submit solution URL + attachments, late days dialog)
- Comments section with attachment support
- Task events timeline

### Phase 8: Profile & Notifications (Days 17–18)

**Profile**:
- `ProfileComponent.State` — profile, avatar, lmsProfile
- `ProfileComponent.Intent` — UploadAvatar, DeleteAvatar, Logout
- `ProfileScreen` — student info, avatar management, logout

**Notifications**:
- `NotificationsComponent.State` — educationNotifications, otherNotifications, selectedTab
- `NotificationsComponent.Intent` — SwitchTab, OpenNotification, Refresh
- `NotificationsScreen` — two tabs (Education/Other), notification cards, deep-linking

### Phase 9: Performance & Gradebook (Days 19–20)

- `CoursePerformanceComponent` — course exercises with scores, activity summaries
- `CoursePerformanceScreen` — scores tab (exercises with scores) + performance tab (weighted averages)
- Gradebook display integrated into CoursesTab

### Phase 10: Files Tab (Day 21)

- `FilesComponent` — manages locally downloaded files
- `FilesScreen` — file list with open/share/delete actions
- File download tracking integrated from LongreadScreen

### Phase 11: Polish & Testing (Days 22–25)

1. Dark theme: background `#121212`, accent `#00E676`, font: Ubuntu
2. Loading states (shimmer/skeleton), error states (retry), empty states
3. Pull-to-refresh on all data screens
4. Edge-to-edge display, status bar coloring
5. Unit tests for use cases and repositories
6. Integration tests for ApiService (mock HTTP engine)
7. UI testing on Android emulator
8. Build verification on iOS (if Mac available)

---

## 6. Key Design Decisions

1. **Local storage**: DataStore Preferences (KMP-compatible) for cookie, filter settings, course order. No expect/actual needed for storage.

2. **WebView for auth**: The ONLY significant platform-specific UI. Use expect/actual pattern for `PlatformWebView` composable. Android = `android.webkit.WebView`, iOS = `WKWebView` via UIKit interop.

3. **Content rendering**: Markdown/HTML in longreads — evaluate Compose-native markdown library first. Fall back to platform WebView if quality insufficient.

4. **MVI state management**: Decompose components hold `MutableValue<State>`, expose `Value<State>`. Compose screens observe via `subscribeAsState()`. Intents dispatched via `fun onIntent(intent: Intent)`.

5. **Clean Architecture boundaries**: Domain layer has NO dependencies on data or presentation. Repository interfaces in domain, implementations in data. Use cases orchestrate repository calls.

6. **File downloads**: Ktor streaming with `onDownload` progress callback. Store in platform app directories (via `expect/actual` for path resolution only).

---

## 7. Estimated File Count

| Category              | Estimated files |
|------------------------|----------------|
| Data models (DTOs)     | ~15            |
| Network / ApiService   | ~3             |
| Local data sources     | ~2             |
| Repository interfaces  | ~7             |
| Repository impls       | ~7             |
| Use cases              | ~10            |
| Decompose components   | ~14            |
| Compose screens        | ~12            |
| Compose widgets        | ~15            |
| DI modules             | ~3             |
| Platform-specific      | ~4             |
| Utilities / theme      | ~5             |
| **Total**              | **~97**        |

---

## 8. Risk Assessment

| Risk | Mitigation |
|------|------------|
| WebView cookie interception differs per platform | Test on real devices early; use well-documented platform APIs |
| Compose HTML/Markdown rendering quality | Evaluate libraries; fall back to platform WebView |
| API response format mismatch with models | Test against real API during Phase 3; use `ignoreUnknownKeys = true` |
| iOS build issues (no Mac for testing) | Design for KMP from start; test iOS in CI or on teammate's Mac |
| Large longread page complexity | Break into smaller composables; extract task management into sub-component |
| DataStore multiplatform maturity | DataStore 1.2.0 supports KMP; fallback to expect/actual if issues arise |

---

## 9. References

- Flutter reference app: `/home/olowo/StudioProjects/lms-mobile`
- DeepWiki: `cu-3rd-party/lms-mobile`
- API base: `https://my.centraluniversity.ru/api`
- Deferred features: `memorybank/deferred-features.md`
