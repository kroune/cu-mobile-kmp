# CU LMS Website — JavaScript Reverse Engineering

## Overview

Complete capture and analysis of all JavaScript loaded by the CU LMS website at
`https://my.centraluniversity.ru`.

## Architecture Discovery

- **Framework**: Angular 21.1.4 (zoneless — no Zone.js)
- **Build system**: esbuild (Angular CLI, content-hashed filenames)
- **Two separate Angular applications**:
    - **Learn app** (`/learn/`): LMS core — courses, tasks, performance, gradebook, timetable
    - **Root app** (`/`): Hub — events, support cases, profile, achievements, notifications

### Learn App

- Entry: `learn/main-EVFPTRTH.js`
- Config: `learn/dynamical-config/configuration.json`
- Scripts: `learn/scripts-V24VZB7E.js` (third-party)
- Polyfills: `learn/polyfills-T4AEJMKQ.js`
- **352 JS files, 8.0 MB total** (from `/learn/` prefix; includes all static+dynamic import chain)
- Boot: fetches dynamic config → imports `AppComponent` + `appConfig` → bootstrapApplication

### Root App

- Entry: `main-NI5Y7GEG.js`
- Polyfills: `polyfills-LXTXMF5S.js`
- **504 JS files, 9.5 MB total** (from `/` prefix; full static+dynamic import chain including grants, admission, onboarding, news, dashboard, profile, error, and all sub-dependencies)

### Dynamic Configuration

```json
{
    "authUrl": "https://id.centraluniversity.ru",
    "hubAppUrl": "https://my.centraluniversity.ru",
    "adminAppUrl": "https://my.centraluniversity.ru/admin",
    "tiMeUrl": "https://time.cu.ru",
    "statistEndpointUrl": "https://api-statist.tinkoff.ru",
    "env": "prod",
    "fliptProviderUrl": "https://my.centraluniversity.ru",
    "informerScriptUrl": "https://forge-informer-module.t-static.ru/informer-web-components.js"
}
```

## Pages Visited (for chunk discovery)

### Learn App (`/learn/`)

| Page                                   | URL                                                                     |
|----------------------------------------|-------------------------------------------------------------------------|
| Actual courses                         | `/learn/courses/view/actual`                                            |
| Archived courses                       | `/learn/courses/view/archived`                                          |
| Course detail                          | `/learn/courses/view/actual/{id}`                                       |
| Week themes (accordion)                | Within course detail                                                    |
| Longread (materials)                   | `/learn/courses/view/actual/{courseId}/themes/{themeId}/longreads/{id}` |
| Longread (homework)                    | Same pattern, with task card                                            |
| Task detail (inline)                   | Opened via "Открыть задание" button                                     |
| Task solution tab                      | Default tab showing event timeline                                      |
| Task comments tab                      | Chat with reviewer                                                      |
| Task info tab                          | Metadata (deadline, status, grade)                                      |
| Syllabus accordion                     | Longread link within course                                             |
| Credit transfer accordion              | Longread link within course                                             |
| HW info accordion                      | Longread link within course                                             |
| Archived course detail                 | Extra sections: feedback, retry, final grade                            |
| Course feedback longread               | Links to external polls.tbank.ru                                        |
| Active tasks                           | `/learn/tasks/actual-student-tasks`                                     |
| Archived tasks                         | `/learn/tasks/archived-student-tasks`                                   |
| Gradebook                              | `/learn/reports/student-grade-book`                                     |
| Performance (actual, by semester)      | `/learn/reports/student-performance/actual/by-semester`                 |
| Performance (actual, without semester) | `/learn/reports/student-performance/actual/without-semester`            |
| Performance (archived)                 | `/learn/reports/student-performance/archived/by-semester`               |
| Course performance detail              | `/learn/reports/student-performance/actual/{courseId}`                  |
| Grade formula                          | `/learn/reports/student-performance/actual/{courseId}/activity`         |
| Timetable                              | `/learn/timetable`                                                      |

### Root App (`/`)

| Page                     | URL                                                         |
|--------------------------|-------------------------------------------------------------|
| Events list              | `/events`                                                   |
| Event detail             | `/events/{slug}`                                            |
| Support (active cases)   | `/case-list`                                                |
| Support (archived cases) | `/case-list/archived`                                       |
| Support case detail      | `/case-list/case/{id}`                                      |
| Events (past tab)        | `/events` (Прошедшие 173, with pagination)                  |
| Profile                  | `/profile/info`                                             |
| Achievements             | `/profile/experience`                                       |
| Edit bio                 | `/profile/experience/edit-bio`                              |
| Add experience           | `/profile/experience/add-experience` (12 achievement types) |
| Notifications            | Sidebar panel (Учеба / Другое tabs)                         |
| Dashboard                | `/dashboard` (redirects to `/learn/courses/view/actual`)    |

## Directory Structure

```
docs/web-reverse/
├── README.md                    # This file
├── raw/
│   ├── 00-index.md              # All files with URLs, sizes, types
│   ├── learn-inline-00.js       # Inline preloader script
│   ├── learn-dynamical-config.json
│   ├── learn/                   # 240 learn app JS files
│   └── root/                    # 219 root app JS files
├── analysis/                    # Per-chunk analysis docs
│   ├── learn-app-config-detailed.md
│   ├── learn-vendor-chunks-detailed.md
│   ├── learn-business-logic-1-detailed.md
│   ├── learn-ui-components-detailed.md
│   ├── learn-medium-chunks-detailed.md
│   ├── root-app-main-detailed.md
│   ├── root-app-features-detailed.md
│   ├── all-files-api-scan.md
│   └── (supplementary docs for each)
└── summary/
    ├── api-endpoints.md
    ├── data-models.md
    ├── state-management.md
    ├── routing.md
    └── discrepancies.md
```

## Methodology

1. **Login**: User authenticated via browser (Keycloak OIDC)
2. **Framework detection**: `browser_evaluate` checking `window.ng`, `[ng-version]`
3. **Page navigation**: SPA clicks to trigger lazy-loaded chunks
4. **JS tracking**: `PerformanceObserver` + `performance.getEntriesByType('resource')`
5. **Download**: `curl` with 20 parallel connections (static assets are public)
6. **Lazy chunk discovery**: Traced `import()` calls in route configs to find unloaded chunks
7. **Full import chain closure**: Both static (`from"./chunk-X.js"`) and dynamic (`import("./chunk-X.js")`) imports traced recursively until zero missing files
8. **Analysis**: Parallel Opus 4.6 subagents analyzing minified JS
9. **Completeness audit**: 4 parallel audit subagents cross-checking routes vs visits, chunks vs
   downloads, API scan vs docs

## Date Captured

2026-03-22
