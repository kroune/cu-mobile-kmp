# API Endpoints — CU LMS Website

Extracted from minified JavaScript source files of `https://my.centraluniversity.ru`.

## Base URLs

| Token                               | Default                           | Description           |
|-------------------------------------|-----------------------------------|-----------------------|
| `LMS_BASE_API_URL`                  | `/api/micro-lms`                  | Learn app LMS API     |
| `BASE_AUTH_API_URL_TOKEN`           | `/api`                            | Auth API base         |
| `NEWS_API_BASE_URL`                 | `/api/event-builder/public/news`  | News API base         |
| Dynamic config `authUrl`            | `https://id.centraluniversity.ru` | Keycloak OIDC         |
| Dynamic config `statistEndpointUrl` | `https://api-statist.tinkoff.ru`  | Tinkoff analytics     |
| Dynamic config `fliptProviderUrl`   | `https://my.centraluniversity.ru` | Feature flags (Flipt) |

---

## Auth & Session (shared)

Source: `chunk-H2KAKJ6J.js` (learn), `chunk-ROFNVQNW.js` (root)

| Method | Path                       | Description                    |
|--------|----------------------------|--------------------------------|
| GET    | `/api/account/me`          | Get current session/user token |
| -      | `/account/signin/callback` | OIDC sign-in callback          |
| -      | `/account/signout`         | Logout                         |
| PUT    | `/api/account/me/locale`   | Update locale preference       |

**Session model**: `{ sub, roles, groups, resource_access: { "realm-management": { roles } } }`

---

## Hub / Profile

Source: root app chunks

| Method   | Path                                      | Description                                         |
|----------|-------------------------------------------|-----------------------------------------------------|
| GET      | `/api/hub/students/me`                    | Current student profile                             |
| GET      | `/api/hub/enrollees/me`                   | Enrollee info                                       |
| **PUT**  | **`/api/hub/enrollees/me`**               | **Update enrollee profile**                         |
| **POST** | **`/api/hub/enrollees/onboarding/me`**    | **Submit onboarding data (POST, not GET)**          |
| GET      | `/api/hub/avatars/me`                     | User avatar                                         |
| GET      | `/api/hub/grants/me`                      | All grants                                          |
| GET      | `/api/hub/grants/me/{year}/{type}/active` | Active grant by type (InformationTechnology/Design) |

### Student Profile (extended)

| Method | Path                                   | Description              |
|--------|----------------------------------------|--------------------------|
| GET    | `/api/hub/students/me/bio`             | Get student bio          |
| PUT    | `/api/hub/students/me/bio`             | Update student bio       |
| GET    | `/api/hub/students/me/contacts`        | Get student contacts     |
| PUT    | `/api/hub/students/me/contacts`        | Update student contacts  |
| GET    | `/api/hub/students/me/experiences`     | List experiences         |
| POST   | `/api/hub/students/me/experiences`     | Create experience        |
| PUT    | `/api/hub/students/me/experiences`     | Update experience        |
| DELETE | `/api/hub/students/me/experiences`     | Delete experience        |
| GET    | `/api/hub/students/me/general-info`    | Get general info         |
| GET    | `/api/hub/students/me/student-card`    | Get student card         |
| PUT    | `/api/hub/students/me/telegram`        | Update Telegram handle   |

### Enrollee (extended)

| Method | Path                                | Description            |
|--------|-------------------------------------|------------------------|
| GET    | `/api/hub/enrollees/track-type/me`  | Get enrollee track type|

---

## Activities & Documents

Source: root app chunks (comprehensive scan)

| Method | Path                                                | Description                        |
|--------|-----------------------------------------------------|------------------------------------|
| GET    | `/api/hub/activities-files/me`                      | List activity files                |
| DELETE | `/api/hub/activities-files/me/{id}`                 | Delete activity file               |
| GET    | `/api/hub/documents/activities/me`                  | List activity documents            |
| GET    | `/api/hub/documents/master-data/activities/search`  | Search master-data activities      |
| GET    | `/api/hub/education-info/me`                        | Get education info                 |
| GET    | `/api/hub/experience-files/me`                      | List experience files              |
| POST   | `/api/hub/experience-files/me`                      | Upload experience file             |
| DELETE | `/api/hub/experience-files/me/{id}`                 | Delete experience file             |

