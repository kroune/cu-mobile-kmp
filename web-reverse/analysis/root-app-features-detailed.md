# CU LMS Root App -- Detailed Feature Analysis

Angular 21.1.4 application. Entry point: `main-NI5Y7GEG.js` -> loads `chunk-TJTDBR6Z.js` (
AppComponent) + `chunk-OIDSCTRK.js` (appConfig). Configuration fetched from
`./dynamical-config/configuration.json`.

Locales: Russian (primary), English.

---

## 1. Application Routing Structure

Route constants (from `chunk-F4UDLC3X.js`):

```
Events       = "events"
News         = "news"
Dashboard    = "dashboard"
Profile      = "profile"
InformerCaseList = "case-list"
Grants       = "grants"
MasterGrants = "master-grants"
Grants2026   = "grants-2026"
Onboarding   = "onboarding"
Admission    = "admission"
Error        = "error"
```

### Route tree (from `chunk-OIDSCTRK.js`, lines 437-445):

```
/ (root, canMatch: Ea -- auth guard)
  |-- (enrollee + not-yet-onboarded branch)
  |   |-- /onboarding  -> chunk-65PL4UYO.js (OnboardingComponent)
  |   |-- /**           -> redirectTo dashboard
  |
  |-- /onboarding       -> redirectTo /dashboard (for already onboarded)
  |
  |-- / (layout: rc -- ShellComponent with header/sidebar)
  |   |-- /dashboard       -> chunk-NNLUD57V.js (dashboardRoutes)
  |   |-- /grants          -> chunk-4ACXTDD6.js (hubGrantsRoutes)         canMatch: [bachelor guard]
  |   |-- /master-grants   -> chunk-XBPQU6OA.js (masterGrantsRoutes)      canMatch: [master guard]
  |   |-- /admission       -> chunk-MYPYERCK.js (admissionRoutes)         canMatch: [admission available]
  |   |-- /admission       -> chunk-DU2L5VZK.js (futureStudentApplicationFormRoutes) canMatch: [PreStudent|Student]
  |   |-- /grants-2026     -> chunk-O5N3J4AB.js (grant2026Routes)         canMatch: [grants2026 guard]
  |   |-- /news            -> chunk-J2FWVIQV.js (newsRoutes)              canMatch: [enrollee|student]
  |   |-- /profile         -> chunk-XYNDJCEW.js (profileRoutes)
  |   |-- /events          -> chunk-ISH5QSO5.js (eventsRoutes)            canMatch: [events guard]
  |   |-- /case-list       -> chunk-V2W4HQVK.js (HubInformerFeatureCaseListComponent)
  |   |-- /                -> redirectTo /dashboard
  |   |-- /error           -> chunk-ZN6X5W4H.js (ErrorScreenComponent)
  |   |-- /**              -> error component
```

---

## 2. Events System

### 2.1 API Endpoints (from `chunk-44YKIWJR.js`)

Service: `EventsApiService` (private field `#t = "/api/event-builder/public/events"`)

| Method                       | HTTP | URL                                                      | Body / Params                                                                                                                                                             |
|------------------------------|------|----------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `list$(body)`                | POST | `/api/event-builder/public/events/list`                  | `{ paging: { limit, offset, sorting: [{ by, isAsc }] }, filter: { usePersonalSuggestionFilter, showOnlyMine, endDateGreaterThanOrEqualTo?, endDateLessThanOrEqualTo? } }` |
| `getEvent$(slug)`            | GET  | `/api/event-builder/public/events/slug/{slug}`           | --                                                                                                                                                                        |
| `getEventCalendarById({id})` | GET  | `/api/event-builder/public/events/{id}/appointment/file` | responseType: blob                                                                                                                                                        |
| `apply$({id})`               | POST | `/api/event-builder/public/events/apply/{id}`            | `{}`                                                                                                                                                                      |

### 2.2 Higher-level Service (from `chunk-44YKIWJR.js`)

