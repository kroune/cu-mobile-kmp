# Completeness Audit: CU LMS Website Reverse Engineering

Audit date: 2026-03-22
Auditor: Claude Opus 4.6

This audit cross-references every route, UI element, API endpoint, and lazy chunk
from the JavaScript analysis against the pages actually visited (documented in README.md).

---

## 1. Routes That Exist but Were NOT Visited

### 1.1 Root App -- Unvisited Routes

| Route               | Chunk               | Status          | Notes                                                                         |
|---------------------|---------------------|-----------------|-------------------------------------------------------------------------------|
| `/onboarding`       | `chunk-65PL4UYO.js` | **NOT VISITED** | Enrollee onboarding flow. Requires Enrollee role + not-yet-onboarded status.  |
| `/grants`           | `chunk-4ACXTDD6.js` | **NOT VISITED** | Bachelor grants page. Guarded by `Jt` (bachelor grants availability).         |
| `/master-grants`    | `chunk-XBPQU6OA.js` | **NOT VISITED** | Master grants page. Guarded by `Vi` (master grants availability).             |
| `/grants-2026`      | `chunk-O5N3J4AB.js` | **NOT VISITED** | 2026 grants page. Guarded by `ks` (grants-2026 guard).                        |
| `/admission`        | `chunk-MYPYERCK.js` | **NOT VISITED** | Admission info page (available). Guarded by server-driven availability check. |
| `/admission` (form) | `chunk-DU2L5VZK.js` | **NOT VISITED** | Admission application form. Requires PreStudent or Student role.              |
| `/news`             | `chunk-J2FWVIQV.js` | **NOT VISITED** | News list/detail pages. Guarded by enrollee/student role check.               |
| `/not-found`        | inline              | **NOT VISITED** | 404 page. Shows role-based CTA.                                               |
| `/maintenance`      | inline              | **NOT VISITED** | 503 maintenance page.                                                         |
| `/unsupported`      | inline              | **NOT VISITED** | Unsupported browser page.                                                     |
| `/error`            | `chunk-ZN6X5W4H.js` | **NOT VISITED** | Error screen (e.g., InconsistentUserData).                                    |

### 1.2 Learn App -- Unvisited Routes