---

## Orders

Source: root app chunks (comprehensive scan)

| Method | Path                  | Description       |
|--------|-----------------------|-------------------|
| GET    | `/api/hub/orders/me`  | List user orders  |

---

## XP Games / Gamification

Source: root app chunks (comprehensive scan)

| Method | Path                                                    | Description               |
|--------|---------------------------------------------------------|---------------------------|
| GET    | `/api/hub/grants/xp-games/users-xp-states/me`          | Get user XP state         |
| GET    | `/api/hub/grants/xp-games/users-xp-states/me/points-history` | Get XP points history|
| GET    | `/api/hub/grants/xp-games/xp-grades`                   | List XP grade levels      |

---

## Certificates

Source: root app chunks (comprehensive scan)

| Method | Path                                                          | Description                        |
|--------|---------------------------------------------------------------|------------------------------------|
| GET    | `/api/certificates/user-certificates/me/published`            | List published user certificates   |
| GET    | `/api/certificates/user-certificates/{certId}/{type}/me`      | Get certificate by ID and type     |
| POST   | `/api/certificates/user-certificates/{certId}/{type}/me`      | Submit certificate action          |
| POST   | `/api/certificates/user-certificates/{certId}/{type}/me/complete-poll` | Complete certificate poll |

---

## CU Guide / Telegram

Source: root app chunks (comprehensive scan)

| Method | Path                                                       | Description                      |
|--------|------------------------------------------------------------|----------------------------------|
| GET    | `/api/cu-guide/authorization/telegram/code-for-link/me`    | Get Telegram link code for user  |

---

## Courses

Source: `chunk-FY752YIR.js`, `chunk-SQDJS5LQ.js`
Base: `{LMS_BASE_API_URL}/courses` = `/api/micro-lms/courses`

| Method | Path                                    | Description                                       |
|--------|-----------------------------------------|---------------------------------------------------|
| GET    | `{base}/courses/slim`                   | Slim course list (params: `ignoreUserAssignment`) |
| GET    | `{base}/courses/student`                | Student courses (params: `state`, pagination)     |
| GET    | `{base}/courses/student/slim?slim=true` | Slim student courses                              |
| GET    | `{base}/courses/student/count`          | Count of student courses                          |
| GET    | `{base}/courses/count`                  | Count of all courses                              |
| GET    | `{base}/courses/{id}/overview`          | Course overview (params: `includeExercises`)      |
| GET    | `{base}/courses/{id}/themes`            | Course themes                                     |
| GET    | `{base}/courses/{id}/activities`        | Course activities                                 |
| GET    | `{base}/courses/{id}/students`          | Course students                                   |
| GET    | `{base}/courses/{id}/students/all`      | All course students                               |
| GET    | `{base}/courses/{id}/groups`            | Course groups                                     |
| GET    | `{base}/courses/{id}/groups/{gid}`      | Single group                                      |
| GET    | `{base}/courses/{id}/reviewers`         | Course reviewers                                  |
| GET    | `{base}/courses/{id}/listeners`         | Course listeners                                  |
| GET    | `{base}/courses/{id}/teachers`          | Course teachers                                   |
| GET    | `{base}/courses/{id}/settings`          | Course settings                                   |
| GET    | `{base}/courses/{id}/exercises`         | Course with exercises (for performance)           |
| POST   | `{base}/courses`                        | Create course                                     |
| POST   | `{base}/courses/{id}/duplicate`         | Duplicate course                                  |
| PUT    | `{base}/courses/{id}`                   | Update course settings                            |
| PUT    | `{base}/courses/{id}/archive`           | Archive course                                    |
| PUT    | `{base}/courses/{id}/unarchive`         | Unarchive course                                  |
| PUT    | `{base}/courses/{id}/publish`           | Publish course                                    |
| PUT    | `{base}/courses/{id}/return-to-draft`   | Move to draft                                     |
| PUT    | `{base}/courses/{id}/themes-order`      | Reorder themes                                    |
| PUT    | `{base}/courses/{id}/teachers`          | Update teachers                                   |
| PUT    | `{base}/courses/{id}/reviewers`         | Update reviewers                                  |
| DELETE | `{base}/courses/{id}`                   | Delete course                                     |
| DELETE | `{base}/courses/{id}/listeners/{lid}`   | Remove listener                                   |
| DELETE | `{base}/courses/{id}/teachers/{tid}`    | Remove teacher                                    |
| DELETE | `{base}/courses/{id}/reviewers/{rid}`   | Remove reviewer                                   |
| POST   | `{base}/courses/{id}/groups`            | Create group                                      |
| PUT    | `{base}/courses/{id}/groups/{gid}`      | Update group                                      |
| DELETE | `{base}/courses/{id}/groups/{gid}`      | Delete group                                      |