`EventsFacadeService` wraps `EventsApiService` with:

- **Retry logic for getEvent$**: polls up to 5 times with 3-second intervals; stops on success or
  after `#t` (5) retries.
- **Polling list$**: if `requestsCount > 1`, creates interval-based polling with configurable
  `intervalTime`.

### 2.3 Event Ticket Registration Status

Enum `EventTicketRegistrationStatus`:

```
None
ReadyToRegister
TicketsLimitHasBeenReached
TicketsLimitHasBeenReachedTryOnline
RegistrationEndDateHasBeenReached
```

### 2.4 Event Action Types

```
apply      = "apply"
broadcast  = "broadcast"
record     = "record"
force-applying = "force-applying"
```

### 2.5 Events List Component (`chunk-3AKKBUTX.js`)

`HubEventsFeatureEventsListComponent`:

- **Two tabs**: Active (upcoming) and Archive (past)
- **Toggle**: "Only mine" (show only events user registered for)
- **Pagination**: limit = 24 events per page, "Load more" button
- **Active request body**: `sorting: [{by: "startDate", isAsc: true}]`, filter:
  `endDateGreaterThanOrEqualTo: now`
- **Archive request body**: `sorting: [{by: "startDate", isAsc: false}]`, filter:
  `endDateLessThanOrEqualTo: now`
- **Cache**: separate caches for active-all, active-mine, archived-all, archived-mine
- **Post-onboarding optimization**: if just completed onboarding (< 5 sec), uses higher polling (6
  requests / 1s interval for active tab, 2 requests / 5s for other)

### 2.6 Event View Component (`chunk-N2T4BRFD.js`)

`HubEventsFeatureEventViewComponent`:

- Loads event by slug from route params
- Shows: title, description, start/end dates with timezone, location, preview image, tags, format
- **Registration states**:
    - `isPossibleToRegister === false` -> "Registration closed" with reason
    - `TicketsLimitHasBeenReachedTryOnline` -> "Register online" button
    - `RegistrationEndDateHasBeenReached` -> clock icon + "Registration ended"
    - Normal -> "Register" button
- **Registration URL**: can be external link, Bitrix inline form, or internal apply API call
- **Apply error handling**: catches "ticket_already_exists" detail -> treats as success
- **Calendar download**: downloads .ics file via `getEventCalendarById`
- **Broadcast**: link to broadcast URL
- **LMS course URI**: link to course

---

## 3. Support / Case System

### 3.1 Route

```
/case-list -> HubInformerFeatureCaseListComponent (chunk-V2W4HQVK.js, lazy-loaded)
```

Title: "Поддержка" (Support).
Resolve: `UserName` from `userToken$.pipe(map(e => e.name))`

Note: The actual case-list component chunk (`chunk-V2W4HQVK.js`) is loaded separately and was not
found in the root bundle. The route configuration shows it exists as a lazy-loaded module with
access controlled by general authentication.

---

## 4. Profile Management

### 4.1 Route

```
/profile -> chunk-XYNDJCEW.js (profileRoutes), lazy-loaded
```

Title: "Профиль".

### 4.2 Profile Menu (from `chunk-OIDSCTRK.js`)

`UserProfileComponent`:

- Displays: `userName`, `userEmail` from token
- Avatar URL: `/api/hub/avatars/me` (GET, returns image)
- Actions dropdown:
    - "Профиль" -> navigates to `/profile`
    - "Достижения" (Achievements) -> navigates to `/profile/experience-tab` (only if student)
    - "Выйти" (Logout)

### 4.3 Enrollee Service (`chunk-2RNYUVXP.js`)

Manages enrollee (pre-student) profile data.

| Method              | HTTP | URL                                | Body                    |
|---------------------|------|------------------------------------|-------------------------|
| `getCurrent$()`     | GET  | `/api/hub/enrollees/me`            | --                      |
| `update$(body)`     | PUT  | `/api/hub/enrollees/me`            | enrollee update payload |
| `onboarding$(body)` | POST | `/api/hub/enrollees/onboarding/me` | onboarding data         |

