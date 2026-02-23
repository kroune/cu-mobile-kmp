# Flutter LMS Mobile App — Complete UI Reference

Source: DeepWiki research of `cu-3rd-party/lms-mobile`

---

## 1. Color System

### Accent Color

- **Primary accent**: `#00E676` (green) — used everywhere: bottom nav selected items, loading
  indicators, avatar border, date picker primary, blockquote borders, grade "excellent/passed",
  progress indicators, profile initials background, add icons

### Background Colors

| Hex       | Usage                                                                                             |
|-----------|---------------------------------------------------------------------------------------------------|
| `#121212` | Main scaffold, AppBar, CupertinoTabBar, BottomNavigationBar backgrounds                           |
| `#1E1E1E` | Surface color in date picker, container backgrounds (longread, top nav, course page, courses tab) |
| `#2A2A2A` | Code block backgrounds                                                                            |
| `#1A1A2E` | Blockquote backgrounds                                                                            |
| `#2D2D2D` | iOS login button background                                                                       |

### Text Colors

| Color              | Usage                                           |
|--------------------|-------------------------------------------------|
| `Colors.white`     | Titles, primary content                         |
| `Colors.grey[500]` | Inactive nav items, secondary text              |
| `Colors.grey[400]` | Tertiary text, Late Days balance, info icons    |
| `Colors.grey[300]` | Unsupported content text, system event initials |
| `Colors.grey[600]` | Chevron icons                                   |
| `#E6DB74`          | Code text in longread                           |

### Grade Colors

| Hex       | Meaning                 |
|-----------|-------------------------|
| `#00E676` | Excellent / Passed      |
| `#FFCA28` | Good                    |
| `#FF9800` | Satisfactory            |
| `#EF5350` | Failed / Unsatisfactory |

### Task State Colors

| State             | Label (RU)   | Color        |
|-------------------|--------------|--------------|
| backlog           | Не начато    | Grey         |
| inProgress        | В работе     | Blue         |
| hasSolution       | Есть решение | Green        |
| revision / rework | Доработка    | Red          |
| review            | На проверке  | Orange       |
| evaluated         | Проверено    | Bright green |
| failed / rejected | Не сдано     | Red          |

### Level Score Colors (Longread)

| Hex       | Level            |
|-----------|------------------|
| `#3044FF` | Basic (Базовый)  |
| `#E63F07` | Medium (Средний) |
| `#141414` | Default          |

### Theme

- **Dark mode only** (no light mode). Colors are hardcoded inline, no centralized theme file.
- `#00E676` with alpha 0.2 for HTML `<mark>` tag background

---

## 2. App Structure & Navigation

### Bottom Navigation Bar

4 tabs, left to right:

| # | Label    | iOS Icon                     | Android Icon       |
|---|----------|------------------------------|--------------------|
| 1 | Главная  | `CupertinoIcons.house`       | `Icons.home`       |
| 2 | Задания  | `CupertinoIcons.square_list` | `Icons.assignment` |
| 3 | Обучение | `CupertinoIcons.book`        | `Icons.school`     |
| 4 | Файлы    | `CupertinoIcons.folder`      | `Icons.folder`     |

- **iOS**: `CupertinoTabBar`, bg `#121212`, active `#00E676`, inactive `Colors.grey[500]`
- **Android**: `BottomNavigationBar`, bg `#121212`, selected `#00E676`, unselected
  `Colors.grey[500]`
- Uses `IndexedStack` to preserve state across tabs

### HomeTopNavigation (Top Bar)

- Row layout with horizontal padding 16, vertical 12
- **Left**: Avatar (circular, green `#00E676` border)
    - Shows activity indicator while loading
    - Shows image if avatarBytes available
    - Shows initials (first letters of first+last name) if no image
    - Shows generic person icon as fallback
- **Center**: Title (18pt, bold, white) + Late Days balance below (12pt, grey)
- **Right**: Notification bell icon
    - iOS: `CupertinoIcons.bell` in `CupertinoButton`
    - Android: `Icons.notifications_none` in `IconButton`

