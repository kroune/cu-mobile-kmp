# Root App (CU Hub) -- Detailed Reverse Engineering Analysis

Angular 21.1.4 application served from `/`. This is the main "Hub" application
for the Central University (CU) LMS, handling events, support, profile, grants,
admissions, onboarding, dashboard, and notifications.

---

## 1. Bootstrap & Entry Point

**File:** `main-NI5Y7GEG.js`

The app bootstraps by:

1. Fetching `./dynamical-config/configuration.json` at runtime (dynamic config)
2. Lazy-importing `AppComponent` from `chunk-TJTDBR6Z.js`
3. Lazy-importing `appConfig` from `chunk-OIDSCTRK.js`
4. Merging the fetched JSON config into the `appConfig.providers` via a
   `DYNAMIC_CONFIG` injection token (exported as `s` from `chunk-7E42AYBV.js`)
5. Calling Angular's `bootstrapApplication(AppComponent, mergedConfig)`

Both `en` and `ru` locales are registered in the entry point. Russian is primary.

---

## 2. Complete Route Table

**Source:** `chunk-OIDSCTRK.js` line 12 (appConfig) + `chunk-F4UDLC3X.js` (route enum)

### 2.1 Route Path Enum (R)

```
R = {
  Events:          "events",
  News:            "news",
  Dashboard:       "dashboard",
  Profile:         "profile",
  InformerCaseList: "case-list",
  Grants:          "grants",
  MasterGrants:    "master-grants",
  Grants2026:      "grants-2026",
  Onboarding:      "onboarding",
  Admission:       "admission",
  Error:           "error"
}
```

### 2.2 Full Route Tree

```
/ (root)
  canMatch: [Ea]  (authentication guard)
  title: "Центральный университет"
  children:

  ├── / (enrollee onboarding branch)
  │   canMatch: [ln(w.Enrollee), Rs]  -- enrollee role + onboarding-needed guard
  │   providers: [Ke.provide("hub-onboarding")]
  │   children:
  │     ├── /onboarding         -> chunk-65PL4UYO.js (OnboardingComponent)
  │     └── /**                 -> redirectTo: lc (saves current URL, redirects to /onboarding)
  │
  ├── /onboarding               -> redirectTo: /dashboard  (for non-enrollees)
  │
  ├── / (main layout: rc component)
  │   providers: [Zi, Va, Pa, {provide: qi, useClass: Mn}]
  │   children:
  │     ├── /dashboard          -> chunk-NNLUD57V.js  (dashboardRoutes)
  │     ├── /grants             -> chunk-4ACXTDD6.js  (hubGrantsRoutes)
  │     │                          canMatch: [Jt] (grants availability guard)
  │     │                          providers: [wa, Yt, nn, {provide: aa, ...}]
  │     ├── /master-grants      -> chunk-XBPQU6OA.js  (masterGrantsRoutes)
  │     │                          canMatch: [Vi] (master grants availability)
  │     │                          providers: [fo, _o, Yt, nn]
  │     ├── /admission          -> chunk-MYPYERCK.js  (admissionRoutes) [enrollment available]
  │     │                          canMatch: [Es([() => r(ct).getIsAdmissionAvailableOnce$()])]
  │     │                          providers: [Aa]
  │     ├── /admission          -> chunk-DU2L5VZK.js  (futureStudentApplicationFormRoutes)
  │     │                          canMatch: [ln([w.PreStudent, w.Student])]
  │     ├── /grants-2026        -> chunk-O5N3J4AB.js  (grant2026Routes)
  │     │                          canMatch: [ks]
  │     │                          providers: [Yt]
  │     ├── /news               -> chunk-J2FWVIQV.js  (newsRoutes)
  │     │                          title: "Уведомления"
  │     │                          canMatch: [zs]
  │     ├── /profile            -> chunk-XYNDJCEW.js  (profileRoutes)
  │     │                          title: "Профиль"
  │     ├── /events             -> chunk-ISH5QSO5.js  (eventsRoutes)
  │     │                          title: "Мероприятия"
  │     │                          canMatch: [Qr]
  │     ├── /case-list (Ks)     -- support informer cases (injected from Ks array)
  │     ├── / (empty, full)     -> redirectTo: /dashboard
  │     ├── /not-found (cn)     -> Yi (NotFound component)
  │     ├── /maintenance (dr)   -> sc component
  │     ├── /unsupported (ur)   -> ac component (unsupported browser)
  │     ├── /error              -> chunk-ZN6X5W4H.js  (ErrorScreenComponent)
  │     └── /**                 -> Yi (NotFound component)
  │
  └── /not-found (cn)           -> Yi (NotFound component)

/** (fallback)                  -> ba (unauthenticated landing)
```