**StudyDegreeType enum**: `Bachelor`, `Master`, `None`

**Enrollee data model** (deserialized from API):

```
firstName, lastName, middleName
phone (stored without "+", displayed with "+")
birthdate (format: "YYYY-MM-DD", parsed to TuiDay)
email
graduationYear (int)
studyDegreeType: Bachelor | Master | None
lastStudyDegreeType: School | University | None
city, citizenship
educationPlaceName, foreignEducationPlace (boolean)
snils
telegram
livesAbroad (boolean)
fullName (computed: firstName + lastName + middleName)
```

**Computed flags**:

- `isMasterEnrollee$`: studyDegreeType === Master
- `isBachelorEnrollee$`: studyDegreeType === Bachelor
- `isNineClassSchoolchild$()`: graduationYear >= 2028 && lastStudyDegreeType === School
- `isTenClassSchoolchild$()`: graduationYear >= 2027 && lastStudyDegreeType === School
- `isEnrolleeOnSecondCourseOrLess()`: graduationYear > 2027 && lastStudyDegreeType === University

### 4.4 Student Service (`chunk-QHDVAQXE.js`)

| Method            | HTTP | URL                    |
|-------------------|------|------------------------|
| `currentStudent$` | GET  | `/api/hub/students/me` |

**EducationLevel enum**: `None`, `Bachelor`, `Master`, `Dpo`, `DpoMaster`

Computed flags: `isMaster$`, `isDPOMaster$`, `isBachelor$`.

Access: only for users with roles `PreStudent` or `Student`.

### 4.5 Locale Service (`chunk-OIDSCTRK.js`)

| Method                  | HTTP | URL                      |
|-------------------------|------|--------------------------|
| `changeLocale$(locale)` | PUT  | `/api/account/me/locale` |

---

## 5. Grants System

### 5.1 Grants API (`chunk-H2UV6AAT.js`)

Service: `GrantsApiService`, providedIn root.

| Method                        | HTTP | URL                                                      |
|-------------------------------|------|----------------------------------------------------------|
| `getGrants$()`                | GET  | `/api/hub/grants/me`                                     |
| `getActiveItGrant$(year)`     | GET  | `/api/hub/grants/me/{year}/InformationTechnology/active` |
| `getActiveDesignGrant$(year)` | GET  | `/api/hub/grants/me/{year}/Design/active`                |

**Grant status enum**:

```
None, Denied, NotConfirmed, Confirmed, ConfirmedIfTop
```

---

## 6. News System

### 6.1 News API (`chunk-EFZ55JZJ.js`)

Base URL token: `NEWS_API_BASE_URL = "/api/event-builder/public/news"`

| Method        | HTTP | URL                                        |
|---------------|------|--------------------------------------------|
| `view$(id)`   | GET  | `/api/event-builder/public/news/{id}`      |
| `getFile(id)` | GET  | `/api/event-builder/public/news/file/{id}` |

### 6.2 Admissions State API (`chunk-2NELPLXO.js`)

| Method        | HTTP | URL                                        |
|---------------|------|--------------------------------------------|
| `getState$()` | GET  | `/api/event-builder/admissions/2025/state` |

---

## 7. Notification System

### 7.1 API Endpoints (`chunk-KUQINTYY.js`)

Service: `NotificationApiService`

| Method        | HTTP | URL                                                | Body                      |
|---------------|------|----------------------------------------------------|---------------------------|
| `list$(body)` | POST | `/api/notification-hub/notifications/in-app`       | notification list request |
| `stats$()`    | GET  | `/api/notification-hub/notifications/in-app/stats` | --                        |
| `read$(body)` | POST | `/api/notification-hub/notifications/in-app/read`  | mark-as-read payload      |

