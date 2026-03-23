# Comprehensive API Scan: CU LMS Website JS Chunks

Scan date: 2026-03-22
Scanned directories: `learn/` and `root/` under `docs/web-reverse/raw/`

---

## 1. API URL Endpoints

### 1.1 Base URL Tokens

| File                      | Token Name          | Base URL                         |
|---------------------------|---------------------|----------------------------------|
| `learn/chunk-NJXQAJX7.js` | `LMS_BASE_API_URL`  | `/api/micro-lms`                 |
| `learn/chunk-XZDI5NMD.js` | `BASE_API_URL`      | `/api/micro-lms`                 |
| `root/chunk-EFZ55JZJ.js`  | `NEWS_API_BASE_URL` | `/api/event-builder/public/news` |

Both LMS base URL tokens resolve to `/api/micro-lms`. All LMS API calls in the `learn/` app use this
prefix.

### 1.2 LMS Course API (`/api/micro-lms/courses/...`)

**Source: `learn/chunk-FY752YIR.js` -- CourseApiService**

| Method | Endpoint                                       | Purpose                                   |
|--------|------------------------------------------------|-------------------------------------------|
| GET    | `/courses`                                     | Get courses list (with pagination params) |
| GET    | `/courses/{id}/overview?includeExercises=true` | Get course overview with exercises        |
| GET    | `/courses/{id}/overview`                       | Get course overview without exercises     |
| GET    | `/courses/{id}/students/all`                   | Get all course members                    |
| GET    | `/courses/{id}/groups`                         | Get course groups                         |
| GET    | `/courses/{id}/groups/{groupId}`               | Get specific course group                 |
| GET    | `/courses/{id}/activities`                     | Read course activities                    |
| POST   | `/courses`                                     | Create a new course                       |
| PUT    | `/courses/{id}/archive`                        | Archive a course                          |
| PUT    | `/courses/{id}/unarchive`                      | Unarchive a course                        |
| POST   | `/courses/{id}/duplicate`                      | Duplicate a course                        |
| PUT    | `/courses/{id}/themes-order`                   | Reorder themes in a course                |
| DELETE | `/courses/{id}`                                | Delete a course                           |
| PUT    | `/courses/{id}/publish`                        | Publish a course                          |
| GET    | `/courses/{id}/reviewers`                      | Get course reviewers                      |
| GET    | `/courses/{id}/students`                       | Get students (without listeners)          |
| GET    | `/courses/{id}/students/all`                   | Get all students                          |
| PUT    | `/courses/{id}/return-to-draft`                | Move course back to draft                 |

**Source: `learn/chunk-SQDJS5LQ.js` -- CourseListApiService**

| Method | Endpoint                                  | Purpose                                                |
|--------|-------------------------------------------|--------------------------------------------------------|
| GET    | `/courses/slim`                           | Get slim courses list (params: `ignoreUserAssignment`) |
| GET    | `/courses/student?{params}&state={state}` | Get student courses with state filter                  |
| GET    | `/courses/student`                        | Get paginated student courses                          |
| GET    | `/courses/{courseId}/themes?{params}`     | Get themes for a course                                |
| GET    | `/themes/{themeId}/longreads?{params}`    | Get longreads for a theme                              |
| GET    | `/courses/student/slim?slim=true`         | Get slim student courses                               |
| GET    | `/courses/student/count`                  | Get count of student courses                           |
| GET    | `/courses/count`                          | Get count of all courses                               |

### 1.3 LMS Theme API (`/api/micro-lms/themes/...`)

**Source: `learn/chunk-OLC3OSQO.js` -- ThemeApiService**