| Route                                                                            | Chunk               | Status          | Notes                                             |
|----------------------------------------------------------------------------------|---------------------|-----------------|---------------------------------------------------|
| `/courses/manage/actual`                                                         | `chunk-4DX4QBZY.js` | **NOT VISITED** | Teacher/admin course management table.            |
| `/courses/manage/actual/:courseId`                                               | `chunk-BF6EGGKK.js` | **NOT VISITED** | Course editor (themes, longreads, drag-and-drop). |
| `/courses/manage/actual/:courseId/settings/main`                                 | `chunk-7VUA3I56.js` | **NOT VISITED** | Course main settings.                             |
| `/courses/manage/actual/:courseId/settings/students`                             | `chunk-7VUA3I56.js` | **NOT VISITED** | Course students settings.                         |
| `/courses/manage/actual/:courseId/settings/listeners`                            | `chunk-7VUA3I56.js` | **NOT VISITED** | Course listeners settings.                        |
| `/courses/manage/actual/:courseId/settings/groups`                               | `chunk-7VUA3I56.js` | **NOT VISITED** | Course groups settings.                           |
| `/courses/manage/actual/:courseId/settings/teachers`                             | `chunk-7VUA3I56.js` | **NOT VISITED** | Course teachers settings.                         |
| `/courses/manage/actual/:courseId/settings/reviewers`                            | `chunk-7VUA3I56.js` | **NOT VISITED** | Course reviewers settings.                        |
| `/courses/manage/actual/:courseId/settings/activities`                           | `chunk-7VUA3I56.js` | **NOT VISITED** | Course activities settings.                       |
| `/courses/manage/actual/:courseId/themes/:themeId/longreads/:longreadId`         | `chunk-23VGFXOH.js` | **NOT VISITED** | Longread editor (teacher).                        |
| `/courses/manage/actual/:courseId/themes/:themeId/longreads/:longreadId/preview` | `chunk-23VGFXOH.js` | **NOT VISITED** | Longread preview (teacher).                       |
| `/courses/manage/archived`                                                       | `chunk-4DX4QBZY.js` | **NOT VISITED** | Archived course management.                       |
| `/courses/manage/archived/:courseId`                                             | same                | **NOT VISITED** | Same as actual but with isArchived=true.          |
| `/tasks/students-tasks`                                                          | `chunk-3HLKTFK2.js` | **NOT VISITED** | Teacher task evaluation list.                     |
| `/tasks/students-tasks/:taskId`                                                  | `chunk-3HLKTFK2.js` | **NOT VISITED** | Teacher task evaluation detail.                   |
| `/tasks/student-tasks/:taskId`                                                   | `chunk-PODCHSSJ.js` | **NOT VISITED** | Direct student task view (redirects to longread). |
| `/reports/students-performance/actual`                                           | `chunk-XUXMCUIT.js` | **NOT VISITED** | Teacher performance list (actual).                |
| `/reports/students-performance/archived`                                         | `chunk-XUXMCUIT.js` | **NOT VISITED** | Teacher performance list (archived).              |
| `/reports/students-performance/:courseId`                                        | `chunk-XUXMCUIT.js` | **NOT VISITED** | Teacher course performance detail.                |
| `/reports/grades-upload/:uploadId`                                               | `chunk-XUXMCUIT.js` | **NOT VISITED** | Grades upload detail.                             |
| `/reports/grade-book`                                                            | `chunk-XUXMCUIT.js` | **NOT VISITED** | Teacher gradebook.                                |
| `/no-layout/*`                                                                   | various             | **NOT VISITED** | Routes rendered without sidebar layout.           |
| `/maintenance` (learn)                                                           | inline              | **NOT VISITED** | Learn app maintenance page.                       |

### 1.3 Profile Sub-Routes -- Partially Visited

The README lists these as visited:

- `/profile/info` -- visited
- `/profile/experience` -- visited (achievements)
- `/profile/experience/edit-bio` -- visited
- `/profile/experience/add-experience` -- visited

The routing.md mentions:

- `/profile` -- main profile page
- `/profile/experience-tab` -- achievements (from profile menu in chunk-OIDSCTRK.js)

The chunk `chunk-XYNDJCEW.js` was not analyzed in detail, so there may be additional
sub-routes within the profile that were not discovered or visited.

### 1.4 Events Sub-Routes -- Visited

All defined event routes were visited:

- `/events` (list, both active and past tabs) -- VISITED
- `/events/:slug` (event detail) -- VISITED

### 1.5 Support/Case Sub-Routes -- Visited

- `/case-list` (active) -- VISITED
- `/case-list/archived` -- VISITED (noted in README as "archived cases")
- `/case-list/case/:id` -- VISITED (case detail)

Note: The case-list sub-routes come from `chunk-V2W4HQVK.js` which was loaded
but whose internal route structure was not fully analyzed. There may be additional
sub-routes within the case system.

---

## 2. UI Elements, Tabs, Menus, and Dialogs NOT Explored

### 2.1 Event Filters and Features Not Exercised

| Element                                         | Source              | Status                                                                                                 |
|-------------------------------------------------|---------------------|--------------------------------------------------------------------------------------------------------|
| "Only mine" toggle on events list               | `chunk-3AKKBUTX.js` | **NOT EXERCISED** -- The toggle to show only events the user registered for was not explicitly tested. |
| Event registration flow (apply button)          | `chunk-N2T4BRFD.js` | **UNCLEAR** -- No evidence that the registration/apply button was clicked.                             |
| Event calendar download (.ics)                  | `chunk-N2T4BRFD.js` | **NOT EXERCISED** -- The "Add to calendar" button was not clicked.                                     |
| Event broadcast link                            | `chunk-N2T4BRFD.js` | **NOT EXERCISED** -- The "Watch broadcast" feature was not tested.                                     |
| Bitrix inline form (event registration variant) | `chunk-N2T4BRFD.js` | **NOT EXERCISED** -- External registration forms were not tested.                                      |