---

## Themes

Source: `chunk-OLC3OSQO.js`
Base: `{LMS_BASE_API_URL}/themes`

| Method | Path                                 | Description       |
|--------|--------------------------------------|-------------------|
| GET    | `{base}/themes/{id}`                 | Get theme         |
| POST   | `{base}/themes`                      | Create theme      |
| PUT    | `{base}/themes/{id}`                 | Update theme      |
| PUT    | `{base}/themes/{id}/longreads-order` | Reorder longreads |
| PUT    | `{base}/themes/{id}/publish`         | Publish theme     |
| PUT    | `{base}/themes/{id}/return-to-draft` | Move to draft     |
| DELETE | `{base}/themes/{id}`                 | Delete theme      |

---

## Longreads

Source: `chunk-OLC3OSQO.js`
Base: `{LMS_BASE_API_URL}/longreads`

| Method | Path                                    | Description               |
|--------|-----------------------------------------|---------------------------|
| GET    | `{base}/longreads/{id}`                 | Get longread              |
| GET    | `{base}/longreads/{id}/materials`       | Get materials in longread |
| POST   | `{base}/longreads`                      | Create longread           |
| PUT    | `{base}/longreads/{id}`                 | Update longread           |
| PUT    | `{base}/longreads/{id}/materials-order` | Reorder materials         |
| PUT    | `{base}/longreads/{id}/publish`         | Publish longread          |
| PUT    | `{base}/longreads/{id}/return-to-draft` | Move to draft             |
| DELETE | `{base}/longreads/{id}`                 | Delete longread           |

---

## Materials

Source: `chunk-RDZTKWJR.js`
Base: `{LMS_BASE_API_URL}/materials`

| Method | Path                                    | Description                                     |
|--------|-----------------------------------------|-------------------------------------------------|
| GET    | `{base}/materials/{id}`                 | Get material                                    |
| GET    | `{base}/materials/slim`                 | Slim list (params: `courseId`, `materialTypes`) |
| POST   | `{base}/materials`                      | Create material                                 |
| POST   | `{base}/materials` (with longreadId)    | Create video platform material                  |
| PUT    | `{base}/materials/{id}`                 | Update material                                 |
| PUT    | `{base}/materials/{id}/publish`         | Publish material                                |
| PUT    | `{base}/materials/{id}/return-to-draft` | Unpublish material                              |
| PUT    | `{base}/materials/{id}/timecodes`       | Update video timecodes                          |
| DELETE | `{base}/materials/{id}`                 | Delete material                                 |
| SSE    | `{base}/materials/{id}/event-stream`    | Video processing state (Server-Sent Events)     |

---

## Tasks

Source: `chunk-MFCYSNLD.js` (admin), `chunk-PODCHSSJ.js` (student)
Base: `{LMS_BASE_API_URL}`

