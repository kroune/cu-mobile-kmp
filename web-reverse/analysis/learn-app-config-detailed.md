# CU Learn App -- Detailed Architecture Analysis

Source: Angular 21.1.4 (zoneless) app at `learn.centraluniversity.ru`
Files analysed: `chunk-HTCSOUNZ.js` (appConfig + routes + providers), `main-EVFPTRTH.js` (
bootstrap), `chunk-PBADBHDX.js` (AppComponent), plus ~20 referenced chunk files.

---

## 1. Bootstrap Flow

**File: `main-EVFPTRTH.js`**

1. Sets `globalThis.$localize` for i18n (Russian locale `ru`).
2. Registers Russian locale data (date formats, currencies RUB, etc.).
3. `M()` async function:
    - Fetches `./dynamical-config/configuration.json` at runtime.
    - Dynamically imports `AppComponent` from `chunk-PBADBHDX.js`.
    - Dynamically imports `appConfig` from `chunk-HTCSOUNZ.js`.
    - Calls `bootstrapApplication(AppComponent, mergedConfig)`.
    - The dynamic config is injected via `{provide: LMS_APP_CONFIGURATION, useValue: configJson}`.

**Dynamic config token:** `LMS_APP_CONFIGURATION` (from `chunk-GD2ZCBMN.js`)

```
InjectionToken("LMS_APP_CONFIGURATION")
```

Runtime config shape:

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

---

## 2. AppComponent

**File: `chunk-PBADBHDX.js`** -- exported as `AppComponent` (selector: `app-root`)

### Structure

```
<tui-root>
  <router-outlet />
  <!-- projected: tuiOverPopups, tuiOverNotifications, tuiOverPortals, tuiOverHints -->
</tui-root>
```

### Injected services

| Field                              | Service                            | Purpose                                              |
|------------------------------------|------------------------------------|------------------------------------------------------|
| `#Y`                               | `Y` (imported from chunk-TWQRS7DC) | Unknown (injected but not directly used in template) |
| `#re` (ServiceWorkerUpdateService) | `re` class                         | Handles SW updates, version checks, reload prompts   |
| `#Jt` (AppLoadedService)           | `Jt` (from chunk-DNFIPLFW)         | Calls `sendAppLoaded()` in `ngAfterViewInit`         |

### ServiceWorkerUpdateService (`re`)

Injected dependencies:

- `SwUpdate` (Angular service worker)
- `pI` (NgZone)
- `A` (Window/Document)
- `Lt` (TuiAlertService)
- `ot` (Injector)
- `Qt` (ErrorLoggerService)
- `$` (TUI_IS_MOBILE)
- `_t` (PLATFORM_ID)
- `$t` (Router)

Behavior:

- Checks for updates every 3600 seconds (1 hour) when app is stable.
- On `VERSION_READY`: waits for next navigation event, then activates update and reloads.
- On `VERSION_INSTALLATION_FAILED`: shows error alert with "Ctrl+Shift+R" / "Cmd+Shift+R" hint.
- On `unrecoverable`: shows permanent error alert, reloads page.

---

## 3. Route Table

### Top-Level Route Segments (from chunk-5JTPJSK6.js)

```typescript
enum AppRoutes {
  NoLayout    = "no-layout",
  Courses     = "courses",
  Reports     = "reports",
  Tasks       = "tasks",
  Timetable   = "timetable"
}
```

### Course Sub-Routes (from chunk-TZDR3JE7.js)

```typescript
enum CourseRoutes {
  ViewRoot    = "view",
  Actual      = "actual",
  Archived    = "archived",
  ManageRoot  = "manage",
  Learning    = "learning",
  Themes      = "themes",
  LongReads   = "longreads",
  Settings    = "settings",
  Preview     = "preview"
}
```

### Report Sub-Routes (from chunk-XUXMCUIT.js)

```typescript
enum ReportRoutes {
  StudentsPerformance  = "students-performance",
  Actual               = "actual",
  Archived             = "archived",
  GradesUpload         = "grades-upload",
  StudentPerformance   = "student-performance",
  GradeBook            = "grade-book",
  StudentGradeBook     = "student-grade-book"
}

enum ReportParams {
  CourseId   = "courseId",
  UploadId   = "uploadId"
}

enum ReportQueryParams {
  Course     = "course",
  Courses    = "courses",
  IsArchived = "isArchived"
}
```

