# Root App Large Chunks Analysis

Analysis of newly discovered large JS chunks found via static import tracing from the root Angular
application.

## File Classification Summary

| File              | Size  | Lines | Domain                                                               |
|-------------------|-------|-------|----------------------------------------------------------------------|
| chunk-3NSU5MPO.js | 819KB | 5785  | **Third-party: dash.js** (MPEG-DASH video player + CEA-608 captions) |
| chunk-KRMWPOTS.js | 201KB | 1306  | **Third-party: EventEmitter polyfill** + dash.js streaming internals |
| chunk-EMSJOP65.js | 97KB  | 811   | **Third-party: HLS/DASH player** + Telegram community widget         |
| chunk-F6YLKJD6.js | 93KB  | 99    | **CU Validators library** (form validators, shared)                  |
| chunk-XXXVP653.js | 77KB  | 811   | **Third-party: MessageFormat** (ICU i18n message formatting)         |
| chunk-OO7VWEVR.js | 62KB  | 366   | **Grant Apply Form - Activity Groups** (olympiads, competitions)     |
| chunk-TOVOQLRA.js | 55KB  | 340   | **Grant Apply Form - Activity UI** (add/view/files)                  |
| chunk-TYXSATCA.js | 53KB  | 416   | **Grant Competition UI** (roadmap, banner, increase grant, tracks)   |
| chunk-EEZOTMIH.js | 48KB  | 121   | **Admission Forms - Payer/Organization** (event-builder admissions)  |

---

## 1. chunk-3NSU5MPO.js (819KB) -- dash.js Video Player

**Classification: Third-party library, not app code.**