### 2.3 Events Sub-Routes

**Source:** `chunk-ISH5QSO5.js`

```
/events
  ├── /                              -> chunk-3AKKBUTX.js (HubEventsFeatureEventsListComponent)
  ├── /:slug                         -> chunk-N2T4BRFD.js (HubEventsFeatureEventViewComponent)
  │                                     data: { ... breadcrumb config }
  │                                     providers: [{provide: s, useExisting: r}, ...]
  └── /**                            -> redirectTo: ""
```

### 2.4 Event Path Constants

```
EventPaths = {
  Root: "",
  EventBySlug: ""   // slug param appended as `:slug`
}
```

---

## 3. API Endpoints

### 3.1 Authentication & Session

| Method | Endpoint                   | Description                                       |
|--------|----------------------------|---------------------------------------------------|
| GET    | `/api/account/me`          | Get current user token/session (UserToken)        |
| GET    | `/auth`                    | Redirect to Keycloak sign-in (with OAuth2 params) |
| GET    | `/account/signin/callback` | OAuth2 callback after sign-in                     |
| GET    | `/registrations`           | Sign-up page                                      |
| GET    | `/account/signout`         | Logout                                            |

OAuth2 flow parameters:

```
response_type: "code"
client_id: "api-gateway"
scope: "openid email offline_access"
redirect_uri: "<origin>/<baseHref>/account/signin/callback?app=<appName>"
```

The `authUrl` is loaded from `dynamical-config/configuration.json` at runtime.

### 3.2 Events

| Method | Endpoint                                                | Description                    |
|--------|---------------------------------------------------------|--------------------------------|
| POST   | `/api/event-builder/public/events/list`                 | List events (with filter body) |
| GET    | `/api/event-builder/public/events/slug/:slug`           | Get event by slug              |
| GET    | `/api/event-builder/public/events/:id/appointment/file` | Download calendar file (blob)  |
| POST   | `/api/event-builder/public/events/apply/:id`            | Apply/register for event       |

Event registration statuses:

```
RegistrationStatus = {
  None,
  ReadyToRegister,
  TicketsLimitHasBeenReached,
  TicketsLimitHasBeenReachedTryOnline,
  RegistrationEndDateHasBeenReached
}
```

Event types: `"apply"`, `"broadcast"`, `"record"`, `"force-applying"`

### 3.3 News

| Method | Endpoint                                  | Description              |
|--------|-------------------------------------------|--------------------------|
| GET    | `/api/event-builder/public/news/:id`      | View single news item    |
| GET    | `/api/event-builder/public/news/file/:id` | Get news file/attachment |

### 3.4 Students

| Method | Endpoint               | Description              |
|--------|------------------------|--------------------------|
| GET    | `/api/hub/students/me` | Get current student info |

Student education levels:

```
EducationLevel = { None, Bachelor, Master, Dpo, DpoMaster }
```

### 3.5 Enrollees

| Method | Endpoint                           | Description                  |
|--------|------------------------------------|------------------------------|
| GET    | `/api/hub/enrollees/me`            | Get current enrollee profile |
| PUT    | `/api/hub/enrollees/me`            | Update enrollee profile      |
| POST   | `/api/hub/enrollees/onboarding/me` | Submit onboarding data       |

