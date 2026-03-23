# CU Learn App -- Supplementary Analysis

Covers: styling, analytics, debug hints, feature flags, UI library, i18n, third-party integrations.

---

## 1. Feature Flags

### Role-Based Feature Flags (Sidebar visibility)

From `chunk-ORCZSRVH.js` -- these are Keycloak realm roles that control sidebar menu items:

| Flag Key            | Role String                          | Controls                                |
|---------------------|--------------------------------------|-----------------------------------------|
| ViewTasksToEvaluate | `lms_view_sidebar_tasks_to_evaluate` | "Tasks to evaluate" menu item (teacher) |
| ViewTasksToSolve    | `lms_view_sidebar_tasks_to_solve`    | "Tasks to solve" menu item (student)    |
| ViewCoursesToManage | `lms_view_sidebar_courses_to_manage` | "Courses to manage" menu item (teacher) |
| ViewCoursesToLearn  | `lms_view_sidebar_courses_to_learn`  | "Courses to learn" menu item (student)  |

### HasRole Pipe (`chunk-IGQL27KA.js`)

Used in templates:

```html

<ng-container *ngIf="[Role.Teacher, Role.Admin] | hasRole">
```

The pipe wraps `RolesService.hasAccess$()`.

### Access Check Strategies (chunk-EYVG5CRO.js)

```typescript
enum Strategy {
  Every  = "every",   // ALL roles required
  Some   = "some",    // ANY role sufficient (default)
  NoOne  = "noOne"    // NONE of the roles
}

// Default options:
{ strategy: "some", defaultValue: false }
```

### Flipt Feature Flags

The dynamic config includes `fliptProviderUrl: "https://my.centraluniversity.ru"`, indicating Flipt
is used for feature flags. The `isEnabled` checks in the ServiceWorkerUpdateService and potentially
other services likely come from a Flipt client. The exact flag names are in the larger chunks (lines
6-7, 14-15) that could not be fully extracted due to minification.

### Dev Feature Flag

Role `ViewDevFeature = "default_view_dev_feature"` gates access to development/experimental
features.

---

## 2. Analytics / Tracking

### Statist (Tinkoff analytics)

Config: `statistEndpointUrl: "https://api-statist.tinkoff.ru"`

From `chunk-4SKQR4GO.js`, the markdown material viewer sends analytics on link clicks:

```typescript
// On click of links inside markdown content:
analyticsService.sendLmsClick({
  elementName: linkText,
  elementId: linkHref,
  elementType: "markdown_material_link_click",
  ...clickContext
});
```

The analytics service (`bn`) and click context service (`Fn`) are injected and used to track user
interactions within longread materials.

### Informer Script

Config: `informerScriptUrl: "https://forge-informer-module.t-static.ru/informer-web-components.js"`

This loads a web component for in-app informer/survey overlays (Tinkoff Forge infrastructure).

### AppLoaded Event

`AppLoadedService` (from `chunk-DNFIPLFW.js`) sends an `appLoaded` event in
`AppComponent.ngAfterViewInit()`, marking the app as fully rendered for performance tracking.

---

## 3. UI Library / Styling

### Taiga UI v4.73.0

The app uses Taiga UI extensively:

- **Root component:** `<tui-root data-tui-version="4.73.0">`
- **Theme:** Applied via `data-tui-theme` attribute on `<html>` (lowercased theme name)
- **Scrollbar:** Custom Taiga scrollbars unless native mode is configured
- **Mobile detection:** Adds `_mobile` host class on mobile viewports

### Key Taiga UI Components Used

| Component                | Usage                                                                    |
|--------------------------|--------------------------------------------------------------------------|
| `tui-root`               | App wrapper (scroll controls, popups, dialogs, alerts, dropdowns, hints) |
| `tui-icon`               | Icon display                                                             |
| `tui-line-clamp`         | Text truncation with "Read more"                                         |
| `tui-badge-notification` | Notification badges (sizes: xs, s, m)                                    |
| `tui-segmented`          | Segmented controls (tabs)                                                |
| `tui-scroll-controls`    | Custom scrollbar overlay                                                 |
| `tui-alerts`             | Alert/toast notifications                                                |
| `tui-dialogs`            | Modal dialogs                                                            |
| `tui-dropdowns`          | Dropdown overlays                                                        |
| `tui-hints`              | Tooltip hints                                                            |
| `tuiButton`              | Button directive                                                         |
| `tuiLink`                | Link directive                                                           |
| `tuiPopup`               | Popup structural directive                                               |

