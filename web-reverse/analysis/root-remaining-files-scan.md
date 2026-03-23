# Root Remaining Files Scan

Comprehensive scan of all unanalyzed root app JS files for API endpoints, HTTP calls, component
selectors, and route paths.

---

## 1. API Endpoints Found Across All Root Files

### Already documented (confirmed present in files):

| Path                                                               | Found In                             |
|--------------------------------------------------------------------|--------------------------------------|
| `/api/event-builder/public/news` (base URL)                        | chunk-EFZ55JZJ.js                    |
| `/api/event-builder/admissions/2025/state`                         | chunk-2NELPLXO.js                    |
| `/api/interview-scheduler/public/interview/active`                 | chunk-NDA7QB4E.js                    |
| `/api/referrals/invitations/me/invitees`                           | chunk-SK6ZLY2L.js                    |
| `/api/notification-hub/notifications/in-app` (POST)                | chunk-KUQINTYY.js                    |
| `/api/notification-hub/notifications/in-app/stats`                 | chunk-KUQINTYY.js                    |
| `/api/notification-hub/notifications/in-app/read` (POST)           | chunk-KUQINTYY.js                    |
| `/api/cu-guide/authorization/telegram/code-for-link/me`            | chunk-TYXSATCA.js                    |
| `/api/whitelist/channels/grants/{channelId}/2026`                  | chunk-TYXSATCA.js                    |
| `/api/hub/experience-files/me`                                     | chunk-USXWS6MS.js                    |
| `/api/hub/master-grant-competition-cv-files/me`                    | chunk-2ZEADH6T.js                    |
| `/api/hub/competitions/master-2026/me/state`                       | chunk-4YAAVVPY.js                    |
| `/api/hub/unified-state-exams-files/me/{id}`                       | chunk-MO375SGD.js                    |
| `/api/hub/activities-files/me`                                     | chunk-OO7VWEVR.js, chunk-TOVOQLRA.js |
| `/api/hub/activities-files/me/{id}`                                | chunk-OO7VWEVR.js                    |
| `/api/event-builder/admissions/forms/future-student/files/upload`  | chunk-VOKE7BVC.js                    |
| `/api/event-builder/admissions/documents/files/upload`             | chunk-TLLM6G74.js                    |
| `/api/event-builder/admissions/files/upload`                       | chunk-EEZOTMIH.js                    |
| `/api/account/me/locale` (PUT)                                     | chunk-OIDSCTRK.js                    |
| `/api/micro-lms` (LMS_BASE_API_URL)                                | chunk-OIDSCTRK.js                    |
| `/api/analytics` (API_ANALYTICS_API_URL)                           | chunk-OIDSCTRK.js                    |
| `/api/hub/avatars/me`                                              | chunk-OIDSCTRK.js, chunk-W4PG2AYH.js |
| `/api/hub/competitions-cases-solutions-files/me`                   | chunk-JKUM7O6Y.js                    |
| `/api/hub/competitions-cases-solutions-files/me/{id}`              | chunk-JKUM7O6Y.js                    |
| `/api/hub/competitions/bachelor-2024/waitlist/me` (POST)           | chunk-FPXM5OVY.js                    |
| `/api/hub/competitions/bachelor-2024/waitlist/me/exists`           | chunk-FPXM5OVY.js                    |
| `/api/hub/competitions/master-2025/waitlist/me` (POST)             | chunk-FPXM5OVY.js                    |
| `/api/hub/competitions/master-2025/waitlist/me/exists`             | chunk-FPXM5OVY.js                    |
| `/api/hub/competitions/grant-bachelor-2026-design/case/{id}`       | chunk-O5N3J4AB.js                    |
| `/api/hub/competitions/grant-bachelor-2026-design/case/{id}/guide` | chunk-O5N3J4AB.js                    |
| `/api/micro-lms/students` (get, with params)                       | chunk-OIDSCTRK.js                    |
| `/api/micro-lms/students/me`                                       | chunk-OIDSCTRK.js                    |
| `/api/micro-lms/tasks/{id}/late-days-prolong` (PUT)                | chunk-OIDSCTRK.js                    |
| `/api/micro-lms/tasks/{id}/late-days-cancel` (PUT)                 | chunk-OIDSCTRK.js                    |

### No new API endpoints found beyond what is already documented in api-endpoints.md.

All API endpoints found in these root files are already cataloged in the existing
`api-endpoints.md`.

---

## 2. HTTP Service Calls (`.get()`, `.post()`, `.put()`, `.delete()`, `.patch()`)

HTTP service calls were found in the following prettified root files:

