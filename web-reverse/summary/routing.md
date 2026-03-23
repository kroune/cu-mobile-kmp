# Routing -- CU LMS Website

Extracted from minified JavaScript source files of `https://my.centraluniversity.ru`.
Two separate Angular 21.1.4 apps: **Root** (hub/portal) and **Learn** (LMS).

---

## Root App Route Table

Base URL: `https://my.centraluniversity.ru/`

Source: `root/chunk-OIDSCTRK.js` (appConfig), `root/chunk-F4UDLC3X.js` (route enum)

### Route Path Enum

```
Events          = "events"
News            = "news"
Dashboard       = "dashboard"
Profile         = "profile"
InformerCaseList = "case-list"
Grants          = "grants"
MasterGrants    = "master-grants"
Grants2026      = "grants-2026"
Onboarding      = "onboarding"
Admission       = "admission"
Error           = "error"
```

### Full Route Tree

```
/ (root)
  canMatch: [Ea]  -- top-level authentication guard
  title: "Центральный университет"

  +-- / (enrollee onboarding branch)
  |   canMatch: [Enrollee role, onboarding-needed guard]
  |   children:
  |     +-- /onboarding         -> OnboardingComponent (chunk-65PL4UYO.js, lazy)
  |     +-- /**                 -> redirectTo /onboarding (saves current URL first)
  |
  +-- /onboarding               -> redirectTo /dashboard (for already-onboarded users)
  |
  +-- / (main layout: ShellComponent -- header + sidebar)
  |   children:
  |     +-- /dashboard          -> dashboardRoutes          (chunk-NNLUD57V.js, lazy)
  |     +-- /grants             -> hubGrantsRoutes          (chunk-4ACXTDD6.js, lazy)
  |     |                          canMatch: [bachelor grants guard]
  |     +-- /master-grants      -> masterGrantsRoutes       (chunk-XBPQU6OA.js, lazy)
  |     |                          canMatch: [master grants guard]
  |     +-- /admission          -> admissionRoutes          (chunk-MYPYERCK.js, lazy)
  |     |                          canMatch: [admission-available check]
  |     +-- /admission          -> futureStudentFormRoutes  (chunk-DU2L5VZK.js, lazy)
  |     |                          canMatch: [PreStudent or Student role]
  |     +-- /grants-2026        -> grant2026Routes          (chunk-O5N3J4AB.js, lazy)
  |     |                          canMatch: [grants-2026 guard]
  |     +-- /news               -> newsRoutes               (chunk-J2FWVIQV.js, lazy)
  |     |                          canMatch: [enrollee or student guard]
  |     +-- /profile            -> profileRoutes            (chunk-XYNDJCEW.js, lazy)
  |     +-- /events             -> eventsRoutes             (chunk-ISH5QSO5.js, lazy)
  |     |                          canMatch: [events visibility guard]
  |     +-- /case-list          -> HubInformerFeatureCaseListComponent (lazy)
  |     +-- /                   -> redirectTo /dashboard
  |     +-- /not-found          -> NotFoundComponent
  |     +-- /maintenance        -> MaintenanceComponent
  |     +-- /unsupported        -> UnsupportedBrowserComponent
  |     +-- /error              -> ErrorScreenComponent     (chunk-ZN6X5W4H.js, lazy)
  |     +-- /**                 -> NotFoundComponent
  |
  +-- /not-found                -> NotFoundComponent

/** (unauthenticated fallback) -> landing page
```

### Events Sub-Routes

Source: `root/chunk-ISH5QSO5.js`

```
/events
  +-- /                    -> EventsListComponent       (chunk-3AKKBUTX.js, lazy)
  +-- /:slug               -> EventViewComponent        (chunk-N2T4BRFD.js, lazy)
  +-- /**                  -> redirectTo ""
```

### Profile Sub-Routes

Source: `root/chunk-XYNDJCEW.js` (lazy, not fully analyzed)