Enrollee study degree types:

```
StudyDegreeType = { Bachelor, Master, None }
```

### 3.6 Grants

| Method | Endpoint                                | Description                 |
|--------|-----------------------------------------|-----------------------------|
| GET    | `/api/hub/grants/me`                    | Get grants for current user |
| GET    | `/api/hub/grants/me/:year/:type/active` | Get active grant            |

Grant types: `"InformationTechnology"`, `"Design"`

Grant status:

```
GrantStatus = { None, Denied, NotConfirmed, Confirmed, ConfirmedIfTop }
```

### 3.7 Admissions

| Method | Endpoint                                   | Description                  |
|--------|--------------------------------------------|------------------------------|
| GET    | `/api/event-builder/admissions/2025/state` | Get admission state for 2025 |

### 3.8 Notifications (In-App)

| Method | Endpoint                                           | Description                        |
|--------|----------------------------------------------------|------------------------------------|
| POST   | `/api/notification-hub/notifications/in-app`       | List in-app notifications          |
| GET    | `/api/notification-hub/notifications/in-app/stats` | Notification stats (unread counts) |
| POST   | `/api/notification-hub/notifications/in-app/read`  | Mark notifications as read         |

Notification categories:

```
NotificationCategory = { Education: "Education", Others: "Others" }
NotificationCategoryId = { Education: 1, Other: 2 }
```

### 3.9 User Profile/Locale

| Method | Endpoint                 | Description                 |
|--------|--------------------------|-----------------------------|
| PUT    | `/api/account/me/locale` | Change user locale/language |

### 3.10 Admin Configurations

| Method | Endpoint                         | Description                    |
|--------|----------------------------------|--------------------------------|
| GET    | `/api/admin/configurations`      | Get all configurations         |
| GET    | `/api/admin/configurations/:key` | Get single configuration value |
| POST   | `/api/admin/configurations`      | Save configuration(s)          |
| DELETE | `/api/admin/configurations`      | Delete configuration(s)        |

Configuration value types:

```
ConfigValueType = { None, Object, Boolean, Int, Double, DateOnly, DateTime, Text }
```

---

## 4. Data Models

### 4.1 User Token (Session)

Retrieved from `GET /api/account/me`:

```typescript
interface UserToken {
  sub: string;           // userId
  email: string;
  phone_number: string;
  roles: string[];
  locale: string;        // "ru" | "en"
  groups: string[];
  resource_access: {
    "realm-management"?: {
      roles: string[];
    };
  };
}
```

### 4.2 User Roles

```typescript
enum UserRole {
  Teacher = "default_teacher",
  Student = "default_student",
  Enrollee = "default_enrollee",
  Assistant = "lms_assistant",
  PreStudent = "default_pre_student",
  Staff = "default_staff",
  LmsViewApp = "lms_view_app",
  AuthViewTime = "auth_view_time",
  AIAssistantViewApp = "ai_assistant-view-app",
  AdminViewApp = "admin_view_app",
  AdminViewJob = "admin_view_job",
  AdminEditUser = "admin_edit_user",
  ViewDevFeature = "default_view_dev_feature",
  InterviewSchedulerViewInterviewer = "interview_scheduler_view_interviewer",
  InterviewSchedulerEditInterviewer = "interview_scheduler_edit_interviewer",
  InterviewSchedulerViewSettings = "interview_scheduler_view_settings",
  InterviewSchedulerEditSettings = "interview_scheduler_edit_settings",
  InterviewSchedulerViewTimeslot = "interview_scheduler_view_timeslot",
  InterviewSchedulerEditTimeslot = "interview_scheduler_edit_timeslot",
  InterviewSchedulerViewBooking = "interview_scheduler_view_booking",
  InterviewSchedulerEditBooking = "interview_scheduler_edit_booking",
  InterviewSchedulerViewCandidate = "interview_scheduler_view_candidate",
  EnrollmentFormView = "university_hub_view_enrollment_form",
  EnrollmentFormEdit = "university_hub_edit_enrollment_form",
  HubMmisMigration = "university_hub_mmis_migration",
  UseStudentHub = "use_student_hub"
}
```