### Custom CU Components (prefixed `cu-`)

| Component                             | Purpose                                                                    |
|---------------------------------------|----------------------------------------------------------------------------|
| `cu-card-icon`                        | Notification icon with read/unread state                                   |
| `cu-card-time`                        | Date/time display for notifications                                        |
| `cu-notification-card`                | Full notification card in sidebar                                          |
| `cu-notification-separator`           | "Already seen" separator                                                   |
| `cu-counter`                          | Numeric badge (sizes: s, m; appearances: primary, secondary)               |
| `cu-longread-material`                | Material renderer (markdown, video, file, audio, image, exercise)          |
| `cu-longread-material-markdown`       | Markdown content with ngx-markdown                                         |
| `cu-longread-material-video-platform` | Video player (platform videos with timecodes)                              |
| `cu-longread-material-video`          | Direct video file player                                                   |
| `cu-longread-material-file`           | File download                                                              |
| `cu-longread-material-audio`          | Audio player                                                               |
| `cu-longread-material-image`          | Image display                                                              |
| `cu-longread-material-exercise`       | Exercise renderer (Coding, Questions)                                      |
| `cu-longread-material-empty`          | Empty state placeholder                                                    |
| `cu-lms-editor-view`                  | TUI editor content viewer                                                  |
| `cu-text` (directive)                 | Typography directive (`cuText="s"`, `cuText="s-bold"`, `cuText="l"`, etc.) |
| `cu-level` (directive)                | Nesting level tracker for hierarchical components                          |

### Custom Directives

| Directive                        | Purpose                                                     |
|----------------------------------|-------------------------------------------------------------|
| `cuText`                         | Text sizing (s, s-bold, m, l, xs)                           |
| `cuLevel`                        | Hierarchical nesting level counter                          |
| `cuLimitedLinesWithHintOverflow` | Line-clamped text with tooltip on overflow                  |
| `externalRouterLink`             | Router link that supports external URLs (http/https/mailto) |
| `hasRole` (pipe)                 | Template-level role checking                                |
| `infiniteScroll`                 | ngx-infinite-scroll integration                             |

### CSS Custom Properties

```css
--sidebar-layout-content-background-color: var(--background);
--tui-viewport-x, --tui-viewport-y, --tui-viewport-height, --tui-viewport-width;
--tui-viewport-scale, --tui-viewport-vh, --tui-viewport-vw;
--tui-duration: {reducedMotion ? "auto" : "smooth"};
--cu-icon-size, --cu-icon-size-s, --cu-icon-size-xl;
--cu-size-0_375, --cu-size-0_125;
--cu-button-height-s;
```

### Breakpoints

```typescript
{
  mobile:       600,
  tablet:       900,
  desktopSmall: 1200,
  desktopLarge: 1620
}
```

Breakpoint priority values: Mobile=1, Tablet=2, DesktopSmall=3, DesktopLarge=4.

---

## 4. i18n / Localization

### Locale: Russian (`ru`)

- Set in `main-EVFPTRTH.js` via `registerLocaleData(ruLocale, "ru")`.
- `$localize` is used throughout for translated strings.
- Date formats use `DatePipe` with format strings from `chunk-MT6TUUT5.js`.

### Date Format Constants

**Russian (default):**

```
dateFormatView              = "dd.MM.yyyy"
dateTimeFormatView          = "dd.MM.yyyy HH:mm"
dateTimeSeparatedFormatView = "dd.MM.yyyy, HH:mm"
dateWordTimeFormat          = "dd MMMM, HH:mm"
dateWordNoYearFormat        = "d MMMM"
timeFormat                  = "shortTime"
dateWithFullMonthFormat     = "dd MMMM yyyy"
```

**English (fallback):**

```
dateFormatView              = "M/d/yyyy"
dateTimeFormatView          = "MM/dd/yyyy h:mm a"
dateWordTimeFormat          = "MMMM dd, h:mm a"
timeFormat                  = "h:mm a"
```

### Localized Strings (samples)

