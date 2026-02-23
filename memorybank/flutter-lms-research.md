# Flutter LMS Mobile App (cu-3rd-party/lms-mobile) - Complete Research

## Base URL

`https://my.centraluniversity.ru/api`

## Authentication

- Cookie-based auth via `bff.cookie`
- Login via embedded WebView (InAppWebView) loading `https://my.centraluniversity.ru`
- Cookie stored in SharedPreferences + in-memory
- 401 response triggers re-auth via `onAuthRequired` stream
- Backend can refresh cookie via `Set-Cookie` header

---

## ALL DATA MODELS

### StudentProfile

| Field            | Type      |
|------------------|-----------|
| id               | int       |
| firstName        | String    |
| lastName         | String    |
| email            | String    |
| phone            | String?   |
| studentId        | String    |
| group            | String?   |
| faculty          | String?   |
| program          | String?   |
| course           | int?      |
| githubUsername   | String?   |
| telegramUsername | String?   |
| discordUsername  | String?   |
| city             | String?   |
| country          | String?   |
| timezone         | String?   |
| birthDate        | DateTime? |
| avatarUrl        | String?   |

### StudentLmsProfile

| Field           | Type |
|-----------------|------|
| id              | int  |
| lateDaysBalance | int  |

### StudentTask

| Field             | Type                                       |
|-------------------|--------------------------------------------|
| id                | int                                        |
| state             | String                                     |
| score             | double?                                    |
| deadline          | DateTime?                                  |
| submitAt          | DateTime?                                  |
| exercise          | TaskExercise                               |
| course            | TaskCourse                                 |
| isLateDaysEnabled | bool                                       |
| lateDays          | int?                                       |
| **Computed**      |                                            |
| normalizedState   | String (getter)                            |
| effectiveDeadline | DateTime? (getter, accounts for late days) |
| canExtendDeadline | bool (getter)                              |
| canCancelLateDays | bool (getter)                              |
| stateColor        | Color (getter)                             |

### TaskExercise

| Field    | Type      |
|----------|-----------|
| id       | int       |
| name     | String    |
| type     | String    |
| maxScore | int       |
| deadline | DateTime? |

### TaskCourse

| Field      | Type   |
|------------|--------|
| id         | int    |
| name       | String |
| isArchived | bool   |

### TaskDetails

| Field               | Type                           |
|---------------------|--------------------------------|
| id                  | int                            |
| score               | double?                        |
| extraScore          | double?                        |
| maxScore            | int?                           |
| scoreSkillLevel     | int?                           |
| state               | String?                        |
| solutionUrl         | String?                        |
| solutionAttachments | List&lt;MaterialAttachment&gt; |
| submitAt            | DateTime?                      |
| hasSolution         | bool                           |
| isLateDaysEnabled   | bool                           |
| lateDays            | int?                           |
| lateDaysBalance     | int?                           |
| deadline            | DateTime?                      |

### TaskEvent

| Field      | Type             |
|------------|------------------|
| id         | String           |
| occurredOn | DateTime?        |
| type       | String           |
| actorEmail | String?          |
| actorName  | String?          |
| content    | TaskEventContent |

### TaskEventContent

| Field           | Type                           |
|-----------------|--------------------------------|
| state           | String?                        |
| score           | TaskEventScore?                |
| estimation      | TaskEventEstimation?           |
| attachments     | List&lt;MaterialAttachment&gt; |
| solutionUrl     | String?                        |
| reviewerName    | String?                        |
| reviewersNames  | List&lt;String&gt;?            |
| taskState       | String?                        |
| taskDeadline    | DateTime?                      |
| exerciseName    | String?                        |
| lateDaysValue   | int?                           |
| contentDeadline | DateTime?                      |

### TaskComment

| Field       | Type                       |
|-------------|----------------------------|
| id          | int                        |
| authorId    | int                        |
| authorName  | String                     |
| text        | String                     |
| createdAt   | DateTime                   |
| isMentor    | bool                       |
| attachments | List&lt;TaskAttachment&gt; |

### Course