| Method | Endpoint                       | Purpose                    |
|--------|--------------------------------|----------------------------|
| GET    | `/themes/{id}`                 | Get single theme           |
| POST   | `/themes`                      | Create theme               |
| PUT    | `/themes/{id}`                 | Update theme               |
| PUT    | `/themes/{id}/longreads-order` | Reorder longreads in theme |
| DELETE | `/themes/{id}`                 | Delete theme               |
| PUT    | `/themes/{id}/publish`         | Publish theme              |
| PUT    | `/themes/{id}/return-to-draft` | Move theme to draft        |

### 1.4 LMS Longread API (`/api/micro-lms/longreads/...`)

**Source: `learn/chunk-OLC3OSQO.js` -- LongreadApiService**

| Method | Endpoint                             | Purpose                     |
|--------|--------------------------------------|-----------------------------|
| POST   | `/longreads`                         | Create longread             |
| GET    | `/longreads/{id}`                    | Get longread                |
| PUT    | `/longreads/{id}`                    | Update longread             |
| DELETE | `/longreads/{id}`                    | Delete longread             |
| GET    | `/longreads/{id}/materials?{params}` | Read materials for longread |
| PUT    | `/longreads/{id}/materials-order`    | Reorder materials           |
| PUT    | `/longreads/{id}/publish`            | Publish longread            |
| PUT    | `/longreads/{id}/return-to-draft`    | Move longread to draft      |

### 1.5 LMS Task API (`/api/micro-lms/tasks/...`)

**Source: `learn/chunk-MFCYSNLD.js` -- TaskApiService**

| Method | Endpoint               | Purpose                              |
|--------|------------------------|--------------------------------------|
| GET    | `/tasks`               | Get students tasks (with pagination) |
| GET    | `/tasks/{id}`          | Get task by ID                       |
| PUT    | `/tasks/{id}/reviewer` | Set reviewer for task                |

**Source: `learn/chunk-DPBB7AEG.js` -- StudentLateDaysService**

| Method | Endpoint                        | Purpose                                                      |
|--------|---------------------------------|--------------------------------------------------------------|
| GET    | `/students`                     | Get students (with params)                                   |
| GET    | `/students/me`                  | Get current student info                                     |
| PUT    | `/tasks/{id}/late-days-prolong` | Prolong task deadline with late days (body: `{lateDays: N}`) |
| PUT    | `/tasks/{id}/late-days-cancel`  | Cancel late days prolongation for task                       |

### 1.6 LMS Course Settings API (`/api/micro-lms/courses/{id}/settings/...`)

**Source: `learn/chunk-EHAMAETQ.js` -- CourseSettingsApiService**

| Method | Endpoint                               | Purpose                     |
|--------|----------------------------------------|-----------------------------|
| GET    | `/courses/{id}/settings`               | Get main course settings    |
| PUT    | `/courses/{id}`                        | Update main course settings |
| GET    | `/courses/{id}/students`               | Get course students         |
| GET    | `/courses/{id}/listeners`              | Get course listeners        |
| POST   | `/courses/{id}/groups`                 | Create student group        |
| PUT    | `/courses/{id}/groups/{groupId}`       | Update student group        |
| DELETE | `/courses/{id}/groups/{groupId}`       | Delete student group        |
| DELETE | `/courses/{id}/listeners/{listenerId}` | Delete listener             |
| GET    | `/courses/{id}/teachers`               | Get course teachers         |
| PUT    | `/courses/{id}/teachers`               | Update teachers             |
| DELETE | `/courses/{id}/teachers/{teacherId}`   | Delete teacher              |
| GET    | `/courses/{id}/reviewers`              | Get reviewers               |
| PUT    | `/courses/{id}/reviewers`              | Update reviewers            |
| DELETE | `/courses/{id}/reviewers/{reviewerId}` | Delete reviewer             |

### 1.7 Calendar/Timetable Registration API (`/api/micro-lms/calendar-events/...`)

**Source: `learn/chunk-CSB6UCKE.js` -- CalendarEventService**