### 2.2 Notification Sidebar Features Not Exercised

| Element                                     | Source                      | Status                                                                                                                       |
|---------------------------------------------|-----------------------------|------------------------------------------------------------------------------------------------------------------------------|
| Category tab switching (Education / Others) | `chunk-HTCSOUNZ.js`         | **UNCLEAR** -- Notification sidebar was viewed but tab switching between "Учеба" and "Другое" was not explicitly documented. |
| Mark notifications as read                  | API: `POST .../in-app/read` | **NOT EXERCISED** -- No evidence that mark-as-read was tested.                                                               |
| Notification grouping (collapsed/expanded)  | `chunk-HTCSOUNZ.js`         | **NOT EXERCISED** -- Grouped notifications with unionCount were not tested.                                                  |
| "Already seen" separator                    | `cu-notification-separator` | **NOT EXERCISED** -- UI separator between new and old notifications.                                                         |

### 2.3 Task UI Features Not Exercised

| Element                                | Source              | Status                                                                                                                       |
|----------------------------------------|---------------------|------------------------------------------------------------------------------------------------------------------------------|
| Quiz-type task solving                 | `chunk-PODCHSSJ.js` | **NOT EXERCISED** -- The entire quiz flow (SingleChoice, MultipleChoice, NumberMatch, StringMatch, OpenText) was not tested. |
| Task timer (countdown)                 | `chunk-PODCHSSJ.js` | **NOT EXERCISED** -- Timed tasks with countdown and auto-submit were not tested.                                             |
| Attempt management (start new attempt) | `chunk-PODCHSSJ.js` | **NOT EXERCISED** -- Quiz attempt start/complete flow was not tested.                                                        |
| Evaluation feedback dialog             | `chunk-PODCHSSJ.js` | **NOT EXERCISED** -- Post-evaluation "How was the review?" poll was not tested.                                              |
| Task coding form (URL + file upload)   | `chunk-PODCHSSJ.js` | **NOT EXERCISED** -- The submit-solution form was not tested from the student perspective.                                   |
| Late days editor dialog                | `chunk-TQM5AJH7.js` | **NOT EXERCISED** -- The late days prolongation dialog with number input was not opened.                                     |
| Late days cancel confirmation          | `chunk-DPBB7AEG.js` | **NOT EXERCISED**                                                                                                            |
| Task canDeactivate guard               | `chunk-PODCHSSJ.js` | **NOT EXERCISED** -- Navigation-away prevention for unsaved changes.                                                         |

### 2.4 Course View Features Not Exercised

| Element                                                       | Source                       | Status                                                                                                                                                                               |
|---------------------------------------------------------------|------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Exercise preview chips (open date, deadline, timer, attempts) | `chunk-4SKQR4GO.js`          | **PARTIALLY EXERCISED** -- Task card was viewed but the countdown chip states (opening countdown, deadline countdown, timer display, attempts limit) were not systematically tested. |
| MarkStatus indicators (passed/pristine/blocked/inProgress)    | `chunk-Y2I2TH22.js`          | **NOT VERIFIED** -- Progress status per theme/longread was not explicitly documented.                                                                                                |
| Course content drag-and-drop reordering                       | `chunk-BF6EGGKK.js`          | **NOT EXERCISED** -- Teacher-only feature, course editor not visited.                                                                                                                |
| Video player (TCS/Tinkoff platform)                           | `chunk-NXLFTGU7.js`          | **NOT EXERCISED** -- No video material playback was tested.                                                                                                                          |
| Audio player                                                  | `cu-longread-material-audio` | **NOT EXERCISED**                                                                                                                                                                    |
| Image material with scaling                                   | `cu-longread-material-image` | **NOT EXERCISED**                                                                                                                                                                    |
| KaTeX math rendering in markdown                              | optional external            | **NOT EXERCISED**                                                                                                                                                                    |
| Mermaid diagram rendering in markdown                         | optional external            | **NOT EXERCISED**                                                                                                                                                                    |
| PrismJS syntax highlighting                                   | optional external            | **NOT EXERCISED**                                                                                                                                                                    |