| Method  | Path                           | Description                                                 |
|---------|--------------------------------|-------------------------------------------------------------|
| GET     | `{base}/tasks`                 | Get student tasks (paginated)                               |
| GET     | `{base}/tasks/{id}`            | Get task by ID                                              |
| **PUT** | **`{base}/tasks/{id}/start`**  | **Start a task (returns `{quizSessionId}` for quiz tasks)** |
| **PUT** | **`{base}/tasks/{id}/submit`** | **Submit task answer/solution**                             |
| PUT     | `{base}/tasks/{id}/reviewer`   | Set reviewer (`{ reviewerId }`)                             |

## Quiz / Attempts

Source: `chunk-FBVLZLM6.js`
Base: `{LMS_BASE_API_URL}`

| Method | Path                                           | Description                              |
|--------|-------------------------------------------------|------------------------------------------|
| POST   | `{base}/quizzes/attempts`                       | Start new quiz attempt                   |
| GET    | `{base}/quizzes/attempts/{id}`                  | Get single attempt                       |
| GET    | `{base}/quizzes/sessions/{id}/attempts`         | Get all attempts for a session           |
| POST   | `{base}/quizzes/attempts/{id}/submit`           | Submit answers for an attempt            |
| POST   | `{base}/quizzes/attempts/{id}/complete`         | Complete an attempt                      |
| GET    | `{base}/quizzes/{id}/questions`                 | Get quiz questions                       |

---

## Task Comments

Source: `chunk-FBVLZLM6.js`
Base: `{LMS_BASE_API_URL}`

| Method | Path                                    | Description                                      |
|--------|-----------------------------------------|--------------------------------------------------|
| GET    | `{base}/{entity}/{entityId}/comments`   | List comments for an entity (e.g. task)          |
| POST   | `{base}/comments`                       | Create comment (with optional file attachment)   |
| PUT    | `{base}/comments/{id}`                  | Edit comment                                     |
| DELETE | `{base}/comments/{id}`                  | Delete comment                                   |

---

## Task Events

Source: `chunk-FBVLZLM6.js`
Base: `{LMS_BASE_API_URL}`

| Method | Path                        | Description              |
|--------|-----------------------------|--------------------------|
| GET    | `{base}/tasks/{id}/events`  | Get task event history   |

---

## Whitelist

Source: `chunk-TYXSATCA.js`

| Method | Path                                                 | Description                    |
|--------|------------------------------------------------------|--------------------------------|
| GET    | `/api/whitelist/channels/grants/{channelId}/2026`    | Grant channel whitelist check  |

---

## Polls / Evaluation Feedback

Source: `chunk-PODCHSSJ.js`
Base: `{LMS_BASE_API_URL}`

| Method   | Path                                       | Description                          |
|----------|--------------------------------------------|--------------------------------------|
| **POST** | **`{base}/polls`**                         | **Submit evaluation feedback**       |
| **GET**  | **`{base}/polls/{entityType}/{entityId}`** | **Check if feedback already posted** |

---

## Students & Late Days

Source: `chunk-DPBB7AEG.js`
Base: `{LMS_BASE_API_URL}`

| Method | Path                                  | Description                                      |
|--------|---------------------------------------|--------------------------------------------------|
| GET    | `{base}/students`                     | Get students (paginated)                         |
| GET    | `{base}/students/me`                  | Get current student (includes `lateDaysBalance`) |
| PUT    | `{base}/tasks/{id}/late-days-prolong` | Prolong deadline (`{ lateDays: number }`)        |
| PUT    | `{base}/tasks/{id}/late-days-cancel`  | Cancel late days prolongation                    |

**Late days prolong response**: `{ lateDaysUsed: number }`
**Late days cancel response**: `{ lateDaysCancelled: number }`

---

## Performance

Source: `chunk-2NQCAL5L.js`
Base: `{LMS_BASE_API_URL}/courses`

| Method | Path                                                        | Description                            |
|--------|-------------------------------------------------------------|----------------------------------------|
| GET    | `{base}/courses/{id}/activities`                            | Course activities                      |
| GET    | `{base}/courses/{id}/performance`                           | Student course performance (paginated) |
| GET    | `{base}/courses/{id}/exercises`                             | Course overview with exercises         |
| POST   | `{base}/performance/courses/{id}/jobs`                      | Start performance formation job        |
| SSE    | `{base}/performance/courses/{id}/jobs/{jobId}/event-stream` | Job progress (SSE)                     |

