# Discrepancies: Website vs KMP App

Comparison between the CU LMS website (from JS reverse engineering) and the KMP mobile app (from
`memorybank/ARCHITECTURE.md`).

---

## 1. API Endpoints the Website Uses That the KMP App Does Not

The KMP app's `ApiService` has 25 endpoints. The website uses ~80+. Missing from the KMP app:

### Hub/Portal APIs (entire category missing)

| Endpoint                                      | Purpose                 | Priority                                                                                                                  |
|-----------------------------------------------|-------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `GET /api/hub/enrollees/me`                   | Enrollee profile        | Low (mobile targets students)                                                                                             |
| `PUT /api/hub/enrollees/me`                   | Update enrollee profile | Low                                                                                                                       |
| `POST /api/hub/enrollees/onboarding/me`       | Onboarding flow         | Low                                                                                                                       |
| `GET /api/hub/grants/me`                      | User grants             | Low                                                                                                                       |
| `GET /api/hub/grants/me/{year}/{type}/active` | Active grant by type    | Low                                                                                                                       |
| `GET /api/hub/avatars/me`                     | User avatar             | **Medium** (KMP uses `fetchAvatar` but path differs -- KMP uses a different endpoint, website uses `/api/hub/avatars/me`) |

### Events APIs (entire category missing)

| Endpoint                                                     | Purpose                | Priority |
|--------------------------------------------------------------|------------------------|----------|
| `POST /api/event-builder/public/events/list`                 | List events            | Medium   |
| `GET /api/event-builder/public/events/slug/{slug}`           | Event detail           | Medium   |
| `GET /api/event-builder/public/events/{id}/appointment/file` | Download .ics calendar | Low      |
| `POST /api/event-builder/public/events/apply/{id}`           | Register for event     | Medium   |
| `GET /api/event-builder/admissions/2025/state`               | Admission state        | Low      |

### News APIs (entire category missing)

| Endpoint                                       | Purpose         | Priority |
|------------------------------------------------|-----------------|----------|
| `GET /api/event-builder/public/news/{id}`      | View news item  | Medium   |
| `GET /api/event-builder/public/news/file/{id}` | News attachment | Low      |

### Course Management APIs (teacher/admin, not student)

| Endpoint                                                           | Purpose                                 | Priority    |
|--------------------------------------------------------------------|-----------------------------------------|-------------|
| `POST /api/micro-lms/courses`                                      | Create course                           | Low (admin) |
| `POST /api/micro-lms/courses/{id}/duplicate`                       | Duplicate course                        | Low         |
| `PUT /api/micro-lms/courses/{id}`                                  | Update course settings                  | Low         |
| `PUT /api/micro-lms/courses/{id}/archive`                          | Archive course                          | Low         |
| `PUT /api/micro-lms/courses/{id}/unarchive`                        | Unarchive course                        | Low         |
| `PUT /api/micro-lms/courses/{id}/publish`                          | Publish course                          | Low         |
| `PUT /api/micro-lms/courses/{id}/return-to-draft`                  | Move to draft                           | Low         |
| `DELETE /api/micro-lms/courses/{id}`                               | Delete course                           | Low         |
| All theme/longread CRUD endpoints                                  | Create/edit/delete themes and longreads | Low         |
| All material CRUD endpoints                                        | Create/edit/delete materials            | Low         |
| Course settings (students, listeners, groups, teachers, reviewers) | Manage course members                   | Low         |

### Task Admin APIs

| Endpoint                                           | Purpose                    | Priority                  |
|----------------------------------------------------|----------------------------|---------------------------|
| `PUT /api/micro-lms/tasks/{id}/reviewer`           | Assign reviewer            | Low (teacher)             |
| `PUT /api/micro-lms/tasks/{id}/start`              | Start task                 | **High** (student action) |
| `PUT /api/micro-lms/tasks/{id}/submit`             | Submit task solution       | **High** (student action) |
| `POST /api/micro-lms/polls`                        | Submit evaluation feedback | Low                       |
| `GET /api/micro-lms/polls/{entityType}/{entityId}` | Check feedback status      | Low                       |

### Calendar/Timetable APIs

| Endpoint                                                          | Purpose                   | Priority    |
|-------------------------------------------------------------------|---------------------------|-------------|
| `GET /api/micro-lms/calendar-events/slot-management/config`       | Registration state        | Low         |
| `PUT /api/micro-lms/calendar-events/slot-management/config`       | Save registration config  | Low (admin) |
| `GET /api/micro-lms/calendar-events/reports/lesson-registrations` | Download timetable report | Low         |