### 2.5 Profile Features Not Exercised

| Element                             | Source                | Status                                                        |
|-------------------------------------|-----------------------|---------------------------------------------------------------|
| Language switcher (Russian/English) | `chunk-OIDSCTRK.js`   | **NOT EXERCISED** -- PUT `/api/account/me/locale`             |
| Enrollee profile editing            | `chunk-2RNYUVXP.js`   | **NOT APPLICABLE** -- User is likely a Student, not Enrollee. |
| Profile avatar update               | `/api/hub/avatars/me` | **NOT EXERCISED** -- Only GET (display) was tested.           |

### 2.6 Timetable Features Not Exercised

| Element                                  | Source              | Status                                                                                        |
|------------------------------------------|---------------------|-----------------------------------------------------------------------------------------------|
| Timetable registration (slot management) | `chunk-CSB6UCKE.js` | **NOT EXERCISED** -- The registration config (open/close dates) admin feature was not tested. |
| Timetable report download                | `chunk-CSB6UCKE.js` | **NOT EXERCISED** -- GET `/calendar-events/reports/lesson-registrations` (blob).              |

### 2.7 Search and Filter Components

| Element                                         | Source                            | Status                                                                           |
|-------------------------------------------------|-----------------------------------|----------------------------------------------------------------------------------|
| `cu-expandable-search-input`                    | `chunk-5URS2G45.js`               | **NOT EXERCISED** -- Expandable search bar used across the app.                  |
| `cu-multiselect-filter`                         | `chunk-5URS2G45.js`               | **NOT EXERCISED** -- Filter dropdowns (e.g., course/semester/status filters).    |
| `cu-multiselect-searchable-list`                | `chunk-5URS2G45.js`               | **NOT EXERCISED** -- Searchable dropdown lists in filter panels.                 |
| Task list filters (by status, course, semester) | inferred from paginated task list | **NOT EXERCISED** -- Filter parameters in task list API calls were not explored. |

### 2.8 Gradebook and Performance Detail Features

| Element                                  | Source                                         | Status                                                                    |
|------------------------------------------|------------------------------------------------|---------------------------------------------------------------------------|
| Performance job formation (SSE progress) | `POST /performance/courses/{id}/jobs`          | **NOT EXERCISED** -- The teacher-initiated performance report generation. |
| Gradebook template download              | `GET /gradebook/{subjectId}/{semesterId}/file` | **NOT EXERCISED** -- Blob download of gradebook Excel template.           |
| Gradebook update (teacher)               | `PUT /gradebook/`                              | **NOT EXERCISED** -- Teacher gradebook editing.                           |
| Gradebook record deletion                | `DELETE /gradebook/records/{id}`               | **NOT EXERCISED** -- Individual gradebook record removal.                 |

---

## 3. API Endpoints Indicating Unvisited Pages

### 3.1 Polls/Feedback API -- No Polls Page Visited

| Endpoint                                           | Evidence                                                                                      |
|----------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `POST /api/micro-lms/polls`                        | Submit evaluation feedback. No polls page or feedback dialog was opened.                      |
| `GET /api/micro-lms/polls/{entityType}/{entityId}` | Check if feedback already posted. Implies a feedback submission UI exists and was not tested. |

### 3.2 Orders API -- Referenced but Never Visited

The Student Hub URL rewriter in `chunk-HTCSOUNZ.js` lists 29 URL patterns including:

- `/api/hub/orders/{id}`
- `/api/hub/orders/student/{id}`
- `/api/hub/orders/search`
- `/api/hub/orders`
- `/api/hub/orders/upload`
- `/api/hub/orders/me`
- `/api/hub/admin/orders`

