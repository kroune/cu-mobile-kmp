# Chunk Verification Report

Verification of all JavaScript chunks referenced in routing and analysis docs
against the actually downloaded files.

---

## Root App Lazy-Loaded Route Chunks (from routing.md)

14 chunks are referenced as lazy-loaded in the root app route table.

### PRESENT (4 of 14)

| Chunk               | Route                         | Status  |
|---------------------|-------------------------------|---------|
| `chunk-ISH5QSO5.js` | `/events` (eventsRoutes)      | PRESENT |
| `chunk-V2W4HQVK.js` | `/case-list` (informer cases) | PRESENT |
| `chunk-3AKKBUTX.js` | Events list sub-route         | PRESENT |
| `chunk-N2T4BRFD.js` | Event detail sub-route        | PRESENT |

### MISSING (10 of 14)

| Chunk               | Route            | Feature                             |
|---------------------|------------------|-------------------------------------|
| `chunk-NNLUD57V.js` | `/dashboard`     | Dashboard                           |
| `chunk-4ACXTDD6.js` | `/grants`        | Bachelor grants                     |
| `chunk-XBPQU6OA.js` | `/master-grants` | Master grants                       |
| `chunk-MYPYERCK.js` | `/admission`     | Admission (available)               |
| `chunk-DU2L5VZK.js` | `/admission`     | Admission form (PreStudent/Student) |
| `chunk-O5N3J4AB.js` | `/grants-2026`   | Grants 2026                         |
| `chunk-J2FWVIQV.js` | `/news`          | News list/detail                    |
| `chunk-XYNDJCEW.js` | `/profile`       | Profile                             |
| `chunk-65PL4UYO.js` | `/onboarding`    | Onboarding                          |
| `chunk-ZN6X5W4H.js` | `/error`         | Error screen                        |

---

## Learn App Lazy-Loaded Route Chunks (from routing.md)

8 chunks are referenced as lazy-loaded in the learn app route table.

### PRESENT (8 of 8) -- ALL PRESENT

| Chunk               | Route               | Feature                  | Status  |
|---------------------|---------------------|--------------------------|---------|
| `chunk-4DX4QBZY.js` | Courses view/manage | Course routes            | PRESENT |
| `chunk-3HLKTFK2.js` | Tasks               | Task routes              | PRESENT |
| `chunk-7VUA3I56.js` | Course settings     | Settings sub-routes      | PRESENT |
| `chunk-XUXMCUIT.js` | Reports             | Reports routes           | PRESENT |
| `chunk-CSB6UCKE.js` | Timetable           | Timetable                | PRESENT |
| `chunk-23VGFXOH.js` | Longread editor     | Longread editing         | PRESENT |
| `chunk-BF6EGGKK.js` | Course editor       | Course structure editing | PRESENT |
| `chunk-PODCHSSJ.js` | Student task        | Task solving UI          | PRESENT |

---

## Root App Non-Lazy Support Chunks (from analysis docs)

These are chunks referenced in `root-app-main-detailed.md` as sources for
services, guards, and infrastructure.

### PRESENT -- ALL PRESENT

| Chunk               | Purpose                        | Status  |
|---------------------|--------------------------------|---------|
| `main-NI5Y7GEG.js`  | Bootstrap entry point          | PRESENT |
| `chunk-OIDSCTRK.js` | appConfig + routes + providers | PRESENT |
| `chunk-F4UDLC3X.js` | Route enum                     | PRESENT |
| `chunk-TJTDBR6Z.js` | AppComponent                   | PRESENT |
| `chunk-N3DZQN6I.js` | HasAccessService, guards       | PRESENT |
| `chunk-ROFNVQNW.js` | SessionService                 | PRESENT |
| `chunk-BSYKMDXA.js` | Auth navigation                | PRESENT |
| `chunk-KUQINTYY.js` | Notifications, SW, cobrowsing  | PRESENT |
| `chunk-WNGRPCQW.js` | Performance monitoring         | PRESENT |
| `chunk-FGZGDGP3.js` | UTM tracking                   | PRESENT |
| `chunk-NOMFNW4V.js` | Cookie utilities               | PRESENT |
| `chunk-7E42AYBV.js` | DYNAMIC_CONFIG token           | PRESENT |
| `chunk-7HYSEYNL.js` | XSRF interceptor               | PRESENT |

### NOTE

`chunk-H2KAKJ6J.js` is referenced in `root-app-main-detailed.md` section 5.1
(SessionService source) and section 3 (auth paths). This chunk is NOT in the
root downloads but IS in the learn downloads. It is likely a shared chunk that
is identical between the two apps (both use the same SessionService and auth
infrastructure).

---

## Learn App Non-Lazy Support Chunks (from analysis docs)

These are chunks referenced in `learn-app-config-detailed.md` as sources for
services, models, enums, and infrastructure.

### PRESENT -- ALL PRESENT

