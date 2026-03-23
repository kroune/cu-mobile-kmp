# Learn App -- Remaining Files Comprehensive Scan

## 1. API Endpoints Found

### Base API URLs (InjectionTokens)

- `BASE_API_URL` -> `/api/micro-lms` (chunk-XZDI5NMD.js)
- `LMS_BASE_API_URL` -> `/api/micro-lms` (chunk-NJXQAJX7.js)

### Direct API Paths (from chunk-HTCSOUNZ.js -- shared/platform services)

| Endpoint                                           | Method          | Description                                          |
|----------------------------------------------------|-----------------|------------------------------------------------------|
| `/api/account/me/locale`                           | PUT             | Change user locale                                   |
| `/api/hub/avatars/me`                              | GET             | User avatar URL                                      |
| `/api/notification-hub/notifications/in-app`       | POST            | List in-app notifications                            |
| `/api/notification-hub/notifications/in-app/stats` | GET             | Notification stats                                   |
| `/api/notification-hub/notifications/in-app/read`  | POST            | Mark notifications read                              |
| `/api/event-builder/admissions/2025/state`         | GET             | Admissions state                                     |
| `/api/hub/grants/me`                               | GET             | User grants list                                     |
| `/api/hub/grants/me/{id}/{type}/active`            | GET             | Active grant by type (InformationTechnology, Design) |
| `/api/hub/enrollees/me`                            | GET             | Current enrollee profile                             |
| `/api/hub/students/me`                             | GET             | Current student profile                              |
| `/api/admin/configurations`                        | GET/POST/DELETE | Admin configuration CRUD                             |
| `/api/admin/configurations/{key}`                  | GET             | Get specific configuration                           |

### LMS-Specific API Paths (relative to BASE_API_URL = /api/micro-lms)

| Service File      | Endpoint Pattern                                                  | Method              | Description                          |
|-------------------|-------------------------------------------------------------------|---------------------|--------------------------------------|
| chunk-UTCFHQBH.js | `/gradebook/subjects`                                             | GET                 | Get gradebook subjects for semesters |
| chunk-UTCFHQBH.js | `/semesters` (Re.Root)                                            | GET                 | Get semesters list                   |
| chunk-HBFGWOGI.js | `/tasks/student`                                                  | GET                 | Get student tasks                    |
| chunk-2LITL7XF.js | `/tasks/{id}/reject`                                              | PUT                 | Reject task                          |
| chunk-2LITL7XF.js | `/tasks/{id}/evaluate`                                            | PUT                 | Evaluate task                        |
| chunk-2LITL7XF.js | `/tasks/{id}/grant-extra-score`                                   | PUT                 | Grant extra score                    |
| chunk-2LITL7XF.js | `/tasks/{id}/refuse-extra-score`                                  | PUT                 | Refuse extra score                   |
| chunk-2LITL7XF.js | `/tasks/{id}/prolong`                                             | PUT                 | Prolong task deadline                |
| chunk-PODCHSSJ.js | `/polls`                                                          | POST                | Submit feedback poll                 |
| chunk-PODCHSSJ.js | `/polls/{pollId}/{studentId}`                                     | GET                 | Check if poll posted                 |
| chunk-PODCHSSJ.js | `/tasks/{id}/start`                                               | PUT                 | Start a task                         |
| chunk-PODCHSSJ.js | `/tasks/{id}/submit`                                              | PUT                 | Submit task answer                   |
| chunk-QCPI75QD.js | `/content/download-link`                                          | GET                 | Get file download link               |
| chunk-QCPI75QD.js | `/content/upload-link`                                            | GET                 | Get file upload link                 |
| chunk-CSB6UCKE.js | `/calendar-events/slot-management/config`                         | GET/PUT             | Registration state config            |
| chunk-CSB6UCKE.js | `/calendar-events/reports/lesson-registrations`                   | GET (blob)          | Download timetable report            |
| chunk-RUM7ADXP.js | `/students/me/timetables`                                         | GET                 | Get student timetable                |
| chunk-RUM7ADXP.js | `/students/me/timetables/{courseId}/{eventType}/{eventRowNumber}` | GET/POST            | Event options & assignment           |
| chunk-ZGW7Q7IQ.js | `/courses/{courseId}/activities`                                  | GET/POST/PUT/DELETE | Course activities CRUD               |
| chunk-VDAJCDWK.js | `/performance/uploads`                                            | GET/POST            | Performance import uploads           |
| chunk-VDAJCDWK.js | `/performance/uploads/{id}`                                       | GET                 | Import result                        |
| chunk-VDAJCDWK.js | `/performance/uploads/{id}/event-stream`                          | SSE                 | Import status stream                 |
| chunk-FBVLZLM6.js | `/tasks/{id}`                                                     | GET                 | Get task by ID                       |
| chunk-FBVLZLM6.js | `/tasks/{id}/events`                                              | GET                 | Get task events/history              |
| chunk-FBVLZLM6.js | `/{entity}/{entityId}/comments`                                   | GET/POST/PUT/DELETE | Comments CRUD                        |

