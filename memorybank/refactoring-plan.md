# CuMobile — Code Quality Refactoring Plan

## Created: 2026-02-23

Full audit of 50+ source files. Issues organized by priority with specific file locations and code snippets.

---

## Table of Contents

1. [Critical: Bugs & Runtime Risks](#1-critical-bugs--runtime-risks)
2. [High: Architectural & Structural Issues](#2-high-architectural--structural-issues)
3. [Medium: Code Duplication & DRY Violations](#3-medium-code-duplication--dry-violations)
4. [Medium: Access Modifier Corrections](#4-medium-access-modifier-corrections)
5. [Low: Magic Strings/Numbers & Naming](#5-low-magic-stringsnumbers--naming)
6. [Detekt Findings (9 issues)](#6-detekt-findings)
7. [Execution Order](#7-execution-order)

---

## 1. Critical: Bugs & Runtime Risks

### 1.1 `catch(Exception)` swallows `CancellationException` — breaks structured concurrency

**Affected files:**
- `data/network/ApiService.kt` — 26 catch blocks (lines 73, 88, 103, 119, 131, 144, 166, 182, 197, 212, 227, 257, 280, 299, 336, 350, 365, 383, 398, 424, 449, 486, 500, 518, 534, 547)
- `data/repository/FileRepositoryImpl.kt` — 1 catch block (line 57)

**Problem:** In Kotlin coroutines, `CancellationException` must propagate for structured concurrency. Catching it means a cancelled coroutine will not actually cancel — it silently returns null/false instead.

**Current code pattern (ApiService.kt):**
```kotlin
} catch (e: Exception) {
    logger.error(e) { "Failed to ..." }
    null
}
```

**Fix:** Add `ensureActive()` or rethrow `CancellationException`:
```kotlin
} catch (e: Exception) {
    coroutineContext.ensureActive()
    logger.error(e) { "Failed to ..." }
    null
}
```
Or use a helper that handles this automatically (see item 2.2).

---

### 1.2 Non-200 HTTP responses silently swallowed (no logging)

**Affected:** `ApiService.kt` — all 20+ endpoint methods

**Problem:** When server returns 4xx/5xx, methods return `null`/`false` with zero logging. A `403 Forbidden` or `500 Internal Server Error` is indistinguishable from a successful empty response.

**Current pattern:**
```kotlin
if (response.status == HttpStatusCode.OK) response.body() else null
```

**Fix:** Log non-OK status:
```kotlin
if (response.status == HttpStatusCode.OK) {
    response.body()
} else {
    logger.warn { "fetchProfile returned ${response.status}" }
    null
}
```

---

### 1.3 `isOverdue()` is a no-op stub — deadlines never show as overdue

**File:** `presentation/common/TaskCard.kt`, lines 163-166

**Current code:**
```kotlin
fun isOverdue(deadline: String?): Boolean {
    if (deadline == null) return false
    return false  // always false!
}
```

**Impact:** `DeadlineDateRow` (line 98) uses this to color-code deadlines (`AppColors.Error` for overdue). Since it always returns `false`, users get no visual overdue warning.

**Fix:** Implement using ISO 8601 lexicographic comparison or `kotlinx-datetime`:
```kotlin
fun isOverdue(deadline: String?): Boolean {
    if (deadline == null) return false
    return try {
        val deadlineInstant = Instant.parse(deadline)
        Clock.System.now() > deadlineInstant
    } catch (e: Exception) {
        false
    }
}
```

---

### 1.4 `gradeColor()` yellow color divergence between two screens

**Locations:**
- `CoursesScreen.kt` line 557: `Color(0xFFFFEE58)` (Material Yellow 400)
- `CoursePerformanceScreen.kt` line 267: `Color(0xFFFFEB3B)` (Material Yellow 500)

**Problem:** Same grade produces different yellow shades on different screens. This is a visual inconsistency bug.

**Fix:** Consolidate into a shared `gradeColor()` function with a single yellow value.

---

### 1.5 No HTTP timeout configuration

**File:** `data/network/HttpClientFactory.kt`

**Problem:** `HttpClient` created with no connect/request/socket timeout. Network calls can hang indefinitely.

**Fix:** Add timeout configuration:
```kotlin
install(HttpTimeout) {
    requestTimeoutMillis = 30_000
    connectTimeoutMillis = 15_000
    socketTimeoutMillis = 30_000
}
```

---

### 1.6 `CookieAwareRepository.withCookie()` — no logging when cookie absent

**File:** `data/repository/CookieAwareRepository.kt`, lines 22-25

**Current code:**
```kotlin
protected suspend fun <T> withCookie(block: suspend (String) -> T?): T? {
    val cookie = authLocal.cookieFlow.first() ?: return null
    return block(cookie)
}
```

**Problem:** Returns `null` silently when user is not authenticated. Callers cannot distinguish "not authenticated" from "API returned null." Makes debugging difficult.

**Fix:** Add `logger.debug { "No auth cookie available, skipping API call" }` before returning null.

---

### 1.7 IosFileStorage — OS errors silently ignored (`error = null`)

**File:** `iosMain/data/local/IosFileStorage.kt`, lines 26-44, 47-50, 61-64, 85, 89-92, 98

**Problem:** Multiple NSFileManager calls pass `error = null`, silently swallowing OS-level errors (permission denied, disk full). The `createDirectoryAtPath` call in `downloadsDir` init could fail, causing all subsequent file operations to fail with confusing errors.

**Fix:** Pass actual `NSError` pointers and log errors:
```kotlin
val errorPtr = nativeHeap.alloc<ObjCObjectVar<NSError?>>()
val success = fileManager.createDirectoryAtPath(path, ..., error = errorPtr.ptr)
errorPtr.value?.let { logger.error { "Failed to create downloads dir: ${it.localizedDescription}" } }
```

---

### 1.8 Path traversal vulnerability in FileStorage implementations

**Files:**
- `androidMain/data/local/AndroidFileStorage.kt` — `deleteFile()`, `saveFile()`, `fileExists()`
- `iosMain/data/local/IosFileStorage.kt` — same methods

**Problem:** If `name` contains `../`, a caller could access files outside the downloads directory.

**Fix:** Validate resolved path:
```kotlin
val file = File(downloadsDir, name)
require(file.canonicalPath.startsWith(downloadsDir.canonicalPath)) {
    "Invalid file name: $name"
}
```

---

## 2. High: Architectural & Structural Issues

### 2.1 `ApiService.kt` exceeds 400-line limit (553 lines) — God class

**Problem:** Single class with 20+ endpoint methods and `@Suppress("TooManyFunctions")`. Violates the 400-line rule and single responsibility principle.

**Fix:** Split into domain-specific service classes:
- `AuthApiService` — `validateAuth`
- `ProfileApiService` — `fetchProfile`, `fetchProfileRaw`, `fetchAvatar`, `deleteAvatar`, `fetchLmsProfile`
- `TaskApiService` — `fetchTasks`, `fetchTaskDetails`, `fetchTaskEvents`, `fetchTaskComments`, `startTask`, `submitTask`, `prolongLateDays`, `cancelLateDays`, `createComment`
- `CourseApiService` — `fetchCourses`, `fetchCourseOverview`
- `ContentApiService` — `fetchLongreadMaterials`, `fetchMaterial`, `getDownloadLink`, `getUploadLink`
- `NotificationApiService` — `fetchNotifications`
- `PerformanceApiService` — `fetchPerformance`, `fetchCourseExercises`, `fetchCoursePerformance`, `fetchGradebook`

Also extract the repeated try/catch boilerplate into a helper (see 2.2).

---

### 2.2 Massive boilerplate duplication in ApiService — every method repeats the same pattern

**Problem:** 20+ methods all follow the identical pattern:
```kotlin
try {
    val response = httpClient.get/post/put(...) {
        header("Cookie", cookieHeader(cookie))
    }
    if (response.status == HttpStatusCode.OK) response.body() else null
} catch (e: Exception) {
    logger.error(e) { "Failed to ..." }
    null
}
```

**Fix:** Extract into a generic helper:
```kotlin
private suspend inline fun <reified T> safeApiCall(
    description: String,
    block: () -> HttpResponse,
): T? = try {
    val response = block()
    if (response.status == HttpStatusCode.OK) {
        response.body<T>()
    } else {
        logger.warn { "$description returned ${response.status}" }
        null
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    logger.error(e) { "Failed to $description" }
    null
}
```

This solves items 1.1, 1.2, and 2.2 simultaneously.

---

### 2.3 `createRootComponent()` duplicated between Android and iOS

**Files:**
- `androidMain/di/AndroidKoin.kt`, lines 38-53
- `iosMain/MainViewController.kt`, lines 24-39

**Problem:** Both functions are character-for-character identical. They use no platform-specific APIs — just `KoinPlatform.getKoin()` and construct common classes.

**Fix:** Move to `commonMain` (e.g., `di/Koin.kt` or a new `di/RootComponentFactory.kt`).

---

### 2.4 Domain layer depends on data layer models — Clean Architecture violation

**Affected:** All 8 domain repository interfaces in `domain/repository/`

**Problem:** Every domain interface imports from `data.model` and `data.local`. The `PerformanceRepository` returns types literally named `*Response` (API DTOs). This inverts the dependency rule.

**Scope:** This is a significant structural refactoring. The proper fix is:
1. Move model classes to `domain/model/` (or a shared `model/` package)
2. Create separate DTOs in `data/model/` for API responses
3. Map DTOs → domain models in repository implementations

**Pragmatic alternative:** Since the current models are simple `@Serializable` data classes with no framework dependencies, the models could be moved to a package outside both `data` and `domain` (e.g., `model/` at the root), keeping existing code structure mostly intact. The key fix is renaming `*Response` types to non-API-specific names.

**Recommendation for now:** Move models to `shared/.../model/` (a package independent of both `data` and `domain`). Rename `*Response` types. This avoids the full DTO mapping complexity while fixing the dependency direction.

---

### 2.5 Four presentation files exceed 400-line limit

| File | Lines | Recommended split |
|------|-------|-------------------|
| `CoursesScreen.kt` | 601 | → `CoursesListContent.kt`, `GradeSheetContent.kt`, `GradebookContent.kt` |
| `LongreadTaskSection.kt` | 477 | → Extract `SolutionTab.kt`, `LateDaysSection.kt` |
| `CourseDetailScreen.kt` | 466 | → Extract `CourseDetailCards.kt` (ThemeCard, LongreadRow, ExerciseRow) |
| `TasksScreen.kt` | 429 | → Extract `TasksFilters.kt` (filter chips, segment control) |

---

### 2.6 `fetchProfileRaw()` appears to be dead code

**File:** `ApiService.kt`, lines 93-106

**Problem:** The method `fetchProfileRaw` is defined but never called from any repository or anywhere else. It duplicates the `fetchProfile` endpoint.

**Fix:** Remove or document why it exists.

---

### 2.7 Duplicate `validateAuth`/`fetchProfile` endpoints

**File:** `ApiService.kt`

**Problem:** Three methods hit the same endpoint `"hub/students/me"`:
- `validateAuth` (line 69) — checks if status is OK, returns Boolean
- `fetchProfile` (line 83) — parses the body, returns `StudentProfile?`
- `fetchProfileRaw` (line 95) — returns the raw string

**Fix:** `validateAuth` could delegate to `fetchProfile() != null`. Remove `fetchProfileRaw` if unused.

---

## 3. Medium: Code Duplication & DRY Violations

### 3.1 Loading/Error/Empty state composables duplicated across 7-9 screens

**Loading state** (nearly identical `Box(fillMaxSize, Center) { CircularProgressIndicator }`) in:
`CoursesScreen.kt`, `CourseDetailScreen.kt`, `TasksScreen.kt`, `HomeScreen.kt`, `FilesScreen.kt`, `NotificationsScreen.kt`, `LongreadScreen.kt`, `ProfileScreen.kt`, `CoursePerformanceScreen.kt`

**Error state** (error text + "Повторить" retry button, some with warning emoji) in:
`CoursesScreen.kt`, `CourseDetailScreen.kt`, `TasksScreen.kt`, `HomeScreen.kt`, `FilesScreen.kt`, `NotificationsScreen.kt`, `LongreadScreen.kt`, `ProfileScreen.kt`, `CoursePerformanceScreen.kt`

**Empty state** in:
`CoursesScreen.kt`, `HomeScreen.kt`, `FilesScreen.kt`, `NotificationsScreen.kt`

**Fix:** Create shared composables in `presentation/common/`:
```kotlin
// CommonStates.kt
@Composable fun LoadingContent()
@Composable fun ErrorContent(error: String?, onRetry: (() -> Unit)?)
@Composable fun EmptyContent(text: String)
```

---

### 3.2 SegmentControl / TabSelector duplicated 5 times

| File | Function | Lines |
|------|----------|-------|
| `TasksScreen.kt` | `SegmentControl()` | 157-206 |
| `CoursesScreen.kt` | `CoursesSegmentControl()` | 89-129 |
| `CoursePerformanceScreen.kt` | `TabSelector()` | 204-247 |
| `LongreadTaskSection.kt` | `TabSelector()` | 219-250 |
| `NotificationsScreen.kt` | `TabSelector()` | 112-137 |

**Fix:** Create a shared `SegmentedControl(labels, selectedIndex, onSelect)` in `presentation/common/`.

---

### 3.3 DetailTopBar (back button + title) duplicated 4 times

| File | Function |
|------|----------|
| `CourseDetailScreen.kt` | `CourseDetailTopBar()` |
| `LongreadScreen.kt` | `LongreadTopBar()` |
| `NotificationsScreen.kt` | `NotificationsTopBar()` |
| `ProfileScreen.kt` | `ProfileTopBar()` |

All are `Row` with `TextButton("← Назад")` and title `Text`. `ProfileTopBar` adds a trailing logout button.

**Fix:** Create `DetailTopBar(title, onBack, trailingContent?)` in `presentation/common/`.

---

### 3.4 `gradeColor()` + `gradeDescription()` duplicated between two files

- `CoursesScreen.kt` lines 554-569
- `CoursePerformanceScreen.kt` lines 264-279

**Fix:** Move both to a shared `GradeUtils.kt` in `presentation/common/`. Fix the yellow color divergence (see 1.4).

---

### 3.5 `TargetCookieName` constant duplicated in 3 places

- `PlatformWebView.android.kt` line 14: `"bff.cookie"`
- `PlatformWebView.ios.kt` line 18: `"bff.cookie"`
- `ApiService.kt` line 54: `"bff.cookie"` in `cookieHeader()`

**Fix:** Define once in `commonMain` (e.g., a `NetworkConstants` object) and reference from all locations.

---

### 3.6 `AuthDomain` URL partially duplicated

- `PlatformWebView.android.kt` line 13: `"https://my.centraluniversity.ru"`
- `HttpClientFactory.kt` line 31: `"https://my.centraluniversity.ru/api/"`

**Fix:** Derive the WebView URL from the base URL or define a common domain constant.

---

## 4. Medium: Access Modifier Corrections

### 4.1 Data layer classes — should be `internal`

All of these are only referenced via DI bindings and should not be in the public API:

| File | Current | Should be |
|------|---------|-----------|
| `ApiService.kt` | `class ApiService` | `internal class` |
| `HttpClientFactory.kt` | `fun createHttpClient()` | `internal fun` |
| `CookieAwareRepository.kt` | `open class CookieAwareRepository` | `internal open class` |
| `AuthRepositoryImpl.kt` | `class AuthRepositoryImpl` | `internal class` |
| `ProfileRepositoryImpl.kt` | `class ProfileRepositoryImpl` | `internal class` |
| `TaskRepositoryImpl.kt` | `class TaskRepositoryImpl` | `internal class` |
| `CourseRepositoryImpl.kt` | `class CourseRepositoryImpl` | `internal class` |
| `ContentRepositoryImpl.kt` | `class ContentRepositoryImpl` | `internal class` |
| `NotificationRepositoryImpl.kt` | `class NotificationRepositoryImpl` | `internal class` |
| `PerformanceRepositoryImpl.kt` | `class PerformanceRepositoryImpl` | `internal class` |
| `FileRepositoryImpl.kt` | `class FileRepositoryImpl` | `internal class` |
| `AuthLocalDataSource.kt` | `class AuthLocalDataSource` | `internal class` |

### 4.2 Presentation layer — public functions that should be internal/private

| File | Function | Used in | Should be |
|------|----------|---------|-----------|
| `TasksComponent.kt` | `ACTIVE_STATES`, `ARCHIVE_STATES`, `ALL_API_STATES` | tasks package | `internal` |
| `TasksComponent.kt` | `normalizeTaskState()`, `effectiveTaskState()` | tasks package | `internal` |
| `DefaultTasksComponent.kt` | `filteredTasks()`, `availableCourses()`, `availableStatuses()` | `TasksScreen.kt` | `internal` |
| `CoursesComponent.kt` | `activeCourses()`, `archivedCourses()` | `CoursesScreen.kt` | `internal` |
| `CourseDetailComponent.kt` | `filteredThemes()` | `CourseDetailScreen.kt` | `internal` |
| `TaskCard.kt` | `taskStateBadgeLabel()`, `isOverdue()` | `TaskCard.kt` only | `private` |

### 4.3 DI module visibility

| File | Symbol | Should be |
|------|--------|-----------|
| `Koin.kt` | `networkModule`, `dataModule`, `repositoryModule` | `private` |
| `Koin.kt` | `initKoin()` | `internal` |
| `AndroidKoin.kt` | `initKoinAndroid()` | `internal` |
| `DataStorePath.ios.kt` | `dataStorePath()` | `internal` |

### 4.4 `CookieAwareRepository.apiService` is `protected` but should be `private`

**File:** `CookieAwareRepository.kt`, line 16

All subclasses access `apiService` only through `withCookie`/`withCookieOrFalse` lambdas. Making it `protected` allows bypassing the cookie pattern. Should be `private`.

---

## 5. Low: Magic Strings/Numbers & Naming

### 5.1 `BaseUrl` naming convention

**File:** `HttpClientFactory.kt` line 31: `const val BaseUrl = ...`
**Fix:** Rename to `BASE_URL` (Kotlin `SCREAMING_SNAKE_CASE` convention for `const val`).

### 5.2 Magic numbers in ApiService

- Line 346: `limit=10000` — extract to `private const val MAX_LIST_LIMIT = 10_000`
- Line 378: `limit=10000` — same

### 5.3 Magic strings in ApiService

- Line 320: `"type" to "task"` — extract to `private const val COMMENT_ENTITY_TYPE = "task"`
- Line 329: `"commentId"` JSON key
- Line 420: `"url"` JSON key

### 5.4 Magic strings in data models

- `LongreadMaterial.kt` lines 43-46: `"markdown"`, `"file"`, `"coding"`, `"questions"` — extract to companion constants
- `Gradebook.kt` line 57: `"elective"` — extract to companion constant

### 5.5 Hardcoded Russian text in data model

- `Gradebook.kt` line 24: `"семестр"` in `GradebookSemester.title` property — this is presentation-layer concern, move to UI

### 5.6 Task state strings scattered across 8+ files

States like `"inProgress"`, `"backlog"`, `"review"`, `"evaluated"`, `"failed"`, `"rejected"`, `"revision"`, `"rework"` appear as raw strings in multiple files. Should be centralized in a constants object or enum.

### 5.7 Notification category IDs should be named constants

`NotificationRepository.fetchNotifications(category: Int)` uses raw `Int` values. Add:
```kotlin
object NotificationCategory {
    const val EDUCATION = 1
    const val OTHER = 2
}
```

### 5.8 `isLenient = true` in JSON config may mask deserialization bugs

**File:** `HttpClientFactory.kt` line 19 — document why this is needed or remove it.

### 5.9 `Dispatchers.Default` used for file I/O

**File:** `FileRepositoryImpl.kt` line 26 — should use a platform-abstracted IO dispatcher, not `Dispatchers.Default` which is for CPU-bound work.

### 5.10 Version info inconsistent between build files

- `shared/build.gradle.kts`: `APP_VERSION = "1.0.1"`, `APP_VERSION_INT = 101`
- `androidApp/build.gradle.kts`: `versionName = "1.0"`, `versionCode = 1`

These should be centralized (e.g., in `libs.versions.toml` or a shared `AppInfo` object).

---

## 6. Detekt Findings

### 6.1 `UnsafeCallOnNullableType` (!! usage) — 8 occurrences

| File | Line |
|------|------|
| `CoursesScreen.kt` | 77 |
| `CourseDetailScreen.kt` | 77 |
| `FilesScreen.kt` | 74 |
| `HomeScreen.kt` | 58 |
| `LongreadScreen.kt` | 63 |
| `NotificationsScreen.kt` | 73 |
| `CoursePerformanceScreen.kt` | 67 |
| `ProfileScreen.kt` | 67 |

**Fix:** Replace `!!` with safe alternatives (`?.let`, `requireNotNull()` with message, or null checks).

### 6.2 `UnusedParameter` — 1 occurrence

| File | Line | Parameter |
|------|------|-----------|
| `LongreadScreen.kt` | 45 | `onBack` |

**Fix:** Either use the parameter or remove it if not needed.

---

## 7. Execution Order

Recommended order for implementation, grouping related items to minimize file conflicts:

### Batch 1: API layer refactoring (items 1.1, 1.2, 1.5, 2.1, 2.2, 2.6, 2.7, 5.1, 5.2, 5.3)
1. Add HTTP timeout config to `HttpClientFactory.kt`
2. Rename `BaseUrl` → `BASE_URL`
3. Create `safeApiCall` helper that handles CancellationException + non-200 logging
4. Split `ApiService.kt` into domain-specific service classes
5. Remove `fetchProfileRaw` dead code, simplify `validateAuth` to use `fetchProfile`
6. Extract magic constants (`MAX_LIST_LIMIT`, `COMMENT_ENTITY_TYPE`, cookie name)
7. Update DI bindings
8. Run detekt + ktlint, fix any new issues

### Batch 2: Repository & data layer cleanup (items 1.6, 1.7, 1.8, 4.1, 4.4, 5.9)
1. Add logging to `CookieAwareRepository.withCookie()` for missing cookie
2. Make `apiService` `private` in `CookieAwareRepository`
3. Add `internal` modifier to all repository impls and data layer classes
4. Fix `Dispatchers.Default` → platform IO dispatcher in `FileRepositoryImpl`
5. Fix path traversal in `AndroidFileStorage` and `IosFileStorage`
6. Fix `IosFileStorage` `error = null` patterns — pass real error pointers and log
7. Run detekt + ktlint

### Batch 3: Presentation common components (items 3.1, 3.2, 3.3, 3.4)
1. Create `CommonStates.kt` with `LoadingContent`, `ErrorContent`, `EmptyContent`
2. Create `SegmentedControl.kt` composable
3. Create `DetailTopBar.kt` composable
4. Create `GradeUtils.kt` with unified `gradeColor()` + `gradeDescription()`
5. Replace all duplicate implementations across 9+ screen files
6. Run detekt + ktlint

### Batch 4: File splitting (items 2.5, related @Suppress removal)
1. Split `CoursesScreen.kt` (601 lines) → `CoursesListContent.kt`, `GradeSheetContent.kt`, `GradebookContent.kt`
2. Split `LongreadTaskSection.kt` (477 lines) → extract `SolutionTab.kt`, `LateDaysSection.kt`
3. Split `CourseDetailScreen.kt` (466 lines) → extract `CourseDetailCards.kt`
4. Split `TasksScreen.kt` (429 lines) → extract `TasksFilters.kt`
5. Remove `@file:Suppress("TooManyFunctions")` from split files
6. Run detekt + ktlint

### Batch 5: Bug fixes & detekt issues (items 1.3, 1.4, 6.1, 6.2)
1. Implement `isOverdue()` properly using `kotlinx-datetime` or ISO 8601 comparison
2. Fix `gradeColor()` yellow divergence (already unified in batch 3)
3. Fix all 8 `!!` usages flagged by detekt
4. Fix `onBack` unused parameter in `LongreadScreen.kt`
5. Run detekt + ktlint

### Batch 6: Access modifiers & constants (items 4.2, 4.3, 5.4-5.7)
1. Reduce visibility of 14+ public presentation functions to `internal`/`private`
2. Make DI module vals `private`, `initKoin()` `internal`
3. Centralize task state strings into a constants object
4. Add notification category constants
5. Extract `LongreadMaterial` discriminator constants to companion
6. Move `GradebookSemester.title` Russian text to presentation layer
7. Run detekt + ktlint

### Batch 7: Structural improvements (items 2.3, 2.4, 3.5, 3.6, 5.10)
1. Move `createRootComponent()` to `commonMain`
2. Extract `TargetCookieName` to a shared constant in `commonMain`
3. Unify auth domain URL with base URL
4. Centralize version info in `libs.versions.toml`
5. Consider using typed `get<T>()` in Koin bindings
6. Evaluate moving models to a package independent of `data`/`domain` (or at minimum rename `*Response` types)
7. Run detekt + ktlint

### Batch 8: Final verification
1. Run full build: `./gradlew :androidApp:assembleDebug`
2. Run `./gradlew detektMainAndroid` — should pass clean
3. Run `./gradlew ktlintCheck` — should pass clean
4. Verify on Android emulator that all screens still work
5. Update `memorybank/progress.md` with completed refactoring

---

## Issue Count Summary

| Severity | Count | Categories |
|----------|-------|------------|
| Critical / Bug | 8 | CancellationException, silent HTTP errors, isOverdue stub, gradeColor divergence, no timeouts, missing cookie logging, iOS error swallowing, path traversal |
| High / Architectural | 7 | ApiService 553 lines, boilerplate duplication, createRootComponent duplicate, domain→data dependency, 4 files over 400 lines, dead code, duplicate endpoints |
| Medium / DRY | 6 | Loading/Error/Empty state duplication, SegmentControl x5, DetailTopBar x4, grade functions x2, cookie constant x3, auth URL partial dupe |
| Medium / Access | 16+ | 12 data layer classes, 14+ presentation functions, 4 DI symbols |
| Low / Constants | 10 | Magic strings/numbers, naming conventions, version inconsistency, JSON config, dispatcher |
| Detekt | 9 | 8x UnsafeCallOnNullableType, 1x UnusedParameter |

**Total: ~56 issues across 50+ files**