### Maintenance Route

```
path: "maintenance"   -->  fE = ["/", "maintenance"]
```

### Reconstructed Full Route Tree

```
/                              (redirects or main layout)
/courses/view                  (student course list - "actual" tab)
/courses/view/actual
/courses/view/archived
/courses/manage                (teacher/admin course management)
/courses/manage/actual
/courses/manage/archived
/courses/{courseId}/learning    (course learning view)
/courses/{courseId}/themes      (themes view inside a course)
/courses/{courseId}/longreads   (longread content view)
/courses/{courseId}/settings    (course settings - teacher)
/courses/{courseId}/preview     (course preview)
/reports/students-performance
/reports/actual
/reports/archived
/reports/grades-upload
/reports/student-performance
/reports/grade-book
/reports/student-grade-book
/reports/:courseId
/reports/:uploadId
/tasks                         (tasks view - student/teacher)
/timetable                     (timetable/schedule view)
/no-layout/*                   (routes without sidebar)
/maintenance                   (maintenance/503 page)
```

### Auth-Related Paths (from chunk-H2KAKJ6J.js)

These are auth redirect paths, not Angular routes. They go to the `authUrl` IdP:

```typescript
enum AuthPaths {
  UserToken       = "/account/me",
  SignIn           = "/auth",
  SignInCallback   = "/account/signin/callback",
  SignUp           = "/registrations",
  Logout           = "/account/signout"
}
```

### Guards

**`fi` (AuthGuard)** -- used as `canActivate`:

- Injects `AuthService` (cg) and `Router`.
- Checks `isAuthenticated$` observable.
- If not authenticated and `notAuthorisedRedirectUrl` exists in route data, redirects there.
- If not authenticated and no redirect URL, calls `navigateToSignIn$()`.
- Returns `false` if unauthenticated.

---

## 4. API Endpoints

### Base URLs

| Token                     | Value            | Source            |
|---------------------------|------------------|-------------------|
| `LMS_BASE_API_URL`        | `/api/micro-lms` | chunk-NJXQAJX7.js |
| `BASE_AUTH_API_URL_TOKEN` | `/api`           | chunk-H2KAKJ6J.js |

### Notification Hub API (NotificationApiService, chunk-HTCSOUNZ.js line 13)

| Method | Path                                               | Purpose                                |
|--------|----------------------------------------------------|----------------------------------------|
| POST   | `/api/notification-hub/notifications/in-app`       | List in-app notifications              |
| GET    | `/api/notification-hub/notifications/in-app/stats` | Get notification stats (unread counts) |
| POST   | `/api/notification-hub/notifications/in-app/read`  | Mark notifications as read             |

### LMS Courses API (CoursesApiService, chunk-SQDJS5LQ.js)

Base: `/api/micro-lms`

| Method | Path                                   | Purpose                                               |
|--------|----------------------------------------|-------------------------------------------------------|
| GET    | `/courses/slim`                        | Get slim course list (params: `ignoreUserAssignment`) |
| GET    | `/courses/student?{params}`            | Get student courses by state                          |
| GET    | `/courses/student`                     | Get paginated student courses                         |
| GET    | `/courses/{courseId}/themes?{params}`  | Get themes for a course                               |
| GET    | `/themes/{themeId}/longreads?{params}` | Get longreads for a theme                             |
| GET    | `/courses/student/slim?slim=true`      | Get slim student courses                              |
| GET    | `/courses/student/count`               | Count of student courses                              |
| GET    | `/courses/count`                       | Count of all courses (published, archived, draft)     |

### Students API (StudentApiService, chunk-DPBB7AEG.js)

Base: `/api/micro-lms`

| Method | Path                                | Purpose                              |
|--------|-------------------------------------|--------------------------------------|
| GET    | `/students`                         | Get students (paginated)             |
| GET    | `/students/me`                      | Get current student                  |
| PUT    | `/tasks/{taskId}/late-days-prolong` | Prolong task deadline with late days |
| PUT    | `/tasks/{taskId}/late-days-cancel`  | Cancel task deadline prolongation    |

### Calendar Events API (chunk-CSB6UCKE.js)

Base: `/api/micro-lms/calendar-events`