| File              | Methods Found                                          |
|-------------------|--------------------------------------------------------|
| chunk-TYXSATCA.js | `.get()` x2 (telegram code, whitelist check)           |
| chunk-O5N3J4AB.js | `.get()` x2 (design case, guide)                       |
| chunk-OIDSCTRK.js | `.get()` x2, `.put()` x3 (students, locale, late-days) |
| chunk-GSQF2SOP.js | Multiple calls (already analyzed)                      |
| chunk-VOKE7BVC.js | `.post()` (admission form)                             |
| chunk-FPXM5OVY.js | `.post()` x2, `.get()` x2 (waitlist operations)        |
| chunk-KUQINTYY.js | `.post()` x2, `.get()` x1 (notifications)              |
| chunk-EFZ55JZJ.js | `.get()` x2 (news)                                     |
| chunk-2NELPLXO.js | `.get()` x1 (admissions state)                         |
| chunk-NDA7QB4E.js | `.get()` x1 (interviews)                               |
| chunk-SK6ZLY2L.js | `.get()` x1 (referrals)                                |

None of the 15 target large files (listed below) contain direct API calls -- they are all
UI/template/library chunks.

---

## 3. Component Selectors Found

### `cu-*` selectors found across root files:

- `cu-referral-form-page`
- `cu-hub-admission-ui-warning-screen`
- `cu-hub-master-past-steps-banner`
- `cu-hub-master-grant-banner`
- `cu-hub-master-no-ended-grant-agreement-banner`
- `cu-hub-master-agreement-banner`
- `cu-hub-admission-common-ui-next-step-expand`
- `cu-hub-admission-master-next-step`
- `cu-hub-master-heading`
- `cu-hub-master-memo-banner`
- `cu-past-steps-trigger`
- `cu-past-steps-item`
- `cu-hub-bachelor-agreement-banner`
- `cu-hub-ui-card`
- `cu-hub-ui-card-tag`
- `cu-dpo-master-apply-documents-form-group`
- `cu-dpo-master-apply-form-other-form-group`
- `cu-hub-admission-common-person-info-form-group-ui`
- `cu-hub-admission-common-disabilities-form-group-ui`
- `cu-faq`
- `cu-how-does-it-work`
- `cu-invite-friends`
- `cu-master-document-submission-banner`
- `cu-hub-admission-common-notebook-requirements-drawer-ui`

### `hub-*` selectors:

- `hub-grants-2026-referrals-feature-referral-form-page` (text content, not selector)

---

## 4. Route Paths

Route paths found across root files (from chunk-OIDSCTRK.js, the main routing chunk):

| Path            | Description                  |
|-----------------|------------------------------|
| `case-list`     | Support / Informer case list |
| `""` (root)     | Main hub with admission flow |
| `Onboarding`    | Onboarding page              |
| `Dashboard`     | Main dashboard               |
| `Error`         | Error screen                 |
| `**` (wildcard) | Fallback routes              |

Routing pages loaded dynamically:

- `chunk-V2W4HQVK.js` -> `HubInformerFeatureCaseListComponent`
- `chunk-65PL4UYO.js` -> `OnboardingComponent`
- `chunk-NNLUD57V.js` -> `dashboardRoutes`
- `chunk-ZN6X5W4H.js` -> `ErrorScreenComponent`

---

## 5. File-by-File Summaries (15 Unanalyzed Files >30KB)

### chunk-6LVSJXDE.js (76KB)

**Master admission dashboard feature.** Contains the `HubMasterDashboardFeatureComponent` -- a
complex Angular component for the master's program admission flow. Manages deal status, grant
competition state, enrollment steps (document signing, payment, Python course access, student card
photo), and displays next-step cards. Includes past-steps banner, grant/agreement banners, and
notebook requirements drawer. No direct API calls -- uses services injected from other chunks.

### chunk-K5JABNJT.js (75KB)

**DPO/Master admission form feature.** Contains `cu-dpo-master-apply-documents-form-group` and
`cu-dpo-master-apply-form-other-form-group` components for the DPO masters admission form. Handles
document upload, personal info, identity documents, and disabilities form groups. Multi-step
admission form with draft saving. No direct API calls.

### chunk-S4PHRIVZ.js (69KB)

**XP Games / Gamification dashboard feature.** Contains the XP game bonus carousel, analytics event
tracking (bootcamp, excursion, merch, scholarship events), and the gamification UI for bachelor
students. Defines XP grade levels (Intern/Junior/Middle/Senior) and bonus card components with
action buttons. References `/api/admin/configurations` for XP game bonuses config. No direct API
calls itself.

### chunk-QGZ3H5CV.js (67KB)

**Bachelor admission dashboard feature.** Contains `HubAdmissionBachelorFeatureDashboardComponent`
with agreement banners, deal status tracking, past-steps display, and next-step expansion panels.
Handles DPO master deal status flow (DocsSignRequired, DocsVerification, DocsNeedFix,
DocsOriginalSignRequired). UI-only, no direct API calls.

### chunk-NYMXBRZF.js (50KB)

**Bachelor admission agreement banner and deal flow UI.** Contains
`cu-hub-bachelor-agreement-banner` and related UI components for bachelor deal signing steps.
Displays cards for contract signing, document verification, document re-upload, and original
signature. References campus address (Gasheka 7). No API calls.

### chunk-DC4QYSQK.js (46KB)