### Notification Endpoints (partially present)

| Endpoint                                               | Website Method        | KMP Status   |
|--------------------------------------------------------|-----------------------|--------------|
| `POST /api/notification-hub/notifications/in-app`      | POST with filter body | KMP uses GET |
| `GET /api/notification-hub/notifications/in-app/stats` | Stats/unread counts   | **Missing**  |
| `POST /api/notification-hub/notifications/in-app/read` | Mark as read          | **Missing**  |

### Quiz / Attempts APIs (entire category missing)

| Endpoint                                                     | Purpose                   | Priority                    |
|--------------------------------------------------------------|---------------------------|-----------------------------|
| `POST /api/micro-lms/quizzes/attempts`                       | Start new quiz attempt    | **High** (student action)   |
| `GET /api/micro-lms/quizzes/attempts/{id}`                   | Get single attempt        | **High**                    |
| `GET /api/micro-lms/quizzes/sessions/{id}/attempts`          | List attempts for session | **High**                    |
| `POST /api/micro-lms/quizzes/attempts/{id}/submit`           | Submit answers            | **High** (student action)   |
| `POST /api/micro-lms/quizzes/attempts/{id}/complete`         | Complete attempt          | **High** (student action)   |
| `GET /api/micro-lms/quizzes/{id}/questions`                  | Get quiz questions        | **High**                    |

### Task Comments APIs (partially missing)

| Endpoint                                                     | Purpose                          | Priority                  |
|--------------------------------------------------------------|----------------------------------|---------------------------|
| `GET /api/micro-lms/{entity}/{entityId}/comments`            | List comments for entity         | **Medium**                |
| `POST /api/micro-lms/comments`                               | Create comment                   | **Medium** (KMP has this) |
| `PUT /api/micro-lms/comments/{id}`                           | Edit comment                     | **Medium** (KMP missing)  |
| `DELETE /api/micro-lms/comments/{id}`                        | Delete comment                   | **Medium** (KMP missing)  |

### Task Events APIs

| Endpoint                                                     | Purpose                   | Priority   |
|--------------------------------------------------------------|---------------------------|------------|
| `GET /api/micro-lms/tasks/{id}/events`                       | Task event history        | **Medium** |

### Whitelist APIs

| Endpoint                                                     | Purpose                       | Priority |
|--------------------------------------------------------------|-------------------------------|----------|
| `GET /api/whitelist/channels/grants/{channelId}/2026`        | Grant channel whitelist check | Low      |

### Other Missing

| Endpoint                                             | Purpose                 | Priority |
|------------------------------------------------------|-------------------------|----------|
| `GET /api/micro-lms/users?limit=10000`               | All users list          | Low      |
| `GET /api/micro-lms/subjects/{id}`                   | Get subject             | Low      |
| `GET /api/micro-lms/subjects?limit=10000`            | All subjects            | Low      |
| `PUT /api/micro-lms/gradebook/`                      | Update gradebook        | Low      |
| `DELETE /api/micro-lms/gradebook/records/{id}`       | Delete gradebook record | Low      |
| `PUT /api/account/me/locale`                         | Change locale           | Low      |
| `GET /api/admin/configurations/{key}`                | Feature flags/config    | Low      |
| `POST /api/analytics`                                | Analytics tracking      | Low      |
| SSE endpoints (material processing, performance job) | Real-time progress      | Low      |

---

## 2. Features the Website Has That the KMP App Is Missing

### Entirely Missing Feature Screens