---

## Gradebook

Source: `chunk-P4BJN2XN.js`
Base: `{LMS_BASE_API_URL}`

| Method | Path                                                 | Description               |
|--------|------------------------------------------------------|---------------------------|
| GET    | `{base}/subjects/{id}`                               | Get subject               |
| GET    | `{base}/subjects?limit=10000`                        | All subjects              |
| GET    | `{base}/gradebook/{subjectId}/{semesterId}`          | Gradebook items           |
| GET    | `{base}/gradebook/{subjectId}/{semesterId}/file`     | Gradebook template (blob) |
| GET    | `{base}/gradebook/{subjectId}/{semesterId}/students` | Gradebook students        |
| PUT    | `{base}/gradebook/`                                  | Update gradebook          |
| DELETE | `{base}/gradebook/records/{id}`                      | Delete gradebook record   |

---

## Users

Source: `chunk-23XZHKFP.js`
Base: `{LMS_BASE_API_URL}`

| Method | Path                       | Description |
|--------|----------------------------|-------------|
| GET    | `{base}/users?limit=10000` | All users   |

---

## LMS Enrollees

Source: comprehensive scan

| Method | Path                                | Description                |
|--------|-------------------------------------|----------------------------|
| GET    | `/api/micro-lms/enrollees/contest`  | Get enrollee contest data  |

---

## File Storage (Content)

Source: `chunk-QCPI75QD.js`
Base: `{LMS_BASE_API_URL}`

| Method | Path                           | Description                                                                        |
|--------|--------------------------------|------------------------------------------------------------------------------------|
| GET    | `{base}/content/download-link` | Get presigned download URL (params: `filename`, `version`)                         |
| GET    | `{base}/content/upload-link`   | Get presigned upload URL (params: `directory`, `filename`, `contentType`)          |
| PUT    | presigned URL                  | Upload file to S3 (headers: Content-Disposition, Content-Type, x-amz-meta-version) |
| POST   | `{base}/attachments`           | Attach uploaded file to entity                                                     |

---

## Calendar / Timetable

Source: `chunk-CSB6UCKE.js`
Base: `/api/micro-lms/calendar-events`

| Method | Path                                  | Description                                          |
|--------|---------------------------------------|------------------------------------------------------|
| GET    | `{base}/slot-management/config`       | Get timetable registration state                     |
| PUT    | `{base}/slot-management/config`       | Save registration config (`{ openDate, closeDate }`) |
| GET    | `{base}/reports/lesson-registrations` | Download timetable (blob, params: `semesterId`)      |

**Student timetable** — Source: `chunk-RUM7ADXP.js`, Base: `{LMS_BASE_API_URL}/students/me/timetables`

| Method | Path                                                                  | Description                            |
|--------|-----------------------------------------------------------------------|----------------------------------------|
| GET    | `{base}/students/me/timetables`                                       | Get student's full timetable           |
| GET    | `{base}/students/me/timetables/{courseId}/{eventType}/{eventRowNumber}` | Get available event slot options       |
| POST   | `{base}/students/me/timetables/{courseId}/{eventType}/{eventRowNumber}` | Assign student to a timetable slot     |

---

## Notifications

Source: root app `chunk-OIDSCTRK.js` (133KB)

| Method   | Path                                               | Description                                    |
|----------|----------------------------------------------------|------------------------------------------------|
| **POST** | `/api/notification-hub/notifications/in-app`       | List in-app notifications (**POST, not GET**)  |
| **POST** | `/api/notification-hub/notifications/in-app/read`  | Mark notifications as read (**POST, not PUT**) |
| GET      | `/api/notification-hub/notifications/in-app/stats` | Notification stats (unread count)              |

---

## Informer / Support

Source: comprehensive scan

