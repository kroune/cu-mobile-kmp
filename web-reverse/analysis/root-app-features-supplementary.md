# CU LMS Root App -- Supplementary Analysis

Supplementary details: chunk-to-feature mapping, data flow patterns, and implementation notes for
the KMP mobile app.

---

## 1. Complete API Endpoint Registry

### Hub Service (`/api/hub/...`)

```
GET  /api/hub/enrollees/me               -- current enrollee profile
PUT  /api/hub/enrollees/me               -- update enrollee profile
POST /api/hub/enrollees/onboarding/me    -- complete onboarding
GET  /api/hub/students/me                -- current student profile
GET  /api/hub/grants/me                  -- all grants for current user
GET  /api/hub/grants/me/{year}/{type}/active  -- active grant (type: InformationTechnology|Design)
GET  /api/hub/avatars/me                 -- user avatar image
GET  /api/hub/orders/...                 -- (referenced in interceptor, lazy-loaded)
```

### Event Builder (`/api/event-builder/...`)

```
POST /api/event-builder/public/events/list       -- list events with filters
GET  /api/event-builder/public/events/slug/{slug} -- get event by slug
GET  /api/event-builder/public/events/{id}/appointment/file -- download .ics calendar
POST /api/event-builder/public/events/apply/{id}  -- register for event
GET  /api/event-builder/public/news/{id}           -- view news item
GET  /api/event-builder/public/news/file/{id}      -- get news file
GET  /api/event-builder/admissions/2025/state      -- admission state
```

### Notification Hub (`/api/notification-hub/...`)

```
POST /api/notification-hub/notifications/in-app        -- list in-app notifications
GET  /api/notification-hub/notifications/in-app/stats  -- notification stats (unread counts)
POST /api/notification-hub/notifications/in-app/read   -- mark notifications as read
```

### Account (`/api/account/...`)

```
PUT  /api/account/me/locale  -- change user locale
```

### Admin / Config (`/api/admin/...`)

```
GET    /api/admin/configurations         -- list all configs
GET    /api/admin/configurations/{key}   -- get config by key
POST   /api/admin/configurations         -- save config(s)
DELETE /api/admin/configurations         -- delete config(s)
```

### Micro LMS (`/api/micro-lms/...`)

```
GET  /api/micro-lms/students  -- list students (with optional pagination params)
```

### Analytics (`/api/analytics/...`)

```
(report endpoint -- exact URL in lazy-loaded chunk)
```

---

## 2. Chunk-to-Feature Mapping

| Chunk                   | Size   | Purpose                                                                                  |
|-------------------------|--------|------------------------------------------------------------------------------------------|
| `chunk-HSCACK2H.js`     | 98KB   | Taiga UI mobile components (bottom sheet, dropdown mobile, sheet dialog, elastic sticky) |
| `chunk-OD35FLTN.js`     | 82KB   | Angular Router (URL parsing, navigation, guards, lazy loading)                           |
| `chunk-ZOG2R7BE.js`     | 69KB   | Angular animations engine + focus management utilities + Safari/iOS detection            |
| `chunk-AIXYYQ6K.js`     | 59KB   | Calendar/date components (calendar sheet, year picker, spin navigation)                  |
| `chunk-SIQ3MUTR.js`     | 56KB   | T-Bank Statist analytics SDK + performance monitoring + page tracking                    |
| `polyfills-LXTXMF5S.js` | 57KB   | DOMPurify 3.1.7 sanitizer + Angular sanitization integration                             |
| `chunk-OIDSCTRK.js`     | ~100KB | App config, routing, interceptors, header/sidebar, locale, shell                         |
| `chunk-44YKIWJR.js`     | small  | Events API service + facade with retry/polling logic                                     |
| `chunk-KUQINTYY.js`     | small  | Notification API + service worker push + cobrowsing integration                          |
| `chunk-H2UV6AAT.js`     | small  | Grants API service                                                                       |
| `chunk-QHDVAQXE.js`     | small  | Student service (education level)                                                        |
| `chunk-2RNYUVXP.js`     | small  | Enrollee service (profile management)                                                    |
| `chunk-VMZTUSWJ.js`     | small  | Configuration service (admin configs)                                                    |
| `chunk-N3DZQN6I.js`     | small  | Roles/access control system                                                              |
| `chunk-7HYSEYNL.js`     | ~10KB  | Angular HttpClient with XSRF protection                                                  |
| `chunk-F4UDLC3X.js`     | tiny   | Route path constants                                                                     |
| `chunk-EFZ55JZJ.js`     | tiny   | News API service                                                                         |
| `chunk-2NELPLXO.js`     | tiny   | Admissions state API                                                                     |
| `chunk-ISH5QSO5.js`     | tiny   | Events route definitions                                                                 |
| `chunk-3AKKBUTX.js`     | medium | Events list page component                                                               |
| `chunk-N2T4BRFD.js`     | medium | Event detail/view page component                                                         |
| `chunk-U6ZV2IWF.js`     | small  | DOMPurify sanitizer service                                                              |
| `main-NI5Y7GEG.js`      | small  | Bootstrap: fetches config, registers RU/EN locales, bootstraps AppComponent              |