| Feature               | Website Location                            | Description                                                               |
|-----------------------|---------------------------------------------|---------------------------------------------------------------------------|
| **Events**            | `/events`, `/events/:slug`                  | Event listing (active/archived), detail view, registration, .ics download |
| **News**              | `/news`                                     | News articles from the university                                         |
| **Grants**            | `/grants`, `/master-grants`, `/grants-2026` | Grant status, applications (bachelor/master)                              |
| **Dashboard**         | `/dashboard`                                | Main hub landing page (content unknown, lazy-loaded chunk)                |
| **Admission**         | `/admission`                                | Admission forms and status                                                |
| **Onboarding**        | `/onboarding`                               | First-time enrollee onboarding flow                                       |
| **Support/Cases**     | `/case-list`                                | Support ticket system (Tinkoff Informer integration)                      |
| **Course Management** | `/courses/manage/*`                         | Teacher/admin course editing (themes, longreads, materials, settings)     |
| **Longread Editor**   | `/courses/manage/.../longreads/:id`         | Full material editor (markdown, video, file, audio, image, exercises)     |
| **Teacher Tasks**     | `/tasks/students-tasks`                     | Task evaluation view for teachers                                         |
| **Reports**           | `/reports/*`                                | Performance reports, gradebook, grades upload                             |
| **Timetable Admin**   | `/timetable` (admin part)                   | Timetable registration config management                                  |

### Partially Present Features (missing functionality)

| Feature           | What KMP Has                     | What Website Adds                                                                                                                                                                                                   |
|-------------------|----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Notifications** | List view                        | Mark-as-read, unread badge polling (every 5 min), category filtering (Education/Others), notification grouping                                                                                                      |
| **Tasks**         | List with filtering, detail view | Quiz-type tasks (SingleChoice, MultipleChoice, NumberMatch, StringMatch, OpenText), timed quizzes with countdown, auto-save answers (500ms debounce), attempt management (start/complete), evaluation feedback form |
| **Course Detail** | Overview with themes             | Exercise preview with countdown chips (open date, deadline, timer, attempts), MarkStatus (passed/pristine/blocked/inProgress) per theme/longread                                                                    |
| **Profile**       | Basic profile display            | Enrollee profile editing, education level display, language switching, achievements/experience tab                                                                                                                  |
| **Late Days**     | Prolong and cancel               | Visibility conditions (exercise type = Coding, specific roles), balance display in late days editor, max validation (1..availableDays)                                                                              |

---

### Specific Feature Gaps (from latest analysis)

| Gap Description                                                                                  | Priority   |
|--------------------------------------------------------------------------------------------------|------------|
| KMP app has `createComment` but website also supports edit/delete comments -- KMP is missing edit/delete | **Medium** |
| KMP app has `fetchTaskEvents` but website defines 18 event types -- verify KMP handles all types | **Medium** |
| Website has complete quiz/attempt system (5 question types, timed attempts, auto-save) -- KMP has no quiz support at all | **High** |
| Website supports 512MB file uploads with specific extension validation -- check KMP upload limits | **Medium** |
| Website has task comments with file attachments -- check if KMP supports attachment in comments   | **Medium** |

---

## 3. Data Model Differences

### Task States

| State         | Website                                      | KMP App                                              |
|---------------|----------------------------------------------|------------------------------------------------------|
| `backlog`     | Yes                                          | Yes                                                  |
| `inProgress`  | Yes                                          | Yes                                                  |
| `review`      | Yes                                          | Yes                                                  |
| `evaluated`   | Yes                                          | Yes                                                  |
| `failed`      | Yes                                          | Yes                                                  |
| `hasSolution` | Not explicit (inProgress + submitAt != null) | Virtual state in KMP (inProgress + submitAt != null) |
| `revision`    | Not found in website enums                   | KMP normalizes `rework` -> `revision`                |
| `rework`      | Not found in website enums                   | KMP normalizes `rework` -> `revision`                |
| `rejected`    | Not found in website enums                   | KMP normalizes `rejected` -> `failed`                |

The website uses only 5 task states. The KMP app defines additional virtual/normalized states (
`hasSolution`, `revision`/`rework`). This suggests the website may handle these edge cases
differently or the API returns these states even though the website JS doesn't enumerate them.

### Task Object Shape

| Field                                  | Website                    | KMP App                            |
|----------------------------------------|----------------------------|------------------------------------|
| `quizSessionId`                        | Present                    | **Missing**                        |
| `currentAttemptId`                     | Present                    | **Missing**                        |
| `evaluatedAttemptId`                   | Present                    | **Missing**                        |
| `lastAttemptId`                        | Present                    | **Missing**                        |
| `attemptStartedAt`                     | Present                    | **Missing**                        |
| `exercise.type`                        | `"Coding"` / `"Questions"` | Not distinguished in model         |
| `exercise.timer`                       | Duration string            | **Missing**                        |
| `exercise.settings.attemptsLimit`      | Present                    | **Missing**                        |
| `exercise.settings.evaluationStrategy` | `"Last"` / `"Best"`        | **Missing**                        |
| `exercise.questions`                   | Full question objects      | **Missing**                        |
| `scoreSkillLevel`                      | Present                    | **Missing**                        |
| `isLateDaysEnabled`                    | Present                    | Not in model (checked differently) |

