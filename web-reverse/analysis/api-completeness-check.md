# API Completeness Check

Cross-reference of `api-endpoints.md` (manually compiled summary) against `all-files-api-scan.md` (
automated grep scan) and `learn-business-logic-1-detailed.md` (business logic analysis), plus all
other analysis files.

Date: 2026-03-22

---

## 1. Endpoints in all-files-api-scan.md NOT in api-endpoints.md

### 1.1 Enrollee Endpoints -- Methods Differ

The summary lists `GET /api/hub/enrollees/me` and `GET /api/hub/enrollees/onboarding/me`, but the
scan (section 1.8, chunk-2RNYUVXP.js) reveals additional methods:

| Method   | Path                               | Status in summary                                                    |
|----------|------------------------------------|----------------------------------------------------------------------|
| **PUT**  | `/api/hub/enrollees/me`            | MISSING -- update enrollee profile                                   |
| **POST** | `/api/hub/enrollees/onboarding/me` | MISSING -- submit onboarding (summary lists GET, but actual is POST) |

The summary shows `GET /api/hub/enrollees/onboarding/me` ("Onboarding state"), but the scan and
detailed analysis both confirm this is `POST` (submit onboarding data), not `GET`.

### 1.2 Notification Hub -- Method Discrepancies

The summary lists:

- `GET /api/notification-hub/notifications/in-app` -- list notifications
- `PUT /api/notification-hub/notifications/in-app/read` -- mark as read

But the scan (section 1.10, chunk-KUQINTYY.js) and all detailed analysis files consistently show:

- **POST** `/api/notification-hub/notifications/in-app` -- list notifications (not GET)
- **POST** `/api/notification-hub/notifications/in-app/read` -- mark as read (not PUT)

These are **incorrect HTTP methods** in the summary.

### 1.3 Grant Parameter Difference

The summary uses `{semesterId}` in `/api/hub/grants/me/{semesterId}/{type}/active`, but the scan (
section 1.8, chunk-H2UV6AAT.js) uses `{year}` and the detailed analysis confirms the parameter is a
year (e.g., `getActiveItGrant$(year)`). This is a naming inconsistency -- the parameter is a **year
**, not a semesterId.

### 1.4 No Net-New Path Endpoints

Beyond the method/parameter discrepancies above, the scan does not contain any entirely new URL
paths that are absent from the summary. The summary has good path coverage for all endpoints found
in the automated scan.

---

## 2. Endpoints in learn-business-logic-1-detailed.md NOT in api-endpoints.md

### 2.1 Task Start and Submit -- MISSING

These are the most critical missing endpoints for the mobile app:

| Method  | Path                               | Description                                             | Source                        |
|---------|------------------------------------|---------------------------------------------------------|-------------------------------|
| **PUT** | `/api/micro-lms/tasks/{id}/start`  | Start a task (returns `{quizSessionId}` for quiz tasks) | chunk-PODCHSSJ.js, class `xa` |
| **PUT** | `/api/micro-lms/tasks/{id}/submit` | Submit task answer/solution                             | chunk-PODCHSSJ.js, class `xa` |

The summary's "Tasks (Admin)" section only lists `GET /tasks`, `GET /tasks/{id}`, and
`PUT /tasks/{id}/reviewer`. The student-facing start/submit operations are completely missing.

### 2.2 Polls/Feedback -- MISSING

| Method   | Path                                           | Description                      | Source                        |
|----------|------------------------------------------------|----------------------------------|-------------------------------|
| **POST** | `/api/micro-lms/polls`                         | Submit evaluation feedback       | chunk-PODCHSSJ.js, class `ka` |
| **GET**  | `/api/micro-lms/polls/{entityType}/{entityId}` | Check if feedback already posted | chunk-PODCHSSJ.js, class `ka` |

These are entirely absent from the summary.

### 2.3 Attempt/Quiz Session -- PARTIALLY MISSING

The business logic mentions three attempt-related operations delegated to an injected service (
`Ge`):