### 4.3 Enrollee Profile

```typescript
interface EnrolleeProfile {
  firstName: string | null;
  lastName: string | null;
  middleName: string | null;
  email: string | null;
  phone: string | null;           // stored without "+", displayed with "+"
  birthdate: string | null;       // "YYYY-MM-DD"
  graduationYear: number | null;
  studyDegreeType: "Bachelor" | "Master" | "None";
  lastStudyDegreeType: string;    // e.g. "School", "University"
  city: string | null;
  citizenship: string | null;
  educationPlaceName: string | null;
  foreignEducationPlace: boolean;
  snils: string | null;           // formatted as "XXX-XXX-XXX-XX"
  telegram: string | null;
  livesAbroad: boolean;
  // computed:
  fullName: string;               // derived from first+last+middle name
}
```

### 4.4 Student

```typescript
interface Student {
  educationLevel: "None" | "Bachelor" | "Master" | "Dpo" | "DpoMaster";
  // other fields from /api/hub/students/me
}
```

### 4.5 Event Model

```typescript
interface Event {
  slug: string;
  id: string;
  // items list returned from POST /list
}

interface EventListRequest {
  // filter body for POST /api/event-builder/public/events/list
}

interface EventCalendarFile {
  fileName: string;     // extracted from Content-Disposition header
  content: Blob;
}
```

---

## 5. Auth/Session Handling

### 5.1 Session Service (`chunk-ROFNVQNW.js`)

- **Token endpoint:** `GET /api/account/me` with anonymous-request context
- **Observable streams:**
    - `session$` -- full user token or `null` if 401
    - `userToken$` -- filtered non-null session
    - `userId$` -- `userToken$.sub` (distinct)
    - `roles$` -- `userToken$.roles` (shareReplay)
    - `realmManagementRoles$` -- from `resource_access["realm-management"]`
    - `groups$` -- `userToken$.groups`
- On 401 response, returns `Observable.of(null)` (not authenticated)
- Session is cached with `shareReplay({bufferSize: 1, refCount: false})`

### 5.2 Auth Navigation (`chunk-BSYKMDXA.js`)

- Uses `AUTH_URL_TOKEN` loaded from `dynamical-config/configuration.json`
- `navigateToSignIn$()` -- builds OAuth2 URL with `response_type=code`, redirects browser
- `navigateToSignUp$()` -- navigates to `/registrations`
- `logout()` -- navigates to `/account/signout`
- `navigateToSignInCallback()` -- constructs callback URL with `?app=<appName>`
- `isAuthenticated$` -- derived from `session$`, defaults to `false`
- `beforeNavigateToSignIn` -- Subject that emits before sign-in redirect (used to save current
  route)

### 5.3 XSRF Protection

The HttpClient includes XSRF protection:

- Cookie name: `XSRF-TOKEN` (default)
- Header name: `X-XSRF-TOKEN` (default)
- Implemented in `chunk-7HYSEYNL.js` (`WeFunction` interceptor)
- Skipped for GET/HEAD requests and cross-origin requests

### 5.4 Cookie Utilities (`chunk-NOMFNW4V.js`)

```typescript
function deleteCookie(name, document, domain?, path?)
function getCookie(name, document): string | null
function getQueryParam(name, window): string | null
```

---

## 6. Notification System

### 6.1 In-App Notifications (`chunk-KUQINTYY.js`)

**NotificationService** (API wrapper):

- `list$(body)` -- `POST /api/notification-hub/notifications/in-app`
- `stats$()` -- `GET /api/notification-hub/notifications/in-app/stats`
- `read$(body)` -- `POST /api/notification-hub/notifications/in-app/read`