Known paths from menu:
- `/profile` -- main profile page
- `/profile/experience-tab` -- achievements (only for students)

---

## Learn App Route Table

Base URL: `https://my.centraluniversity.ru/learn/`

Source: `learn/chunk-HTCSOUNZ.js` (appConfig), `learn/chunk-5JTPJSK6.js`, `learn/chunk-TZDR3JE7.js`, `learn/chunk-XUXMCUIT.js`

### Top-Level Route Segments

```
NoLayout   = "no-layout"
Courses    = "courses"
Reports    = "reports"
Tasks      = "tasks"
Timetable  = "timetable"
```

### Course Route Segments

```
ViewRoot   = "view"
Actual     = "actual"
Archived   = "archived"
ManageRoot = "manage"
Learning   = "learning"
Themes     = "themes"
LongReads  = "longreads"
Settings   = "settings"
Preview    = "preview"
```

### Task Route Segments

```
StudentTasks         = "student-tasks"
ActualStudentTasks   = "actual-student-tasks"
ArchivedStudentTasks = "archived-student-tasks"
StudentsTasks        = "students-tasks"
```

### Report Route Segments

```
StudentsPerformance  = "students-performance"
Actual               = "actual"
Archived             = "archived"
GradesUpload         = "grades-upload"
StudentPerformance   = "student-performance"
GradeBook            = "grade-book"
StudentGradeBook     = "student-grade-book"
```

### Course Settings Tab Segments

```
Main       = "main"
Students   = "students"
Listeners  = "listeners"
Groups     = "groups"
Teachers   = "teachers"
Reviewers  = "reviewers"
Activities = "activities"
```

### Route Param Names

```
CourseId   = "courseId"    (chunk-P56QO3TA.js)
ThemeId    = "themeId"     (chunk-P56QO3TA.js)
LongreadId = "longreadId" (chunk-P56QO3TA.js)
TaskId     = "taskId"      (chunk-CC24ZP4J.js)
```

### Full Route Tree

Source: `learn/chunk-4DX4QBZY.js`, `learn/chunk-3HLKTFK2.js`, `learn/chunk-7VUA3I56.js`

```
/                                            -> redirects or main layout
/maintenance                                 -> maintenance (503) page
/no-layout/*                                 -> routes without sidebar layout

/courses
  /view
    /actual                                  -> CourseLearningComponent (student view)
      /:courseId                              -> CourseOutletComponent
        /                                    -> CourseOverviewComponent
        /themes
          /:themeId
            /longreads
              /:longreadId                   -> LongreadComponent (student reading)
    /archived                                -> ArchiveCoursesComponent (same structure)

  /manage
    /actual                                  -> CoursesTableComponent (teacher/admin)
      /:courseId                              -> CourseOutletComponent
        /                                    -> CourseEditComponent
        /settings                            -> courseSettingsRoutes (see below)
        /themes
          /:themeId
            /longreads
              /:longreadId                   -> LongreadEditorComponent
              /:longreadId/preview           -> LongreadContentComponent
    /archived                                -> (same structure, isArchived=true)

/courses/:courseId/settings
  /main                                      -> CourseMainSettingsComponent
  /students                                  -> CourseStudentsSettingsComponent
  /listeners                                 -> CourseListenersSettingsComponent
  /groups                                    -> CourseGroupsSettingsComponent
  /teachers                                  -> teachersSettingsRoutes
  /reviewers                                 -> reviewersSettingsRoutes
  /activities                                -> CourseActivitiesComponent

/tasks
  /student-tasks/:taskId                     -> StudentTaskComponent (redirects to longread)
  /actual-student-tasks                      -> StudentTasksComponent (isArchived=false)
  /archived-student-tasks                    -> StudentTasksComponent (isArchived=true)
  /students-tasks                            -> StudentsTasksComponent (teacher view)
    /:taskId                                 -> TeacherTaskComponent

/reports
  /students-performance
    /actual                                  -> performance list
    /archived                                -> performance list (archived)
    /:courseId                               -> course performance detail
  /grades-upload
    /:uploadId                               -> grades upload detail
  /student-performance                       -> student's own performance
  /grade-book                                -> gradebook (teacher)
  /student-grade-book                        -> gradebook (student)

/timetable                                   -> timetable/schedule view
```