---

## 3. Home Tab (Главная)

Three vertically stacked sections in `SingleChildScrollView`:

### 3.1 Deadlines Section (Дедлайны)

- Header: "Дедлайны" + green badge with count
- Horizontal scrolling list of `_TaskCard` widgets
- Card: 200×115 px, 1px colored border (color = task state)
    - Exercise name: 14pt bold white, 1 line truncated
    - Course name: 12pt grey, 1 line truncated
    - Deadline: time icon + formatted date, red if overdue
    - Status: colored pill-shaped badge
- Shows up to 5 tasks in states: backlog, inProgress, revision, rework
- Sorted by deadline
- Empty: "Нет активных заданий" + grey checkmark icon
- Error: "Не удалось загрузить задания" + red exclamation icon
- Loading: platform activity indicator

### 3.2 Schedule Section (Расписание)

- Header: "Расписание" + formatted date (e.g., "понедельник, 23 февраля")
- Nav controls: Previous Day, Next Day, "Сегодня" buttons
- Tapping date label opens date picker (dark theme, accent `#00E676`)
- Vertical timeline with hour lines
- "Now" indicator line for today
- `_ClassCard`: time range, room, optional link icon, class title (optionally prefixed with type),
  professor name
- Empty: "Нет занятий на этот день" or "Подключите календарь в профиле"

### 3.3 Courses Section (Курсы)

- Header: "Курсы" + green badge with count
- `GridView.builder`: 2 columns, 12px spacing, aspect ratio 1.4
- `CourseCard`: category icon (circular colored bg), cleaned course name, localized category label
- Card theme based on course category color
- Empty: "Нет активных курсов" + school icon

---

## 4. Tasks Tab (Задания)

### Segment Control

- Two segments: "Active" (index 0) and "Archive" (index 1)
- Active states: backlog, inProgress, hasSolution, revision, rework, review
- Archive states: evaluated, failed, rejected

### Filters

- **Status dropdown** (`_StatusDropdown`): filter by task status, persisted in SharedPreferences
- **Course dropdown** (`_CourseDropdown`): filter by course, persisted
- **Search field** (`_TaskSearchField`): filter by name, persisted
- **Reset all** button: "Сбросить все"

### Task Cards (`_TaskListItem`)

- Exercise name (title)
- Course name (clean name)
- Status: colored label + color
- Deadline: icon + formatted date, red if overdue
- Border color = semi-transparent state color

### Filter Bottom Sheets

- iOS: `showCupertinoModalPopup` + `CupertinoPopupSurface`
- Android: `showModalBottomSheet`

---

## 5. Courses Tab (Обучение)

### Three Segments (platform-adaptive segmented control)

1. **Курсы** (Courses)
2. **Ведомость** (Grade Sheet)
3. **Зачетка** (Record Book)

### Courses Segment

- **Active Courses**: reorderable list, editing mode shows archive icon
- **Archive**: list of archived courses, editing mode shows unarchive icon (if not backend-archived)
- `_CourseListTile`: colored category icon + bg tint, clean name, category name

### Grade Sheet Segment

- `_CourseGradeTile` per course: total grade, descriptive text (Отлично/Хорошо/etc.), color-coded
  indicator
- Tapping opens `CoursePerformancePage`

### Record Book Segment

- Organized by semester, expandable cards
- Card: semester title, subject count, icon
- Expanded: regular + elective subject grades
- Each grade: subject name, assessment type, color-coded grade

---

## 6. Course Detail Page (CoursePage)

- Platform-adaptive nav bar with course name and search icon
- Search transforms nav bar into text input (iOS: Cancel button, Android: close icon)
- Content: list of themes as expandable cards
- Each theme card contains longreads
- Each longread lists associated exercises with name + deadline (red if overdue)
- Tapping navigates to `LongreadPage`
- Loading: activity indicator; Error: error message with icon

---

## 7. Longread Page

### Page Structure

- Platform-adaptive nav bar (CupertinoNavigationBar / AppBar)
- Materials listed in ListView: non-coding first, then coding

### Material Rendering