### 7.2 Notification Categories

```
Education = 1
Other = 2
```

Category names: `"Education"`, `"Others"`

### 7.3 Notification Stats Service (`chunk-KUQINTYY.js`)

`NotificationService`:

- Polls stats every **5 minutes** (300,000 ms)
- Exposes: `hasUnreadAnyCategory$`, `notificationCategoryStatus$`
- Category status: maps API categories to `{ education: boolean, others: boolean }`
- Configurable via `NOTIFICATION_IN_APP_CONFIG_TOKEN` (filters which categories to track)

### 7.4 Service Worker Push (`chunk-KUQINTYY.js`)

`SwPush` service:

- `messages`: PUSH event data
- `notificationClicks`: NOTIFICATION_CLICK event data
- `notificationCloses`: NOTIFICATION_CLOSE event data
- `requestSubscription(serverPublicKey)`: subscribe to push
- `unsubscribe()`: unsubscribe from push

---

## 8. Authentication & Authorization

### 8.1 Roles System (`chunk-N3DZQN6I.js`)

**User Roles**:

```
Teacher              = "default_teacher"
Student              = "default_student"
Enrollee             = "default_enrollee"
Assistant            = "lms_assistant"
PreStudent           = "default_pre_student"
Staff                = "default_staff"
LmsViewApp           = "lms_view_app"
AuthViewTime         = "auth_view_time"
AIAssistantViewApp   = "ai_assistant-view-app"
AdminViewApp         = "admin_view_app"
AdminViewJob         = "admin_view_job"
AdminEditUser        = "admin_edit_user"
ViewDevFeature       = "default_view_dev_feature"
UseStudentHub        = "use_student_hub"
... (+ interview scheduler roles, enrollment form roles, HubMmisMigration)
```

**Access Control Service** (`HasAccessService`):

- `hasAccess$(roles, {strategy, defaultValue})`: checks user roles
- Strategies: `Some` (any match), `Every` (all match), `NoOne` (none match)
- Default: `{ strategy: "some", defaultValue: false }`

### 8.2 User Token

From `chunk-OIDSCTRK.js`, the `UserTokenService` exposes `userToken$` observable with:

- `name`: user display name
- `email`: user email
- `phone_number`: user phone
- `id`: user ID (ssoId)
- `locale`: user locale

### 8.3 HTTP Layer (`chunk-7HYSEYNL.js`)

Angular `HttpClient` service (standard Angular HTTP). Key features:

- XSRF protection: reads `XSRF-TOKEN` cookie, sends as `X-XSRF-TOKEN` header
- Same-origin enforcement for XSRF
- Standard interceptor chain pattern
- Response types: json, blob, arraybuffer, text

### 8.4 Interceptors (from `chunk-OIDSCTRK.js`)

1. **Circuit Breaker Interceptor**: fetches blocked URL patterns from config, returns 503 for
   blocked requests. Retries config loading with exponential backoff (300s base, 1.1x factor, max
   1200s).

2. **Student Hub URL Rewriter**: for users with `UseStudentHub` role, rewrites certain
   `/api/hub/...` URLs. Pattern-matched URLs include:
    - `/api/hub/orders/student/{id}`
    - `/api/hub/orders/search`
    - `/api/hub/orders`
    - `/api/hub/orders/upload`
    - `/api/hub/orders/me`
    - `/api/hub/admin/orders`
    - `/api/hub/students/me`
    - `/api/hub/admin/students`

3. **Inconsistent User Data Interceptor**: catches error responses with
   `title === "InconsistentUserData"`, redirects to error page with details about duplicated
   email/phone.

---

## 9. Configuration Service

### 9.1 Admin Config API (`chunk-VMZTUSWJ.js`)

Service: `ConfigurationService`, providedIn root.

