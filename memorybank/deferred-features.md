# Deferred Features — CuMobile KMP

Features from the Flutter reference app that are NOT included in the initial plan.
These should be implemented after the core LMS functionality is stable.

---

## 1. Document Scanner

**Priority**: Medium
**Complexity**: High (heavily platform-specific)

**What it does**:
- Camera capture or gallery import of document photos
- Multi-page support with drag-to-reorder
- Image editing: rotation, cropping
- PDF generation from scanned images with optional compression
- Used to scan and attach homework/assignments

**Flutter implementation**: `lib/features/home/pages/scan_work_page.dart` (~1579 lines)

**KMP approach**:
- Will need expect/actual for camera access (CameraX on Android, AVFoundation on iOS)
- Image manipulation libraries (platform-specific)
- PDF generation (platform-specific or use a KMP library)
- Consider using an existing KMP camera/scanner library if available
- The compose UI (reorder, preview) can be shared

**Depends on**: File upload system (Phase 7 in main plan), basic app infrastructure

---

## 2. Schedule / Calendar Integration

**Priority**: Medium
**Complexity**: Medium

**What it does**:
- Fetches ICS calendar files from Yandex Calendar
- Parses iCal format with RRULE expansion (DAILY/WEEKLY/MONTHLY/YEARLY)
- Handles EXDATE (exception dates)
- Local file caching of calendar data
- Day view with time grid showing class events
- Displays: class name, time, room, professor, type badge

**Flutter implementation**:
- `lib/data/services/ical_service.dart` (~414 lines) — ICS parser
- Schedule section in `lib/features/home/pages/home_page.dart`

**Data model** (`ClassData`):
- startTime, endTime, room, type, title, professor, link, badge, badgeColor

**KMP approach**:
- ICS parsing can be fully shared (it's text parsing + date math)
- Calendar UI (day view with time grid) can be shared via Compose
- Consider kotlinx-datetime for date/time handling
- May need to add the Yandex Calendar ICS URL configuration to ProfileScreen

**Depends on**: Profile screen (for calendar URL setup), Home screen (for display)

---

## 3. In-App Update Checker

**Priority**: Low
**Complexity**: Low

**What it does**:
- Checks GitHub releases API: `GET https://api.github.com/repos/cu-3rd-party/lms-mobile/releases/latest`
- Compares current app version with latest release version
- Shows update dialog with download link for APK (Android) or IPA (iOS)
- Used for distributing updates outside app stores

**Flutter implementation**: `lib/core/services/update_service.dart`

**KMP approach**:
- Simple Ktor GET request + version comparison — fully shared
- Download/install is platform-specific (Android: download APK + intent, iOS: TestFlight link)
- Low priority since app store distribution handles this

**Depends on**: Basic networking (Phase 3)

---

## 4. File Rename Templates

**Priority**: Low
**Complexity**: Low

**What it does**:
- Allows users to configure naming templates per course/activity type
- Applied when attaching files to task submissions
- Templates stored in SharedPreferences (we'd use DataStore)
- Settings page for managing templates

**Flutter implementation**:
- `lib/core/services/file_rename_service.dart`
- `lib/features/settings/pages/file_rename_settings_page.dart` (~837 lines)
- `lib/features/longread/widgets/file_rename_dialog.dart`

**KMP approach**:
- Template storage via DataStore — fully shared
- Settings UI — fully shared via Compose
- Template application logic — fully shared

**Depends on**: File attachment system (Phase 7), DataStore setup (Phase 0)

---

## 5. Course Reordering

**Priority**: Low
**Complexity**: Low

**What it does**:
- Allows users to reorder courses in the Courses tab
- Order persisted locally (SharedPreferences → DataStore)
- Drag-and-drop interface

**KMP approach**:
- Order storage via DataStore — fully shared
- Compose drag-and-drop (LazyColumn with reorder support)
- Straightforward implementation

**Depends on**: Courses tab (Phase 6)

---

## Implementation Order Suggestion

1. **Schedule/Calendar** — high user value, moderate complexity, mostly shared code
2. **Document Scanner** — high user value but high platform-specific complexity
3. **File Rename Templates** — small feature, low effort
4. **Course Reordering** — small feature, low effort
5. **In-App Update Checker** — lowest priority
