# Comprehensive Testing Plan — CuMobile KMP App

## Overview

This plan covers end-to-end verification of every feature of the CuMobile KMP app using the
Android emulator (Medium Phone 2, emulator-5554). Each feature will be tested for:

1. **API correctness** — data is fetched and deserialized without errors
2. **UI correctness** — screens render properly, layout is correct, no visual glitches
3. **Interaction correctness** — taps, scrolls, navigation work as expected
4. **Comparison with Flutter** — verify feature parity and visual similarity

**Important context**: The test account may not have typical student data (e.g., no lessons,
possibly empty task lists, empty courses). For each screen:
- First verify the API call succeeds and data deserializes correctly (check logs/behavior)
- If data is genuinely empty (API returns empty list, not an error), confirm the empty state UI
  renders correctly
- If there IS an error (crash, deserialization failure, HTTP error), fix it
- Only after confirming data is received and deserialized correctly AND is actually empty, consider
  adding a mock repository for visual testing

---

## Phase 1: Build & Launch Verification

### 1.1 Build the app
- [ ] Run the Android build configuration
- [ ] Verify the app installs on the emulator without errors
- [ ] Verify the app launches without crashing

### 1.2 Initial launch state
- [ ] Take screenshot of the launch screen
- [ ] Verify the Login screen appears with "Центральный Университет LMS" title and "Войти" button
- [ ] Compare Login screen layout with Flutter version

---

## Phase 2: Authentication Flow

### 2.1 Login screen
- [ ] Verify "Войти" button is visible and tappable
- [ ] Tap "Войти" — verify WebView login screen opens
- [ ] Verify the WebView loads `https://my.centraluniversity.ru`

### 2.2 WebView login
- [ ] **Ask the user to perform login** (credentials needed)
- [ ] After login, verify the cookie (`bff.cookie`) is captured
- [ ] Verify automatic navigation to Main screen after successful auth
- [ ] Verify cookie is persisted (kill and relaunch app — should skip login)

---

## Phase 3: Home Screen ("Главная")

### 3.1 Top bar
- [ ] Verify profile initials circle is displayed in top-left
- [ ] Verify late days balance badge is shown (if available)
- [ ] Verify notification bell icon in top-right
- [ ] Tap profile initials — verify navigation to Profile screen
- [ ] Tap notification bell — verify navigation to Notifications screen

### 3.2 Deadlines section
- [ ] Verify "Дедлайны" section header is visible
- [ ] Verify horizontal scrollable list of deadline task cards
- [ ] If empty: verify empty state message or section is hidden gracefully
- [ ] If populated: verify each card shows task name, course, deadline, score
- [ ] Tap a deadline card — verify navigation to the appropriate Longread screen
- [ ] Compare layout with Flutter's deadlines section

### 3.3 Courses section
- [ ] Verify "Курсы" section header is visible
- [ ] Verify 2-column grid of course cards
- [ ] If empty: verify empty state is shown correctly
- [ ] If populated: verify each card shows course name and category cover color
- [ ] Tap a course card — verify navigation to Course Detail screen
- [ ] Compare layout with Flutter's home courses section

### 3.4 General home screen
- [ ] Verify scrolling works smoothly
- [ ] Take screenshot and compare with Flutter home tab

---

## Phase 4: Tasks Screen ("Задания")

### 4.1 Task list display
- [ ] Switch to Tasks tab
- [ ] Verify Active/Archive segment control is visible
- [ ] Verify status filter chips are displayed
- [ ] Verify course filter chips are displayed
- [ ] Verify search field is present

### 4.2 Task filtering
- [ ] Toggle between Active and Archive segments — verify list updates
- [ ] Tap different status filter chips — verify filtering works
- [ ] Tap different course filter chips — verify filtering works
- [ ] Type in search field — verify search filters tasks by name
- [ ] Clear search — verify all tasks reappear

### 4.3 Task list items
- [ ] Verify each task card shows: task name, course name, deadline, status badge, score
- [ ] Verify correct color coding for different statuses
- [ ] Verify overdue indicators (if applicable)
- [ ] If empty: verify empty state message is shown
- [ ] Compare task card layout with Flutter version