These suggest an **orders system** (possibly enrollment orders or document requests)
that has no corresponding route or page in the visited list. The orders endpoints
are likely accessed from within the profile or a sub-page not yet discovered.

### 3.3 Documents API -- Referenced but Never Visited

The URL rewriter also lists:

- `/api/hub/documents/addresses/generate-full`
- `/api/hub/documents/addresses/me`
- `/api/hub/documents/keys`
- `/api/hub/documents/education/me`
- `/api/hub/documents/identity/me`
- `/api/hub/documents/master-data`

These indicate a **documents management system** (address, education, identity documents)
that was not visited. This may be part of the profile or admission flows.

### 3.4 Education Info API -- Referenced but Never Visited

- `/api/hub/education-info/me`
- `/api/hub/admin/education-info/student/{id}`
- `/api/hub/admin/education-info/student/{id}/program/{programId}`
- `/api/hub/admin/education-info/check-by-emails/{email}`

These suggest student education program information management that was not explored.

### 3.5 Contracts API -- Referenced but Never Visited

- `/api/hub/admin/contracts/student/{id}`
- `/api/hub/admin/contracts/sync-ep-transfer`
- `/api/hub/contracts/student/{id}`

Student contract management was not visited.

### 3.6 Admin API Endpoints -- Never Visited

| Endpoint                           | Purpose                                                                 |
|------------------------------------|-------------------------------------------------------------------------|
| `GET /api/admin/configurations`    | Configuration management. The admin app at `/admin/` was never visited. |
| `POST /api/admin/configurations`   | Save configurations.                                                    |
| `DELETE /api/admin/configurations` | Delete configurations.                                                  |

### 3.7 Analytics and Feedback APIs

| Endpoint                      | Purpose                                                       |
|-------------------------------|---------------------------------------------------------------|
| `POST /api/analytics`         | Analytics event tracking. No analytics dashboard was visited. |
| `POST {feedbackApiUrl}/claim` | Statist feedback/claims API. Never triggered.                 |

### 3.8 Content/File Storage APIs -- Never Directly Tested

| Endpoint                                   | Purpose                         |
|--------------------------------------------|---------------------------------|
| `GET /api/micro-lms/content/download-link` | Presigned download URL.         |
| `GET /api/micro-lms/content/upload-link`   | Presigned upload URL.           |
| `POST /api/micro-lms/attachments`          | Attach uploaded file to entity. |

File upload/download flows were not tested from the UI.

### 3.9 Users API

| Endpoint                               | Purpose                                                         |
|----------------------------------------|-----------------------------------------------------------------|
| `GET /api/micro-lms/users?limit=10000` | All users list. Used in teacher course settings. Never visited. |

### 3.10 Subjects API

| Endpoint                                  | Purpose                                                                       |
|-------------------------------------------|-------------------------------------------------------------------------------|
| `GET /api/micro-lms/subjects/{id}`        | Get subject. Used in gradebook. Never visited (teacher gradebook not opened). |
| `GET /api/micro-lms/subjects?limit=10000` | All subjects. Used in gradebook filters.                                      |

---

## 4. Lazy-Loaded Chunks That Were Never Triggered

### 4.1 Root App -- Unloaded Lazy Chunks