| Method | Path                            | Purpose                                               |
|--------|---------------------------------|-------------------------------------------------------|
| GET    | `/slot-management/config`       | Get timetable registration state                      |
| PUT    | `/slot-management/config`       | Save registration config (open/close dates)           |
| GET    | `/reports/lesson-registrations` | Download timetable report (blob, param: `semesterId`) |

### User Session API (SessionService, chunk-H2KAKJ6J.js)

| Method | Path              | Purpose                        |
|--------|-------------------|--------------------------------|
| GET    | `/api/account/me` | Get current user token/session |

### Student Hub API Rewriting (VB Interceptor, chunk-HTCSOUNZ.js)

The `VB` interceptor rewrites `api/hub/*` to `api/student-hub/*` for users with the `UseStudentHub`
role. The following URL patterns are intercepted:

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

---

## 5. Data Models / Interface Shapes

### User Token (Session)

```typescript
interface UserToken {
  sub: string;                    // user ID
  roles: string[];                // realm roles
  groups: string[];               // user groups
  resource_access?: {
    "realm-management"?: {
      roles: string[];
    };
  };
}
```

### Roles Enum (chunk-L6URXEQJ.js)

```typescript
enum Roles {
  Teacher                              = "default_teacher",
  Student                              = "default_student",
  Enrollee                             = "default_enrollee",
  Assistant                            = "lms_assistant",
  PreStudent                           = "default_pre_student",
  Staff                                = "default_staff",
  LmsViewApp                           = "lms_view_app",
  AuthViewTime                         = "auth_view_time",
  AIAssistantViewApp                   = "ai_assistant-view-app",
  AdminViewApp                         = "admin_view_app",
  AdminViewJob                         = "admin_view_job",
  AdminEditUser                        = "admin_edit_user",
  ViewDevFeature                       = "default_view_dev_feature",
  InterviewSchedulerViewInterviewer    = "interview_scheduler_view_interviewer",
  InterviewSchedulerEditInterviewer    = "interview_scheduler_edit_interviewer",
  InterviewSchedulerViewSettings       = "interview_scheduler_view_settings",
  InterviewSchedulerEditSettings       = "interview_scheduler_edit_settings",
  InterviewSchedulerViewTimeslot       = "interview_scheduler_view_timeslot",
  InterviewSchedulerEditTimeslot       = "interview_scheduler_edit_timeslot",
  InterviewSchedulerViewBooking        = "interview_scheduler_view_booking",
  InterviewSchedulerEditBooking        = "interview_scheduler_edit_booking",
  InterviewSchedulerViewCandidate      = "interview_scheduler_view_candidate",
  EnrollmentFormView                   = "university_hub_view_enrollment_form",
  EnrollmentFormEdit                   = "university_hub_edit_enrollment_form",
  HubMmisMigration                     = "university_hub_mmis_migration",
  UseStudentHub                        = "use_student_hub"
}
```

### Sidebar Feature Flags (chunk-ORCZSRVH.js)

```typescript
enum SidebarFeatureFlags {
  ViewTasksToEvaluate  = "lms_view_sidebar_tasks_to_evaluate",
  ViewTasksToSolve     = "lms_view_sidebar_tasks_to_solve",
  ViewCoursesToManage  = "lms_view_sidebar_courses_to_manage",
  ViewCoursesToLearn   = "lms_view_sidebar_courses_to_learn"
}
```

### GradeBook Roles (chunk-ULV6EIU5.js)

```typescript
enum GradeBookRoles {
  EditGradeBookRole  = "lms-gradebook-manage-gradebook",
  DeleteRecordRole   = "lms-gradebook-delete-gradebook_record"
}
```

### Notification Model (reconstructed from chunk-HTCSOUNZ.js)

```typescript
interface InAppNotification {
  id: number;
  notificationId: string;
  title: string;
  description: string;
  createdAt: string;          // ISO datetime
  category: NotificationCategory;
  groupingKey: string;
  icon: NotificationIconType;  // "Education" | "News" | "ServiceDesk"
  startDate?: string;
  endDate?: string;
  previewImageUri?: string;
  link?: NotificationLink;
}

interface NotificationLink {
  uri: string;
  label: string;
  target?: "Blank" | "Self";
}

interface GroupedNotification {
  id: number;
  notificationId: string;
  title: string;
  description: string;
  createdAt: string;
  category: NotificationCategory;
  groupingKey: string;
  icon: NotificationIconType;
  startDate?: string;
  endDate?: string;
  previewImageUri?: string;
  links: NotificationLink[];
  unionCount: number;
  collapsed: boolean;
}

interface NotificationStats {
  categories: NotificationCategoryStat[];
}

interface NotificationCategoryStat {
  category: string;
  hasUnread: boolean;
}
```