## 2. HTTP Service Calls Summary

The main HTTP call patterns observed:

- GET: Fetching data (tasks, subjects, timetables, configurations, grants, notifications)
- PUT: Updating resources (evaluate tasks, reject tasks, prolong deadlines, save configs, submit
  tasks)
- POST: Creating resources (comments, feedback polls, activities, upload imports, notifications)
- DELETE: Removing resources (activities, comments, configurations)

File upload uses a two-step process:

1. GET upload-link to get a presigned URL
2. PUT file content directly to the presigned URL with appropriate Content-Disposition header

## 3. Component Selectors (cu-* prefixed)

From across all learn files, the following `cu-*` Angular component selectors were found:

### Course & Performance Components

- `cu-course-member` -- individual course member display
- `cu-course-member-list` -- list of course members
- `cu-course-members-group` -- grouped course members
- `cu-course-card` -- course card preview
- `cu-category-badge` -- course category badge
- `cu-skill-level` -- skill level display (Basic/Intermediate/Advanced)
- `cu-skill-level-control` -- skill level form control
- `cu-skill-level-multiselect-control` -- multi-select skill level control
- `cu-course-preview` -- course preview card with cover image

### Task & Evaluation Components

- `cu-score` -- score display
- `cu-counter` -- counter badge
- `cu-task-history` -- task event history
- `cu-task-comments` -- task comments section
- `cu-task-layout` -- task page layout wrapper
- `cu-task-coding-overview` -- coding task overview
- `cu-task-coding-solution` -- coding task solution view
- `cu-task-questions-item-*` -- various question type components (single-choice, multiple-choice,
  auto-evaluation, etc.)
- `cu-task-questions-nav` -- question navigation
- `cu-teacher-task-info` -- teacher task info panel
- `cu-teacher-task-score` -- teacher score input
- `cu-teacher-task-extra-score` -- extra score input
- `cu-teacher-task-actions` -- teacher action buttons (reject/evaluate)
- `cu-teacher-task-tabs` -- teacher task tab navigation
- `cu-teacher-task-coding-evaluate-block` -- coding task evaluation block
- `cu-teacher-task-questions-player` -- teacher question player
- `cu-teacher-task-questions-player-navigation` -- question navigation for teacher
- `cu-teacher-task-questions-item` -- teacher question item
- `cu-attempts-select` -- attempt selection dropdown
- `cu-lms-editor-view` -- LMS editor content viewer

### Table & Data Components

- `cu-table-actions` -- table row action buttons
- `cu-table-actions-trigger` -- trigger for table actions
- `cu-table-link-action` -- link-style table action
- `cu-table-button-action` -- button-style table action
- `cu-pagination` -- pagination control
- `cu-empty-content` -- empty state display
- `cu-copy` -- copy-to-clipboard button
- `cu-tooltip` -- tooltip wrapper

### Grade Book & Performance