### Question Types (entirely missing from KMP)

The website supports 5 question types with distinct answer UIs:

- `OpenText` -- free text input
- `SingleChoice` -- radio buttons
- `MultipleChoice` -- checkboxes
- `NumberMatch` -- numeric input
- `StringMatch` -- exact string input

### Material Discriminator Types

Website has 8 material types:

```
Coding, Questions, Markdown, VideoPlatform, File, Audio, Video, Image
```

KMP `LongreadMaterial` model does not distinguish these explicitly. The KMP app renders materials
but without type-specific UI components.

### Notification Model

| Field                   | Website                                    | KMP App             |
|-------------------------|--------------------------------------------|---------------------|
| `id` (numeric)          | Present                                    | Not in model        |
| `groupingKey`           | Present                                    | **Missing**         |
| `icon`                  | `"Education"` / `"News"` / `"ServiceDesk"` | **Missing**         |
| `startDate` / `endDate` | Present                                    | **Missing**         |
| `previewImageUri`       | Present                                    | **Missing**         |
| `link.target`           | `"Blank"` / `"Self"`                       | **Missing**         |
| Category filtering      | Education=1, Other=2                       | No category support |
| Grouped notifications   | `unionCount`, `collapsed`                  | No grouping         |

### Course Model

| Feature                      | Website                                                       | KMP App              |
|------------------------------|---------------------------------------------------------------|----------------------|
| Course states                | Draft, Published                                              | Not tracked in model |
| Publish/archive workflow     | Full lifecycle (publish, archive, unarchive, return-to-draft) | No state management  |
| Theme/Longread `publishDate` | Deferred publish date support                                 | Not in model         |
| Longread types               | Common (lesson) vs Handout (teacher material)                 | Not distinguished    |
| MarkStatus per item          | passed/pristine/blocked/inProgress                            | Not tracked          |

---

## 4. UI Behavior Differences

### Navigation

| Aspect       | Website                                                        | KMP App                                              |
|--------------|----------------------------------------------------------------|------------------------------------------------------|
| Structure    | Two separate Angular apps (root + learn) with sidebar + header | Single app with ChildPages tabs + ChildStack details |
| Tabs         | Sidebar with role-based menu items                             | Bottom nav: Home, Courses, Tasks, Files, Scanner     |
| Breadcrumbs  | Full breadcrumb trail from route data                          | No breadcrumbs                                       |
| Deep linking | URL-based with slugs and IDs                                   | Decompose navigation stack                           |
| Error pages  | Dedicated 404, 503, error, unsupported browser pages           | No dedicated error screens                           |

### Task Interaction

| Aspect              | Website                                                                                          | KMP App                         |
|---------------------|--------------------------------------------------------------------------------------------------|---------------------------------|
| Start task          | `PUT /tasks/{id}/start` with confirmation dialog for timed quizzes                               | `startTask` endpoint available  |
| Submit task         | Complex form with URL + attachments (max 5 files, 1GB each)                                      | `submitTask` endpoint available |
| Quiz answering      | Real-time auto-save (500ms debounce), question navigation, attempt management                    | No quiz support                 |
| Timer               | Countdown with color-coded indicator (green/yellow/red), blinking at <10%, auto-submit on expiry | No timer support                |
| Evaluation feedback | Post-evaluation poll (bad/neutral/good + details)                                                | No feedback mechanism           |
| canDeactivate       | Prevents leaving page with unsaved form changes                                                  | No equivalent                   |

### Late Days UI

| Aspect           | Website                                               | KMP App                                             |
|------------------|-------------------------------------------------------|-----------------------------------------------------|
| Visibility       | Only for Coding exercises, only with specific roles   | Shows for all tasks with appropriate state          |
| Balance display  | Shows available balance with calendar icon            | Shows balance                                       |
| Editor           | Number input with min=1, max=availableDays validation | Slider or input                                     |
| Cancel condition | `task.lateDays > 0 AND task.isLateDaysEnabled`        | `lateDays > 0 AND effectiveDeadline > 24h from now` |