| Context                           | Russian Text                                                                |
|-----------------------------------|-----------------------------------------------------------------------------|
| SW update button                  | "Обновить"                                                                  |
| SW install error                  | "Ошибка установки"                                                          |
| SW unrecoverable                  | "Неисправимая ошибка"                                                       |
| SW install error detail (mobile)  | "Произошла ошибка установки новой версии. Пожалуйста, обновите страницу..." |
| SW install error detail (desktop) | "...обновите страницу с помощью сочетания Ctrl+Shift+R..."                  |
| Notification separator            | "Эти уведомления ты уже видел"                                              |
| Notification expand               | "Читать полностью"                                                          |
| Notification collapse             | "Свернуть"                                                                  |
| Yesterday                         | "Вчера"                                                                     |
| Late days cancel                  | "Отменить перенос"                                                          |
| Late days cancel confirm          | "Списанные late days вернутся"                                              |
| No late days                      | "Нет late days"                                                             |
| Deadline moved                    | "Дедлайн перенесен"                                                         |
| Timetable open                    | "Запись на пары"                                                            |
| Timetable closed                  | "Мои пары"                                                                  |
| Courses (sidebar)                 | "Курсы"                                                                     |
| Actual courses                    | "Актуальные"                                                                |
| Archived courses                  | "Архивные"                                                                  |
| Main page (breadcrumb)            | "Главная"                                                                   |

---

## 5. Third-Party Libraries

### Runtime Dependencies (from imports and chunk analysis)

| Library             | Version/Evidence      | Purpose                         |
|---------------------|-----------------------|---------------------------------|
| Angular             | 21.1.4                | Framework (zoneless)            |
| Taiga UI            | 4.73.0                | UI component library            |
| RxJS                | (bundled)             | Reactive programming            |
| ngx-infinite-scroll | (bundled in HTCSOUNZ) | Infinite scroll                 |
| ngx-markdown        | (bundled in 4SKQR4GO) | Markdown rendering              |
| marked.js           | (bundled in 4SKQR4GO) | Markdown parser                 |
| Lenis               | 1.3.17                | Smooth scrolling                |
| Prism.js            | (external)            | Syntax highlighting in markdown |
| KaTeX               | (optional external)   | Math rendering                  |
| Mermaid             | (optional external)   | Diagram rendering               |
| joypixels           | (optional external)   | Emoji rendering                 |
| ClipboardJS         | (optional external)   | Code block copy                 |
| deep-equal          | (bundled in VITV7FAW) | Deep object comparison          |

### Service Worker

Registered with `SwModule`/`ServiceWorkerModule`:

- Strategy: `registerWhenStable:30000` (register after stable or 30s timeout)
- Version checks every 1 hour
- Auto-activates updates on next navigation

---

## 6. Debug Hints

### Console Error Grouping

`ErrorLoggerService` (the console-based fallback) logs errors in console groups:

```
console.group("Error Report from ConsoleErrorLoggerService")
  User: ...
  Tags: ...
  Breadcrumbs: ...
  Error: ...
console.groupEnd()
```

### HttpErrorResponse Cleanup

HTTP errors have hostnames stripped:

```
"Http failure response for https://id.centraluniversity.ru/api/..."
-> "Http failure response for /api/..."
```

This means browser console errors will show clean paths without the host.

### Preloader Element

The app looks for `#cu-app-preloader` DOM element and fades it out after first navigation. If the
preloader is stuck, it means Angular routing never completed.

### Service Worker Debug

SW events are logged:

```javascript
console.error("Failed to install app version", event);
```

The SW generates nonces for operation tracking and has `OPERATION_COMPLETED` event types with
`result`/`error` fields.

### Route Data Properties

Routes can carry these data properties:

- `breadcrumb` -- static object `{ caption, icon }` or factory function
- `notAuthorisedRedirectUrl` -- redirect URL for unauthenticated users
- `styles` -- layout customization (e.g., background color override)

### Injection Tokens for Debugging