| Method | Endpoint                                                        | Purpose                                   |
|--------|-----------------------------------------------------------------|-------------------------------------------|
| GET    | `/calendar-events/slot-management/config`                       | Get registration state (open/close dates) |
| PUT    | `/calendar-events/slot-management/config`                       | Save registration config                  |
| GET    | `/calendar-events/reports/lesson-registrations?semesterId={id}` | Download timetable report (blob)          |

### 1.8 Root App -- Hub/Portal APIs

**Source: `root/chunk-QHDVAQXE.js` -- StudentService**

| Method | Endpoint               | Purpose                     |
|--------|------------------------|-----------------------------|
| GET    | `/api/hub/students/me` | Get current student profile |

**Source: `root/chunk-H2UV6AAT.js` -- GrantsService**

| Method | Endpoint                                  | Purpose                                                   |
|--------|-------------------------------------------|-----------------------------------------------------------|
| GET    | `/api/hub/grants/me`                      | Get user grants                                           |
| GET    | `/api/hub/grants/me/{year}/{type}/active` | Get active grant (type = InformationTechnology or Design) |

**Source: `root/chunk-2RNYUVXP.js` -- EnrolleeService**

| Method | Endpoint                           | Purpose                 |
|--------|------------------------------------|-------------------------|
| PUT    | `/api/hub/enrollees/me`            | Update enrollee profile |
| POST   | `/api/hub/enrollees/onboarding/me` | Enrollee onboarding     |
| GET    | `/api/hub/enrollees/me`            | Get current enrollee    |

### 1.9 Root App -- Event Builder APIs

**Source: `root/chunk-44YKIWJR.js` (identical to `root/chunk-H2UV6AAT.js`) -- EventsApiService**

| Method | Endpoint                                                 | Purpose                             |
|--------|----------------------------------------------------------|-------------------------------------|
| POST   | `/api/event-builder/public/events/list`                  | List events                         |
| GET    | `/api/event-builder/public/events/slug/{slug}`           | Get event by slug                   |
| GET    | `/api/event-builder/public/events/{id}/appointment/file` | Download event calendar file (blob) |
| POST   | `/api/event-builder/public/events/apply/{id}`            | Apply to event                      |

**Source: `root/chunk-2NELPLXO.js` -- AdmissionsService**

| Method | Endpoint                                   | Purpose              |
|--------|--------------------------------------------|----------------------|
| GET    | `/api/event-builder/admissions/2025/state` | Get admissions state |

**Source: `root/chunk-EFZ55JZJ.js` -- NewsApiService**

| Method | Endpoint                                   | Purpose                  |
|--------|--------------------------------------------|--------------------------|
| GET    | `/api/event-builder/public/news/{id}`      | View news article        |
| GET    | `/api/event-builder/public/news/file/{id}` | Get news file/attachment |

### 1.10 Root App -- Notification Hub APIs

**Source: `root/chunk-KUQINTYY.js` -- NotificationService**

| Method | Endpoint                                           | Purpose                    |
|--------|----------------------------------------------------|----------------------------|
| POST   | `/api/notification-hub/notifications/in-app`       | List in-app notifications  |
| GET    | `/api/notification-hub/notifications/in-app/stats` | Get notification stats     |
| POST   | `/api/notification-hub/notifications/in-app/read`  | Mark notifications as read |

### 1.11 Root App -- Admin Configurations API

**Source: `root/chunk-VMZTUSWJ.js` -- ConfigurationService**

| Method | Endpoint                          | Purpose                  |
|--------|-----------------------------------|--------------------------|
| GET    | `/api/admin/configurations/{key}` | Get single configuration |
| GET    | `/api/admin/configurations`       | Get all configurations   |
| POST   | `/api/admin/configurations`       | Save configuration(s)    |
| DELETE | `/api/admin/configurations`       | Delete configuration(s)  |

---

## 2. Route Definitions

### 2.1 Learn App -- Top-Level Route Segments

**Source: `learn/chunk-5JTPJSK6.js` -- AppRouteSegments**