### 4.4 Empty data handling
- [ ] If task list is empty for this account, verify API call succeeds (no crash)
- [ ] Verify deserialization works (no parsing errors)
- [ ] Verify empty state UI is user-friendly

---

## Phase 5: Courses Screen ("Обучение")

### 5.1 Courses tab — Course list
- [ ] Switch to Courses tab
- [ ] Verify 3-segment control: Курсы / Ведомость / Зачётка
- [ ] Verify active courses are listed
- [ ] Verify archived courses section (collapsible) if any
- [ ] If empty: verify empty state

### 5.2 Course cards
- [ ] Verify each course card shows name and state
- [ ] Tap a course — verify navigation to Course Detail screen

### 5.3 Grade Sheet ("Ведомость")
- [ ] Switch to "Ведомость" segment
- [ ] Verify performance courses list loads
- [ ] If empty: verify empty state or loading indicator
- [ ] If populated: verify course names and total grades are shown
- [ ] Tap a course — verify navigation to Course Performance screen

### 5.4 Gradebook ("Зачётка")
- [ ] Switch to "Зачётка" segment
- [ ] Verify semesters are displayed
- [ ] Verify each grade entry shows: subject, grade, assessment type
- [ ] Verify grade colors (excellent=green, good=blue, satisfactory=orange, etc.)
- [ ] If empty: verify empty state
- [ ] Compare layout with Flutter's gradebook

---

## Phase 6: Course Detail Screen

### 6.1 Course overview
- [ ] Open a course from the course list
- [ ] Verify course name in the top bar
- [ ] Verify back navigation button works
- [ ] Verify themes are displayed as expandable sections

### 6.2 Themes and longreads
- [ ] Expand a theme — verify longreads are listed inside
- [ ] Verify each longread shows name and type icon
- [ ] Verify exercises within themes show deadline and max score
- [ ] Tap a longread — verify navigation to Longread screen

### 6.3 Search
- [ ] Verify search functionality works within the course detail
- [ ] Type a search query — verify results filter correctly
- [ ] Compare layout with Flutter's course page

---

## Phase 7: Longread Screen

### 7.1 Longread loading
- [ ] Open a longread from course detail
- [ ] Verify materials load correctly
- [ ] Verify back navigation works

### 7.2 Markdown materials
- [ ] Find a longread with markdown content
- [ ] Verify markdown text renders correctly (headings, paragraphs, lists, links)
- [ ] Verify stripped HTML renders without raw tags

### 7.3 File materials
- [ ] Find a longread with file materials
- [ ] Verify file card shows filename and size
- [ ] Tap download — verify download initiates
- [ ] Verify downloaded file appears in Files tab

### 7.4 Coding (task) materials
- [ ] Find a longread with a coding task
- [ ] Verify task info section: status, score, deadline
- [ ] Verify Solution tab with URL input and submit button
- [ ] Verify History tab with events timeline
- [ ] Verify Comments tab with comment list and input
- [ ] Test "Start task" button (for Backlog tasks, if available)
- [ ] Test late days section (extend/cancel, if available)
- [ ] Compare with Flutter's longread page

### 7.5 Questions materials
- [ ] Find a longread with questions (quiz)
- [ ] Verify placeholder/fallback is shown (not fully implemented)

---

## Phase 8: Files Screen ("Файлы")

### 8.1 File list display
- [ ] Switch to Files tab
- [ ] If no files downloaded: verify empty state message
- [ ] If files exist: verify list shows filename, extension badge, size, date

### 8.2 File interactions
- [ ] Long-press a file — verify selection mode activates
- [ ] Verify batch delete functionality
- [ ] Verify single file delete (swipe or menu)
- [ ] Tap a file — verify it opens with system viewer
- [ ] Compare with Flutter's files tab

---

## Phase 9: Profile Screen

### 9.1 Profile data display
- [ ] Navigate to Profile screen (tap initials on Home)
- [ ] Verify avatar/initials circle is displayed
- [ ] Verify full name is shown
- [ ] Verify course year and education level
- [ ] Verify login (timeLogin or timeAccount)
- [ ] Verify Telegram handle
- [ ] Verify emails list (with masking if applicable)
- [ ] Verify phones list (with masking if applicable)

