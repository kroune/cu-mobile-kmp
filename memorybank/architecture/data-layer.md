# Слой данных

## Репозитории

Все реализации наследуют `CookieAwareRepository` (базовый класс в `data/repository/`).

- Предоставляет `withCookie {}` и `withCookieOrFalse {}` — получают cookie через `authLocal.cookieFlow.first()`, оборачивают в `withContext(dispatchers.io)`
- ApiService-методы принимают `cookie: String` первым параметром; репозитории скрывают это от domain-слоя
- ApiService возвращает `null` / `false` / пустой список при ошибке — **никогда не бросает исключений**, всегда логирует ошибки

### Список репозиториев (8 шт.)

Привязки в `di/Koin.kt`:

- `AuthRepository` / `AuthRepositoryImpl`
- `CalendarRepository` / `CalendarRepositoryImpl`
- `CourseRepository` / `CourseRepositoryImpl`
- `FileRepository` / `FileRepositoryImpl`
- `NotificationRepository` / `NotificationRepositoryImpl`
- `PerformanceRepository` / `PerformanceRepositoryImpl`
- `ProfileRepository` / `ProfileRepositoryImpl`
- `TaskRepository` / `TaskRepositoryImpl`

---

## API-эндпоинты (ApiService, 25 шт.)

- **Auth**: `validateAuth`
- **Профиль**: `fetchProfile`, `fetchProfileRaw`, `fetchAvatar`, `deleteAvatar`, `fetchLmsProfile`
- **Задачи**: `fetchTasks`, `fetchTaskDetails`, `fetchTaskEvents`, `fetchTaskComments`, `startTask`, `submitTask`, `prolongLateDays`, `cancelLateDays`, `createComment`
- **Курсы**: `fetchCourses`, `fetchCourseOverview`
- **Контент**: `fetchLongreadMaterials`, `fetchMaterial`, `getDownloadLink`, `getUploadLink`
- **Уведомления**: `fetchNotifications`
- **Успеваемость**: `fetchPerformance`, `fetchCourseExercises`, `fetchCoursePerformance`, `fetchGradebook`

Полный список эндпоинтов бэкенда (175+) — в `web-reverse/summary/api-endpoints.md`.

---

## Модели данных (`data/model/`, 13 файлов, ~37 DTO)

Ключевые типы:

- `StudentProfile`, `StudentLmsProfile`
- `Course`, `CourseOverview`, `CourseTheme`, `Longread`, `ThemeExercise`
- `StudentTask`, `TaskDetails`, `TaskEvent`, `TaskComment`
- `LongreadMaterial`, `MaterialAttachment`
- `NotificationItem`, `NotificationLink`
- `StudentPerformanceResponse`, `CourseExercise`, `TaskScore`, `GradebookResponse`
- `UploadLinkData`
- `DownloadedFileInfo` (локальная, не из API)

Полные TypeScript-интерфейсы бэкенда — в `web-reverse/summary/data-models.md`.

---

## Task State Machine

```
backlog → inProgress → hasSolution → review → evaluated / revision / failed
```

- `hasSolution` — виртуальное состояние: inProgress + submitAt != null
- `rework` нормализуется в `revision`, `rejected` в `failed`

### Фильтрация задач (TasksComponent)

- **Active**: backlog, inProgress, hasSolution, revision, rework, review
- **Archive**: evaluated, failed, rejected
- **Сортировка**: evaluated/failed/review внизу; остальное по дедлайну по возрастанию (null в конце)

**Форма State:** сырые фильтры (`segment`, `statusFilter`, `courseFilter`, `searchQuery`) в `State` рядом с `content: ContentState<Content>`, где `Content` хранит отфильтрованные/отсортированные списки + счётчики + доступные значения фильтров.

**Деривация:** `DefaultTasksComponent` хранит сырые поля в приватном `RawState`, запускает `scheduleDerive()` после каждой мутации. Отменяет in-flight деривацию (`deriveJob?.cancel()`). `buildTasksContent(...)` (в `TasksContentBuilder.kt`) работает на `dispatchers.default`, предварительно вычисляет эффективное состояние каждой задачи, проходит `allTasks` за один проход.

---

## Система Late Days

- Максимум 7 дней продления на задачу
- Нельзя продлить в состояниях: review, evaluated, revision, rework
- Отмена разрешена если: lateDays > 0 AND effectiveDeadline > 24ч от текущего момента

---

## Порядок курсов

Пользовательский порядок сохраняется в DataStore.