| Method | Path                   | Description                        |
|--------|------------------------|------------------------------------|
| GET    | `/api/informer/token`  | Get support widget / informer token|

---

## Events

Source: `chunk-44YKIWJR.js`

| Method | Path                                                     | Description                         |
|--------|----------------------------------------------------------|-------------------------------------|
| POST   | `/api/event-builder/public/events/list`                  | List events (POST with body filter) |
| GET    | `/api/event-builder/public/events/slug/{slug}`           | Get event by slug                   |
| GET    | `/api/event-builder/public/events/{id}/appointment/file` | Download event calendar (blob)      |
| POST   | `/api/event-builder/public/events/apply/{id}`            | Apply/register for event            |
| GET    | `/api/event-builder/admissions/2025/state`               | Admission state                     |

---

## News

Source: `chunk-EFZ55JZJ.js`

| Method | Path                                       | Description    |
|--------|--------------------------------------------|----------------|
| GET    | `/api/event-builder/public/news/{id}`      | View news item |
| GET    | `/api/event-builder/public/news/file/{id}` | Get news file  |

---

## Admission

Source: `chunk-MYPYERCK.js`, `chunk-DU2L5VZK.js`, `chunk-VOKE7BVC.js` + comprehensive scan

| Method | Path                                                                      | Description                               |
|--------|---------------------------------------------------------------------------|-------------------------------------------|
| GET    | `/api/event-builder/admissions/forms/future-student/dictionaries`         | Admission form dictionaries               |
| POST   | `/api/event-builder/admissions/forms/future-student/files/upload`         | Upload admission files                    |
| GET    | `/api/event-builder/admissions/{type}/forms/future-student/me`            | Get future-student form by admission type |
| PUT    | `/api/event-builder/admissions/{type}/forms/future-student/me/draft/{step}` | Save draft step for future-student form|
| POST   | `/api/event-builder/admissions/{type}/forms/future-student/me/submit`     | Submit future-student form                |
| GET    | `/api/event-builder/admissions/{type}/forms/payer/dictionaries`           | Get payer form dictionaries               |
| GET    | `/api/event-builder/admissions/{type}/forms/payer/me`                     | Get payer form by admission type          |
| PUT    | `/api/event-builder/admissions/{type}/forms/payer/me/draft/{step}`        | Save draft step for payer form            |
| POST   | `/api/event-builder/admissions/{type}/forms/payer/me/submit`              | Submit payer form                         |
| GET    | `/api/event-builder/admissions/documents/user-admissions/{id}/documents`  | Get admission documents                   |
| GET    | `/api/event-builder/admissions/documents/user-admissions/{id}/enrollee-documents` | Get enrollee documents           |
| POST   | `/api/event-builder/admissions/documents/files/upload`                    | Upload admission document files           |
| POST   | `/api/event-builder/admissions/files/upload`                              | Upload admission files (generic)          |
| GET    | `/api/event-builder/admissions/masters-dpo-2025/forms/dictionaries`       | Masters/DPO 2025 form dictionaries        |
| GET    | `/api/event-builder/admissions/masters-dpo-2025/forms/me`                 | Get masters/DPO 2025 form                 |
| POST   | `/api/event-builder/admissions/masters-dpo-2025/forms/me/submit`          | Submit masters/DPO 2025 form              |
| PUT    | `/api/event-builder/admissions/masters-dpo-2025/forms/me/draft/{step}`    | Save masters/DPO 2025 draft step          |
| GET    | `/api/event-builder/public/forms/GrantCompetitionBachelor2411/submission-info` | Grant competition submission info    |

---

## Admin Configuration (Feature Flags)

Source: `chunk-VMZTUSWJ.js` (root), also in learn app chunk-HTCSOUNZ.js

| Method | Path                              | Description                   |
|--------|-----------------------------------|-------------------------------|
| GET    | `/api/admin/configurations/{key}` | Get config value              |
| GET    | `/api/admin/configurations`       | Get all configs               |
| POST   | `/api/admin/configurations`       | Save config (array)           |
| DELETE | `/api/admin/configurations`       | Delete config (array in body) |