| Field         | Type                          |
|---------------|-------------------------------|
| id            | int                           |
| name          | String                        |
| state         | String                        |
| category      | String                        |
| categoryCover | String                        |
| isArchived    | bool                          |
| **Computed**  |                               |
| cleanName     | String (removes emoji prefix) |
| categoryColor | Color                         |
| categoryIcon  | IconData                      |
| categoryName  | String                        |

### CourseOverview

| Field        | Type                    |
|--------------|-------------------------|
| id           | int                     |
| name         | String                  |
| isArchived   | bool                    |
| themes       | List&lt;CourseTheme&gt; |
| **Computed** |                         |
| cleanName    | String                  |

### CourseTheme

| Field          | Type                 |
|----------------|----------------------|
| id             | int                  |
| name           | String               |
| order          | int                  |
| state          | String               |
| longreads      | List&lt;Longread&gt; |
| **Computed**   |                      |
| totalExercises | int                  |
| hasExercises   | bool                 |

### Longread (nested in CourseTheme)

| Field     | Type                      |
|-----------|---------------------------|
| id        | int                       |
| type      | String                    |
| name      | String                    |
| state     | String                    |
| exercises | List&lt;ThemeExercise&gt; |

### ThemeExercise

| Field             | Type              |
|-------------------|-------------------|
| id                | int               |
| name              | String            |
| maxScore          | int               |
| deadline          | DateTime?         |
| activity          | ExerciseActivity? |
| **Computed**      |                   |
| isOverdue         | bool              |
| formattedDeadline | String            |

### LongreadMaterial

| Field         | Type                                               |
|---------------|----------------------------------------------------|
| id            | int                                                |
| discriminator | String ('markdown', 'file', 'coding', 'questions') |
| viewContent   | String?                                            |
| filename      | String?                                            |
| version       | String?                                            |
| length        | int?                                               |
| contentName   | String?                                            |
| name          | String?                                            |
| attachments   | List&lt;MaterialAttachment&gt;                     |
| estimation    | MaterialEstimation?                                |
| taskId        | int?                                               |
| **Computed**  |                                                    |
| isMarkdown    | bool                                               |
| isFile        | bool                                               |
| isCoding      | bool                                               |
| isQuestions   | bool                                               |
| formattedSize | String                                             |

### MaterialAttachment

| Field         | Type   |
|---------------|--------|
| name          | String |
| filename      | String |
| mediaType     | String |
| length        | int    |
| version       | String |
| **Computed**  |        |
| formattedSize | String |
| extension     | String |

### MaterialEstimation

| Field             | Type      |
|-------------------|-----------|
| deadline          | DateTime? |
| maxScore          | int       |
| activityName      | String?   |
| activityWeight    | double?   |
| **Computed**      |           |
| isOverdue         | bool      |
| formattedDeadline | String    |

### StudentPerformanceCourse

| Field        | Type    |
|--------------|---------|
| id           | int     |
| name         | String  |
| description  | String? |
| total        | int     |
| **Computed** |         |
| cleanName    | String  |

### TaskScore

| Field           | Type              |
|-----------------|-------------------|
| id              | int               |
| state           | String            |
| score           | double            |
| scoreSkillLevel | String?           |
| extraScore      | double?           |
| exerciseId      | int               |
| maxScore        | int               |
| activity        | TaskScoreActivity |

### TaskScoreActivity

| Field                 | Type    |
|-----------------------|---------|
| id                    | int     |
| name                  | String  |
| weight                | double  |
| averageScoreThreshold | double? |

### ActivitySummary

| Field             | Type                             |
|-------------------|----------------------------------|
| activityId        | int                              |
| activityName      | String                           |
| count             | int                              |
| averageScore      | double                           |
| weight            | double                           |
| **Computed**      |                                  |
| totalContribution | double (= averageScore * weight) |

### GradebookSemester

| Field          | Type                            |
|----------------|---------------------------------|
| year           | int                             |
| semesterNumber | int                             |
| grades         | List&lt;GradebookGrade&gt;      |
| **Computed**   |                                 |
| title          | String (e.g. "2023, 1 семестр") |
| regularGrades  | List&lt;GradebookGrade&gt;      |
| electiveGrades | List&lt;GradebookGrade&gt;      |

### GradebookGrade