**NotificationFacade** (business logic):

- Polls stats every 5 minutes (`1000 * 60 * 5 = 300000ms`)
- `checkNotificationStats$` -- switchMap to interval polling on subscribe
- `hasUnreadAnyCategory$` -- boolean derived from stats
- `notificationCategoryStatus$` -- maps categories to `{education: boolean, others: boolean}`
- `retryNotificationStats()` -- forces re-fetch
- `switchNotificationType$` -- Subject for category switching

**NotificationSidebarService:**

- `isOpen$` -- BehaviorSubject for sidebar open/close state
- `open()` / `close()` methods

### 6.2 Push Notifications (Service Worker)

From `chunk-KUQINTYY.js` (SwPush, SwUpdate):

- Uses Angular Service Worker (`ngsw-worker.js`)
- Registration strategy: `registerWhenStable:30000`
- SwPush: `messages`, `notificationClicks`, `notificationCloses`, `pushSubscriptionChanges`
- SwUpdate: `versionUpdates`, `unrecoverable`
- Push subscription uses VAPID (applicationServerKey from base64)

---

## 7. Analytics & Performance

### 7.1 Tinkoff Statist Integration

**Source:** `chunk-OIDSCTRK.js` (appConfig)

```typescript
// Configuration in appConfig:
providers: [
  Gr.forRoot({
    sendPerformanceMetrics: true,
    client: "cu-hub",
    clientVersion: ji.version
  }),
  { provide: Vr /* STATIST_ENDPOINT_URL */, useFactory: () => r(K).statistEndpointUrl },
  { provide: Hr /* STATIST_USER_ID */,      useFactory: () => r(H).userId$ }
]
```

- `statistEndpointUrl` -- loaded from dynamic config (`configuration.json`)
- User ID comes from session token's `sub` field

### 7.2 Client Performance Monitoring (`chunk-WNGRPCQW.js`)

Uses `@perfume.js` + custom Tinkoff metrics library:

**Tracked metrics:**

- CLS (Cumulative Layout Shift)
- FID (First Input Delay)
- LCP (Largest Contentful Paint)
- FCP (First Contentful Paint)
- TTFB (Time to First Byte)
- TBT (Total Blocking Time)
- INP (Interaction to Next Paint)
- Navigation Timing (DNS, connect, redirect, download times)
- Network Information (RTT, downlink, effectiveType, saveData)
- Resource Timing (JS/CSS/img/font transfer sizes)
- Storage Estimate (quota, usage, caches, indexedDB)

**Statist event format:**

```
topic: "coretech.web.metrics"
category: "performance.rum"
data: {
  deviceType: "mobile" | "desktop" | "webview",
  urlMask: string,
  navigationType: string,
  ...metricValues
}
```

Metrics are debounced (500ms) and sent in batches, except CLS and INP which
are sent immediately.

### 7.3 UTM Tracking (`chunk-FGZGDGP3.js`)

- Reads/writes UTM parameters from cookies and URL query params
- UTM fields: `utm_source`, `utm_medium`, `utm_campaign`, `utm_term`, `utm_content`
- Metadata: `utmDataMarkers`, `utmDataTimestamp`, `utmDataSource`, `referrer`
- Traffic sources: `paid` (priority 2), `organic` (priority 1), `direct` (priority 0)
- Cookie expiry: 60 days (`5184e6` ms)
- Also tracks `siteToLkNavTriggerPage` for Tilda-to-LK navigation

### 7.4 Cobrowsing (`chunk-KUQINTYY.js`)

- Uses `@tinkoff/cobrowsing` v5.0.0
- Configured with injection tokens: `COBROWSING_APP_ID`, `COBROWSING_UNIT_ID`,
  `COBROWSING_ENVIRONMENT`, `COBROWSING_USER_ID`