- `cu-grade-book-empty-content` -- grade book empty state
- `cu-students-performance-list` -- mobile student performance list
- `cu-students-performance-table` -- desktop student performance table
- `cu-students-course-performance-list` -- student course performance list
- `cu-students-course-performance-table` -- student course performance table
- `cu-student-activity-performance-table` -- activity-level performance table
- `cu-student-course-performance` -- student course performance page wrapper
- `cu-exercise-status-badge` -- exercise status badge

### UI Utilities

- `cu-segmented` -- segmented control
- `cu-expandable-search-input` -- expandable search input
- `cu-multiselect-filter` -- multi-select filter
- `cu-multiselect-searchable-list` -- searchable multi-select list

## 4. Route Paths

From chunk-HTCSOUNZ.js (main app routing, line 1848):

```
Root: /learn
  |-- no-layout/
  |   `-- courses/ (lazy: chunk-BAETR5PM.js -> coursesRoutes)
  |-- / (with breadcrumb "Obuchenie")
  |   |-- courses/ (lazy: chunk-BAETR5PM.js -> coursesRoutes)
  |   |-- reports/ (lazy: chunk-DYLXJNPQ.js -> reportsRoutes)
  |   |-- tasks/   (lazy: chunk-3HLKTFK2.js -> tasksRoutes)
  |   |-- timetable/ (lazy: chunk-QBA2FXNQ.js -> routes, conditional on visibility)
  |   |-- ** -> redirect to courses
  |   `-- (default fallback component)
  `-- ** -> fallback component (XB)
```

From chunk-554VJVOQ.js / chunk-ZAK4CPW3.js (file management routes):

```
  / -> root component
  / -> children:
    |-- FileAdd
    |-- ManualAdd
  ** -> redirect to ""