| Field                 | Type   |
|-----------------------|--------|
| subject               | String |
| grade                 | num?   |
| normalizedGrade       | String |
| assessmentType        | String |
| subjectType           | String |
| **Computed**          |        |
| assessmentTypeDisplay | String |
| gradeDisplay          | String |
| isElective            | bool   |

### NotificationItem

| Field       | Type                            |
|-------------|---------------------------------|
| title       | String                          |
| createdAt   | DateTime                        |
| description | String?                         |
| link        | NotificationLink? (uri + label) |
| icon        | String                          |
| category    | String                          |

### CalendarEvent

| Field       | Type     |
|-------------|----------|
| uid         | String   |
| summary     | String   |
| description | String?  |
| location    | String?  |
| dtstart     | DateTime |
| dtend       | DateTime |
| url         | String?  |

### ClassData (derived from CalendarEvent)

| Field     | Type    |
|-----------|---------|
| startTime | String  |
| endTime   | String  |
| room      | String  |
| type      | String  |
| title     | String  |
| link      | String? |

### UploadLinkData

| Field     | Type   |
|-----------|--------|
| shortName | String |
| filename  | String |
| objectKey | String |
| version   | String |
| url       | String |

### Response Wrappers

- **StudentPerformanceResponse**: `{ courses: List<StudentPerformanceCourse> }`
- **CourseStudentPerformanceResponse**: `{ tasks: List<TaskScore> }`
- **GradebookResponse**: `{ semesters: List<GradebookSemester> }`

---

## ALL API ENDPOINTS

### Profile

| Method | Path                   | Body             | Response                |
|--------|------------------------|------------------|-------------------------|
| GET    | /hub/students/me       | -                | StudentProfile          |
| GET    | /hub/avatars/me        | -                | Uint8List (image bytes) |
| POST   | /hub/avatars/me        | Multipart (File) | bool                    |
| DELETE | /hub/avatars/me        | -                | bool                    |
| GET    | /micro-lms/students/me | -                | StudentLmsProfile       |

### Tasks

| Method | Path                                        | Body/Params                                           | Response                |
|--------|---------------------------------------------|-------------------------------------------------------|-------------------------|
| GET    | /micro-lms/tasks/student                    | ?state=inProgress\|review\|backlog\|failed\|evaluated | List&lt;StudentTask&gt; |
| GET    | /micro-lms/tasks/{taskId}                   | -                                                     | TaskDetails             |
| GET    | /micro-lms/tasks/{taskId}/events            | -                                                     | List&lt;TaskEvent&gt;   |
| GET    | /micro-lms/tasks/{taskId}/comments          | -                                                     | List&lt;TaskComment&gt; |
| PUT    | /micro-lms/tasks/{taskId}/submit            | { solutionUrl?, attachments[] }                       | bool                    |
| PUT    | /micro-lms/tasks/{taskId}/start             | -                                                     | bool                    |
| PUT    | /micro-lms/tasks/{taskId}/late-days-prolong | { lateDays: int }                                     | bool                    |
| PUT    | /micro-lms/tasks/{taskId}/late-days-cancel  | -                                                     | bool                    |

### Comments

| Method | Path                | Body                                              | Response         |
|--------|---------------------|---------------------------------------------------|------------------|
| POST   | /micro-lms/comments | { entityId, type:"task", content, attachments[] } | int (comment ID) |

### Courses

| Method | Path                                        | Params       | Response                     |
|--------|---------------------------------------------|--------------|------------------------------|
| GET    | /micro-lms/courses/student                  | ?limit=10000 | List&lt;Course&gt;           |
| GET    | /micro-lms/courses/{courseId}/overview      | -            | CourseOverview               |
| GET    | /micro-lms/longreads/{longreadId}/materials | ?limit=10000 | List&lt;LongreadMaterial&gt; |

### Performance & Gradebook

| Method | Path                                              | Response                         |
|--------|---------------------------------------------------|----------------------------------|
| GET    | /micro-lms/performance/student                    | StudentPerformanceResponse       |
| GET    | /micro-lms/courses/{courseId}/student-performance | CourseStudentPerformanceResponse |
| GET    | /micro-lms/gradebook                              | GradebookResponse                |

### Files