### Notification Categories

```typescript
enum NotificationCategory {
  Education = 1,
  Other     = 2
}

enum NotificationCategoryName {
  Education = "Education",
  Others    = "Others"
}

enum NotificationIconType {
  Education   = "Education",
  News        = "News",
  ServiceDesk = "ServiceDesk"
}
```

### Pagination Model (chunk-WPLOIEQW.js)

```typescript
// API request format
interface PaginationApiRequest {
  params: {
    offset: number;       // pageNumber * pageSize
    limit: number;        // pageSize
    sortProperty?: string;
    sortDirection?: string;
    [filterKey: string]: any;
  }
}

// API response format
interface PaginatedResponse<T> {
  items: T[];
  paging: {
    offset: number;
    limit: number;
    totalCount: number;
  }
}

// Client-side format
interface PaginatedResult<T> {
  items: T[];
  pagination: {
    pageNumber: number;
    pageSize: number;
    total: number;
  };
  sort: { property: string; direction: string };
}
```

### Timetable Registration State (chunk-CSB6UCKE.js)

```typescript
enum RegistrationStatus {
  Opened    = "opened",
  Scheduled = "scheduled",
  Closed    = "closed"
}

interface RegistrationConfig {
  openDate?: string;    // ISO datetime
  closeDate?: string;   // ISO datetime
}

interface RegistrationState {
  status: RegistrationStatus;
  openDate: string | null;
  closeDate: string | null;
}
```

### Student Model (partial, from chunk-DPBB7AEG.js)

```typescript
interface Student {
  lateDaysBalance: number;
  // ...other fields
}

interface LateDaysProlongResult {
  lateDaysUsed: number;
}

interface LateDaysCancelResult {
  lateDaysCancelled: number;
}
```

### Longread Material Types (from chunk-4SKQR4GO.js)

```typescript
type MaterialDiscriminator =
  | "Coding"        // exercise
  | "Questions"     // exercise
  | "Markdown"      // text content
  | "VideoPlatform" // video player
  | "File"          // downloadable file
  | "Audio"         // audio player
  | "Video"         // video file
  | "Image";        // image

interface LongreadMaterial {
  id: string;
  discriminator: MaterialDiscriminator;
  content?: {
    value?: string;
    isTuiEditor?: boolean;
    file?: FileContent;
    url?: string;         // for VideoPlatform
    description?: string;
    timecodes?: Timecode[];
    imageScale?: number;
  };
}

interface Timecode {
  time: string;         // "HH:mm:ss"
  description: string;
}
```

### Button/Badge Appearances (chunk-2PCIRN5N.js)

```typescript
enum ButtonAppearance {
  Primary                          = "primary",
  PrimaryOnDark                    = "primary-on-dark",
  Secondary                        = "secondary",
  SecondaryOnDark                  = "secondary-on-dark",
  SecondaryDestructive             = "secondary-destructive",
  Tertiary                         = "tertiary",
  TertiaryDestructive              = "tertiary-destructive",
  FilledDestructive                = "filled-destructive",
  FilledPositive                   = "filled-positive",
  FlatLink                         = "flat-link",
  // ... more variants
}

enum BadgeAppearance {
  Secondary                = "secondary",
  AccentElectricBlue       = "accent-electric-blue",
  Neutral                  = "neutral",
  SupportLightGreen        = "support-light-green",
  SupportGreen             = "support-green",
  SupportYellow            = "support-yellow",
  SupportRed               = "support-red",
  // ... 20+ more variants
}

enum LinkTarget {
  Blank = "_blank",
  Self  = "_self"
}

enum Breakpoint {
  DesktopLarge = "desktopLarge",
  DesktopSmall = "desktopSmall",
  Tablet       = "tablet",
  Mobile       = "mobile"
}
```

---

## 6. Services and Their Dependencies

### AuthService (`cg`, chunk-HTCSOUNZ.js)

**Injected:**

- `SessionService` (HA)
- `Window` (_g)
- `AUTH_URL_TOKEN` (se)
- `APP_NAME` (ae)
- `BASE_AUTH_API_URL_TOKEN` (BB)