```

From chunk-6KSXS3ZZ.js: Contains Angular Router core implementation (not app-specific routes).

## 5. Injection Tokens

### Platform/Shared Tokens (chunk-HTCSOUNZ.js)

| Token                                         | Purpose                                                   |
|-----------------------------------------------|-----------------------------------------------------------|
| `SERVICE_UNAVAILABLE_REDIRECT_URL_TOKEN`      | URL for service-unavailable redirect                      |
| `SERVICE_UNAVAILABLE_HANDLER_TOKEN`           | Handler for service unavailability                        |
| `AUTH_URL_TOKEN`                              | Authentication URL from dynamic config                    |
| `LEVEL_DIRECTIVE_PARENT_INJECTION_TOKEN`      | Parent injection for level directive                      |
| `LANGUAGE_CODES_TOKEN`                        | Supported language codes [ru, en]                         |
| `LANGUAGE_OPTIONS_TOKEN`                      | Language display options                                  |
| `LMS_URL_TOKEN`                               | LMS base URL                                              |
| `LOGOUT_HANDLER_TOKEN`                        | Logout handler function                                   |
| `MENU_ITEMS_TOKEN`                            | Menu items configuration                                  |
| `MOBILE_MENU_STATE_TOKEN`                     | Mobile menu open/close state                              |
| `SELF_SERVICE_URL_TOKEN`                      | Self-service URL                                          |
| `TIME_URL_TOKEN`                              | Time service URL                                          |
| `LAYOUT_FOOTER_CONFIGURATION_TOKEN`           | Footer config (includes t.me/hello_centraluniversity_bot) |
| `HEADER_LOGO_LINK_URL_TOKEN`                  | Header logo link URL                                      |
| `HEADER_CUSTOM_ACTIONS_TOKEN`                 | Header custom actions                                     |
| `LINK_CLICK_LISTENER_TOKEN`                   | Link click listener                                       |
| `SIDEBAR_ROOT_ITEMS_PROVIDER_INJECTION_TOKEN` | Sidebar root items                                        |
| `TREE_MANAGER_INJECTION_TOKEN`                | Tree manager for sidebar                                  |
| `NOTIFICATION_IN_APP_CONFIG_TOKEN`            | Notification config                                       |
| `FEATURE_FLAGS_CLIENT_INITIALIZED_TOKEN`      | Feature flags init state                                  |
| `FEATURE_FLAGS_PROVIDER_URL_TOKEN`            | Feature flags provider URL                                |
| `STUDENT_HANDBOOK_URL_TOKEN`                  | Student handbook URL (note.cu.ru)                         |
| `BLOCK_REQUEST_KEY_TOKEN`                     | Block request cache key                                   |

### LMS-Specific Tokens

| Token                                           | File              | Purpose                                          |
|-------------------------------------------------|-------------------|--------------------------------------------------|
| `BASE_API_URL`                                  | chunk-XZDI5NMD.js | Base LMS API URL (/api/micro-lms)                |
| `LMS_BASE_API_URL`                              | chunk-NJXQAJX7.js | LMS base API URL (/api/micro-lms)                |
| `STUDENT_TASKS_FILTER_CACHE_KEY_TOKEN`          | chunk-XVW65H4W.js | Cache key for student task filters               |
| `TASKS_FILTER_CACHE_KEYS_PREFIX_TOKEN`          | chunk-QXVQLTTJ.js | Prefix for task filter cache keys                |
| `TASKS_SORTING_CACHE_KEYS_PREFIX_TOKEN`         | chunk-QXVQLTTJ.js | Prefix for task sorting cache keys               |
| `EXECUTION_ACCESSOR`                            | chunk-YSPW2MHC.js | Execution accessor function                      |
| `LONGREED_NAVIGATION_INTERSECTION_PARAMS_TOKEN` | chunk-AGJHJTQY.js | Longread navigation intersection observer params |
| `DEBOUNCE_SEARCH_TOKEN`                         | chunk-K3PAG7Z6.js | Search debounce delay (300ms)                    |
| `LMS_FILES_UPLOADER`                            | chunk-QCPI75QD.js | LMS file uploader service                        |
| `FILE_READER`                                   | chunk-VDAJCDWK.js | File reader service                              |

### Messenger/Comments Tokens (chunk-FBVLZLM6.js)

| Token                                           | Purpose                            |
|-------------------------------------------------|------------------------------------|
| `CONTENT_FILES_UPLOADER_UPLOAD_DIRECTORY_TOKEN` | Upload directory for content files |
| `CONTENT_FILES_UPLOADER_ERROR_MAP_TOKEN`        | Error messages for file upload     |
| `MESSENGER_GUID_TOKEN`                          | Messenger GUID                     |
| `MESSENGER_SERVICE_TOKEN`                       | Messenger service                  |
| `EDITED_MESSAGE_COMPONENT_TOKEN`                | Component for edited message       |
| `EMPTY_MESSAGE_COMPONENT_TOKEN`                 | Component for empty message list   |
| `MESSAGE_INPUT_COMPONENT_TOKEN`                 | Component for message input        |
| `MESSAGE_TEMPLATE_TOKEN`                        | Component for message template     |

## 6. Large File Summaries (>20KB)

### chunk-GEDG5BC4.js (37KB)

**libphonenumber-js library.** Contains international phone number parsing, validation, and
formatting utilities. Includes phone number metadata structures, country calling codes, number type
detection (MOBILE, FIXED_LINE, TOLL_FREE, etc.), and extension parsing. Not LMS-specific -- a
vendored third-party library.

### chunk-TIVWZ5KK.js (35KB)

**TUI Editor (TipTap) integration.** Contains the `tui-edit-link` component and the TipTap rich text
editor adapter (`ot` class extending `ae`). Provides editor operations (bold, italic, lists, tables,
headings, links, anchors, images, code blocks, etc.), link editing popup, and anchor management.
Used for the LMS rich text editor in content/task creation.

### chunk-X67WS2RG.js (32KB)

**Student course performance page.** Contains the `cu-student-course-performance` component and
`cu-student-activity-performance-table`. Displays student performance data including activity
scores, weights, averages, totals. Has exercise status badges with states: InProgress, Backlog,
Review, Evaluated, Failed. Russian UI strings for performance views. Includes sorting, filtering by
activities/statuses, and formula tabs.

### chunk-VOUIKCOZ.js (28KB)

**Russian (ru) locale/translations for TUI components.** Contains localized strings for all TUI UI
components -- card number labels, toolbar tool names, editor controls, file upload messages, date
picker labels, pagination text, etc. Purely a translation/i18n resource file, no business logic.

### chunk-XSAJ4CI5.js (25KB)

**Linkify.js URL/email detection library.** Contains a full URL/email/link detection and parsing
library (linkifyjs). Implements a state machine for tokenizing text to find URLs, email addresses,
and scheme-based links. Includes TLD database, Unicode TLD support, and TipTap editor
autolink/paste/click link plugins. Not LMS-specific -- a vendored library.

### chunk-4LFQDMNU.js (22KB)

**TUI Table components library.** Implements the `tuiTable` directive and supporting components:
`tuiTh` (table header with sorting), `tuiTd` (table cell), `tuiTr` (table row), `tuiTbody` (table
body with collapsible groups), `tuiThead` (sticky header), `tuiThGroup` (header group), `tuiCell` (
cell template), `tuiSortBy`/`tuiSortable` (sorting directives). Includes sticky header support and
resize handles.

### chunk-IRQEKOPL.js (21KB)

**Web performance monitoring (Perfume.js integration).** Contains client-side performance metrics
collection: Web Vitals (FCP, LCP, CLS, FID, INP, TTFB, TBT), resource timing, navigation timing,
network information. Sends metrics to `coretech.web.metrics` / `performance.rum` via statist client.
Includes debounced metric reporting. Not LMS-specific -- infrastructure monitoring code.

### chunk-BYAHOTYO.js (21KB)

**TUI File upload components.** Contains `tui-file` (file display with preview, loading state, error
state, delete button), `tui-files` (file list with expand/collapse), `tuiInputFiles` (drag-and-drop
file input label), and file validation utilities (size check `tuiSize`, format check `tuiFormat`).
Includes file size formatting (bytes/KB/MB).

### chunk-3DXJXGDN.js (20KB)

**TUI Calendar & Date/Time input components.** Contains `tui-calendar-range` (date range picker with
period presets), `tuiInputTime` (time input with mask), `tuiInputDate` (date input with calendar
dropdown), `tuiInputDateTime` (combined date-time input), and a custom
`cuNewTuiDayTimeToISODateTransformer` directive. Also includes `cuIsoDateToTuiDayTime` pipe for ISO
date parsing.

### chunk-A6I53KWK.js (20KB)

**Course creation/editing UI components.** Contains `cu-skill-level` (skill level display with icons
for None/Basic/Intermediate/Advanced), `cu-skill-level-multiselect-control` (form control for
selecting skill levels), `cu-image-picker` (course cover image picker dialog), `cu-course-preview` (
course card preview with cover selection), and `cu-course-card` (course card component). Used in
course management admin views.

## 7. Key Observations

1. **No new API endpoints beyond what was already documented** -- All LMS API calls use the
   `/api/micro-lms` base with the patterns already identified.

2. **Platform API endpoints** are separate from LMS: notifications (`/api/notification-hub`),
   grants (`/api/hub`), enrollees, students, admin configurations, and account locale changes all
   live under different microservice paths.

3. **Large vendor chunks** (GEDG5BC4, XSAJ4CI5, IRQEKOPL) are third-party libraries (
   libphonenumber-js, linkifyjs, perfume.js) and contain no LMS business logic.

4. **TUI component chunks** (TIVWZ5KK, VOUIKCOZ, 4LFQDMNU, BYAHOTYO, 3DXJXGDN) are Taiga UI library
   components/locales, providing the UI framework used by the app.

5. **LMS-specific large chunks** (X67WS2RG, A6I53KWK) contain student performance views and course
   management UI. These are the most relevant for mobile app implementation.

6. **Main routing structure** is: `/learn` -> courses (default), reports, tasks, timetable. The
   timetable route is conditionally loaded based on feature visibility.

7. **Telegram bot link**: `t.me/hello_centraluniversity_bot` is referenced in footer configuration.