| Chunk               | Purpose                                           | Status  |
|---------------------|---------------------------------------------------|---------|
| `main-EVFPTRTH.js`  | Bootstrap entry point                             | PRESENT |
| `chunk-HTCSOUNZ.js` | appConfig + routes + providers                    | PRESENT |
| `chunk-PBADBHDX.js` | AppComponent                                      | PRESENT |
| `chunk-5JTPJSK6.js` | AppRoutes enum                                    | PRESENT |
| `chunk-TZDR3JE7.js` | CourseRoutes enum                                 | PRESENT |
| `chunk-H2KAKJ6J.js` | SessionService, auth paths                        | PRESENT |
| `chunk-GD2ZCBMN.js` | LMS_APP_CONFIGURATION token                       | PRESENT |
| `chunk-NJXQAJX7.js` | LMS_BASE_API_URL                                  | PRESENT |
| `chunk-SQDJS5LQ.js` | CoursesApiService                                 | PRESENT |
| `chunk-DPBB7AEG.js` | StudentApiService, LateDaysService                | PRESENT |
| `chunk-EYVG5CRO.js` | RolesService, HasAccessService                    | PRESENT |
| `chunk-ORCZSRVH.js` | Sidebar feature flags                             | PRESENT |
| `chunk-MT6TUUT5.js` | BreadcrumbService                                 | PRESENT |
| `chunk-HV4RE2NY.js` | BreakpointService                                 | PRESENT |
| `chunk-K3PAG7Z6.js` | ScrollingService, SmoothScrollService             | PRESENT |
| `chunk-VITV7FAW.js` | AsyncState container                              | PRESENT |
| `chunk-7ADN7E2A.js` | TreeStateManager                                  | PRESENT |
| `chunk-WPLOIEQW.js` | Pagination model                                  | PRESENT |
| `chunk-P56QO3TA.js` | Route param names (CourseId, ThemeId, LongreadId) | PRESENT |
| `chunk-CC24ZP4J.js` | Route param names (TaskId)                        | PRESENT |
| `chunk-LH6TJVOX.js` | Resolvers                                         | PRESENT |
| `chunk-L6URXEQJ.js` | Roles enum                                        | PRESENT |
| `chunk-ULV6EIU5.js` | GradeBook roles                                   | PRESENT |
| `chunk-4SKQR4GO.js` | Longread material types                           | PRESENT |
| `chunk-2PCIRN5N.js` | Button/Badge appearances                          | PRESENT |
| `chunk-DNFIPLFW.js` | AppLoadedService                                  | PRESENT |
| `chunk-TWQRS7DC.js` | Unknown injected service                          | PRESENT |

---

## Summary

| Category                    | Total  | Present | Missing |
|-----------------------------|--------|---------|---------|
| Root app lazy route chunks  | 14     | 4       | **10**  |
| Learn app lazy route chunks | 8      | 8       | 0       |
| Root app support chunks     | 13     | 13      | 0       |
| Learn app support chunks    | 27     | 27      | 0       |
| **Total**                   | **62** | **52**  | **10**  |

### Missing Chunks -- All in Root App

All 10 missing chunks are lazy-loaded feature route chunks from the root (hub)
app. They cover the core feature pages that a logged-in user would access:

1. **`chunk-NNLUD57V.js`** -- Dashboard (the default landing page after login)
2. **`chunk-4ACXTDD6.js`** -- Bachelor grants
3. **`chunk-XBPQU6OA.js`** -- Master grants
4. **`chunk-MYPYERCK.js`** -- Admission (available check)
5. **`chunk-DU2L5VZK.js`** -- Admission form
6. **`chunk-O5N3J4AB.js`** -- Grants 2026
7. **`chunk-J2FWVIQV.js`** -- News
8. **`chunk-XYNDJCEW.js`** -- Profile
9. **`chunk-65PL4UYO.js`** -- Onboarding
10. **`chunk-ZN6X5W4H.js`** -- Error screen

These chunks are dynamically loaded only when a user navigates to their
respective routes. They were likely not downloaded because they require
authentication to access (the Angular app only loads them via `loadChildren`
after the auth guard passes).

### Impact Assessment

- **Learn app:** Fully covered. All 8 lazy route chunks and all 27 support
  chunks are present.
- **Root app infrastructure:** Fully covered. All 13 non-lazy support chunks
  (appConfig, auth, guards, analytics, etc.) are present.
- **Root app features:** Only events-related chunks (2) and case-list (1) are
  present. The remaining 10 feature routes (dashboard, grants, profile, news,
  onboarding, admission, error) are missing.

### Recommendation

Download the 10 missing root app chunks. They can be fetched from:

```
https://my.centraluniversity.ru/chunk-NNLUD57V.js
https://my.centraluniversity.ru/chunk-4ACXTDD6.js
https://my.centraluniversity.ru/chunk-XBPQU6OA.js
https://my.centraluniversity.ru/chunk-MYPYERCK.js
https://my.centraluniversity.ru/chunk-DU2L5VZK.js
https://my.centraluniversity.ru/chunk-O5N3J4AB.js
https://my.centraluniversity.ru/chunk-J2FWVIQV.js
https://my.centraluniversity.ru/chunk-XYNDJCEW.js
https://my.centraluniversity.ru/chunk-65PL4UYO.js
https://my.centraluniversity.ru/chunk-ZN6X5W4H.js
```

Note: These chunks are statically served and do not require authentication
to download (only their Angular routes require auth guards). They should be
accessible via direct URL.