The cancel condition differs: the website checks `isLateDaysEnabled` flag, while the KMP app checks
if the effective deadline is more than 24 hours from now. These may be equivalent server-side
checks, or a genuine behavioral difference.

### Course Content Display

| Aspect             | Website                                                                                                                                                                                                 | KMP App                    |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------|
| Material rendering | Type-specific components: markdown (with KaTeX math, PrismJS highlighting for 12+ languages), video player (platform + direct), audio player, image (with scale), file download, exercise (coding/quiz) | Basic material rendering   |
| Content editor     | Full rich text (TUI editor), drag-and-drop reordering, copy/paste materials, publish date scheduling                                                                                                    | Not applicable (view only) |
| Progress tracking  | MarkStatus (passed/pristine/blocked/inProgress) per theme/longread                                                                                                                                      | No progress indicators     |
| Exercise preview   | Open date countdown, deadline countdown, timer display, attempts count                                                                                                                                  | Basic exercise info        |

### Notifications

| Aspect             | Website                                      | KMP App          |
|--------------------|----------------------------------------------|------------------|
| Polling            | Every 5 minutes for unread count             | No polling       |
| Sidebar            | Slide-out sidebar with grouped notifications | Full-screen list |
| Categories         | Education / Others with tab switching        | No categories    |
| Mark as read       | Batch mark-as-read API                       | No mark-as-read  |
| Push notifications | Service Worker push via VAPID                | Not implemented  |
| Badge              | Unread count badge on notification icon      | No badge         |

---

## 5. Task State Handling Differences

### Website State Machine

```
BACKLOG (not started)
  -> Start (PUT /tasks/{id}/start)
  -> If quiz with timer: confirmation dialog first
  -> Response: { quizSessionId }
  -> If quiz: auto-start first attempt

IN_PROGRESS
  Coding:
    -> Fill form (solutionUrl + attachments)
    -> Submit (PUT /tasks/{id}/submit)
    -> Can change answer before deadline
    -> After deadline: auto-submitted

  Quiz:
    -> Answers auto-saved (debounce 500ms)
    -> Complete test -> completeAttempt
    -> Timer expiry -> auto-submit
    -> If attempts remain -> start new attempt

EVALUATED / FAILED
  -> Score display: score/maxScore + extraScore + skillLevel
  -> Evaluation feedback (if conditions met)
```

### KMP App State Machine

```
backlog -> inProgress -> hasSolution -> review -> evaluated/revision/failed

- hasSolution is virtual: inProgress + submitAt != null
- rework normalized to revision, rejected to failed
- Active filter: backlog, inProgress, hasSolution, revision, rework, review
- Archive filter: evaluated, failed, rejected
- Sort: evaluated/failed/review at bottom; rest by deadline ascending
```

Key differences:

1. **Website has no `hasSolution` concept** -- it tracks `inProgress` with solution data directly
2. **Website has no `revision`/`rework` states** -- these may exist in the API but the website JS
   only handles 5 states
3. **KMP sorting logic** puts completed tasks at bottom; website uses server-side pagination with
   explicit active/archived tabs
4. **Quiz task lifecycle** is entirely absent from KMP

---

## 6. Late Days Implementation Differences

### Website Implementation

Source: `learn/chunk-DPBB7AEG.js`, `learn/chunk-TQM5AJH7.js`, `learn/chunk-PODCHSSJ.js`

- **Balance source**: `GET /students/me` -> `lateDaysBalance`
- **Prolong visible when**: user has `ProlongLateDaysTask` role AND exercise type is `Coding`
- **Cancel visible when**: user has `CancelLateDaysTask` role AND `task.lateDays > 0` AND
  `task.isLateDaysEnabled`
- **Max days**: input validation `min(1), max(availableDays)` where
  `availableDays = lateDaysBalance`
- **No explicit 7-day limit** in client-side validation (may be server-enforced)
- **No 24-hour cancel window** in client code (KMP has this)
- **Dialog UI**: Shows task exercise name and course name, Russian labels
- **Success toast**: "Дедлайн перенесен" with `lateDaysUsed` count
- **Cancel confirmation**: Dialog with "Списанные late days вернутся"
- **States where not allowed**: Not explicitly excluded by state in the client; the prolong button
  is shown based on role + exercise type, not task state

### KMP App Implementation

From ARCHITECTURE.md:

- Max 7 days extension per task
- Cannot extend in: review, evaluated, revision, rework
- Cancel allowed if lateDays > 0 AND effectiveDeadline > 24h from now