**Config value types**: None, Object, Boolean, Int, Double, DateOnly, DateTime, Text

---

## Grants / Competitions

Source: `chunk-4ACXTDD6.js`, `chunk-XBPQU6OA.js`, `chunk-O5N3J4AB.js` + sub-chunks + comprehensive scan

### Grants (general)

| Method | Path                                                               | Description                  |
|--------|--------------------------------------------------------------------|------------------------------|
| GET    | `/api/hub/competitions-cases-solutions-files/me`                   | Competition case solutions   |
| DELETE | `/api/hub/competitions-cases-solutions-files/me/{id}`              | Delete solution file         |
| GET    | `/api/hub/master-grant-competition-cv-files/me`                    | Master grant CV files        |
| GET    | `/api/hub/unified-state-exams-files/me`                            | Unified state exam files     |
| DELETE | `/api/hub/unified-state-exams-files/me/{id}`                       | Delete exam file             |
| DELETE | `/api/hub/competition-case-files/{id}`                             | Delete competition case file |
| GET    | `/api/hub/competitions/{id}/me/state`                              | Get competition state by ID  |

### Bachelor 2024

| Method | Path                                                                              | Description                            |
|--------|-----------------------------------------------------------------------------------|----------------------------------------|
| GET    | `/api/hub/competitions/bachelor-2024/waitlist/me`                                 | Bachelor grants waitlist               |
| GET    | `/api/hub/competitions/bachelor-2024/waitlist/me/exists`                           | Check waitlist status                  |
| GET    | `/api/hub/competitions/bachelor-2024/forms/grant-initial/me`                      | Get grant initial form                 |
| POST   | `/api/hub/competitions/bachelor-2024/forms/grant-initial/me/submit`               | Submit grant initial form              |
| PUT    | `/api/hub/competitions/bachelor-2024/forms/grant-initial/me/draft/{step}`         | Save grant initial draft step          |
| GET    | `/api/hub/competitions/bachelor-2024/forms/grant-increase/me`                     | Get grant increase form                |
| GET    | `/api/hub/competitions/bachelor-2024/forms/grant-increase/actual-form/me`         | Get actual grant increase form         |
| PUT    | `/api/hub/competitions/bachelor-2024/forms/grant-increase/{formId}/me`            | Update grant increase form             |
| POST   | `/api/hub/competitions/bachelor-2024/forms/grant-increase/{formId}/me/submit`     | Submit grant increase form             |

### Grant Bachelor 2026 Design

| Method | Path                                                                                    | Description                        |
|--------|-----------------------------------------------------------------------------------------|------------------------------------|
| GET    | `/api/hub/competitions/grant-bachelor-2026-design/case/{id}`                            | Design grant case                  |
| GET    | `/api/hub/competitions/grant-bachelor-2026-design/case/{id}/guide`                      | Design case guide                  |
| GET    | `/api/hub/competitions/grant-bachelor-2026-design/case/active`                          | Get active design case             |
| GET    | `/api/hub/competitions/grant-bachelor-2026-design/case/active/me`                       | Get my active design case          |
| POST   | `/api/hub/competitions/grant-bachelor-2026-design/case/active/me/submit-solution`       | Submit design case solution        |
| GET    | `/api/hub/competitions/grant-bachelor-2026-design/case/last-finished/me`                | Get last finished case             |
| GET    | `/api/hub/competitions/grant-bachelor-2026-design/forms/initial/me`                     | Get initial form                   |
| POST   | `/api/hub/competitions/grant-bachelor-2026-design/forms/initial/me/submit`              | Submit initial form                |
| GET    | `/api/hub/competitions/grant-bachelor-2026-design/forms/increase/me`                    | Get increase form                  |
| GET    | `/api/hub/competitions/grant-bachelor-2026-design/forms/increase/actual-form/me`        | Get actual increase form           |
| PUT    | `/api/hub/competitions/grant-bachelor-2026-design/forms/increase/me/draft`              | Save increase form draft           |
| GET    | `/api/hub/competitions/grant-bachelor-2026-design/presentation/upcoming`                | Get upcoming presentations         |
| GET    | `/api/hub/competitions/grant-bachelor-2026-design/presentation/me/enrolled`             | Check if enrolled in presentation  |