| Chunk               | Route            | Feature               | Status                                                                                                                                         |
|---------------------|------------------|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `chunk-65PL4UYO.js` | `/onboarding`    | Onboarding flow       | **NEVER LOADED** -- Requires Enrollee role.                                                                                                    |
| `chunk-4ACXTDD6.js` | `/grants`        | Bachelor grants       | **NEVER LOADED** -- Guarded by availability check.                                                                                             |
| `chunk-XBPQU6OA.js` | `/master-grants` | Master grants         | **NEVER LOADED** -- Guarded by availability check.                                                                                             |
| `chunk-MYPYERCK.js` | `/admission`     | Admission (available) | **NEVER LOADED** -- Guarded by server-driven availability.                                                                                     |
| `chunk-DU2L5VZK.js` | `/admission`     | Admission form        | **NEVER LOADED** -- Requires PreStudent or Student role.                                                                                       |
| `chunk-O5N3J4AB.js` | `/grants-2026`   | Grants 2026           | **NEVER LOADED** -- Guarded by grants-2026 flag.                                                                                               |
| `chunk-J2FWVIQV.js` | `/news`          | News list/detail      | **NEVER LOADED** -- Guarded by enrollee/student news guard.                                                                                    |
| `chunk-ZN6X5W4H.js` | `/error`         | Error screen          | **NEVER LOADED** -- Only triggered by InconsistentUserData errors.                                                                             |
| `chunk-NNLUD57V.js` | `/dashboard`     | Dashboard             | **LOADED** but dashboard immediately redirects to `/learn/courses/view/actual` according to README. Dashboard content itself was not analyzed. |

### 4.2 Learn App -- Unloaded Lazy Chunks

| Chunk                  | Route/Feature                | Status                                                                             |
|------------------------|------------------------------|------------------------------------------------------------------------------------|
| `chunk-BF6EGGKK.js`    | Course editor (manage view)  | **NEVER LOADED** -- Teacher/admin course editing UI. Analyzed from static JS only. |
| `chunk-23VGFXOH.js`    | Longread editor              | **NEVER LOADED** -- 352KB chunk for material editing.                              |
| `chunk-7VUA3I56.js`    | Course settings sub-routes   | **NEVER LOADED** -- All 7 settings tabs.                                           |
| Various teacher chunks | Students-tasks, teacher-task | **NEVER LOADED** -- Teacher task evaluation view.                                  |

### 4.3 Chunks Loaded but Internal Content Partially Analyzed

| Chunk               | Size    | Feature            | Analysis Status                                                                                         |
|---------------------|---------|--------------------|---------------------------------------------------------------------------------------------------------|
| `chunk-XYNDJCEW.js` | unknown | Profile sub-routes | **LOADED** (profile visited) but **NOT FULLY ANALYZED** -- Internal sub-route structure not documented. |
| `chunk-V2W4HQVK.js` | unknown | Support case list  | **LOADED** but **NOT FULLY ANALYZED** -- Internal component analyzed only from route config.            |
| `chunk-NNLUD57V.js` | unknown | Dashboard routes   | **LOADED** but immediately redirects; dashboard content not analyzed.                                   |

---

## 5. Links and Features Discovered in Page Snapshots That Were Never Followed

### 5.1 Cross-App Navigation Links

| Link                                                 | Source                       | Status                                                                 |
|------------------------------------------------------|------------------------------|------------------------------------------------------------------------|
| Admin app at `https://my.centraluniversity.ru/admin` | Dynamic config `adminAppUrl` | **NEVER VISITED** -- Entirely separate Angular app for administrators. |
| TiMe (timetable) at `https://time.cu.ru`             | Dynamic config `tiMeUrl`     | **NEVER VISITED** -- External timetable application.                   |
| Python course at `lmsPythonCourseUrl`                | Dynamic config               | **NEVER VISITED**                                                      |
| News channel at `newsChannelUrl`                     | Dynamic config               | **NEVER VISITED**                                                      |
| Teletype chat at `teletypeId`                        | Dynamic config               | **NEVER VISITED**                                                      |

### 5.2 External Links from Event Detail Pages

| Link                                                   | Source              | Status             |
|--------------------------------------------------------|---------------------|--------------------|
| Bitrix registration forms (event registration variant) | `chunk-N2T4BRFD.js` | **NEVER FOLLOWED** |
| Broadcast URLs (event livestream links)                | `chunk-N2T4BRFD.js` | **NEVER FOLLOWED** |
| LMS course URIs from events                            | `chunk-N2T4BRFD.js` | **NEVER FOLLOWED** |

### 5.3 External Links from Course Content