```
NoLayout = "no-layout"
Courses  = "courses"
Reports  = "reports"
Tasks    = "tasks"
Timetable = "timetable"
```

### 2.2 Learn App -- Course View/Manage Sub-Routes

**Source: `learn/chunk-TZDR3JE7.js` -- CourseRouteSegments**

```
ViewRoot    = "view"
Actual      = "actual"
Archived    = "archived"
ManageRoot  = "manage"
Learning    = "learning"
Themes      = "themes"
LongReads   = "longreads"
Settings    = "settings"
Preview     = "preview"
```

### 2.3 Learn App -- Course Settings Sub-Routes

**Source: `learn/chunk-2UCY7WTM.js` -- CourseSettingsTabSegments**

```
Main       = "main"
Students   = "students"
Listeners  = "listeners"
Groups     = "groups"
Teachers   = "teachers"
Reviewers  = "reviewers"
Activities = "activities"
```

### 2.4 Learn App -- Task Routes

**Source: `learn/chunk-6JTNIO3G.js` -- TaskRouteSegments**

```
StudentTasks         = "student-tasks"
ActualStudentTasks   = "actual-student-tasks"
ArchivedStudentTasks = "archived-student-tasks"
StudentsTasks        = "students-tasks"
```

### 2.5 Learn App -- Route Param Names

**Source: `learn/chunk-P56QO3TA.js`**

```
CourseId   = "courseId"
ThemeId    = "themeId"
LongreadId = "longreadId"
```

**Source: `learn/chunk-CC24ZP4J.js`**

```
TaskId = "taskId"
```

### 2.6 Learn App -- Full Route Tree (from chunk-4DX4QBZY.js)

```
/courses
  /manage
    /actual     --> CoursesTableComponent
      /:courseId --> CourseOutletComponent
        /         --> CourseEditComponent
        /settings --> courseSettingsRoutes
        /themes
          /:themeId
            /longreads
              /:longreadId --> LongreadEditorComponent
              /:longreadId/preview --> LongreadContentComponent
    /archived   --> (same structure, isArchived=true)
  /view
    /actual     --> CourseLearningComponent
      /:courseId --> CourseOutletComponent
        /         --> CourseOverviewComponent
        /themes
          /:themeId
            /longreads
              /:longreadId --> LongreadComponent
    /archived   --> ArchiveCoursesComponent (same structure)
```

### 2.7 Learn App -- Task Route Tree (from chunk-3HLKTFK2.js)

```
/tasks
  /student-tasks/:taskId --> StudentTaskComponent (redirects to course longread)
  /actual-student-tasks  --> StudentTasksComponent (isArchived=false)
  /archived-student-tasks --> StudentTasksComponent (isArchived=true)
  /students-tasks        --> StudentsTasksComponent (teacher view)
    /:taskId             --> TeacherTaskComponent
```

### 2.8 Learn App -- Course Settings Route Tree (from chunk-7VUA3I56.js)

```
/courses/:courseId/settings
  /main       --> CourseMainSettingsComponent
  /students   --> CourseStudentsSettingsComponent
  /listeners  --> CourseListenersSettingsComponent
  /groups     --> CourseGroupsSettingsComponent
  /teachers   --> teachersSettingsRoutes
  /reviewers  --> reviewersSettingsRoutes
  /activities --> CourseActivitiesComponent
```

---

## 3. Data Models & Enums

### 3.1 Task States

**Source: `learn/chunk-KED2J7FW.js`**

```javascript
TaskStatus = {
  Backlog:    "backlog",
  InProgress: "inProgress",
  Review:     "review",
  Evaluated:  "evaluated",
  Failed:     "failed"
}
```

Color mapping:

- Backlog -> SupportNeutral
- InProgress -> SupportCategorical12Pale
- Review -> SupportCategorical13Pale
- Evaluated -> PositivePale
- Failed -> NegativePale