| Method    | Path Pattern                 | Description                                         | Source                        |
|-----------|------------------------------|-----------------------------------------------------|-------------------------------|
| (unknown) | Quiz session start attempt   | `startAttempt$(sessionId)` -> returns `{attemptId}` | chunk-PODCHSSJ.js, class `ti` |
| (unknown) | Submit single attempt answer | `submitAttempt$(attemptId, answer)`                 | chunk-PODCHSSJ.js, class `ti` |
| (unknown) | Complete attempt             | `completeAttempt$(sessionId, attemptId)`            | chunk-PODCHSSJ.js, class `ti` |

The exact HTTP methods and URL patterns for these are not fully visible (they are delegated to an
injected service defined in a chunk not yet analyzed). However, based on the task lifecycle flow,
these likely map to endpoints under `/api/micro-lms/` with patterns like:

- `POST /api/micro-lms/quiz-sessions/{sessionId}/attempts` (start)
- `PUT /api/micro-lms/attempts/{attemptId}` (submit answer)
- `PUT /api/micro-lms/quiz-sessions/{sessionId}/attempts/{attemptId}/complete` (finish)

These are entirely absent from the summary but are critical for quiz functionality.

### 2.4 Statist Analytics -- URL Details Missing

The business logic reveals specific analytics endpoint paths:

| Method   | Path                                                    | Description               |
|----------|---------------------------------------------------------|---------------------------|
| **POST** | `https://api-statist.tinkoff.ru/gateway/v1/events`      | Production analytics push |
| **POST** | `https://api-statist.dev-tcsgroup.io/gateway/v1/events` | Dev analytics push        |

The summary mentions `https://api-statist.tinkoff.ru` generically but does not include the
`/gateway/v1/events` path.

### 2.5 Feedback/Claims API -- MISSING

| Method   | Path                      | Description         | Source                        |
|----------|---------------------------|---------------------|-------------------------------|
| **POST** | `${feedbackApiUrl}/claim` | Send feedback/claim | chunk-4SKQR4GO.js, class `Mu` |

This is an external service (Tinkoff feedback) not mentioned in the summary.

---

## 3. Specific Endpoints Flagged for Verification

### 3.1 `/polls` endpoints

**Status: MISSING from summary.**

Found in `learn-business-logic-1-detailed.md` section 1 (chunk-PODCHSSJ.js):

- `POST ${baseUrl}/polls` -- post evaluation feedback
- `GET ${baseUrl}/polls/${entityType}/${entityId}` -- check if feedback exists

Related roles found in scan: `lms-polls-submit-poll_answer`, `lms-polls-view-poll`.

### 3.2 `/tasks/{id}/start`

**Status: MISSING from summary.**

Found in `learn-business-logic-1-detailed.md` section 1 (chunk-PODCHSSJ.js):

- `PUT ${baseUrl}/tasks/${taskId}/start` -- body: `{}` (empty)
- Used in task lifecycle: Backlog -> InProgress transition
- For quiz tasks, the response includes `{quizSessionId}`

### 3.3 `/tasks/{id}/submit`

**Status: MISSING from summary.**

Found in `learn-business-logic-1-detailed.md` section 1 (chunk-PODCHSSJ.js):

- `PUT ${baseUrl}/tasks/${taskId}/submit` -- body: `answer` object
- For coding tasks: `{solutionUrl?, attachments[]}`
- For quiz tasks: `{value, questionId, sessionId, type}` (auto-saved with 500ms debounce)

---

## 4. HTTP Service Methods Not Reflected in Summary

### 4.1 Student Hub URL Rewriting

The learn-app-config-detailed.md reveals an HTTP interceptor (`VB`) that rewrites `/api/hub/` to
`/api/student-hub/` for users with the `UseStudentHub` role. This affects 29 URL patterns that are
not mentioned in the summary:

```
/api/hub/documents/addresses/generate-full
/api/hub/documents/addresses/me
/api/hub/admin/documents/addresses/{id}
/api/hub/admin/contracts/student/{id}
/api/hub/admin/contracts/sync-ep-transfer
/api/hub/documents/keys
/api/hub/documents/education/me
/api/hub/admin/documents/education/{id}
/api/hub/education-info/me
/api/hub/admin/education-info/student/{id}
/api/hub/admin/education-info/student/{id}/program/{programId}
/api/hub/admin/education-info/check-by-emails/{email}
/api/hub/documents/identity/me
/api/hub/admin/documents/identity/{id}
/api/hub/documents/master-data
/api/hub/master-data
/api/hub/students/master-data
/api/hub/contracts/student/{id}
/api/hub/education-info/student/{id}
/api/hub/education-info/student/{id}/program/{programId}
/api/hub/orders/{id}
/api/hub/orders/student/{id}
/api/hub/orders/search
/api/hub/orders
/api/hub/orders/upload
/api/hub/orders/me
/api/hub/admin/orders
/api/hub/students/me
/api/hub/admin/students
```

Most of these are admin or document-management endpoints not directly relevant to the mobile student
app, but `/api/hub/orders/me`, `/api/hub/education-info/me`, `/api/hub/documents/education/me`,
`/api/hub/documents/identity/me`, and `/api/hub/documents/addresses/me` may be student-facing.

### 4.2 Auth Navigation Endpoints

The root-app-main-detailed.md reveals additional auth-related endpoints:

| Path             | Description                  | Status in summary |
|------------------|------------------------------|-------------------|
| `/auth`          | Redirect to Keycloak sign-in | MISSING           |
| `/registrations` | Sign-up page                 | MISSING           |

These are navigation redirects rather than API calls, but they are part of the auth flow.

### 4.3 Circuit Breaker Config

The root app fetches a circuit breaker configuration to block certain URLs:

| Path              | Description                                          | Status in summary |
|-------------------|------------------------------------------------------|-------------------|
| (config endpoint) | Blocked URL patterns for client-side circuit breaker | Not documented    |

The exact endpoint URL is not visible in the analyzed chunks.

---

## 5. Patterns Suggesting Undiscovered Endpoints

### 5.1 Lazy-Loaded Feature Chunks Not Yet Analyzed

The route configuration references many lazy-loaded chunks that have NOT been analyzed for API
endpoints:

| Chunk               | Feature           | Likely Contains                                        |
|---------------------|-------------------|--------------------------------------------------------|
| `chunk-NNLUD57V.js` | Dashboard         | Dashboard data API (possibly aggregated student stats) |
| `chunk-XYNDJCEW.js` | Profile           | Student/enrollee profile detail CRUD                   |
| `chunk-4ACXTDD6.js` | Grants (bachelor) | Grant application/confirmation API                     |
| `chunk-XBPQU6OA.js` | Grants (master)   | Master grants API                                      |
| `chunk-O5N3J4AB.js` | Grants 2026       | 2026 grant cycle API                                   |
| `chunk-MYPYERCK.js` | Admission         | Admission form submission API                          |
| `chunk-DU2L5VZK.js` | Application form  | Future student application API                         |
| `chunk-65PL4UYO.js` | Onboarding        | Onboarding flow API (multi-step)                       |
| `chunk-J2FWVIQV.js` | News list         | News list endpoint (possibly paginated)                |
| `chunk-V2W4HQVK.js` | Support/cases     | Support ticket creation/listing API                    |

### 5.2 Template Literal URL Patterns with Dynamic Segments

The following dynamic URL patterns are used in the codebase and suggest additional parameterized
endpoints:

1. **Quiz session/attempt URLs**: The `Ge` service (attempt API) likely uses template literals like:
   ```
   `${baseUrl}/quiz-sessions/${sessionId}/attempts`
   `${baseUrl}/attempts/${attemptId}`
   ```
   These are referenced in the task lifecycle but the actual chunk defining them is not analyzed.