- **Markdown/HTML**: `flutter_html` package, rich text, tables, images, links
- **Code blocks**: `flutter_highlight` with `monokaiSublimeTheme`, bg `#2A2A2A`, text `#E6DB74`,
  header with language + copy button
- **Files**: `LongreadFileCard` — file name, extension, size, download progress/speed, cached
  locally
- **Coding exercises**: coding cards with description
- **Unsupported (quizzes)**: card saying tests not supported + browser open button

### Task Management (for coding exercises)

Tabbed interface:

- iOS: `CupertinoSlidingSegmentedControl`
- Android: `TabBar`

Tabs:

1. **Решение** (Solution): solution composer (URLs, file uploads), task event history
2. **Комментарии** (Comments): discussion thread, comment+attachment composer
3. **Информация** (Information): deadline, status, activity type, weight, course, theme, score,
   extra score

### Task Summary

Quick overview: level, score, extra score, status

### Search

Client-side full-text search for markdown content. Matched text highlighted, navigation buttons to
cycle matches, auto-scroll to active match.

---

## 8. Profile Page

- Platform-adaptive scaffold (CupertinoPageScaffold / Scaffold)
- **Avatar**: circular, green `#00E676` border. Shows image or initials on green bg
    - Camera icon to upload (JPG/PNG, <8MB)
    - Trash icon to delete (with confirmation dialog)
- **Info Card** (dark bg, rounded corners):
    - Full name
    - Course + education level (translated, e.g., "Бакалавриат")
    - Login
    - Telegram handle (if available)
    - Emails (with types, masked values)
    - Phones (with types, masked values)
- **Calendar (iCal) section**:
    - URL text field (masked when displaying)
    - Connect/Save button ("Подключить"/"Сохранить")
    - Edit/Disconnect options
    - Guide link: "Как получить ссылку?"
- **Logout** button in nav bar
- **Error log** link if error log file exists
- Dark bg `#121212`, white/grey text, green accents

---

## 9. Notifications Page

### Platform Navigation

- **Android**: `AppBar` + `TabBar` with tabs "Учеба" (Education) and "Другое" (Other)
- **iOS**: `CupertinoPageScaffold` + `CupertinoSlidingSegmentedControl`

### Notification Card

- Category icon (left side, determined by `_iconFor`)
- Title: bold white text
- Date: formatted `createdAt` timestamp
- Description (if not empty)
- Clickable link (label or URI)
    - Internal links → navigate to `LongreadPage`
    - External links → open in browser

### States

- Loading: platform activity indicator
- Empty: "Нет уведомлений"
- Pull-to-refresh support
- No explicit read/unread visual distinction

---

## 10. Login Page

### Initial Login Page (LoginPage)

- Logo: `assets/icons/cuIconLogo.svg`, white color filter
- Title: "Авторизация"
- Description: "Авторизуйтесь через браузер, мы сохраним сессию автоматически."
- "Как войти" section with numbered steps
- App version at bottom
- **Login button**: "Войти через браузер"
    - iOS: `CupertinoButton`, bg `#2D2D2D`, white text
    - Android: `OutlinedButton`, green `#00E676` border + text

### WebView Login Page

- Nav bar: title "Авторизация", refresh button
    - iOS: `CupertinoNavigationBar` + back button (`CupertinoIcons.back`) + refresh (
      `CupertinoIcons.refresh`)
    - Android: `AppBar` + refresh (`Icons.refresh`)
- `LinearProgressIndicator` at top: bg `Colors.grey[800]`, value color `#00E676`
- `InAppWebView` with LMS portal
- Error: red container at bottom with white text, e.g., "Не удалось подтвердить авторизацию.
  Попробуйте снова."

### Auth Wrapper

- Shows platform activity indicator while checking auth status

---

## 11. Files Tab

- List of downloaded files sorted by modification date (newest first)
- `_FileListItem`: file info, open/long-press/delete actions
- Total file count + combined size displayed
- Multi-select for deletion, delete all option
- Empty: "Нет скачанных файлов" + buttons to open rename templates or start scan