| Method | Path                             | Params                          | Response       |
|--------|----------------------------------|---------------------------------|----------------|
| GET    | /micro-lms/content/upload-link   | ?directory&filename&contentType | UploadLinkData |
| PUT    | {presigned-url}                  | file bytes (streamed)           | bool           |
| GET    | /micro-lms/content/download-link | ?filename&version               | String (URL)   |

### Notifications

| Method | Path                                   | Body                                                           | Response                     |
|--------|----------------------------------------|----------------------------------------------------------------|------------------------------|
| POST   | /notification-hub/notifications/in-app | { paging: {limit, offset, sorting}, filter: {category: 1\|2} } | List&lt;NotificationItem&gt; |

---

## FEATURES DETAIL

### Task State Machine

States: `backlog` → `inProgress` → `hasSolution` → `review` → `evaluated`/`revision`/`failed`

- `hasSolution` is derived (inProgress + submitAt != null)
- `rework` normalized to `revision`, `rejected` to `failed`
- Start: backlog → inProgress
- Submit: inProgress → hasSolution
- Review outcomes: evaluated, revision, failed
- Revision → inProgress (student resumes)

### Late Days System

- Max 7 days extension per task
- `lateDaysBalance` tracked on StudentLmsProfile
- Cannot extend in states: review, evaluated, revision, rework
- Cancel allowed if lateDays > 0 AND effectiveDeadline > 24h from now
- API: PUT /tasks/{id}/late-days-prolong, /late-days-cancel

### Schedule/Calendar

- IcalService parses .ics feeds from user-configured URL
- URL stored in SharedPreferences (key: 'ics_url')
- Supports RRULE (FREQ, INTERVAL, BYDAY, UNTIL, COUNT) and EXDATE
- In-memory cache + local file cache
- CalendarEvent → ClassData conversion extracts room from SUMMARY via regex
- ScheduleSection widget with date navigation

### Document Scanning (ScanWorkPage)

- ImagePicker for camera/gallery capture
- Image editing: rotation + cropping (using `image` library)
- PDF generation (using `pdf` library)
- Compression toggle
- Saves to app documents directory
- Available for attachment in comments/solutions via Files tab

### Upload System

- FilePicker for file selection
- FileRenameDialog for template-based renaming
- getUploadLink → presigned URL → PUT upload with progress
- _PendingCommentAttachment tracks: queued/uploading/uploaded/failed
- Separate attachment lists for comments vs solutions

### Download System

- getDownloadLink → presigned URL → stream to local file
- Progress + speed tracking
- Files stored in app documents directory
- _buildSafeFileName prevents conflicts with versioning

### Content Search & Highlighting

- Case-insensitive search in longread markdown content
- HTML parsing, wrapping matches in <mark> tags
- Code blocks (<pre>/<code>) excluded from search
- GlobalKey per match for scroll navigation
- Active match highlighted differently
- Platform-specific search UI (Cupertino/Material)

### Course Reordering & Archiving

- Drag-and-drop via ReorderableListView (edit mode)
- Archive/restore between _activeCourses and _archivedCourses
- Persisted locally via SharedPreferences (not server-side)

### Notifications

- Categories: Education (1), Other (2)
- Icon types: ServiceDesk, News, Education
- POST endpoint with paging + category filter
- Links can open external URLs or navigate to LongreadPage

### Task Filtering

- Status filters (multi-select dropdown)
- Course filters (multi-select)
- Text search (exercise name)
- Active/Archive segment
- Sort by effective deadline

### Navigation

- AuthWrapper → LoginPage or HomePage
- 4 tabs: Home, Tasks, Courses, Files
- IndexedStack preserves tab state
- Platform-specific routing (MaterialPageRoute/CupertinoPageRoute)
- Pages: AuthWrapper, LoginPage, HomePage, ProfilePage, NotificationsPage, CoursePage, LongreadPage,
  ScanWorkPage, CoursePerformancePage, FileRenameSettingsPage

### Performance Calculation

- Server-side: `total` in StudentPerformanceCourse comes from backend
- Client-side: ActivitySummary.totalContribution = averageScore * weight
- Grade colors: >= 8 green, >= 6 yellow, else red
- Gradebook grades: numerical + normalized string, split by semester