**Public API:**

- `logout$: Observable` -- emits on logout
- `isAuthenticated$: Observable<boolean>` -- derived from session
- `beforeNavigateToSignIn: Subject` -- fires before redirect
- `navigateToSignIn$(): Observable` -- redirects to IdP `/auth` with OIDC params
- `navigateToSignUp$(): Observable` -- redirects to IdP `/registrations`
- `logout(): void` -- navigates to signout URL
- `navigateToSignInCallback(): void` -- constructs callback URL

**OIDC Parameters:**

```
response_type = "code"
client_id     = "api-gateway"
scope         = "openid email offline_access"
redirect_uri  = "{origin}{basePath}/account/signin/callback?app={appName}"
```

### SessionService (HA, chunk-H2KAKJ6J.js)

**Injected:**

- `HttpClient`
- `BASE_AUTH_API_URL_TOKEN`

**Public API:**

- `session$: Observable<UserToken | null>` -- GET `/api/account/me`, shareReplay, catches 401 ->
  null
- `userToken$: Observable<UserToken>` -- session filtered to non-null
- `userId$: Observable<string>` -- extracts `sub`
- `roles$: Observable<string[]>` -- shareReplay
- `realmManagementRoles$: Observable<string[]>`
- `groups$: Observable<string[]>`

### RolesService (chunk-EYVG5CRO.js)

**Injected:**

- `USER_ROLES_TOKEN` (derived from session roles)

**Public API:**

- `hasAccess$(roles, options?): Observable<boolean>` -- checks role access with "Some"/"Every"/"
  NoOne" strategies
- `hasRole$(role): Observable<boolean>` -- single role check
- `hasRoles$(...roles): Observable<boolean[]>` -- multiple role checks
- `filterEntitiesByAccess$(entities, options): Observable<filtered>` -- filters entities by role

### NotificationApiService (`LI`, chunk-HTCSOUNZ.js)

**Injected:**

- `HttpClient`

**Methods:** `list$`, `stats$`, `read$` (see API Endpoints section)

### NotificationStateService (`zg`, chunk-HTCSOUNZ.js)

**Injected:**

- `NotificationApiService`
- `NOTIFICATION_IN_APP_CONFIG_TOKEN`

**Public API:**

- `checkNotificationStats$: Observable` -- polls every 5 minutes
- `hasUnreadAnyCategory$: Observable<boolean>`
- `notificationCategoryStatus$: Observable<Record<string, boolean>>`
- `retryNotificationStats(): void`
- `switchNotificationType(): void`

### NotificationSidebarService (`$g`, chunk-HTCSOUNZ.js)

**State:**

- `isOpen$: Observable<boolean>` -- BehaviorSubject

**Methods:** `open()`, `close()`

### CoursesApiService (chunk-SQDJS5LQ.js)

**Injected:**

- `HttpClient`
- `LMS_BASE_API_URL` (`/api/micro-lms`)

**Methods:** See Courses API section above.

### StudentApiService (chunk-DPBB7AEG.js)

**Injected:**

- `HttpClient`
- `LMS_BASE_API_URL`

### LateDaysService (chunk-DPBB7AEG.js)

**Injected:**

- `StudentApiService`
- `DialogService`
- `Injector`

**State:**

- `lateDaysBalance$: Observable<number>` -- shareReplay

**Methods:**

- `reload(): void`
- `updateLateDaysBalance$(taskDetails, dialogComponent): Observable`
- `resetTaskProlongation$(taskId): Observable`

### CalendarEventsService (chunk-CSB6UCKE.js)

**Public API:**

- `registrationState$: Observable<RegistrationState>` -- shareReplay
- `isTimetableRegistrationAvailable$: Observable<boolean>`
- `rootItemLabel$: Observable<string>` -- "Мои пары" or "Запись на пары"

### BreadcrumbService (chunk-MT6TUUT5.js)

**Injected:**

- `Router`
- `Injector`

**Behavior:**

- Listens to NavigationEnd events.
- Builds breadcrumb trail from route data `breadcrumb` property.
- Supports both static breadcrumb objects and factory functions.

### BreakpointService (chunk-HV4RE2NY.js)

**Signals:**