---

## 3. Data Flow Patterns

### 3.1 Event List Loading Flow

```
User opens /events
  -> EventsListComponent.ngOnInit()
  -> combineLatest([showOnlyMineControl, offset$, activeTab$])
  -> EventsFacadeService.list$(requestBody, pollingConfig?)
    -> EventsApiService.list$(body)    // POST /api/event-builder/public/events/list
  -> Cache.set(response)
  -> Component reads from Cache.items().slice(0, showCount)
```

Request body for active events:

```json
{
    "paging": {
        "limit": 24,
        "offset": 0,
        "sorting": [
            {
                "by": "startDate",
                "isAsc": true
            }
        ]
    },
    "filter": {
        "usePersonalSuggestionFilter": true,
        "showOnlyMine": false,
        "endDateGreaterThanOrEqualTo": "2026-03-22T..."
    }
}
```

### 3.2 Event Registration Flow

```
User clicks "Register" on event view page
  -> EventViewFacade.apply({id})
  -> EventsApiService.apply$({id})   // POST /api/event-builder/public/events/apply/{id}
    -> body: {}
  -> On error "ticket_already_exists" -> treat as success (resolve with event id)
  -> On success -> isApplied$ emits true
  -> UI updates to show "Registered" state
```

### 3.3 Notification Polling Flow

```
App start
  -> NotificationService constructor
  -> retryNotificationStats() -> BehaviorSubject emits
  -> switchMap to timer(0, 300000)  // every 5 minutes
  -> forkJoin([config, stats$])
  -> filter categories by config
  -> hasUnreadAnyCategory$ / notificationCategoryStatus$ updated
```

### 3.4 Authentication Flow

The app uses Keycloak SSO (not visible in root bundle, but referenced). Authentication state is held
in:

- `userToken$`: observable of decoded JWT claims (name, email, phone_number, id, locale)
- `USER_ROLES_TOKEN`: injectable token with array of role strings

Route guards use `HasAccessService.hasAccess$()` to check roles before navigation.

---

## 4. Key Data Models (for KMP Implementation)

### 4.1 Event (inferred from components)

```kotlin
data class Event(
    val id: String,
    val slug: String,
    val title: String,
    val description: String?,
    val startDate: Instant,
    val endDate: Instant?,
    val location: EventLocation?,
    val format: String?,           // e.g., "online", "offline"
    val tags: List<String>?,
    val previewImageUrl: String?,
    val broadcastUrl: String?,
    val lmsCourseUri: String?,
    val registrationUrl: String?,  // external registration link
    val isCurrentUserApplied: Boolean,
    val eventTicketRegistration: EventTicketRegistration?
)

data class EventLocation(
    val city: String?
)

data class EventTicketRegistration(
    val status: EventTicketRegistrationStatus,
    val isPossibleToRegister: Boolean
)

enum class EventTicketRegistrationStatus {
    None, ReadyToRegister, TicketsLimitHasBeenReached,
    TicketsLimitHasBeenReachedTryOnline, RegistrationEndDateHasBeenReached
}
```

### 4.2 Events List Request/Response

```kotlin
data class EventsListRequest(
    val paging: Paging,
    val filter: EventsFilter
)

data class Paging(
    val limit: Int,
    val offset: Int,
    val sorting: List<SortField>
)

data class SortField(
    val by: String,    // "startDate"
    val isAsc: Boolean
)

data class EventsFilter(
    val usePersonalSuggestionFilter: Boolean,
    val showOnlyMine: Boolean,
    val endDateGreaterThanOrEqualTo: String?,  // ISO datetime
    val endDateLessThanOrEqualTo: String?      // ISO datetime
)

data class EventsListResponse(
    val items: List<Event>,
    val pagination: PaginationInfo?
)
```

### 4.3 Enrollee Profile