- Loads cobrowsing script dynamically from CDN based on env:
    - Prod: `https://cobrowsing.tbank.ru/cdn`
    - QA: `https://cobrowsing-qa.tcsbank.ru/static/customer`
- Supports auto-masking, light recording
- URL-based activation: checks route against configured URLs

---

## 8. Error Handling

### 8.1 Micro-Sentry (`chunk-OIDSCTRK.js`)

**Configuration:**

```typescript
ka({
  uniqueRandomNumber: 2,
  releaseVersion: ji.version
})
```

This calls a factory that produces:

```typescript
{
  sageGroup: "university_frontend_errorhub",
  sentryEnabledEnvironments: [Develop, Preprod, Prod],
  environment: Prod,  // overridden from dynamic config via Mt token
  dsn: "https://university_frontend_errorhub@error-hub.tinkoff.ru/2"
}
```

**Sentry SDK:** `micro-sentry.javascript.angular` (lightweight Sentry clone)

- Plugins: `[Oi]` (BrowserPlugin -- tracks DOM clicks/keypresses, console, fetch, XHR, history)
- Error throttling: 1000ms (`nl=1e3`)
- Groups duplicate errors by `toString()` representation
- `beforeSend` and `beforeBreadcrumb` hooks run inside Angular zone
- Sets `user.id` from `userId$` on session

### 8.2 HttpErrorResponse Wrapper

Custom `HttpErrorResponse` subclass (`Mi`) that:

- Strips hostname from error messages (privacy)
- Pattern: `"Http failure response for https://host/path"` -> `"Http failure response for /path"`
- Preserves stack trace with custom formatting

### 8.3 HTTP Error Interceptor

```typescript
const Ra = (request, next) =>
  next(request).pipe(
    catchError(error =>
      error instanceof HttpErrorResponse
        ? throwError(() => new Mi(error))  // custom wrapper
        : throwError(() => error)
    )
  );
```

### 8.4 InconsistentUserData Error Handling

`Fa` class (Enrollee initializer):

- On enrollee load error with `title === "InconsistentUserData"`:
    - If `emailDuplicated && phoneDuplicated`: shows duplicate account message with both
    - If `phoneDuplicated`: shows phone duplicate message
    - Otherwise: shows email duplicate message
- All redirect to `/error` with `skipLocationChange: true`

---

## 9. Dynamic Configuration

**Fetched from:** `./dynamical-config/configuration.json`

**Known config keys** (from injection token factories in appConfig):

```typescript
interface DynamicConfig {
  authUrl: string;              // Keycloak auth base URL
  lmsAppUrl: string;            // LMS app URL (for navigation)
  lmsPythonCourseUrl: string;   // Python course URL
  statistEndpointUrl: string;   // Tinkoff Statist API endpoint
  informerScriptUrl: string;    // Support informer script URL
  mainCampusBuildingOnMapLink: string;
  teletypeId: string;           // Teletype integration
  newsChannelUrl: string;       // News channel URL
  tiMeUrl: string;              // TiMe URL
  env: string;                  // "develop" | "preprod" | "prod" | "test"
  eduGrantOnlineContestId: string;
}
```

---

## 10. Internationalization

- Default language: Russian (`ru`)
- Supported: `["ru", "en"]`
- Language switching:
    - API call: `PUT /api/account/me/locale` with `{locale: string}`
    - Browser redirect to localized URL path
- Date formatting:
    - RU: `dd.MM.y`, `HH:mm`
    - EN: `M/d/yy`, `h:mm a`
- Country codes for forms:
  `["RU","BY","KZ","UA","AZ","AM","KG","MD","TJ","TM","UZ","DE","FR","GB","IT","ES","CN","IN","US","CA","TR","BR","KR","GE"]`

---

## 11. Environment Constants

```typescript
Environment = {
  Develop: "develop",
  Preprod: "preprod",
  Prod: "prod",
  Test: "test"
}
```

Environment is provided via `Mt` token from dynamic config's `env` field.