- `breakpoint: Signal<Breakpoint | null>`
- `isMobile: Signal<boolean>` (computed)
- `isTabletOrLess: Signal<boolean>` (computed)
- `isTabletOrWider: Signal<boolean>` (computed)
- `isDesktopSmallOrLess: Signal<boolean>` (computed)
- `isDesktopSmallOrWider: Signal<boolean>` (computed)

**Breakpoint thresholds:** Mobile=600px, Tablet=900px, DesktopSmall=1200px, DesktopLarge=1620px

### ErrorLoggerService (`yi`, chunk-HTCSOUNZ.js)

Console-based error logger (dev fallback):

- `setTag(key, value)`
- `setUser(user)`
- `report(error)` -- logs with group, tags, breadcrumbs
- `addBreadcrumb(data)`
- `handleError(error)`

### ScrollingService (chunk-K3PAG7Z6.js)

Uses Lenis smooth scroll library. Provides:

- `scrollDown$: Observable`
- `isScrollBarInUse$: Observable<boolean>`
- `scrollTo(selector)`, `scrollToElement(el, options, offset)`
- `disableScroll()`, `enableScroll()`
- `disableScrollDown(key)`, `enableScrollDown(key)`

### SmoothScrollService (chunk-K3PAG7Z6.js)

Wraps Lenis scroll library:

- `scroll$: Observable<LenisScrollEvent>`
- `scrollTo(...args)`
- `disable(key)`, `enable(key)`

---

## 7. HTTP Interceptors

### 1. ServiceUnavailableInterceptor (`Rr`)

- Catches HTTP 503 (ServiceUnavailable) responses.
- Delegates to `SERVICE_UNAVAILABLE_HANDLER_TOKEN`.
- Default handler: redirects via `SERVICE_UNAVAILABLE_REDIRECT_URL_TOKEN`.
- Can redirect with `skipLocationChange: true`.

### 2. UnauthorizedInterceptor (`WB`)

- Catches HTTP 401 (Unauthorized) responses.
- Calls `AuthService.navigateToSignIn$()` to redirect to IdP.
- Returns `EMPTY` after redirect (swallows the error).

### 3. StudentHubRewriteInterceptor (`VB`)

- Checks if request URL matches any of 29 `/api/hub/*` patterns (regex array `PB`).
- Checks user role `UseStudentHub` (cached as shareReplay observable).
- If user has the role and URL matches, rewrites `api/hub/` to `api/student-hub/`.
- Otherwise passes request through unchanged.

### 4. HttpErrorResponse Sanitizer (`ne` class)

Not an interceptor per se, but a custom error class that extends Angular's `HttpErrorResponse`:

- Strips hostname from error messages (privacy/security).
- Replaces: `"Http failure response for https://domain.com/api/..."` ->
  `"Http failure response for /api/..."`
- Cleans stack trace.

---

## 8. Auth / Session Handling

### Flow

1. **App loads** -> `SessionService.session$` fires GET `/api/account/me`.
2. If 401 -> session$ emits `null`, `isAuthenticated$` emits `false`.
3. Route guard (`fi`) checks `isAuthenticated$`:
    - If false + no redirect URL -> calls `navigateToSignIn$()`.
    - `navigateToSignIn$()` builds OIDC authorize URL and navigates browser to IdP.
4. OIDC params: `response_type=code`, `client_id=api-gateway`, `scope=openid email offline_access`.
5. After IdP auth, browser returns to `/account/signin/callback?app={appName}`.
6. On 401 during API call, `UnauthorizedInterceptor` redirects to sign-in.
7. Logout: navigates to `{BASE_AUTH_API_URL}/{Logout}` = `/api/account/signout`.

### Token Storage

No explicit token storage in these chunks. The session is cookie-based (httpOnly cookies set by the
API gateway). The `/api/account/me` endpoint returns the user token if session cookies are valid.

---

## 9. State Management Patterns

### Pattern 1: BehaviorSubject with shareReplay

Most services use RxJS `BehaviorSubject` + `shareReplay` for state:

```typescript
// Example: NotificationStateService
this.checkNotificationStats$ = this.trigger$.pipe(
  switchMap(() => defer(() => interval(300000))),
  switchMap(() => this.pollStats()),
  shareReplay({ refCount: true, bufferSize: 1 })
);
```

### Pattern 2: Angular Signals (computed/signal/effect)

Used extensively in components and the BreakpointService:

```typescript
this.isMobile = computed(() => this.breakpoint() === "mobile");
this.isTabletOrLess = computed(() => this.isEqualToOrLess("tablet"));
```