Differences:

1. **Max cap**: KMP enforces 7-day max; website uses `availableDays = lateDaysBalance` (no hardcoded
   cap)
2. **State restrictions**: KMP blocks prolong in review/evaluated/revision/rework; website restricts
   by exercise type (Coding only) and role, not by task state
3. **Cancel condition**: KMP requires effectiveDeadline > 24h; website requires `isLateDaysEnabled`
   flag
4. **Exercise type filter**: Website only shows prolong for Coding tasks; KMP does not filter by
   exercise type

---

## 7. Authentication Differences

| Aspect            | Website                                                             | KMP App                                                        |
|-------------------|---------------------------------------------------------------------|----------------------------------------------------------------|
| Auth method       | Cookie-based session (`bff.cookie`) with XSRF protection            | Cookie-based (`bff.cookie`) captured via WebView               |
| Session check     | `GET /api/account/me` returns UserToken or 401                      | `GET /api/hub/students/me` (different endpoint) for validation |
| Token format      | Full JWT claims (sub, roles, groups, resource_access)               | Cookie string only, no role parsing                            |
| XSRF              | `XSRF-TOKEN` cookie -> `X-XSRF-TOKEN` header on mutations           | Not implemented                                                |
| Role-based access | Granular role checks (50+ roles) in route guards and template pipes | No role-based UI differentiation                               |
| Interceptors      | 5 interceptors (auth, 503, hub-rewrite, error-sanitize, XSRF)       | Basic cookie injection                                         |
| Circuit breaker   | Client-side URL blocking from server config                         | Not implemented                                                |

---

## 8. Technology & Architecture Differences

| Aspect             | Website                            | KMP App                                      |
|--------------------|------------------------------------|----------------------------------------------|
| Framework          | Angular 21.1.4 (zoneless)          | Compose Multiplatform                        |
| UI library         | Taiga UI 4.73.0                    | Material 3 (custom theme)                    |
| State management   | RxJS observables + Angular Signals | MVI (StateFlow + Channel)                    |
| Navigation         | Angular Router with lazy loading   | Decompose (ChildPages + ChildStack)          |
| Markdown rendering | ngx-markdown + marked.js           | Not specified                                |
| Code highlighting  | PrismJS (12+ languages)            | Not implemented                              |
| Math rendering     | KaTeX                              | Not implemented                              |
| Video player       | TCS/Tinkoff video platform         | Not implemented                              |
| Rich text editor   | TUI editor (ProseMirror-based)     | Not applicable                               |
| Drag-and-drop      | Angular CDK DragDrop               | Up/Down arrows (simplified)                  |
| Analytics          | Tinkoff Statist + Perfume.js       | Not implemented                              |
| Error reporting    | micro-sentry (Sentry-compatible)   | kotlin-logging                               |
| Feature flags      | Flipt                              | Not implemented                              |
| Localization       | Russian + English with $localize   | Russian only                                 |
| Calendar           | ICS from server                    | ICS from user-configured Yandex Calendar URL |
| File upload        | TUS protocol (resumable)           | Presigned S3 URL upload                      |

---

## 9. Summary of High-Priority Gaps

Features that students actively use on the website but are missing/incomplete in KMP:

1. **Quiz task solving** -- timed quizzes with 5 question types (SingleChoice, MultipleChoice, StringMatch, NumberMatch, OpenText), auto-save (500ms debounce), attempt management (start/submit/complete)
2. **Task comment edit/delete** -- KMP has createComment but website supports full CRUD (edit and delete comments with `PUT/DELETE /comments/{id}`)
3. **Task event types** -- website defines 18 event types for task history; KMP needs to handle all of them
4. **Notification management** -- mark-as-read, unread badge with polling, category filtering
5. **Task start/submit flow** -- the full lifecycle including confirmation dialogs and timer
6. **Events listing and registration** -- browsing and signing up for university events
7. **News reading** -- viewing university news articles
8. **Exercise progress tracking** -- MarkStatus indicators on course content
9. **Role-based UI** -- showing/hiding features based on user roles
10. **XSRF protection** -- required for mutation requests on the website's API
11. **File upload validation** -- website enforces 512MB limit for task attachments with specific extension whitelist; KMP limits need verification
12. **Comment file attachments** -- website supports attaching files to task comments; KMP support needs verification