---

## Route Guards and Resolvers

### Root App Guards

Source: `root/chunk-OIDSCTRK.js`, `root/chunk-N3DZQN6I.js`

| Guard ID | Type | Purpose |
|----------|------|---------|
| `Ea` | canMatch | Top-level auth guard -- checks `isAuthenticated$`, redirects to Keycloak sign-in if false |
| `ln(role)` | canMatch | Role-based guard -- checks user has specified role(s) (e.g., `Enrollee`, `PreStudent`, `Student`) |
| `Rs` | canMatch | Onboarding-needed guard -- enrollee not yet completed onboarding |
| `Es([observables])` | canMatch | Observable-based availability -- e.g., `getIsAdmissionAvailableOnce$()` |
| `Qr` | canMatch | Events route visibility guard |
| `zs` | canMatch | News route visibility guard |
| `ks` | canMatch | Grants-2026 visibility guard |
| `Jt` | canMatch | Bachelor grants availability guard |
| `Vi` | canMatch | Master grants availability guard |

**Route-to-guard mapping:**

| Route | Guard | Required Role/Condition |
|-------|-------|------------------------|
| Onboarding branch | `ln(Enrollee)` + `Rs` | Enrollee who hasn't onboarded |
| `/admission` (form) | `ln([PreStudent, Student])` | PreStudent or Student |
| `/admission` (available) | `Es(getIsAdmissionAvailableOnce$())` | Server-driven availability |
| `/grants` | `Jt` | Bachelor grants available |
| `/master-grants` | `Vi` | Master grants available |
| `/grants-2026` | `ks` | Grants-2026 available |
| `/news` | `zs` | Enrollee or student |
| `/events` | `Qr` | Events visibility |

### Learn App Guards

Source: `learn/chunk-HTCSOUNZ.js`, `learn/chunk-H2KAKJ6J.js`

| Guard | Type | Purpose |
|-------|------|---------|
| `fi` (AuthGuard) | canActivate | Checks `isAuthenticated$`; if not authenticated, either redirects to `notAuthorisedRedirectUrl` (from route data) or calls `navigateToSignIn$()` |
| `CanDeactivateGuard` | canDeactivate | Prevents navigation away if form has unsaved changes (`form.dirty && !form.disabled`) |

### Learn App Resolvers

Source: `learn/chunk-LH6TJVOX.js`

Resolve data keys:
```
Course    = "course"     -- resolves course object for child routes
Theme     = "theme"      -- resolves theme object
Longread  = "longread"   -- resolves longread object
IsArchived = "isArchived" -- boolean flag from route config
```

Course subroutes use resolver chains to load course -> theme -> longread hierarchically before activating child components.

### Access Check Service

Source: `root/chunk-N3DZQN6I.js`, `learn/chunk-EYVG5CRO.js`

```typescript
HasAccessService.hasAccess$(roles, options?) -> Observable<boolean>
// options: { strategy: "some"|"every"|"noOne", defaultValue: false }
```

Used in guards and templates (via `hasRole` pipe) to check:
- Sidebar visibility: `lms_view_sidebar_courses_to_learn`, `lms_view_sidebar_tasks_to_solve`, etc.
- Course management: `lms_view_course_to_manage`, `lms_edit_course`, etc.
- Task permissions: `lms_submit_task`, `lms_evaluate_task`, etc.

---

## Lazy Loading Boundaries

### Root App Lazy Chunks

All feature routes are lazy-loaded via `loadChildren` / `loadComponent`:

| Route | Chunk | Feature |
|-------|-------|---------|
| `/dashboard` | `chunk-NNLUD57V.js` | Dashboard |
| `/grants` | `chunk-4ACXTDD6.js` | Bachelor grants |
| `/master-grants` | `chunk-XBPQU6OA.js` | Master grants |
| `/admission` | `chunk-MYPYERCK.js` | Admission (available) |
| `/admission` | `chunk-DU2L5VZK.js` | Admission form (PreStudent/Student) |
| `/grants-2026` | `chunk-O5N3J4AB.js` | Grants 2026 |
| `/news` | `chunk-J2FWVIQV.js` | News list/detail |
| `/profile` | `chunk-XYNDJCEW.js` | Profile |
| `/events` | `chunk-ISH5QSO5.js` | Events list/detail |
| `/case-list` | `chunk-V2W4HQVK.js` | Support/informer cases |
| `/onboarding` | `chunk-65PL4UYO.js` | Onboarding |
| `/error` | `chunk-ZN6X5W4H.js` | Error screen |
| Events list | `chunk-3AKKBUTX.js` | Events list page |
| Event detail | `chunk-N2T4BRFD.js` | Event view page |

### Learn App Lazy Chunks

| Route | Chunk | Feature |
|-------|-------|---------|
| Courses view/manage | `chunk-4DX4QBZY.js` | Course routes |
| Tasks | `chunk-3HLKTFK2.js` | Task routes |
| Course settings | `chunk-7VUA3I56.js` | Settings sub-routes |
| Reports | `chunk-XUXMCUIT.js` | Reports routes |
| Timetable | `chunk-CSB6UCKE.js` | Timetable |
| Longread editor | `chunk-23VGFXOH.js` | Longread editing |
| Course editor | `chunk-BF6EGGKK.js` | Course structure editing |
| Student task | `chunk-PODCHSSJ.js` | Task solving UI |

---

## Navigation Patterns

### Auth Flow Navigation

1. Unauthenticated user hits any route -> auth guard redirects to Keycloak
2. OIDC params: `response_type=code`, `client_id=api-gateway`, `scope=openid email offline_access`
3. After IdP auth -> callback at `/account/signin/callback?app={appName}`
4. On 401 during API call -> `UnauthorizedInterceptor` redirects to sign-in
5. Logout -> navigates to `/api/account/signout`

### Route Save/Restore

Source: `root/chunk-OIDSCTRK.js`

- Before redirecting to sign-in, the app saves the current route
- After authentication, it restores and navigates to the saved route
- Scoped: `"hub"` for main app, `"hub-onboarding"` for onboarding flow

### Breadcrumbs

Source: `learn/chunk-MT6TUUT5.js`

- `BreadcrumbService` listens to `NavigationEnd` events
- Builds trail from route data `breadcrumb` property
- Supports static objects `{ caption, icon }` and factory functions
- Main page label: "Главная"

### Cross-App Navigation

- Root app links to learn app via `lmsAppUrl` from dynamic config
- Learn app links back to hub via `hubAppUrl` from dynamic config
- Admin app accessible at `adminAppUrl` from dynamic config
- TiMe (timetable) at `tiMeUrl` from dynamic config

### Sidebar Navigation (Learn App)

Source: `learn/chunk-ORCZSRVH.js`

Menu items controlled by role-based feature flags:

| Menu Item | Required Role |
|-----------|---------------|
| Courses to learn | `lms_view_sidebar_courses_to_learn` |
| Courses to manage | `lms_view_sidebar_courses_to_manage` |
| Tasks to solve | `lms_view_sidebar_tasks_to_solve` |
| Tasks to evaluate | `lms_view_sidebar_tasks_to_evaluate` |

The timetable item label changes dynamically:
- Registration open: "Запись на пары"
- Registration closed: "Мои пары"

### Error/Maintenance Routes

- `/error` -- general error page with details (e.g., inconsistent user data)
- `/maintenance` -- 503 service unavailable page
- `/unsupported` -- unsupported browser page
- `/not-found` -- 404 page with role-based CTA:
  - Enrollees: "На главную" -> `/`
  - Others: "В обучение" -> lmsUrl