| Method                 | HTTP   | URL                               |
|------------------------|--------|-----------------------------------|
| `getConfig(key)`       | GET    | `/api/admin/configurations/{key}` |
| `getConfigs()`         | GET    | `/api/admin/configurations`       |
| `saveConfig(config)`   | POST   | `/api/admin/configurations`       |
| `deleteConfig(config)` | DELETE | `/api/admin/configurations`       |

**ValueType enum**: `None`, `Object`, `Boolean`, `Int`, `Double`, `DateOnly`, `DateTime`, `Text`

Parsing: Objects parsed from JSON, Booleans from string, Doubles/Ints with comma->dot replacement.

---

## 10. LMS Micro Service

### 10.1 API (`chunk-OIDSCTRK.js`)

Base URL token: `LMS_BASE_API_URL = "/api/micro-lms"`

| Method                  | HTTP | URL                       |
|-------------------------|------|---------------------------|
| `getStudents$(params?)` | GET  | `/api/micro-lms/students` |

---

## 11. Analytics

### 11.1 API (`chunk-OIDSCTRK.js`)

Base URL token: `API_ANALYTICS_API_URL = "/api/analytics"`

Used for UTM reporting. Source systems:

- `elementRegistrationForm`
- `hubUtmReporterService`
- `keycloakSignUpPage`
- `siteRegForm`

---

## 12. Cobrowsing (Screen Sharing)

From `chunk-KUQINTYY.js`: T-Bank (Tinkoff) cobrowsing SDK integration for support sessions.

- Initializes on specific URL patterns from config
- Session types: `SESSION_CONFIRMATION`, `SESSION_REFUSE`, `SESSION_CLOSE`, `START_RECORD`,
  `STOP_RECORD`, `CUSTOM_DATA`
- Auto-mask enabled
- Starts/stops based on URL matching and page visibility

---

## 13. Shared UI Infrastructure

### 13.1 Chunk HSCACK2H (98KB) -- Taiga UI Components

- `TuiBottomSheet`: mobile bottom sheet with scroll-snap
- `TuiDropdownMobile`: mobile dropdown replacement (full-screen overlay)
- `TuiElasticSticky`: scroll-aware sticky header with opacity fade
- `TuiSheetDialog`: sheet-style dialog with swipe-to-close
- `TuiKeyboardService`: virtual keyboard management for mobile inputs

### 13.2 Chunk OD35FLTN (82KB) -- Angular Router

- Full Angular Router implementation: URL parsing, route matching, navigation events, guards, lazy
  loading
- Route events: NavigationStart, NavigationEnd, NavigationCancel, NavigationError, RoutesRecognized,
  GuardsCheckStart/End, ResolveStart/End, Scroll

### 13.3 Chunk ZOG2R7BE (69KB) -- Animations + Utilities

- Angular animation engine (state machine, keyframes, transitions)
- Focus management utilities: `tsFocusedIn`, `tsNativeFocusable`, `tsMoveFocus`
- Safari/iOS detection
- Tree walker for focus navigation

### 13.4 Chunk AIXYYQ6K (59KB) -- Calendar/Date Components

- `TuiCalendarSheet`: day grid with range selection, markers, disabled days
- `TuiCalendarSpin`: month navigation with left/right buttons
- `TuiCalendarYear`: year picker with scroll-into-view
- `TuiOrderWeekDays`: week day header pipe (respects firstDayOfWeek)
- Range mode support: single day, date range, multi-date

### 13.5 Chunk SIQ3MUTR (56KB) -- Analytics/Telemetry

- Statist analytics client (T-Bank analytics SDK)
- Performance monitoring
- Event tracking with `send()`, `forceSend()`
- Client parameter updates (ssoId from user token)
- Page title tracking for analytics

### 13.6 Polyfills (57KB)

- DOMPurify 3.1.7 HTML sanitizer
- Angular DomSanitizer integration via `DompurifySanitizer`
- Standard polyfills for older browsers

---

## 14. Version Info

From `chunk-OIDSCTRK.js`:

```
hub@26.179.0
```