### ScanWork Page

- File name text field (default: date/time-based name)
- Buttons: "Снять камерой" / "Из галереи"
- Toggle: "Сжать изображения"
- Reorderable scanned pages list, each editable (rotate/crop)
- "Сохранить PDF" button

### File Rename Settings Page

- List of `FileRenameRule` objects
- Add via dialog: course, activity, extension, target file name
- Delete with confirmation
- Empty: "Нет шаблонов"

---

## 12. Modal Dialogs & Bottom Sheets

### Dialogs (all platform-adaptive: CupertinoAlertDialog / AlertDialog)

- **Late Days**: stepper for day selection, new deadline display, cancellation confirmation
- **File Rename**: keep original / use rule / custom name
- **Add Rename Rule**: course, activity, extension, name fields
- **Delete Confirmation**: for rename rules, avatars, etc.
- **App Update**: version info + update/ignore options
- **Error dialogs**: iOS uses CupertinoAlertDialog, Android uses SnackBar for transient errors

### Bottom Sheets

- iOS: `showCupertinoModalPopup` + `CupertinoActionSheet` or `CupertinoPopupSurface`
- Android: `showModalBottomSheet`
- Used for: late days, recent scans picker, status filter, course filter, course picker, activity
  picker

---

## 13. Loading / Error / Empty States

### Loading

- iOS: `CupertinoActivityIndicator` (radius 14, color `#00E676`)
- Android: `CircularProgressIndicator` (color `#00E676`)
- Centered in screen/section
- No shimmer/skeleton patterns

### Errors

- Inline: icon + grey text message
    - iOS: `CupertinoIcons.exclamationmark_circle` / `.exclamationmark_triangle`
    - Android: `Icons.error_outline`
- Snackbar: Android transient errors
- Dialog: critical errors

### Empty States (all have platform-adaptive icons)

| Screen         | Message                           | iOS Icon             | Android Icon           |
|----------------|-----------------------------------|----------------------|------------------------|
| Tasks          | Нет заданий по выбранным фильтрам | `check_mark_circled` | `check_circle`         |
| Deadlines      | Нет активных заданий              | checkmark            | checkmark              |
| Courses (home) | Нет активных курсов               | `book`               | `school`               |
| Schedule       | Нет занятий на этот день          | `calendar`           | `event_available`      |
| Longread       | Нет материалов                    | `folder`             | `folder_open`          |
| Files          | Нет скачанных файлов              | `folder`             | `folder_open`          |
| Gradebook      | Нет данных о зачетке              | `book_solid`         | `menu_book`            |
| Templates      | Нет шаблонов                      | `doc_text`           | `description_outlined` |
| Notifications  | Нет уведомлений                   | —                    | —                      |

---

## 14. Platform Differences Summary

| Component          | iOS                                | Android                        |
|--------------------|------------------------------------|--------------------------------|
| Scaffold           | `CupertinoPageScaffold`            | `Scaffold`                     |
| Nav Bar            | `CupertinoNavigationBar`           | `AppBar`                       |
| Bottom Nav         | `CupertinoTabBar`                  | `BottomNavigationBar`          |
| Text Field         | `CupertinoTextField`               | `TextField`                    |
| Switch             | `CupertinoSwitch`                  | `CheckboxListTile`             |
| Activity Indicator | `CupertinoActivityIndicator`       | `CircularProgressIndicator`    |
| Alert Dialog       | `CupertinoAlertDialog`             | `AlertDialog`                  |
| Bottom Sheet       | `showCupertinoModalPopup`          | `showModalBottomSheet`         |
| Action Sheet       | `CupertinoActionSheet`             | `showModalBottomSheet`         |
| Button             | `CupertinoButton` / `.filled`      | `ElevatedButton`               |
| Segmented Control  | `CupertinoSlidingSegmentedControl` | `TabBar`                       |
| Route Transition   | `CupertinoPageRoute` (horizontal)  | `MaterialPageRoute` (vertical) |
| Icons              | `CupertinoIcons.*`                 | `Icons.*` (Material)           |