**Taiga UI legacy textfield infrastructure.** Contains `tui-primitive-textfield`,
`tui-value-decoration`, and related legacy Taiga UI form control classes (`AbstractTuiControl`,
`AbstractTuiInteractive`, `AbstractTuiNullableControl`). Provides textfield appearance, cleaner,
icon, size, prefix/postfix directives. Pure UI library code, no API calls.

### chunk-QRHG4DTJ.js (44KB)

**ngx-image-cropper library.** Contains the image cropper component for avatar/photo uploads.
Handles mouse/touch-based image cropping with resize handles (topleft, topright, bottomleft,
bottomright, top, bottom, left, right). Pure vendor library, no API calls.

### chunk-NVEW2AT7.js (40KB)

**ProseMirror table map module.** Contains `TableMap` class for managing table structures in the
TipTap/ProseMirror rich text editor. Handles cell lookup, column counting, rect computation, and
cell positioning in tables. Pure vendor library for the WYSIWYG editor, no API calls.

### chunk-7WLVJQKM.js (40KB)

**Taiga UI core infrastructure: active zone, event plugins, scrollbar controls.** Contains
`tuiActiveZone` directive for tracking focus zones, custom event plugins (longtap, debounce,
throttle, silent, stop, prevent, self, resize), and `tui-scroll-controls` / `tuiScrollbar` for
custom scrollbars. Pure UI framework code, no API calls.

### chunk-W7IEHNL6.js (40KB)

**DPO/Master admission form UI (continued).** Additional admission form components and step-by-step
UI for the DPO masters program. Contains document upload and verification flows, contract step
cards, and form group UI components. No direct API calls.

### chunk-YWITU7BM.js (37KB)

**libphonenumber-js phone number parsing library.** Contains phone number metadata parsing,
formatting, and validation logic. Handles international phone number formats, country calling codes,
and number type detection (FIXED_LINE, MOBILE, TOLL_FREE, etc.). Pure vendor library, no API calls.

### chunk-ZDUSAF76.js (36KB)

**Referral program ("Invite Friends") feature.** Contains `cu-faq`, `cu-how-does-it-work`,
`cu-invite-friends` UI components for the referral program. Displays FAQ about inviting friends (
unlimited invitations, XP points, MacBook raffle for autumn 2026). Copy link and QR code sharing
functionality. Uses `/api/referrals/invitations/me/invitees` indirectly via chunk-SK6ZLY2L.js. No
direct API calls.

### chunk-YJJJSULJ.js (35KB)

**TipTap/TUI WYSIWYG editor service.** Contains `TuiEditor` service wrapping the TipTap rich text
editor with methods for bold, italic, underline, lists, tables, code blocks, links, images, font
colors, alignment, undo/redo, and more. Also contains `tui-input-inline` component. Pure editor
infrastructure, no API calls.

### chunk-AYK5W3AV.js (34KB)

**One Day Offer registration feature.** Contains the registration dialog/page for "One Day Offer"
events for bachelor grant competition. Handles multiple states: canRegister, canChangeBySupport,
canNotChange, unavailableForRegistration, registerToAnotherEvent, error, loading. Displays
Russian-language text about grant competition track changes and Telegram support bot links. No
direct API calls (uses services).

### chunk-NLZS7FW6.js (31KB)

**Taiga UI new textfield component and form infrastructure.** Contains `tui-textfield` component,
`tuiLabel` directive, `tuiValidator`, `tuiSelectLike`, dropdown integration, and form field
options (appearance, size, cleaner). Also contains `tuiActiveZone` (duplicate reference from
chunk-7WLVJQKM) and items handlers service. Pure UI framework code, no API calls.

---

## 6. New API Endpoints Check

**Result: No new API endpoints found.**

All 15 target files are either:

- **UI/Template components** (6LVSJXDE, K5JABNJT, S4PHRIVZ, QGZ3H5CV, NYMXBRZF, W7IEHNL6, ZDUSAF76,
  AYK5W3AV) -- these use injected services but make no direct HTTP calls
- **Vendor/library code** (DC4QYSQK, QRHG4DTJ, NVEW2AT7, 7WLVJQKM, YWITU7BM, YJJJSULJ, NLZS7FW6) --
  these are Taiga UI, ProseMirror, libphonenumber, and ngx-image-cropper libraries

The comprehensive grep across ALL root files (`/api/` pattern) confirms that every API endpoint
discovered is already present in `/docs/web-reverse/summary/api-endpoints.md`. The existing
documentation covers all ~175+ unique API paths.

---

## Summary

- **API endpoints in root files**: All already documented. No new endpoints found.
- **HTTP service calls**: Found in 11 files, all already analyzed in previous scans.
- **Component selectors**: 24+ `cu-*` component selectors identified, all related to admission,
  grants, referrals, and profile features.
- **Route paths**: Main routes confirmed: Dashboard, Onboarding, case-list (support), Error.
- **15 large files**: 8 are feature UI components (admission/grants/referrals/XP), 7 are vendor
  libraries (Taiga UI, ProseMirror, libphonenumber, image cropper). None contain undocumented API
  endpoints.
