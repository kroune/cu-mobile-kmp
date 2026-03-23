# Root App Completeness Audit

Audit of reverse-engineering coverage for the CU LMS Root App (`https://my.centraluniversity.ru/`).

Date: 2026-03-22

---

## 1. Route Visit Status

Cross-referencing the full route table (from `chunk-OIDSCTRK.js` and `chunk-F4UDLC3X.js`) against
the "Pages Visited" list in `README.md`.

| Route                                | Visited? | Evidence                                                                                                                                                                                 |
|--------------------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/dashboard`                         | YES      | README lists it (redirects to `/learn/courses/view/actual`)                                                                                                                              |
| `/events`                            | YES      | README: "Events list", including past tab with pagination                                                                                                                                |
| `/events/:slug`                      | YES      | README: "Event detail"                                                                                                                                                                   |
| `/profile`                           | YES      | README: `/profile/info`                                                                                                                                                                  |
| `/profile/experience`                | YES      | README: "Achievements" at `/profile/experience`                                                                                                                                          |
| `/profile/experience/edit-bio`       | YES      | README: "Edit bio" at `/profile/experience/edit-bio`                                                                                                                                     |
| `/profile/experience/add-experience` | YES      | README: "Add experience" with 12 achievement types                                                                                                                                       |
| `/case-list`                         | YES      | README: "Support (active cases)"                                                                                                                                                         |
| `/case-list/archived`                | YES      | README: "Support (archived cases)"                                                                                                                                                       |
| `/case-list/case/:id`                | YES      | README: "Support case detail"                                                                                                                                                            |
| `/grants`                            | NO       | Never visited. Route guarded by `Jt` (bachelor grants availability guard). User likely does not have access or grants are not available for this account.                                |
| `/master-grants`                     | NO       | Never visited. Route guarded by `Vi` (master grants availability guard). Same reason as grants.                                                                                          |
| `/admission`                         | NO       | Never visited. Two variants exist: one guarded by `Es(getIsAdmissionAvailableOnce$())`, another guarded by `ln([PreStudent, Student])`. Neither accessible to this user/session.         |
| `/grants-2026`                       | NO       | Never visited. Route guarded by `ks` (grants-2026 visibility guard). Not accessible to this user/session.                                                                                |
| `/news` and `/news/:id`              | NO       | Never visited. Route guarded by `zs` (enrollee or student guard). The route chunk (`chunk-J2FWVIQV.js`) was not downloaded. Likely restricted by role or visibility config.              |
| `/onboarding`                        | NO       | Never visited. Route guarded by `ln(Enrollee)` + `Rs` (onboarding-needed guard). User is not an enrollee needing onboarding. For non-enrollees, `/onboarding` redirects to `/dashboard`. |
| `/not-found`                         | NO       | Never visited. This is a 404 fallback page. Not triggered during normal browsing. Partial analysis exists from source code (shows "Ничего не найдено!" with role-based CTA).             |
| `/maintenance`                       | NO       | Never visited. Only shown when the service is in maintenance mode (503).                                                                                                                 |
| `/unsupported`                       | NO       | Never visited. Only shown for unsupported browsers.                                                                                                                                      |
| `/error`                             | NO       | Never visited. Only triggered by `InconsistentUserData` errors or similar failures.                                                                                                      |

### Summary

- **Visited: 11 routes** (dashboard, events, events/:slug, profile, profile/experience,
  profile/experience/edit-bio, profile/experience/add-experience, case-list, case-list/archived,
  case-list/case/:id, notifications sidebar)
- **Not visited: 9 routes** (grants, master-grants, admission, grants-2026, news, onboarding,
  not-found, maintenance, unsupported, error)

---

## 2. Lazy-Loaded Chunk Download Status

Cross-referencing the 14 lazy-loaded feature chunks referenced in the route table against files
present in `docs/web-reverse/raw/root/`.

| Chunk               | Route                                              | Downloaded? |
|---------------------|----------------------------------------------------|-------------|
| `chunk-NNLUD57V.js` | `/dashboard` (dashboardRoutes)                     | **MISSING** |
| `chunk-4ACXTDD6.js` | `/grants` (hubGrantsRoutes)                        | **MISSING** |
| `chunk-XBPQU6OA.js` | `/master-grants` (masterGrantsRoutes)              | **MISSING** |
| `chunk-MYPYERCK.js` | `/admission` (admissionRoutes -- available)        | **MISSING** |
| `chunk-DU2L5VZK.js` | `/admission` (futureStudentFormRoutes)             | **MISSING** |
| `chunk-O5N3J4AB.js` | `/grants-2026` (grant2026Routes)                   | **MISSING** |
| `chunk-J2FWVIQV.js` | `/news` (newsRoutes)                               | **MISSING** |
| `chunk-XYNDJCEW.js` | `/profile` (profileRoutes)                         | **MISSING** |
| `chunk-ISH5QSO5.js` | `/events` (eventsRoutes)                           | PRESENT     |
| `chunk-V2W4HQVK.js` | `/case-list` (HubInformerFeatureCaseListComponent) | PRESENT     |
| `chunk-65PL4UYO.js` | `/onboarding` (OnboardingComponent)                | **MISSING** |
| `chunk-ZN6X5W4H.js` | `/error` (ErrorScreenComponent)                    | **MISSING** |
| `chunk-3AKKBUTX.js` | Events list sub-route                              | PRESENT     |
| `chunk-N2T4BRFD.js` | Event detail sub-route                             | PRESENT     |

### Summary

- **Downloaded: 4 out of 14** feature chunks (ISH5QSO5, V2W4HQVK, 3AKKBUTX, N2T4BRFD)
- **Missing: 10 out of 14** feature chunks

**Note on missing chunks**: The download methodology used `curl` to fetch static JS files from the
CDN. Lazy-loaded chunks are only fetched by the browser when their route is navigated to. Since the
user only visited events, profile, case-list, and dashboard pages, only those chunks were loaded.
However:

- The **dashboard chunk** (`chunk-NNLUD57V.js`) is missing despite `/dashboard` being visited. This
  is because `/dashboard` immediately redirects to `/learn/courses/view/actual` -- the dashboard
  page may load its chunk transiently before redirecting, or the redirect may happen before the lazy
  load triggers.
- The **profile chunk** (`chunk-XYNDJCEW.js`) is missing despite profile pages being visited.
  Profile pages (`/profile/info`, `/profile/experience`, etc.) were visited per README, so this
  chunk should have been loaded by the browser. It may have been missed during the resource tracking
  phase, or it may have loaded from browser cache without triggering the PerformanceObserver.

**Total root files downloaded: 220** (219 chunks + 1 polyfill + 1 main entry). The 220 files
represent the shared/eagerly-loaded infrastructure, not the feature-specific lazy chunks.

---

## 3. Sub-Page Coverage Within Visited Pages

### 3.1 Events

- **Events list page**: YES -- active tab and past/archive tab visited. Includes pagination ("
  Прошедшие 173"). "Only mine" toggle documented. Two tabs (active/archive) documented.
- **Event detail page**: YES -- slug-based detail with registration flow documented.
- **Missing**: No specific multi-event slugs documented beyond the generic pattern. This is
  acceptable since the component behavior is the same for all events.

### 3.2 Profile

- **Profile info**: YES -- `/profile/info` visited.
- **Experience/Achievements tab**: YES -- `/profile/experience` visited.
- **Edit bio**: YES -- `/profile/experience/edit-bio` visited.
- **Add experience**: YES -- `/profile/experience/add-experience` visited with 12 achievement types
  noted.
- **Missing sub-routes**: The routing doc mentions `/profile/experience-tab` as a route name. The
  README shows `/profile/experience` was visited. These likely map to the same page. The profile
  chunk (`chunk-XYNDJCEW.js`) was NOT downloaded, so the internal sub-routing of the profile feature
  is not fully documented from source code. The analysis only covers the profile from the
  user-facing navigation perspective, not the component internals.

### 3.3 Support / Case List

- **Active cases**: YES -- `/case-list` visited.
- **Archived cases**: YES -- `/case-list/archived` visited.
- **Case detail**: YES -- `/case-list/case/{id}` visited.
- The case-list chunk (`chunk-V2W4HQVK.js`) IS downloaded. However, the analysis notes state: "The
  actual case-list component chunk was loaded separately and was not found in the root bundle." This
  seems contradictory -- the chunk IS present in `raw/root/`. The chunk content may not have been
  analyzed in detail.

### 3.4 Notifications

- **Sidebar panel**: YES -- opened and documented with two tabs (Учеба / Другое).
- **Education tab**: YES -- notification categories documented.
- **Others tab**: YES -- categories documented.
- **Mark-as-read**: API documented but not visually verified.
- The notification system is implemented as a sidebar overlay, not a separate route, so there are no
  sub-routes to check.

### 3.5 Dashboard

- **Visited**: YES -- but immediately redirects to `/learn/courses/view/actual`.
- **Dashboard content**: UNKNOWN. The dashboard chunk (`chunk-NNLUD57V.js`) was NOT downloaded, so
  the actual dashboard content (if any exists before the redirect) is not documented. The redirect
  suggests the dashboard may be a passthrough for students, with actual content only for enrollees
  or other roles.

---

## 4. Gaps and Unvisited Features

### 4.1 Role-Gated Routes Not Accessible to This User

The following routes exist in the code but are guarded by role/availability checks. They could not
be visited because the test user does not have the required role or the feature is not currently
available:

| Route            | Guard                                                               | Likely Reason Inaccessible                                                                          |
|------------------|---------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `/grants`        | `Jt` (bachelor grants guard)                                        | Grants not available for this user's education level or period                                      |
| `/master-grants` | `Vi` (master grants guard)                                          | User is not a master's student, or master grants not available                                      |
| `/grants-2026`   | `ks` (grants-2026 guard)                                            | Feature not yet enabled or user not eligible                                                        |
| `/admission`     | `Es(getIsAdmissionAvailableOnce$())` or `ln([PreStudent, Student])` | Admission period closed, or user is not PreStudent/Student for this variant                         |
| `/news`          | `zs` (enrollee or student guard)                                    | Unclear -- user appears to be a student. Guard may check additional server-side config beyond role. |
| `/onboarding`    | `ln(Enrollee)` + `Rs`                                               | User is not an enrollee needing onboarding                                                          |

### 4.2 Error/Utility Pages

These pages are only shown in error conditions and cannot be visited during normal browsing:

| Route          | Purpose             | Source Code Analysis                                                                                                                       |
|----------------|---------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `/not-found`   | 404 page            | Partial: component content documented from source (message text, role-based CTA)                                                           |
| `/maintenance` | 503 page            | Minimal: referenced as `sc` component, no detailed analysis                                                                                |
| `/unsupported` | Old browser warning | Minimal: referenced as `ac` component, no detailed analysis                                                                                |
| `/error`       | Error screen        | Minimal: chunk `chunk-ZN6X5W4H.js` referenced but not downloaded. Error messages for InconsistentUserData documented from interceptor code |

### 4.3 Information Available from Source Code Despite No Visit

Even though some routes were not visited, the analysis extracted significant information from the
eagerly-loaded source code:

- **Grants**: API endpoints (`GET /api/hub/grants/me`,
  `GET /api/hub/grants/me/{year}/{type}/active`), grant status enum (
  `None, Denied, NotConfirmed, Confirmed, ConfirmedIfTop`), grant types (
  `InformationTechnology, Design`). Source: `chunk-H2UV6AAT.js`.
- **News**: API endpoints (`GET /api/event-builder/public/news/{id}`,
  `GET /api/event-builder/public/news/file/{id}`). Source: `chunk-EFZ55JZJ.js`.
- **Admission**: API endpoint (`GET /api/event-builder/admissions/2025/state`). Source:
  `chunk-2NELPLXO.js`.
- **Onboarding**: API endpoint (`POST /api/hub/enrollees/onboarding/me`), enrollee profile model.
  Source: `chunk-2RNYUVXP.js`.

What is NOT available (would require downloading the lazy chunks):

- Dashboard internal layout and widgets
- Grants page UI and form structure
- Master grants page UI differences from regular grants
- Admission form fields and flow
- Grants-2026 page content
- News list page (pagination, filtering)
- Onboarding form steps and fields
- Profile page internal component tree and sub-routing details
- Error screen detailed layout

---

## 5. Recommendations

### 5.1 Critical Missing Chunks to Download

These chunks should be downloaded to complete the analysis for KMP feature parity:

| Priority | Chunk               | Route            | Reason                                                                                            |
|----------|---------------------|------------------|---------------------------------------------------------------------------------------------------|
| HIGH     | `chunk-XYNDJCEW.js` | `/profile`       | Profile was visited but chunk not captured. Contains sub-routing, form fields, achievement types. |
| HIGH     | `chunk-NNLUD57V.js` | `/dashboard`     | Dashboard was visited but chunk not captured. May contain widgets/data for non-redirect cases.    |
| MEDIUM   | `chunk-J2FWVIQV.js` | `/news`          | News is a student-facing feature. Need a user with the right role/visibility to trigger loading.  |
| MEDIUM   | `chunk-4ACXTDD6.js` | `/grants`        | Grants UI for eligible students. Need eligible user account.                                      |
| LOW      | `chunk-MYPYERCK.js` | `/admission`     | Admission form. Only relevant during admission periods.                                           |
| LOW      | `chunk-DU2L5VZK.js` | `/admission`     | Future student application form variant.                                                          |
| LOW      | `chunk-XBPQU6OA.js` | `/master-grants` | Master grants variant.                                                                            |
| LOW      | `chunk-O5N3J4AB.js` | `/grants-2026`   | Year-specific grants page.                                                                        |
| LOW      | `chunk-65PL4UYO.js` | `/onboarding`    | Enrollee onboarding flow.                                                                         |
| LOW      | `chunk-ZN6X5W4H.js` | `/error`         | Error screen component.                                                                           |

### 5.2 Alternative: Direct Download via URL

Since the JS files are served from a CDN with content-hashed filenames, they can be downloaded
directly:

```
https://my.centraluniversity.ru/chunk-XYNDJCEW.js
https://my.centraluniversity.ru/chunk-NNLUD57V.js
https://my.centraluniversity.ru/chunk-J2FWVIQV.js
```

These URLs do not require authentication (static assets are public). This would avoid the need to
navigate to guarded routes.

### 5.3 Case-List Chunk Analysis Gap

The `chunk-V2W4HQVK.js` file IS present in `raw/root/` but was noted in the analysis as "not found
in the root bundle." This chunk should be analyzed for support/informer feature details including:

- Case list UI and filtering
- Case detail view
- Archived vs active case handling
- Tinkoff Informer web component integration

---

## 6. Overall Coverage Assessment

| Category                     | Coverage                                                         |
|------------------------------|------------------------------------------------------------------|
| **Routes visited**           | 11/20 (55%)                                                      |
| **Lazy chunks downloaded**   | 4/14 (29%)                                                       |
| **API endpoints documented** | High -- most API services are in eagerly-loaded shared chunks    |
| **Data models documented**   | High -- enums, interfaces, and request/response shapes extracted |
| **UI component internals**   | Low for unvisited routes; Good for events and notifications      |
| **Auth/session flow**        | Complete                                                         |
| **Error handling**           | Complete from source code                                        |
| **Analytics/telemetry**      | Complete                                                         |

**Bottom line**: The reverse engineering captured the full infrastructure (routing, auth, APIs, data
models, error handling, analytics) but is missing the UI internals of 10 out of 14 lazy-loaded
feature modules. The most impactful gaps for KMP development are the profile chunk (already visited
but not captured) and the news chunk (student-facing feature). The grants, admission, and onboarding
chunks are lower priority since they target specific user roles that may not be relevant to the
initial KMP mobile app scope.
