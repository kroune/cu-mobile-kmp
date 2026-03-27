# Loading Performance Analysis

## Problem

Most screens make multiple independent network requests **sequentially**, meaning total load time = sum of all requests instead of max(all requests).

---

## Per-Screen Analysis

### 1. Home Screen (`DefaultHomeComponent:110-143`)

**5 sequential network calls** in `loadData()`:
1. `taskRepository.fetchTasks()`
2. `courseRepository.fetchCourses()` — waits for (1)
3. `profileRepository.fetchProfile()` — waits for (2)
4. `profileRepository.fetchLmsProfile()` — waits for (3)
5. `profileRepository.fetchAvatar()` — waits for (4)

None depend on each other. `loadSchedule()` already runs in a separate coroutine (good).

**Fix**: `coroutineScope { async {} }` for all 5 calls.
**Estimated speedup**: ~4-5x.

### 2. Courses Screen (`DefaultCoursesComponent:81-103`)

**3 sequential network calls** in `loadAllData()`:
1. `courseRepository.fetchCourses()`
2. `performanceRepository.fetchPerformance()` — waits for (1)
3. `performanceRepository.fetchGradebook()` — waits for (2)

All independent.

**Fix**: `coroutineScope { async {} }` for all 3 calls.
**Estimated speedup**: ~3x.

### 3. Profile Screen (`DefaultProfileComponent:58-82`)

**3 sequential network calls** in `loadProfile()`:
1. `profileRepository.fetchProfile()`
2. `profileRepository.fetchLmsProfile()` — waits for (1)
3. `profileRepository.fetchAvatar()` — waits for (2)

All independent.

**Fix**: `coroutineScope { async {} }` for all 3 calls.
**Estimated speedup**: ~3x.

### 4. Notifications Screen (`DefaultNotificationsComponent:55-73`)

**2 sequential network calls** in `loadNotifications()`:
1. `fetchNotifications(Education)`
2. `fetchNotifications(Other)` — waits for (1)

Independent.

**Fix**: `coroutineScope { async {} }` for both calls.
**Estimated speedup**: ~2x.

### 5. Course Performance (`DefaultCoursePerformanceComponent:60-89`)

**2 sequential network calls** in `loadData()`:
1. `performanceRepository.fetchCourseExercises(courseId)`
2. `performanceRepository.fetchCoursePerformance(courseId)` — waits for (1)

Independent.

**Fix**: `coroutineScope { async {} }` for both calls.
**Estimated speedup**: ~2x.

### 6. Longread (`DefaultLongreadComponent:165-177`)

**Sequential loop** in `loadTaskDetailsForCodingMaterials()`:
```kotlin
for (taskId in codingTaskIds) {
    val details = taskRepository.fetchTaskDetails(taskId) // one by one!
}
```
Also `loadTaskEventsAndComments()` (line 196-205) runs 2 calls sequentially (events then comments).

**Fix**:
- Task details loop → `coroutineScope { codingTaskIds.map { async { fetchTaskDetails(it) } } }`
- Events + comments → `coroutineScope { async {} }` for both.
**Estimated speedup**: ~Nx for task details (N = number of coding tasks), ~2x for events/comments.

### 7. No issues (single request or local-only)

- **Tasks Screen** (`DefaultTasksComponent`) — single `fetchTasks()` call
- **Course Detail** (`DefaultCourseDetailComponent`) — single `fetchCourseOverview()` call
- **Files Screen** (`DefaultFilesComponent`) — local filesystem only
- **Scanner, FileRenameSettings** — no network loading

---

## Summary Table

| Screen | # Sequential Requests | Can Parallelize | Est. Speedup |
|--------|----------------------|-----------------|--------------|
| Home | 5 | All 5 | ~4-5x |
| Courses | 3 | All 3 | ~3x |
| Profile | 3 | All 3 | ~3x |
| Notifications | 2 | Both | ~2x |
| CoursePerformance | 2 | Both | ~2x |
| Longread (task details) | N (loop) | All N | ~Nx |
| Longread (events+comments) | 2 | Both | ~2x |

---

## Implementation Approach

**Technique**: Use `coroutineScope { async {} }` to parallelize independent requests within each `load*()` function. No new files, no interface changes, no State class changes — purely internal implementation.

**Files to modify** (6 files):
- `shared/.../presentation/home/DefaultHomeComponent.kt`
- `shared/.../presentation/courses/DefaultCoursesComponent.kt`
- `shared/.../presentation/profile/DefaultProfileComponent.kt`
- `shared/.../presentation/notifications/DefaultNotificationsComponent.kt`
- `shared/.../presentation/performance/DefaultCoursePerformanceComponent.kt`
- `shared/.../presentation/longread/DefaultLongreadComponent.kt`

## Optional Phase 2: Progressive Display

For Home screen specifically, could show tasks/courses as they arrive instead of waiting for avatar/profile. This would require splitting the single `isLoading` boolean into per-section loading states (e.g. `isTasksLoading`, `isCoursesLoading`, `isProfileLoading`). Adds complexity to State classes and Screen composables. Lower priority.