### 3.2 Course/Longread Content Status

**Source: `learn/chunk-Y2I2TH22.js` (CourseOverviewComponent)**

```javascript
MarkStatus = {
  Passed:     "passed",
  Pristine:   "pristine",
  Blocked:    "blocked",
  InProgress: "inProgress"
}
```

### 3.3 Exercise/Activity Types

**Source: `learn/chunk-N4N6VP3I.js`**

```javascript
ExerciseType = {
  Coding:    "coding",
  Questions: "questions"
}
```

### 3.4 Material Discriminators

**Source: `learn/chunk-KZ4SBTZ3.js`**

```javascript
FileTypes     = { File: "file", Video: "video", Audio: "audio" }
ImageTypes    = { Image: "image" }
MarkdownTypes = { Markdown: "markdown" }
VideoState    = {
  Unspecified:     "unspecified",
  Empty:           "empty",
  Uploaded:        "uploaded",
  Transcoding:     "transcoding",
  Viewable:        "viewable",
  Ready:           "ready",
  PartiallyReady:  "partiallyReady",
  Error:           "error"
}
VideoPlatformTypes = { VideoPlatform: "videoPlatform" }
```

### 3.5 Education Levels

**Source: `root/chunk-QHDVAQXE.js` (StudentService)**

```javascript
EducationLevel = {
  None:      "None",
  Bachelor:  "Bachelor",
  Master:    "Master",
  Dpo:       "Dpo",
  DpoMaster: "DpoMaster"
}
```

### 3.6 Enrollee Study Degree

**Source: `root/chunk-2RNYUVXP.js` (EnrolleeService)**

```javascript
StudyDegreeType = {
  Bachelor: "Bachelor",
  Master:   "Master",
  None:     "None"
}
```

### 3.7 Grant Confirmation Status

**Source: `root/chunk-H2UV6AAT.js` (GrantsService)**

```javascript
GrantStatus = {
  None:           "None",
  Denied:         "Denied",
  NotConfirmed:   "NotConfirmed",
  Confirmed:      "Confirmed",
  ConfirmedIfTop: "ConfirmedIfTop"
}
```

### 3.8 Event Registration State

**Source: `root/chunk-44YKIWJR.js`**

```javascript
EventRegistrationState = {
  None: "None",
  ReadyToRegister: "ReadyToRegister",
  TicketsLimitHasBeenReached: "TicketsLimitHasBeenReached",
  TicketsLimitHasBeenReachedTryOnline: "TicketsLimitHasBeenReachedTryOnline",
  RegistrationEndDateHasBeenReached: "RegistrationEndDateHasBeenReached"
}
```

Event action types: `apply`, `broadcast`, `record`, `force-applying`

### 3.9 Timetable Registration Status

**Source: `learn/chunk-HYIXHTRH.js`**

```javascript
TimetableRegistrationStatus = {
  Opened:    "opened",
  Scheduled: "scheduled",
  Closed:    "closed"
}
```

### 3.10 Notification Categories

**Source: `root/chunk-KUQINTYY.js`**

```javascript
NotificationCategoryId = { Education: 1, Other: 2 }
NotificationCategory   = { Education: "Education", Others: "Others" }
```

### 3.11 Resolve Data Keys

**Source: `learn/chunk-LH6TJVOX.js`**

```javascript
ResolveKeys = {
  Course:    "course",
  Theme:     "theme",
  Longread:  "longread",
  IsArchived: "isArchived"
}
```

### 3.12 Configuration Value Types

**Source: `root/chunk-VMZTUSWJ.js`**

```javascript
ConfigValueType = {
  None:     "None",
  Object:   "Object",
  Boolean:  "Boolean",
  Int:      "Int",
  Double:   "Double",
  DateOnly: "DateOnly",
  DateTime: "DateTime",
  Text:     "Text"
}
```

---

## 4. Feature Flags / Flipt Integration