This is the bundled [dash.js](https://github.com/Dash-Industry-Forum/dash.js) MPEG-DASH video player
library. Contains:

- DASH manifest parsing and segment fetching
- Media Source Extensions (MSE) buffer management
- ABR (Adaptive Bitrate) switching logic
- CMCD/CMSD (Common Media Client/Server Data) support
- CEA-608/708 closed caption rendering
- Gap jumping and live streaming support
- DRM protection handling

No CU application endpoints or components.

**Import chain:** Imports from `chunk-KRMWPOTS.js` (EventEmitter).

---

## 2. chunk-KRMWPOTS.js (201KB) -- EventEmitter + Streaming Utils

**Classification: Third-party library support.**

Contains a Node.js-style `EventEmitter` polyfill used by dash.js. The rest is streaming utility
code (buffer management, scheduling, etc.).

No CU application endpoints or components. Exports used by chunk-3NSU5MPO.js.

---

## 3. chunk-EMSJOP65.js (97KB) -- HLS/DASH Player + Telegram Widget

**Classification: Mixed third-party + small app component.**

Contains:

- UUID validation library
- HLS/DASH streaming protocol support code
- Network connection type detection

### Component: `cu-telegram-community-widget`

- Selector: `cu-telegram-community-widget`
- Inputs: `link`, `checkVisibility`, `closeable`
- Outputs: `isVisible`, `isPermanentlyHidden`
- Purpose: A widget prompting users to join a Telegram community channel. Shows a link with "Join
  Telegram channel of participants" text.
- Uses IntersectionObserver for visibility tracking.
- No API calls directly.

---

## 4. chunk-F6YLKJD6.js (93KB) -- CU Validators Library

**Classification: Application shared utilities.**

A comprehensive form validation library exporting validators. Not a UI chunk, but critical shared
logic.

### Exported Validators

```
allFilesAreUploaded, capitalized, cuStaffEmailWithTBank, dateRange,
dependentOnRequiredField, email, fileAcceptType, fileSizeMax,
ifValuePresent, inn, json, max, maxDate, maxLength, min,
minArrayLength, minDate, minLength, minValidArrayLength,
moreThanNumber, notValue, numberRange, oneLangForFields,
oneLangForFieldsControl, pattern, phoneNumber, precisionValidator,
required, restricted, ruOrEnName, snils, specificValue, telegram, url,
validateIndividualInn, validateSnils
```

### Key Patterns

- Telegram username regex: `^[a-zA-Z0-9_]{5,32}$`
- CU staff email pattern: `@(centraluniversity|cu)\.ru` and `@tbank\.ru`
- Russian name regex: Cyrillic validation (`^[A-YA-YO]([a-ya-yo])+([' -][A-YA-YO][a-ya-yo]+)*$`)
- English name regex: Latin validation (`^[A-Z]([a-z])+([' -][A-Z][a-z]+)*$`)
- INN validation (Russian tax ID)
- SNILS validation (Russian social insurance number)

No API endpoints, no components.

---

## 5. chunk-XXXVP653.js (77KB) -- MessageFormat i18n

**Classification: Third-party library.**

ICU MessageFormat library for internationalization. Handles date/time formatting, plural rules,
select expressions, and number formatting.

No CU application endpoints or components.

---

## 6. chunk-OO7VWEVR.js (62KB) -- Grant Apply Form Activities

**Classification: Application code. Grant competition apply form.**

### API Endpoints

| Method        | URL                                     | Purpose                         |
|---------------|-----------------------------------------|---------------------------------|
| POST (upload) | `/api/hub/activities-files/me`          | Upload activity proof documents |
| GET (file)    | `/api/hub/activities-files/me/{fileId}` | Download uploaded file          |

### Components

| Selector                                                     | Purpose                                                   |
|--------------------------------------------------------------|-----------------------------------------------------------|
| `cu-hub-grants-apply-form-feature-activity-files-input`      | Activity file upload input with validation                |
| `cu-hub-grants-apply-form-ui-activity-profile-id-input`      | Olympiad profile selector (combo-box)                     |
| `cu-hub-grants-apply-form-activity-rsosh-group`              | RsOSH olympiad form group                                 |
| `cu-hub-grants-apply-form-activity-vsosh-group`              | VsOSH olympiad form group                                 |
| `cu-hub-grants-apply-form-activity-design-competition-group` | Design competition form group                             |
| `cu-hub-grants-apply-form-activity-other-group`              | Other activities form group                               |
| `cu-apply-form-activity-group`                               | Wrapper with Accordion/Plain views for each activity type |
| `cu-school-olympiads-council-info`                           | Info about school olympiads council                       |

### Data Models / Enums

**Activity Group View Type:**

```
{Accordion: "accordion", Plain: "plain"}
```

**Activity Types** (imported as `F`, `tt`):

- `designCompetition` -- Design competitions
- `other` -- Other activities
- `rsosh` -- Russian School Olympiad (RsOSH)
- `vsosh` -- All-Russian School Olympiad (VsOSH)

### Form Fields (per activity type)

All types share: `id`, `userActivityId`, `type`, `status`, `year`, `class`, `criteriaId`, `files`

- **VsOSH**: + `activity` (olympiad name), `activityProfileId` (subject), `activityProfileName`
- **RsOSH**: + `activity` (olympiad name), `activityProfileId` (profile), `activityProfileName`
- **Design Competition**: + `activity` (competition name)
- **Other**: + `activity` (activity name)

File upload: accepts `.jpeg, .jpg, .png, .pdf`, max 5MB per file.

---

## 7. chunk-TOVOQLRA.js (55KB) -- Grant Activity UI Components

**Classification: Application code. Grant activity add/view/file UI.**

### API Endpoints

| Method        | URL                            | Purpose                   |
|---------------|--------------------------------|---------------------------|
| POST (upload) | `/api/hub/activities-files/me` | Upload activity documents |

### Components

| Selector                      | Purpose                                               |
|-------------------------------|-------------------------------------------------------|
| `cu-activity-add-prompt-card` | Prompt card showing "List of grant achievements" link |
| `cu-add-activity-button`      | Button with dropdown to add new activity by type      |
| `cu-view-activity`            | Read-only view of a submitted activity                |
| `cu-activity-files-input`     | File input for activity proof documents               |
| `cu-sheet-dialog-select`      | Mobile bottom-sheet selection dialog                  |

### View Activity Model

The `cu-view-activity` component renders different fields per type:

- **Design Competition**: Year, competition name, class, status, documents
- **RsOSH**: Year, olympiad name, olympiad profile, class, result, documents
- **VsOSH**: Year, olympiad subject, class, result, documents
- **Other**: Year, activity name, class, status, documents

---

## 8. chunk-TYXSATCA.js (53KB) -- Grant Competition Dashboard

**Classification: Application code. Grant competition status/roadmap/UI.**

### API Endpoints

| Method | URL                                                     | Purpose                                      |
|--------|---------------------------------------------------------|----------------------------------------------|
| GET    | `/api/cu-guide/authorization/telegram/code-for-link/me` | Get Telegram bot linking code                |
| GET    | `/api/whitelist/channels/grants/{channelId}/2026`       | Check if user joined Telegram grants channel |

### Components

| Selector                               | Purpose                                                   |
|----------------------------------------|-----------------------------------------------------------|
| `cu-telegram-assistant`                | Telegram bot assistant widget (IT/Design variants)        |
| `cu-telegram-community-sticky-banner`  | Sticky bottom banner promoting Telegram community         |
| `cu-scheduled-event-format`            | Shows event format (Online/Offline/OfflineBank/OfflineCu) |
| `cu-increase-grant-competition`        | Single competition entry with activity status             |
| `cu-increase-grant-detail`             | Expandable detail card for grant increase application     |
| `cu-increase-grant-status`             | Status badge (check/increase/new/rejected/review)         |
| `cu-base-grant-widget`                 | Main grant value display widget (base + additional %)     |
| `cu-grant-roadmap`                     | Visual roadmap of grant competition steps                 |
| `cu-competition-grid`                  | 2-column grid layout for competition tracks               |
| `cu-competition-track`                 | Track card with icon, title, subtitle, description        |
| `cu-competition-track-expand`          | Expandable track detail with scroll-into-view             |
| `cu-past-steps` / `cu-past-steps-item` | Past steps display in competition flow                    |

### Data Models / Enums

**Scheduled Event Format Type (`Ct`):**

- `Online`
- `Offline`
- `OfflineBank` (at T-Bank office, Yandex Maps link: https://yandex.ru/maps/-/CHQimOjH)
- `OfflineCu` (at CU campus, Yandex Maps link: https://yandex.ru/maps/-/CHCkRAYJ)

**Increase Grant Status flow:**

- `new` -> "Sent"
- `check` -> "Under review"
- `review` -> "Returned for revision"
- `increase` -> "Grant increased"
- `rejected` -> "Reviewed without increase"

**Activity Status values** (from imported `Ce`):

- `Approved`, `Declined`, `NeedFix` (visible statuses)

**Grant Widget:**

- `appearance`: "it" | "design"
- `baseGrantValue`: percentage (from selection/competitions/olympiads/EGE scores)
- `additionalGrantValue`: percentage (from special events and XP Game)
- `totalGrantValue`: capped at 100%
- `discountedPrice`: number
- `isTenClassSchoolchild`: boolean (additional grant starts from 11th grade)

### External Links

- Grant handbook: `https://centraluniversity.yonote.ru/share/handbook`
- Grant details: `https://centraluniversity.yonote.ru/share/grant`

### Exports

```javascript
export {
  ri as a,   // IT telegram providers
  si as b,   // Design telegram providers
  _o as c,   // cu-base-grant-widget
  vi as d,   // cu-telegram-assistant
  Ai as e,   // cu-telegram-community-sticky-banner
  Vi as f,   // cu-scheduled-event-format
  Oa as g,   // cu-grant-roadmap (with past steps)
  ho as h,   // cu-competition-grid
  yo as i,   // cu-competition-track
  Ao as j,   // cu-competition-track-expand
  ja as k,   // cu-increase-grant-applications (list)
  Qa as l    // (additional export)
}
```

---

## 9. chunk-EEZOTMIH.js (48KB) -- Admission Payer Forms

**Classification: Application code. Event-builder admission forms.**

### API Endpoints

| Method        | URL                                          | Purpose                    |
|---------------|----------------------------------------------|----------------------------|
| POST (upload) | `/api/event-builder/admissions/files/upload` | Upload admission documents |

### Components

| Selector                                                             | Purpose                                 |
|----------------------------------------------------------------------|-----------------------------------------|
| `cu-hub-admission-common-payer-info-form-entrepreneur-form-group-ui` | Individual Entrepreneur (IP) payer form |
| `cu-hub-admission-common-payer-info-form-organisation-form-group-ui` | Organization payer form                 |
| `cu-hub-admission-common-payer-info-form-individual-form-group-ui`   | Individual payer form                   |

### Data Models / Enums

**Payer Type (`Ct`):**

```
{
  Self: "Self",
  Another: "Another",
  Organization: "Organization",
  IndividualEntrepreneur: "IndividualEntrepreneur"
}
```

**Identity Document Type:**

```
{RussianPassport: "RussianPassport"}
```

### Entrepreneur Form Fields

- `fullName`, `legalAddress`, `mailingAddress` (optional)
- `ogrnip` (OGRNIP business registration number)
- `inn` (INN tax ID, with maskito mask)
- `email`, `phone` (with international phone input)
- `currentAccount`, `correspondentAccount`
- `bankName`, `bankBic`
- `managerPosition` (optional), `managerName` (optional)

---

## API Endpoints Summary (All Root Chunks)

Consolidated from this analysis plus existing root chunk findings:

### Grant Competition

| Method | Endpoint                                                | Source                         |
|--------|---------------------------------------------------------|--------------------------------|
| GET    | `/api/cu-guide/authorization/telegram/code-for-link/me` | chunk-TYXSATCA                 |
| GET    | `/api/whitelist/channels/grants/{channelId}/2026`       | chunk-TYXSATCA                 |
| POST   | `/api/hub/activities-files/me`                          | chunk-TOVOQLRA, chunk-OO7VWEVR |
| GET    | `/api/hub/activities-files/me/{fileId}`                 | chunk-OO7VWEVR                 |

### Admission / Event-Builder

| Method | Endpoint                                     | Source         |
|--------|----------------------------------------------|----------------|
| POST   | `/api/event-builder/admissions/files/upload` | chunk-EEZOTMIH |

### Already Cataloged (other root chunks)

| Method   | Endpoint                                                           | Source         |
|----------|--------------------------------------------------------------------|----------------|
| GET      | `/api/hub/competitions/master-2026/me/state`                       | chunk-4YAAVVPY |
| POST     | `/api/hub/competitions/bachelor-2024/waitlist/me`                  | chunk-FPXM5OVY |
| GET      | `/api/hub/competitions/bachelor-2024/waitlist/me/exists`           | chunk-FPXM5OVY |
| POST     | `/api/hub/competitions/master-2025/waitlist/me`                    | chunk-FPXM5OVY |
| GET      | `/api/hub/competitions/master-2025/waitlist/me/exists`             | chunk-FPXM5OVY |
| GET      | `/api/hub/competitions/grant-bachelor-2026-design/case/{id}`       | chunk-O5N3J4AB |
| GET      | `/api/hub/competitions/grant-bachelor-2026-design/case/{id}/guide` | chunk-O5N3J4AB |
| POST     | `/api/notification-hub/notifications/in-app`                       | chunk-KUQINTYY |
| GET      | `/api/notification-hub/notifications/in-app/stats`                 | chunk-KUQINTYY |
| POST     | `/api/notification-hub/notifications/in-app/read`                  | chunk-KUQINTYY |
| PUT      | `/api/account/me/locale`                                           | chunk-OIDSCTRK |
| GET/POST | `/api/hub/avatars/me`                                              | chunk-W4PG2AYH |
| POST     | `/api/hub/master-grant-competition-cv-files/me`                    | chunk-2ZEADH6T |
| POST     | `/api/hub/experience-files/me`                                     | chunk-USXWS6MS |
| POST     | `/api/hub/unified-state-exams-files/me/{id}`                       | chunk-MO375SGD |
| POST     | `/api/hub/competitions-cases-solutions-files/me`                   | chunk-JKUM7O6Y |
| POST     | `/api/event-builder/admissions/forms/future-student/files/upload`  | chunk-VOKE7BVC |
| POST     | `/api/event-builder/admissions/documents/files/upload`             | chunk-TLLM6G74 |

### Base URL Tokens

| Token                   | URL              |
|-------------------------|------------------|
| `LMS_BASE_API_URL`      | `/api/micro-lms` |
| `API_ANALYTICS_API_URL` | `/api/analytics` |

---

## Third-Party Libraries Identified

1. **dash.js** (chunks 3NSU5MPO + KRMWPOTS) -- MPEG-DASH video player with CEA-608 captions
2. **MessageFormat** (chunk-XXXVP653) -- ICU message formatting for i18n
3. **EventEmitter** (chunk-KRMWPOTS) -- Node.js EventEmitter polyfill
4. **UUID** (in chunk-EMSJOP65) -- UUID generation and validation

These are used for the video player in the LMS/learning sections of the site.

---

## Architecture Notes

The grant competition feature uses a hierarchical form structure:

1. **Activity Types**: VsOSH, RsOSH, Design Competition, Other
2. **Each activity** has: year, activity selector, participation class, criteria/result, file
   uploads
3. **Grant calculation**: Base grant (from admissions) + Additional grant (from activities/XP Game),
   capped at 100%
4. **Increase grant flow**: Submit -> Under Review -> Approved/Rejected/Returned for Revision
5. **Telegram integration**: Bot for reminders + Community channel for grant holders

The payer forms (EEZOTMIH) support 4 payer types:

- Self, Another person, Organization, Individual Entrepreneur
- Each with different required fields (banking details for Org/IE, passport for individuals)