| Link                    | Source                                          | Status                                      |
|-------------------------|-------------------------------------------------|---------------------------------------------|
| `polls.tbank.ru`        | Course feedback longread                        | **NOTED** but not followed (external poll). |
| Markdown material links | Analytics tracks `markdown_material_link_click` | **NEVER SYSTEMATICALLY FOLLOWED**           |

### 5.4 Sidebar Menu Items Not Followed

| Item                | Required Role                        | Status                                                                        |
|---------------------|--------------------------------------|-------------------------------------------------------------------------------|
| "Courses to manage" | `lms_view_sidebar_courses_to_manage` | **NEVER FOLLOWED** -- Teacher-only sidebar item leading to course management. |
| "Tasks to evaluate" | `lms_view_sidebar_tasks_to_evaluate` | **NEVER FOLLOWED** -- Teacher-only sidebar item for task evaluation.          |

### 5.5 Profile Menu Items

| Item                                             | Condition         | Status                                                            |
|--------------------------------------------------|-------------------|-------------------------------------------------------------------|
| "Achievements" link to `/profile/experience-tab` | Only for students | **VISITED** (as `/profile/experience`). Path may differ slightly. |
| "Logout" button                                  | Always visible    | **NOT TESTED**                                                    |

### 5.6 Support Informer Integration

| Element                                 | Source                                       | Status                                                                                |
|-----------------------------------------|----------------------------------------------|---------------------------------------------------------------------------------------|
| Informer web component script           | `informerScriptUrl` config                   | **NOT TESTED** -- External Tinkoff Forge informer overlay for in-app surveys/support. |
| Cobrowsing (screen sharing for support) | `@tinkoff/cobrowsing` in `chunk-KUQINTYY.js` | **NOT TESTED** -- Support screen-sharing feature.                                     |

---

## 6. Summary of Coverage Gaps

### 6.1 By Severity

**CRITICAL GAPS (pages with significant unique content that was never captured):**

1. `/news` -- Entire news feature (list, detail, attachments). Chunk never loaded.
2. `/grants`, `/master-grants`, `/grants-2026` -- All three grant pages. Chunks never loaded.
   Content structure unknown.
3. `/admission` -- Both admission variants (available info + application form). Chunks never loaded.
4. `/onboarding` -- Enrollee onboarding flow. Chunk never loaded.
5. `/courses/manage/*` -- Entire teacher course management tree (editor, settings with 7 tabs,
   longread editor). Never visited.
6. `/tasks/students-tasks` -- Teacher task evaluation view. Never visited.
7. `/reports/students-performance/*`, `/reports/grade-book`, `/reports/grades-upload/*` -- All
   teacher-facing report routes. Never visited.
8. `/admin` -- Entirely separate Angular admin app. Never visited.

**MODERATE GAPS (interactive features within visited pages that were not tested):**

9. Quiz task solving (questions, auto-save, timer, attempts) -- within task detail.
10. Task start/submit coding flow -- within task detail.
11. Evaluation feedback dialog -- within task detail.
12. Late days editor/cancel dialogs -- within task detail.
13. Notification mark-as-read and category switching -- within notification sidebar.
14. Video/audio material playback -- within longread view.
15. Search and filter components across the app.
16. Event registration and calendar download.

**LOW-PRIORITY GAPS (infrastructure/utility pages):**

17. `/not-found`, `/maintenance`, `/unsupported`, `/error` -- Error/status pages.
18. `/no-layout/*` -- Layout-less route branch (unknown purpose).
19. Language switching feature.
20. Timetable admin features (registration config, report download).

### 6.2 By Category