### 4.1 Flipt Provider Configuration

**Source: `learn-dynamical-config.json`**

```json
{
    "authUrl": "https://id.centraluniversity.ru",
    "hubAppUrl": "https://my.centraluniversity.ru",
    "adminAppUrl": "https://my.centraluniversity.ru/admin",
    "tiMeUrl": "https://time.cu.ru",
    "statistEndpointUrl": "https://api-statist.tinkoff.ru",
    "env": "prod",
    "fliptProviderUrl": "https://my.centraluniversity.ru",
    "informerScriptUrl": "https://forge-informer-module.t-static.ru/informer-web-components.js"
}
```

The Flipt feature flag provider URL is `https://my.centraluniversity.ru`.

### 4.2 Feature Flag Usage

Feature flags are evaluated via `isEnabled` calls on a feature flag service (FliptProvider). Found
in multiple chunks:

- `learn/chunk-PBADBHDX.js` -- Uses feature flags for conditional rendering
- `learn/chunk-DNFIPLFW.js` -- Feature flag checks in analytics
- `learn/chunk-3CU6GOFU.js` -- Service worker / update features
- `root/chunk-SIQ3MUTR.js` -- Root app feature checks
- `root/chunk-TJTDBR6Z.js` -- Feature flag provider setup
- `learn/chunk-NXLFTGU7.js` -- Feature gating in learn app
- `learn/chunk-23VGFXOH.js` -- Feature-gated longread editor functionality

The Angular `KNF5L6KI.js` and `E4YLUPE4.js` contain the base injection framework used by the
FliptProvider service.

---

## 5. Permission / Role System

### 5.1 Course Roles

**Source: `learn/chunk-JYGWQKMQ.js`**

```javascript
CourseRoles = {
  AddCourse:              "lms_add_course",
  ViewCourseToLearn:      "lms_view_course_to_learn",
  ViewCourseToManage:     "lms_view_course_to_manage",  // (NOT in this chunk, see below)
  EditCourse:             "lms_edit_course",
  DeleteCourse:           "lms_delete_course",
  ViewCourseEvents:       "lms_view_course_events",
  PublishCourse:          "lms_publish_course",
  ReturnToDraftCourse:    "lms_return_to_draft_course",
  EditCourseSkillLevel:   "lms_edit_course_skill_level",
  ManageGroups:           "lms-tasks-manage-course_group",
  ViewGroups:             "lms-tasks-view-course_group"
}
```

### 5.2 Sidebar Visibility Roles

**Source: `learn/chunk-ORCZSRVH.js`**

```javascript
SidebarRoles = {
  ViewTasksToEvaluate:  "lms_view_sidebar_tasks_to_evaluate",
  ViewTasksToSolve:     "lms_view_sidebar_tasks_to_solve",
  ViewCoursesToManage:  "lms_view_sidebar_courses_to_manage",
  ViewCoursesToLearn:   "lms_view_sidebar_courses_to_learn"
}
```

### 5.3 Task Roles

**Source: `learn/chunk-MWYOKMK6.js`**

```javascript
TaskRoles = {
  ViewAllTasks:           "lms_view_all_tasks",
  ViewTask:               "lms_view_task",
  StartTask:              "lms_start_task",
  SubmitTask:             "lms_submit_task",
  RejectTask:             "lms_reject_task",
  EvaluateTask:           "lms_evaluate_task",
  GrantExtraScoreTask:    "lms_grant_extra_score_for_task",
  RefuseExtraScoreTask:   "lms_refuse_extra_score_for_task",
  ViewTaskEvents:         "lms_view_task_events",
  AttachReviewerToTask:   "lms_attach_reviewer_to_task",
  ProlongTask:            "lms-tasks-prolong_deadline-task",
  ViewLateDays:           "lms-tasks-view_late_days-student",
  ProlongLateDaysTask:    "lms-tasks-prolong_late_days-task",
  CancelLateDaysTask:     "lms-tasks-cancel_late_days-task"
}
```