### 9.2 Profile actions
- [ ] Verify "Delete avatar" button works (if avatar exists)
- [ ] Verify "Logout" button is visible
- [ ] Tap Logout — verify return to Login screen
- [ ] Compare with Flutter's profile page

---

## Phase 10: Notifications Screen

### 10.1 Notification display
- [ ] Navigate to Notifications screen (tap bell on Home)
- [ ] Verify two tabs: "Учёба" and "Другое"
- [ ] Verify notifications load in each tab
- [ ] If empty: verify empty state is shown correctly

### 10.2 Notification items
- [ ] Verify each notification shows: icon, title, date, description
- [ ] Verify date formatting is correct
- [ ] Tap a notification with a link — verify deep linking works
- [ ] Compare with Flutter's notifications page

---

## Phase 11: Course Performance Screen

### 11.1 Scores tab
- [ ] Navigate to Course Performance from Ведомость
- [ ] Verify total grade display at top
- [ ] Verify "Набранные баллы" tab shows exercise tiles
- [ ] Verify activity filter works
- [ ] Verify each tile shows: exercise name, score, max score, status color
- [ ] If empty: verify empty state

### 11.2 Performance tab
- [ ] Switch to "Успеваемость" tab
- [ ] Verify activity summaries with weighted averages
- [ ] Verify weight and percentage calculations look correct
- [ ] Compare with Flutter's performance page

---

## Phase 12: Error Handling & Edge Cases

### 12.1 Network errors
- [ ] Test behavior with slow/no network (if possible on emulator)
- [ ] Verify error states show user-friendly messages, not crashes

### 12.2 Empty data scenarios
- [ ] For each screen where data is empty, verify:
  - [ ] No crash or spinner stuck forever
  - [ ] Appropriate empty state message or UI
  - [ ] API response was actually empty (not a failed request)

### 12.3 Navigation edge cases
- [ ] Press Android back button on each screen — verify correct behavior
- [ ] Rapidly switch between tabs — verify no crashes or wrong content
- [ ] Rotate device (if applicable) — verify layout adapts

---

## Phase 13: Flutter Comparison Summary

After testing all screens, create a comparison matrix:

| Feature | KMP Status | Flutter Status | Notes |
|---------|-----------|---------------|-------|
| Login | | | |
| Home - Deadlines | | | |
| Home - Courses | | | |
| Tasks - List | | | |
| Tasks - Filters | | | |
| Courses - List | | | |
| Courses - Grade Sheet | | | |
| Courses - Gradebook | | | |
| Course Detail | | | |
| Longread - Markdown | | | |
| Longread - Files | | | |
| Longread - Coding | | | |
| Files | | | |
| Profile | | | |
| Notifications | | | |
| Course Performance | | | |

### Features in Flutter but NOT in KMP (deferred):
- Document Scanner (ScanWorkPage)
- iCal Schedule integration
- In-app update checker
- File rename templates
- Course reordering (drag-and-drop)

These are intentionally deferred per `memorybank/deferred-features.md`.

---

## Phase 14: Fix & Mock Strategy

For issues found during testing:

1. **Crashes / Deserialization errors** — Fix immediately in code
2. **UI layout issues** — Fix in composables
3. **Empty data (confirmed empty from API)** — Add mock repository implementation for visual
   testing, keeping original repository intact. Mock should be swappable via DI (Koin)
4. **Missing features vs Flutter** — Document but do not implement (unless critical)

---

## Execution Order

1. Phases 1-2: Build, launch, authenticate (requires user help for login)
2. Phase 3: Home screen (first screen after login)
3. Phases 4-5: Tasks and Courses tabs
4. Phase 6-7: Course Detail and Longread (deeper navigation)
5. Phases 8-10: Files, Profile, Notifications
6. Phase 11: Course Performance
7. Phases 12-13: Edge cases and Flutter comparison
8. Phase 14: Fix/mock as needed throughout

**Estimated time**: This is an extensive manual testing session. Each phase involves multiple
screenshots, element inspections, and comparisons.