| Category                    | Total Routes                                   | Visited                                                  | Coverage |
|-----------------------------|------------------------------------------------|----------------------------------------------------------|----------|
| Root app -- student-facing  | 8 routes                                       | 5 (dashboard, events, case-list, profile, notifications) | **63%**  |
| Root app -- role-gated      | 6 routes (grants x3, admission x2, onboarding) | 0                                                        | **0%**   |
| Root app -- error/utility   | 4 routes                                       | 0                                                        | **0%**   |
| Root app -- not yet created | 1 route (news)                                 | 0                                                        | **0%**   |
| Learn app -- student view   | ~12 route patterns                             | ~11                                                      | **~92%** |
| Learn app -- teacher/admin  | ~20 route patterns                             | 0                                                        | **0%**   |
| Learn app -- error/utility  | 2 routes                                       | 0                                                        | **0%**   |
| **Overall**                 | **~53 unique route patterns**                  | **~16**                                                  | **~30%** |

### 6.3 API Endpoint Coverage

| API Category                             | Endpoints Discovered   | Accessed (by page visits) | Coverage |
|------------------------------------------|------------------------|---------------------------|----------|
| Auth/Session                             | 5                      | 1 (GET /account/me)       | 20%      |
| Courses (student)                        | 8                      | ~5                        | ~63%     |
| Courses (admin)                          | 15                     | 0                         | 0%       |
| Themes                                   | 7                      | ~2                        | ~29%     |
| Longreads                                | 8                      | ~2                        | ~25%     |
| Materials                                | 9                      | ~1                        | ~11%     |
| Tasks (student)                          | 4                      | ~2                        | ~50%     |
| Tasks (admin)                            | 3                      | 0                         | 0%       |
| Late days                                | 3                      | ~1                        | ~33%     |
| Performance                              | 5                      | ~3                        | ~60%     |
| Gradebook                                | 6                      | ~2                        | ~33%     |
| Calendar/Timetable                       | 3                      | ~1                        | ~33%     |
| Events                                   | 4                      | ~2                        | ~50%     |
| News                                     | 2                      | 0                         | 0%       |
| Notifications                            | 3                      | ~1                        | ~33%     |
| Hub/Profile                              | 6                      | ~2                        | ~33%     |
| Admin/Config                             | 4                      | 0                         | 0%       |
| Content/Files                            | 3                      | 0                         | 0%       |
| Users/Subjects                           | 3                      | 0                         | 0%       |
| Polls/Feedback                           | 2                      | 0                         | 0%       |
| Hub documents/orders/education/contracts | 20+ (from interceptor) | 0                         | 0%       |
| **Total**                                | **~120**               | **~25**                   | **~21%** |

### 6.4 Lazy Chunk Coverage

| App       | Total Lazy Chunks | Triggered by Navigation                                      | Coverage |
|-----------|-------------------|--------------------------------------------------------------|----------|
| Root app  | 14 feature chunks | 5 (events list, event detail, dashboard, profile, case-list) | 36%      |
| Learn app | 8 feature chunks  | 5 (course view, tasks, reports, timetable, student task)     | 63%      |
| **Total** | **22**            | **10**                                                       | **45%**  |

---

## 7. Recommendations for Further Exploration

### Priority 1 -- Student-visible features not yet visited

1. Navigate to `/news` to discover news list/detail page structure.
2. Navigate to `/grants` (if available) to see grant status display.
3. Trigger quiz-type task solving to capture question UI and auto-save behavior.
4. Click "Register" on an event to capture the event registration flow.
5. Click the notification mark-as-read and category tabs.

### Priority 2 -- Teacher/admin features

6. Navigate to `/courses/manage/actual` with a teacher account to see course editor.
7. Navigate to `/tasks/students-tasks` to see teacher task evaluation.
8. Navigate to `/reports/students-performance/actual` for teacher performance view.
9. Navigate to `/reports/grade-book` for teacher gradebook.

### Priority 3 -- Role-gated pages requiring specific account types

10. Login as an Enrollee to visit `/onboarding`.
11. Check if `/admission` is available for the current user.
12. Check if any grants pages are accessible.

### Priority 4 -- Infrastructure/utility pages

13. Navigate to `/not-found` to capture 404 page.
14. Navigate to `/maintenance` to capture 503 page.
15. Visit the admin app at `/admin` if authorized.
16. Visit TiMe at `https://time.cu.ru`.