### 5.4 Longread Roles

**Source: `learn/chunk-YBIXENKV.js`**

```javascript
LongreadRoles = {
  OrderLongreads:       "lms_order_theme_longreads",
  AddLongread:          "lms_add_longread",
  ViewLongread:         "lms_view_longread",
  EditLongread:         "lms_edit_longread",
  PreviewLongread:      "lms_preview_longread",
  DeleteLongread:       "lms_delete_longread",
  ViewLongreadEvents:   "lms_view_longread_events",
  PublishLongread:      "lms_publish_longread",
  ReturnToDraftLongread:"lms_return_to_draft_longread"
}

ThemeRoles = {
  OrderThemes:       "lms_order_course_themes",
  AddTheme:          "lms_add_theme",
  ViewTheme:         "lms_view_theme",
  EditTheme:         "lms_edit_theme",
  DeleteTheme:       "lms_delete_theme",
  ViewThemeEvents:   "lms_view_theme_events",
  PublishTheme:      "lms_publish_theme",
  ReturnToDraftTheme:"lms_return_to_draft_theme"
}
```

### 5.5 Material Roles

**Source: `learn/chunk-T4STSLR4.js`**

```javascript
MaterialRoles = {
  OrderMaterials:         "lms_order_longread_materials",
  AddMaterial:            "lms_add_material",
  ViewMaterial:           "lms_view_material",
  EditMaterial:           "lms_edit_material",
  DeleteMaterial:         "lms_delete_material",
  PublishMaterial:        "lms-tasks-publish-material",
  ReturnToDraftMaterial:  "lms-tasks-return_to_draft-material",
  ViewDraftMaterial:      "lms-tasks-view_draft-material"
}
```

### 5.6 Combined Role Groups

**Source: `learn/chunk-EJTNNIJ6.js`**

```
ManageCourseRoles     = [ViewCoursesToManage, AddCourse, EditCourse, DeleteCourse, PublishCourse, ReturnToDraftCourse]
ManageThemeRoles      = [ViewCoursesToManage, AddTheme, EditTheme, DeleteTheme, OrderThemes, PublishTheme, ReturnToDraftTheme]
ManageLongreadRoles   = [ViewCoursesToManage, AddLongread, EditLongread, DeleteLongread, OrderLongreads, PublishLongread, ReturnToDraftLongread]
ManageMaterialRoles   = [ViewCoursesToManage, AddMaterial, EditMaterial, DeleteMaterial, OrderMaterials]
AllManageRoles        = union of above four
ViewLongreadRoles     = [ViewCoursesToLearn, ViewLongread, ViewMaterial]
ViewCourseStructure   = [ViewCoursesToLearn, ViewCourseToLearn, ViewTheme]
AllViewRoles          = union of ViewCourseStructure + ViewLongreadRoles
```

---

## 6. Late Days System

### 6.1 Late Days API Endpoints

**Source: `learn/chunk-DPBB7AEG.js`**

| Method | Endpoint                                          | Purpose                                          |
|--------|---------------------------------------------------|--------------------------------------------------|
| GET    | `/api/micro-lms/students/me`                      | Get current student (includes `lateDaysBalance`) |
| PUT    | `/api/micro-lms/tasks/{taskId}/late-days-prolong` | Prolong task deadline (`{lateDays: N}`)          |
| PUT    | `/api/micro-lms/tasks/{taskId}/late-days-cancel`  | Cancel late days prolongation                    |

### 6.2 Late Days Business Logic

**Source: `learn/chunk-DPBB7AEG.js` -- LateDaysService**

- Gets `lateDaysBalance` from `/students/me`
- If balance > 0: opens editor dialog to select number of days
- If balance == 0: shows "No late days" dialog
- On prolong success: shows "Deadline moved" alert with count of days used
- On cancel: shows confirmation dialog, returns spent late days
- Response includes `lateDaysUsed` (prolong) and `lateDaysCancelled` (cancel)