### Pattern 3: Generic Async State Container (`F` class, chunk-VITV7FAW.js)

A reusable state machine for async operations:

```typescript
class AsyncState<TArgs, TResult> {
  result$: Observable<TResult | null>;
  error$: Observable<Error | null>;
  isLoading$: Observable<boolean>;
  state$: Observable<{ result, error, isLoading }>;
  loadingEnd$: Observable<StateSnapshot>;
  firstLoadingEnd$: Observable<StateSnapshot>;

  next(args, force?): this;
  setResult(result): this;
  reload(): this;
  reset(): this;
  getIsLoadingSync(): boolean;
  getResultSync(): TResult | null;
  getLatestArgsSync(): TArgs;
}
```

Supports:

- `retryInterval` for automatic retry on error.
- `isNextArgsEqualFn` for deduplication (defaults to deep-equal).
- Auto-cleanup on Angular destroy.

### Pattern 4: Tree State Manager (`J` class, chunk-7ADN7E2A.js)

Generic expandable tree with lazy-loaded children:

```typescript
class TreeStateManager {
  storage$: Observable<Map<string, TreeItem>>;

  getItemByKey$(key): Observable<TreeItem>;
  loadChildren({ key, force }): this;
  init(): void;
  reset(): void;
}
```

Used by `CourseTreeManager` for sidebar navigation.

---

## 10. Provider Configuration (appConfig)

From the merged providers in `chunk-HTCSOUNZ.js`:

### Animation Providers (`kr`)

- `BrowserAnimations` provider
- Custom `AnimationRenderer`
- Uses `NgEagerAnimations` for immediate setup

### Auth Providers (`jB`)

```typescript
[
  { provide: xg, useClass: UnauthorizedInterceptor, multi: true },  // HTTP_INTERCEPTORS
  AuthService,
  SessionService
]
```

### ServiceUnavailable Provider

```typescript
{ provide: HTTP_INTERCEPTORS, useClass: ServiceUnavailableInterceptor, multi: true }
```

### Notification Providers

```typescript
function provideNotifications() {
  return [NotificationApiService, NotificationSidebarService, NotificationStateService];
}
```

### Preloader Removal (`ZB`)

Runs after first navigation event:

```typescript
router.events.pipe(
  filter(e => e instanceof NavigationEnd || e instanceof NavigationCancel),
  take(1)
).subscribe(() => {
  const el = document.getElementById("cu-app-preloader");
  if (el) {
    el.style.opacity = "0";
    el.ontransitionend = () => el.remove();
  }
});
```

### Breakpoint Configuration (chunk-K3PAG7Z6.js)

```typescript
{
  provide: TUI_MEDIA,
  useValue: {
    mobile: 600,
    tablet: 900,
    desktopSmall: 1200,
    desktopLarge: 1620
  }
}
```

---

## 11. Error Handling Patterns

### 1. HttpErrorResponse Sanitization

All HTTP errors are wrapped in custom `ne` class that strips hostnames from messages.

### 2. Service-Level catchError

Services typically use `catchError(() => of(fallback))`:

```typescript
// Registration state - on error returns empty object
getRegistrationState$().pipe(catchError(() => of({})))

// Notification stats - on error returns empty categories
stats$().pipe(catchError(() => of({ categories: [] })))
```

### 3. Sentry-Style Error Reporting

The `ErrorLoggerService` has Sentry-compatible stack trace parsing:

- `Fr` pattern: Chrome/Edge style
- `Sr` pattern: Firefox/Safari style
- `Ur` pattern: IE/old Edge style

Constructs Sentry-compatible event payloads with:

- `type`, `value`, `stacktrace.frames[]`
- Sentry DSN parsing via `Yr` regex
- Auth header: `X-Sentry-Auth`

### 4. SW Update Error Handling

- Installation failures: shows permanent alert, reloads.
- Unrecoverable: shows permanent alert, reloads.
- Errors during check: caught and reported to ErrorLoggerService.

### 5. Alert-Based Error Display (LateDaysService pattern)

```typescript
pipe(
  handleHttpResult(injector, {
    successAlertOptions: { label: "..." },
    successMessage: (result) => `...`,
    errorAlertOptions: { label: "..." },
    errorMessage: (error) => extractBadRequestDetail(error) ?? fallback
  })
)
```