### Grant Bachelor 2026 IT

| Method | Path                                                                                | Description                        |
|--------|-------------------------------------------------------------------------------------|------------------------------------|
| GET    | `/api/hub/competitions/grant-bachelor-2026-it/forms/initial/me`                     | Get initial form                   |
| POST   | `/api/hub/competitions/grant-bachelor-2026-it/forms/initial/me/submit`              | Submit initial form                |
| GET    | `/api/hub/competitions/grant-bachelor-2026-it/forms/increase/me`                    | Get increase form                  |
| GET    | `/api/hub/competitions/grant-bachelor-2026-it/forms/increase/actual-form/me`        | Get actual increase form           |
| PUT    | `/api/hub/competitions/grant-bachelor-2026-it/forms/increase/me/draft`              | Save increase form draft           |
| GET    | `/api/hub/competitions/grant-bachelor-2026-it/business-game/upcoming`               | Get upcoming business games        |
| GET    | `/api/hub/competitions/grant-bachelor-2026-it/business-game/me/enrolled`            | Check if enrolled in business game |

### Master 2025

| Method | Path                                                                                | Description                           |
|--------|-------------------------------------------------------------------------------------|---------------------------------------|
| GET    | `/api/hub/competitions/master-2025/waitlist/me`                                     | Master grants waitlist                |
| GET    | `/api/hub/competitions/master-2025/waitlist/me/exists`                               | Check waitlist status                 |
| GET    | `/api/hub/competitions/master-2025/forms/grant-initial/me`                          | Get grant initial form                |
| POST   | `/api/hub/competitions/master-2025/forms/grant-initial/me/submit`                   | Submit grant initial form             |
| PUT    | `/api/hub/competitions/master-2025/forms/grant-initial/me/change-educational-program` | Change educational program          |
| POST   | `/api/hub/competitions/master-2025/promocode/check`                                 | Check promocode                       |

### Master 2026

| Method | Path                                                                    | Description                        |
|--------|-------------------------------------------------------------------------|------------------------------------|
| GET    | `/api/hub/competitions/master-2026/me/state`                            | Get master 2026 competition state  |
| GET    | `/api/hub/competitions/master-2026/me/pre-grant-track-eligibility`      | Check pre-grant track eligibility  |
| GET    | `/api/hub/competitions/master-data/activities/search`                   | Search master-data activities      |

---

## Referrals

Source: comprehensive scan

| Method | Path                                     | Description          |
|--------|------------------------------------------|----------------------|
| GET    | `/api/referrals/invitations/me/invitees` | List my invitees     |

---

## Interview Scheduler

Source: comprehensive scan

| Method | Path                                              | Description               |
|--------|---------------------------------------------------|---------------------------|
| GET    | `/api/interview-scheduler/public/interview/active`| Get active interviews     |

---

## Analytics

| Method | Path                             | Description                |
|--------|----------------------------------|----------------------------|
| POST   | `/api/analytics`                 | Send analytics event       |
| POST   | `/api/hub/events/utm-analysis`   | Send UTM analytics event   |
| -      | `https://api-statist.tinkoff.ru` | Tinkoff analytics endpoint |

---

## Summary Statistics

- **Total unique API paths**: ~175+
- **HTTP methods used**: GET (majority), POST, PUT, DELETE
- **SSE endpoints**: 2 (material processing, performance job)
- **Blob downloads**: 3 (timetable, gradebook template, event calendar)
- **Auth model**: Keycloak OIDC with `bff.cookie`, session at `/api/account/me`
- **Base API gateway**: All requests proxied through `https://my.centraluniversity.ru/api/`
- **API domains**: micro-lms, hub (students/enrollees/competitions/grants), event-builder, notification-hub, certificates, cu-guide, referrals, interview-scheduler, informer, admin, analytics