| Token Name                               | Purpose                          |
|------------------------------------------|----------------------------------|
| `LMS_APP_CONFIGURATION`                  | Dynamic runtime config           |
| `LMS_BASE_API_URL`                       | API base (`/api/micro-lms`)      |
| `BASE_AUTH_API_URL_TOKEN`                | Auth API base (`/api`)           |
| `AUTH_URL_TOKEN`                         | IdP URL (fetched from config)    |
| `APP_NAME`                               | Application name                 |
| `HUB_URL_TOKEN`                          | Hub app URL                      |
| `ADMIN_URL_TOKEN`                        | Admin app URL                    |
| `ERROR_LOGGER_SERVICE`                   | Error logger injection           |
| `ERROR_LOGGER_ENV`                       | Error logger environment         |
| `SERVICE_UNAVAILABLE_REDIRECT_URL_TOKEN` | 503 redirect                     |
| `SERVICE_UNAVAILABLE_HANDLER_TOKEN`      | 503 handler                      |
| `NOTIFICATION_IN_APP_CONFIG_TOKEN`       | Notification config              |
| `USER_ROLES_TOKEN`                       | User roles array                 |
| `DEBOUNCE_SEARCH_TOKEN`                  | Search debounce (default: 300ms) |
| `LEVEL_DIRECTIVE_PARENT_INJECTION_TOKEN` | Nesting level                    |

---

## 7. Chunk Dependency Map

The main `chunk-HTCSOUNZ.js` imports from ~70 chunks. Key dependency chains:

```
main-EVFPTRTH.js
  -> chunk-PBADBHDX.js (AppComponent)
  -> chunk-HTCSOUNZ.js (appConfig, routes, providers)
       -> chunk-L6URXEQJ.js (Roles enum)
       -> chunk-H2KAKJ6J.js (AuthPaths, SessionService)
       -> chunk-EYVG5CRO.js (RolesService, USER_ROLES_TOKEN)
       -> chunk-SQDJS5LQ.js (CoursesApiService)
       -> chunk-NJXQAJX7.js (LMS_BASE_API_URL = /api/micro-lms)
       -> chunk-VITV7FAW.js (AsyncState, date utils, deep-equal)
       -> chunk-MT6TUUT5.js (BreadcrumbService, HUB_URL_TOKEN)
       -> chunk-5JTPJSK6.js (AppRoutes enum)
       -> chunk-TZDR3JE7.js (CourseRoutes enum)
       -> chunk-XUXMCUIT.js (ReportRoutes enum)
       -> chunk-ORCZSRVH.js (SidebarFeatureFlags)
       -> chunk-ULV6EIU5.js (GradeBookRoles)
       -> chunk-CSB6UCKE.js (CalendarEventsService)
       -> chunk-DPBB7AEG.js (StudentApiService, LateDaysService)
       -> chunk-HV4RE2NY.js (BreakpointService)
       -> chunk-P4SIJYOT.js (ExternalRouterLink, ADMIN_URL_TOKEN)
       -> chunk-K3PAG7Z6.js (ScrollingService, Lenis, CanDeactivateGuard)
       -> chunk-GD2ZCBMN.js (LMS_APP_CONFIGURATION token)
       -> chunk-KG6GO4K5.js (ERROR_LOGGER_SERVICE token)
       -> chunk-2PCIRN5N.js (ButtonAppearance, BadgeAppearance, Breakpoint enums)
       -> chunk-IAHWXTKW.js (Date format constants)
       -> chunk-7ADN7E2A.js (TreeStateManager, CourseTreeManager)
       -> chunk-FLSQMGN6.js (Counter component)
       -> chunk-YEFAQ36Q.js (ResizeObserver, tuiItem directive)
       -> chunk-WPLOIEQW.js (PaginationMapper)
       -> chunk-QPPJ5JYF.js (URLSearchParams pagination helper)
       -> chunk-HYIXHTRH.js (RegistrationStatus enum)
       -> chunk-IGQL27KA.js (HasRole pipe)
       -> chunk-3CU6GOFU.js (ServiceWorker module, TuiPopup)
       -> chunk-DNFIPLFW.js (AppLoadedService, error reporting)
```

### Lazy-Loaded Feature Chunks (referenced but not analyzed)

The route configuration in lines 6-7, 14-15 of `chunk-HTCSOUNZ.js` contains `loadComponent` and
`loadChildren` calls that lazy-load feature modules. Based on the route structure, these would
include:

- Courses view/manage modules
- Tasks module
- Reports/GradeBook module
- Timetable module
- Settings module

---

## 8. Security Notes

- OIDC Authorization Code flow with `client_id=api-gateway`.
- Scopes: `openid email offline_access` (refresh token enabled).
- All API calls go through the same origin (no CORS needed for API).
- HTTP-only cookies for session management (no explicit token in JS).
- Hostname stripping in error messages prevents URL leakage in error reports.
- Student Hub URL rewriting (`api/hub/` -> `api/student-hub/`) is role-gated.
- `CanDeactivateGuard` available for forms with unsaved changes.