```kotlin
data class EnrolleeProfile(
    val firstName: String?,
    val lastName: String?,
    val middleName: String?,
    val phone: String?,        // without "+"
    val birthdate: String?,    // "YYYY-MM-DD"
    val email: String?,
    val graduationYear: Int?,
    val studyDegreeType: StudyDegreeType,
    val lastStudyDegreeType: LastStudyDegreeType,
    val city: String?,
    val citizenship: String?,
    val educationPlaceName: String?,
    val foreignEducationPlace: Boolean,
    val snils: String?,
    val telegram: String?,
    val livesAbroad: Boolean
)

enum class StudyDegreeType { Bachelor, Master, None }
enum class LastStudyDegreeType { School, University, None }
```

### 4.4 Notification Stats

```kotlin
data class NotificationStats(
    val categories: List<NotificationCategory>
)

data class NotificationCategory(
    val category: String,   // "Education" | "Others"
    val hasUnread: Boolean
)
```

### 4.5 Grant

```kotlin
data class Grant(
    val status: GrantStatus
    // ... additional fields in lazy-loaded chunks
)

enum class GrantStatus {
    None, Denied, NotConfirmed, Confirmed, ConfirmedIfTop
}
```

### 4.6 Student

```kotlin
data class Student(
    val educationLevel: EducationLevel
    // ... additional fields
)

enum class EducationLevel {
    None, Bachelor, Master, Dpo, DpoMaster
}
```

---

## 5. Implementation Notes for KMP

### 5.1 Auth / XSRF

- The web app uses cookie-based XSRF (`XSRF-TOKEN` cookie -> `X-XSRF-TOKEN` header)
- The mobile app should use Bearer token auth via Keycloak OAuth2 flow
- User roles are embedded in the JWT token

### 5.2 Events Feature Priority

The events system is self-contained and maps cleanly to mobile:

1. List events (active/archived, mine/all filter)
2. View event detail by slug
3. Register for event (POST apply)
4. Download calendar .ics

### 5.3 Notification Polling

- Web polls every 5 minutes
- Mobile should use push notifications (FCM/APNs) instead of polling
- The `/api/notification-hub/notifications/in-app/stats` endpoint is useful for badge counts

### 5.4 Profile

- Two user types: Enrollee (pre-student) and Student
- Enrollee profile is editable (PUT), Student is read-only
- Avatar is at fixed URL `/api/hub/avatars/me`

### 5.5 Lazy-loaded Chunks Not in Root Bundle

These features are in separate lazy-loaded bundles (need separate analysis):

- Dashboard content (`chunk-NNLUD57V.js`)
- Profile detail page (`chunk-XYNDJCEW.js`)
- Grants detail pages (`chunk-4ACXTDD6.js`, `chunk-XBPQU6OA.js`, `chunk-O5N3J4AB.js`)
- Case list / support (`chunk-V2W4HQVK.js`)
- Admission forms (`chunk-MYPYERCK.js`, `chunk-DU2L5VZK.js`)
- News page (`chunk-J2FWVIQV.js`)
- Onboarding (`chunk-65PL4UYO.js`)

### 5.6 Environment

- The app fetches `./dynamical-config/configuration.json` at startup
- Version: `hub@26.179.0`
- Environments: LOCAL, PROD, QA (used for cobrowsing SDK)
- Sentry error tracking is configured via DSN in config

### 5.7 Circuit Breaker

The web app has a client-side circuit breaker that blocks requests to certain URL patterns from a
config source. This is a server-side maintenance mechanism. The mobile app should implement similar
logic or rely on server-side 503 responses.

---

## 6. Cross-reference: Root Chunks Analysis Summary

| Chunk (requested)              | Contains API calls? | Key content                                                       |
|--------------------------------|---------------------|-------------------------------------------------------------------|
| `chunk-HSCACK2H.js` (98KB)     | No                  | Taiga UI mobile components (bottom sheet, dropdown, sheet dialog) |
| `chunk-OD35FLTN.js` (82KB)     | No                  | Angular Router full implementation                                |
| `chunk-ZOG2R7BE.js` (69KB)     | No                  | Animation engine + focus/keyboard utilities                       |
| `chunk-AIXYYQ6K.js` (59KB)     | No                  | Calendar/date picker Taiga UI components                          |
| `chunk-SIQ3MUTR.js` (56KB)     | No                  | Analytics/telemetry SDK (Statist)                                 |
| `polyfills-LXTXMF5S.js` (57KB) | No                  | DOMPurify HTML sanitizer                                          |

All 6 requested chunks are **infrastructure/UI library code** with no direct API endpoints. The
business logic and API calls are in the smaller, feature-specific chunks documented above.