2. **Grant year parameter**: `/api/hub/grants/me/${year}/${type}/active` uses a dynamic year --
   there may be historical grant endpoints.

3. **Admission year**: `/api/event-builder/admissions/2025/state` has a hardcoded year `2025`. This
   likely becomes `2026` or is parameterized in newer code.

4. **Education info with program**: `/api/hub/education-info/student/{id}/program/{programId}`
   suggests a nested resource pattern.

5. **Video platform upload**: The tus-js-client integration uses server-provided URLs for video
   upload (not the main API gateway). These upload URLs come from a response to creating a video
   material and are separate from the documented endpoints.

### 5.3 Comments/History Endpoints

The student task UI has tabs for "Comments" and "History" (`cu-task-comments`, `cu-task-history`),
controlled by roles `ViewTaskComment` and `ViewTaskEvents`. The actual API endpoints for
fetching/posting comments and task history/events are not visible in the analyzed chunks but
certainly exist. Likely patterns:

- `GET /api/micro-lms/tasks/{id}/comments`
- `POST /api/micro-lms/tasks/{id}/comments`
- `DELETE /api/micro-lms/tasks/{id}/comments/{commentId}`
- `GET /api/micro-lms/tasks/{id}/events` (activity log)

The analytics tracking confirms comment operations exist:

- `course_send_comment_exercise_{id}`
- `course_delete_comment_exercise_{id}`

### 5.4 News List Endpoint

The summary documents `GET /api/event-builder/public/news/{id}` (single item) and
`GET /api/event-builder/public/news/file/{id}` (file), but there is no **list** endpoint for news.
The news route (`chunk-J2FWVIQV.js`) is lazy-loaded and not analyzed, but a list endpoint almost
certainly exists (likely `GET /api/event-builder/public/news` with pagination).

### 5.5 Performance/Gradebook Detail Endpoints

The summary lists `GET /api/micro-lms/courses/{id}/performance` but the report routes include:

- `/reports/student-performance` -- individual student performance
- `/reports/student-grade-book` -- individual student gradebook
- `/reports/grades-upload` -- grades upload

These routes likely hit additional endpoints not yet documented.

---

## 6. Summary of Gaps

### Critical Missing Endpoints (Student-Facing)

| Priority | Endpoint                                       | Why Critical                               |
|----------|------------------------------------------------|--------------------------------------------|
| **P0**   | `PUT /tasks/{id}/start`                        | Core task workflow -- starting assignments |
| **P0**   | `PUT /tasks/{id}/submit`                       | Core task workflow -- submitting solutions |
| **P0**   | Quiz attempt lifecycle (start/submit/complete) | Quiz functionality entirely missing        |
| **P1**   | `POST /polls`                                  | Evaluation feedback                        |
| **P1**   | `GET /polls/{entityType}/{entityId}`           | Feedback check                             |
| **P1**   | Task comments CRUD                             | Comments tab functionality                 |
| **P1**   | Task events/history                            | History tab functionality                  |

### Method Corrections Needed

| Endpoint                                            | Summary Says | Actually Is |
|-----------------------------------------------------|--------------|-------------|
| `/api/notification-hub/notifications/in-app` (list) | GET          | **POST**    |
| `/api/notification-hub/notifications/in-app/read`   | PUT          | **POST**    |
| `/api/hub/enrollees/onboarding/me`                  | GET          | **POST**    |

### Missing Write Operations

| Endpoint                    | Description             |
|-----------------------------|-------------------------|
| `PUT /api/hub/enrollees/me` | Update enrollee profile |

### Parameter Name Correction

| Endpoint                                        | Summary Says | Actually Is |
|-------------------------------------------------|--------------|-------------|
| `/api/hub/grants/me/{semesterId}/{type}/active` | semesterId   | **year**    |

### Potentially Relevant but Unanalyzed

- Dashboard data endpoints
- Profile detail endpoints
- Grant application endpoints
- Admission form endpoints
- News list endpoint
- Support/case ticket endpoints
- Student education document endpoints