### 6.3 Late Days Editor UI

**Source: `learn/chunk-TQM5AJH7.js` -- LateDaysEditorComponent**

- Input fields: number of days (min 1, max = availableDays)
- Shows task exercise name and course name
- Validates: required, min(1), max(availableDays)
- Russian labels: "Transfer deadline", available days from balance

### 6.4 Late Days Permission Roles

From `learn/chunk-MWYOKMK6.js`:

- `lms-tasks-view_late_days-student` -- can view late days balance
- `lms-tasks-prolong_late_days-task` -- can prolong task with late days
- `lms-tasks-cancel_late_days-task` -- can cancel late days prolongation

### 6.5 Late Days Visibility Conditions

**Source: `learn/chunk-TQM5AJH7.js`**

- Cancel button visible when: user has `CancelLateDaysTask` role AND `task.lateDays > 0` AND
  `task.isLateDaysEnabled`
- Prolong button visible when: user has `ProlongLateDaysTask` role AND exercise type is `Coding`

---

## 7. Error Handling Patterns

### 7.1 HTTP Error Helper

**Source: `learn/chunk-CCU76LAC.js` (referenced in chunk-DPBB7AEG.js)**

A helper function checks error status (e.g., `BadRequest`) and returns `error.detail` or fallback
message.

### 7.2 Common Error Patterns

Throughout the codebase, errors are handled with:

- `catchError` -> show alert notification with error message
- `handleError(injector, { errorMessage: "..." })` pattern
- Service Worker errors: codes 5600-5604
- API errors mapped to user-friendly Russian messages

---

## 8. External Service URLs

**Source: `learn-dynamical-config.json`**

| Service             | URL                                                                    |
|---------------------|------------------------------------------------------------------------|
| Auth (OAuth/OIDC)   | `https://id.centraluniversity.ru`                                      |
| Hub App             | `https://my.centraluniversity.ru`                                      |
| Admin App           | `https://my.centraluniversity.ru/admin`                                |
| TiMe (Timetable)    | `https://time.cu.ru`                                                   |
| Statist (Analytics) | `https://api-statist.tinkoff.ru`                                       |
| Flipt Feature Flags | `https://my.centraluniversity.ru`                                      |
| Informer Script     | `https://forge-informer-module.t-static.ru/informer-web-components.js` |

---

## 9. Key Architecture Findings

### 9.1 Two Separate Angular Apps

1. **Learn App** (`learn/` chunks): LMS functionality -- courses, themes, longreads, tasks,
   timetable. Base API: `/api/micro-lms`
2. **Root App** (`root/` chunks): Hub/portal -- enrollee profiles, grants, events, news,
   notifications, admin configs. APIs: `/api/hub/*`, `/api/event-builder/*`,
   `/api/notification-hub/*`, `/api/admin/*`

### 9.2 Angular Framework

Both apps are Angular 17+ (based on signals, standalone components, new control flow syntax). They
use:

- Taiga UI component library (tui-* components)
- ProseMirror-based rich text editor
- Service Worker for PWA updates
- Cobrowsing integration (Tinkoff/TBank cobrowsing)
- RxJS-heavy reactive patterns

### 9.3 Authentication

Auth is handled via `https://id.centraluniversity.ru` (OAuth/OIDC). Role-based access control with
granular permissions (see Section 5).

### 9.4 Flipt Feature Flags

Feature flags are served from `https://my.centraluniversity.ru` and used throughout both apps for
conditional feature enablement.

### 9.5 API Pattern Summary

All API calls follow REST conventions:

- Base URL injection tokens define the prefix
- Services use Angular's HttpClient (`http.get/post/put/delete`)
- Pagination via query params
- Blob responses for file downloads
- Error handling via RxJS `catchError` with alert notifications
