# Final Exhaustive Scan -- CU LMS JavaScript Files

Scanned all 536 files across:

- `learn/` (240 files)
- `root/` (296 files)
- Plus prettified versions in `learn-pretty/` and `root-pretty/`

---

## 1. All API URL Patterns

### Confirmed from direct file reads:

#### Auth & Session

| Endpoint                     | Source                                              | In api-endpoints.md? |
|------------------------------|-----------------------------------------------------|----------------------|
| `PUT /api/account/me/locale` | chunk-HTCSOUNZ.js (learn), chunk-OIDSCTRK.js (root) | YES                  |
| `GET /api/account/me`        | chunk-HTCSOUNZ.js, chunk-OIDSCTRK.js                | YES                  |

#### Hub / Profile

| Endpoint                                      | Source                                              | In api-endpoints.md? |
|-----------------------------------------------|-----------------------------------------------------|----------------------|
| `GET /api/hub/students/me`                    | chunk-QHDVAQXE.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |
| `GET /api/hub/enrollees/me`                   | chunk-2RNYUVXP.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |
| `PUT /api/hub/enrollees/me`                   | chunk-2RNYUVXP.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |
| `POST /api/hub/enrollees/onboarding/me`       | chunk-2RNYUVXP.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |
| `GET /api/hub/avatars/me`                     | chunk-OIDSCTRK.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |
| `GET /api/hub/grants/me`                      | chunk-H2UV6AAT.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |
| `GET /api/hub/grants/me/{year}/{type}/active` | chunk-H2UV6AAT.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |

#### Grants / Competitions (root app)

| Endpoint                                                               | Source            | In api-endpoints.md?     |
|------------------------------------------------------------------------|-------------------|--------------------------|
| `GET /api/hub/competitions-cases-solutions-files/me`                   | chunk-JKUM7O6Y.js | YES                      |
| `POST /api/hub/competitions-cases-solutions-files/me` (upload)         | chunk-JKUM7O6Y.js | **NEW** (upload variant) |
| `DELETE /api/hub/competitions-cases-solutions-files/me/{id}`           | chunk-JKUM7O6Y.js | YES                      |
| `POST /api/hub/master-grant-competition-cv-files/me` (upload)          | chunk-2ZEADH6T.js | **NEW** (upload variant) |
| `GET /api/hub/master-grant-competition-cv-files/me`                    | chunk-2ZEADH6T.js | YES                      |
| `GET /api/hub/competition-case-files/{id}` (download)                  | chunk-SRQGCNC5.js | YES                      |
| `GET /api/hub/unified-state-exams-files/me`                            | chunk-MO375SGD.js | YES                      |
| `DELETE /api/hub/unified-state-exams-files/me/{id}`                    | chunk-MO375SGD.js | YES                      |
| `GET /api/hub/competitions/bachelor-2024/waitlist/me`                  | (documented)      | YES                      |
| `GET /api/hub/competitions/bachelor-2024/waitlist/me/exists`           | (documented)      | YES                      |
| `GET /api/hub/competitions/master-2025/waitlist/me`                    | (documented)      | YES                      |
| `GET /api/hub/competitions/master-2025/waitlist/me/exists`             | (documented)      | YES                      |
| `GET /api/hub/competitions/grant-bachelor-2026-design/case/{id}`       | chunk-O5N3J4AB.js | YES                      |
| `GET /api/hub/competitions/grant-bachelor-2026-design/case/{id}/guide` | chunk-O5N3J4AB.js | YES                      |
| `DELETE /api/hub/competition-case-files/{id}`                          | chunk-SRQGCNC5.js | YES                      |

#### LMS Courses (learn app)

| Endpoint                                                   | Source                               | In api-endpoints.md? |
|------------------------------------------------------------|--------------------------------------|----------------------|
| Base: `/api/micro-lms`                                     | chunk-NJXQAJX7.js, chunk-XZDI5NMD.js | YES                  |
| All course CRUD endpoints under `/api/micro-lms/courses/*` | (various learn chunks)               | YES                  |

#### Calendar / Timetable

| Endpoint                                                          | Source            | In api-endpoints.md? |
|-------------------------------------------------------------------|-------------------|----------------------|
| `GET /api/micro-lms/calendar-events/slot-management/config`       | chunk-CSB6UCKE.js | YES                  |
| `PUT /api/micro-lms/calendar-events/slot-management/config`       | chunk-CSB6UCKE.js | YES                  |
| `GET /api/micro-lms/calendar-events/reports/lesson-registrations` | chunk-CSB6UCKE.js | YES                  |

#### Materials

| Endpoint                                            | Source            | In api-endpoints.md? |
|-----------------------------------------------------|-------------------|----------------------|
| `POST /api/micro-lms/materials`                     | chunk-RDZTKWJR.js | YES                  |
| `GET /api/micro-lms/materials/{id}`                 | chunk-RDZTKWJR.js | YES                  |
| `PUT /api/micro-lms/materials/{id}`                 | chunk-RDZTKWJR.js | YES                  |
| `PUT /api/micro-lms/materials/{id}/publish`         | chunk-RDZTKWJR.js | YES                  |
| `PUT /api/micro-lms/materials/{id}/return-to-draft` | chunk-RDZTKWJR.js | YES                  |
| `DELETE /api/micro-lms/materials/{id}`              | chunk-RDZTKWJR.js | YES                  |
| `GET /api/micro-lms/materials/slim`                 | chunk-RDZTKWJR.js | YES                  |
| `PUT /api/micro-lms/materials/{id}/timecodes`       | chunk-RDZTKWJR.js | YES                  |
| `SSE /api/micro-lms/materials/{id}/event-stream`    | chunk-RDZTKWJR.js | YES                  |

#### Performance

| Endpoint                                                                | Source            | In api-endpoints.md? |
|-------------------------------------------------------------------------|-------------------|----------------------|
| `GET /api/micro-lms/courses/{id}/activities`                            | chunk-2NQCAL5L.js | YES                  |
| `GET /api/micro-lms/courses/{id}/performance`                           | chunk-2NQCAL5L.js | YES                  |
| `GET /api/micro-lms/courses/{id}/exercises`                             | chunk-2NQCAL5L.js | YES                  |
| `POST /api/micro-lms/performance/courses/{id}/jobs`                     | chunk-2NQCAL5L.js | YES                  |
| `SSE /api/micro-lms/performance/courses/{id}/jobs/{jobId}/event-stream` | chunk-2NQCAL5L.js | YES                  |

#### Notifications

| Endpoint                                               | Source                                              | In api-endpoints.md? |
|--------------------------------------------------------|-----------------------------------------------------|----------------------|
| `POST /api/notification-hub/notifications/in-app`      | chunk-KUQINTYY.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |
| `GET /api/notification-hub/notifications/in-app/stats` | chunk-KUQINTYY.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |
| `POST /api/notification-hub/notifications/in-app/read` | chunk-KUQINTYY.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |

#### Events & News

| Endpoint                                                               | Source                                              | In api-endpoints.md? |
|------------------------------------------------------------------------|-----------------------------------------------------|----------------------|
| `POST /api/event-builder/public/events/list`                           | chunk-44YKIWJR.js                                   | YES                  |
| `GET /api/event-builder/public/events/slug/{slug}`                     | chunk-44YKIWJR.js                                   | YES                  |
| `GET /api/event-builder/public/events/{id}/appointment/file`           | chunk-44YKIWJR.js                                   | YES                  |
| `POST /api/event-builder/public/events/apply/{id}`                     | chunk-44YKIWJR.js                                   | YES                  |
| `GET /api/event-builder/admissions/2025/state`                         | chunk-2NELPLXO.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |
| `GET /api/event-builder/public/news/{id}`                              | chunk-EFZ55JZJ.js                                   | YES                  |
| `GET /api/event-builder/public/news/file/{id}`                         | chunk-EFZ55JZJ.js                                   | YES                  |
| `GET /api/event-builder/admissions/forms/future-student/dictionaries`  | (in VOKE7BVC.js)                                    | YES                  |
| `POST /api/event-builder/admissions/forms/future-student/files/upload` | (in VOKE7BVC.js)                                    | YES                  |

#### Admin / Configuration

| Endpoint                              | Source                                              | In api-endpoints.md? |
|---------------------------------------|-----------------------------------------------------|----------------------|
| `GET /api/admin/configurations/{key}` | chunk-VMZTUSWJ.js (root), chunk-HTCSOUNZ.js (learn) | YES                  |
| `GET /api/admin/configurations`       | chunk-VMZTUSWJ.js                                   | YES                  |
| `POST /api/admin/configurations`      | chunk-VMZTUSWJ.js                                   | YES                  |
| `DELETE /api/admin/configurations`    | chunk-VMZTUSWJ.js                                   | YES                  |

#### Analytics

| Endpoint               | Source                   | In api-endpoints.md? |
|------------------------|--------------------------|----------------------|
| Base: `/api/analytics` | chunk-OIDSCTRK.js (root) | YES                  |

#### Informer (Support System)

| Endpoint                  | Source                   | In api-endpoints.md?               |
|---------------------------|--------------------------|------------------------------------|
| `GET /api/informer/token` | chunk-V2W4HQVK.js (root) | **NEW** -- not in api-endpoints.md |

#### Sentry (Error Reporting)

| Endpoint                                            | Source                               | In api-endpoints.md?                         |
|-----------------------------------------------------|--------------------------------------|----------------------------------------------|
| Dynamic: `{protocol}://{host}/api/{project}/store/` | chunk-HTCSOUNZ.js, chunk-OIDSCTRK.js | **NEW** -- Sentry DSN API, not a CU endpoint |

---

## 2. Backtick Template URLs with /api/

Found in the following files (all confirmed via direct reads):

- chunk-HTCSOUNZ.js (learn): `/api/hub/grants/me/${year}/${type}/active`, `/api/hub/enrollees/me`,
  `/api/hub/students/me`, `/api/admin/configurations/${key}`
- chunk-OIDSCTRK.js (root): Same patterns duplicated
- chunk-O5N3J4AB.js (root): `/api/hub/competitions/grant-bachelor-2026-design/case/${id}`,
  `/api/hub/competitions/grant-bachelor-2026-design/case/${id}/guide`
- chunk-VOKE7BVC.js (root): `/api/event-builder/admissions/forms/future-student/*`
- chunk-SRQGCNC5.js (root): `/api/hub/competition-case-files/${fileId}`
- chunk-JKUM7O6Y.js (root): `/api/hub/competitions-cases-solutions-files/me/${id}`
- chunk-MO375SGD.js (root): `/api/hub/unified-state-exams-files/me/*`
- chunk-CSB6UCKE.js (learn): `/api/micro-lms/calendar-events/*`
- chunk-RDZTKWJR.js (learn): `/api/micro-lms/materials/${id}/*`
- chunk-2NQCAL5L.js (learn): `/api/micro-lms/performance/courses/${id}/jobs/${jobId}/event-stream`
- chunk-EFZ55JZJ.js (root): `/api/event-builder/public/news/${id}`,
  `/api/event-builder/public/news/file/${id}`

All confirmed already present in api-endpoints.md except `/api/informer/token`.

---

## 3. Angular Route Path Definitions

### Root App Top-Level Routes (from chunk-F4UDLC3X.js):

```
Events: "events"
News: "news"
Dashboard: "dashboard"
Profile: "profile"
InformerCaseList: "case-list"
Grants: "grants"
MasterGrants: "master-grants"
Grants2026: "grants-2026"
Onboarding: "onboarding"
Admission: "admission"
Error: "error"
```

### Root App Routing (from chunk-OIDSCTRK.js):

```
/                          -> Redirect to dashboard or onboarding
/onboarding                -> OnboardingComponent (chunk-65PL4UYO.js)
/dashboard                 -> DashboardComponent (chunk-6Z4MFTQ5.js)
  /dashboard (enrollee)    -> EnrolleeDashboardComponent (chunk-FPXM5OVY.js)
  /dashboard (student)     -> redirect to LMS app
/grants                    -> hubGrantsRoutes (chunk-4ACXTDD6.js)
/master-grants             -> masterGrantsRoutes (chunk-XBPQU6OA.js)
/grants-2026               -> grant2026Routes (chunk-O5N3J4AB.js)
/profile                   -> profileRoutes (chunk-XYNDJCEW.js)
/events                    -> eventsRoutes (chunk-ISH5QSO5.js)
/case-list                 -> HubInformerFeatureCaseListComponent (chunk-V2W4HQVK.js)
/error                     -> ErrorScreenComponent (chunk-ZN6X5W4H.js)
```

### Root App - Profile Sub-routes (from chunk-XYNDJCEW.js):

```
/profile (Student):
  /profile/general-info     -> StudentGeneralInfoComponent
  /profile/experience       -> StudentExperienceTabComponent
  /profile/experience/add-experience   -> StudentExperienceAddComponent
  /profile/experience/edit-experience/:id -> StudentExperienceEditComponent
  /profile/experience/edit-bio -> StudentEditBioComponent

/profile (Enrollee):
  /profile/general-info     -> EnrolleeInfoComponent
  /profile/certificates     -> CertificatesTabComponent (feature-flagged: showEnrolleeCertificatesTab)
```

### Root App - Events Sub-routes (from chunk-ISH5QSO5.js):

```
/events                    -> HubEventsFeatureEventsListComponent
/events/:slug              -> HubEventsFeatureEventViewComponent
```

### Root App - Grants Sub-routes (from chunk-4ACXTDD6.js):

```
/grants/apply              -> GrantsApplyComponent (chunk-MO375SGD.js)
/grants/apply-success/:track -> SuccessScreenComponent (chunk-PKKFQ44O.js)
/grants/increase           -> IncreasePageComponent (chunk-FSQRZ63J.js)
/grants/increase/success   -> SuccessScreenComponent
/grants/increase/current   -> IncreasePageComponent
/grants/increase/:id       -> IncreasePageComponent
/grants/contest            -> GrantContestComponent (chunk-6KXFKLYL.js)
/grants/contest/business-game-registration -> BusinessGameRegistrationComponent (chunk-YES6FLQ5.js)
```

### Root App - Admission Sub-routes (from chunk-MYPYERCK.js):

```
/admission/bachelor        -> bachelorRoutes (chunk-4CTIBQ2Q.js)
/admission/master          -> masterRoutes (chunk-Q7ANKZX3.js)
/admission/dpo-master      -> dpoMasterRoutes (chunk-RVZ2U5C6.js)
```

### Root App - Dashboard Sub-routes (from chunk-NNLUD57V.js):

```
/dashboard (enrollee)      -> EnrolleeDashboardComponent
/dashboard (student/other) -> redirects to LMS app URL
```

### Learn App Routes (from chunk-HTCSOUNZ.js, minified - inferred from path:"..." patterns):

```
Learn app is served at /learn/ subdirectory with routes including:
/courses                   -> Course list
/courses/:id               -> Course detail
/courses/:id/overview      -> Course overview
/courses/:id/performance   -> Student performance
/courses/:id/activities    -> Course activities
/themes/:id                -> Theme detail
/longreads/:id             -> Longread detail
/tasks                     -> Task list
/tasks/:id                 -> Task detail
/gradebook                 -> Gradebook
/timetable                 -> Timetable
/settings                  -> Settings
```

---

## 4. Admin Endpoints

No `/admin/` URL paths found in any file (the admin API is at `/api/admin/configurations/`, not
`/admin/`).

---

## 5. WebSocket / SSE Endpoints

| Type | Endpoint                                                            | Source                                                                    |
|------|---------------------------------------------------------------------|---------------------------------------------------------------------------|
| SSE  | `/api/micro-lms/materials/{id}/event-stream`                        | chunk-RDZTKWJR.js (learn), uses EventSource-like pattern via `B()` helper |
| SSE  | `/api/micro-lms/performance/courses/{id}/jobs/{jobId}/event-stream` | chunk-2NQCAL5L.js (learn), same helper                                    |

**No WebSocket endpoints found** (no `wss://` or raw `websocket` references).

The SSE helper function `B()` (from chunk-DR2JNUWK.js) creates an `EventSource` connection. Both SSE
endpoints were already documented.

---

## 6. External Service URLs

### CU Infrastructure

| URL                                                                                                          | Purpose                                         | Source                                              |
|--------------------------------------------------------------------------------------------------------------|-------------------------------------------------|-----------------------------------------------------|
| `https://id.centraluniversity.ru`                                                                            | Keycloak OIDC auth                              | Dynamic config `authUrl`                            |
| `https://my.centraluniversity.ru`                                                                            | Main site, Flipt provider                       | Dynamic config `fliptProviderUrl`                   |
| `https://t.me/hello_centraluniversity_bot`                                                                   | Telegram support bot (Enrollee/PreStudent only) | chunk-XZTVS2TN.js (root), chunk-HTCSOUNZ.js (learn) |
| `https://note.cu.ru/space/dff5a22f-c6bf-4db2-9138-b7f830e4e921/article/db3f475e-b70a-4423-abc0-b030db55c80b` | Student Handbook (Note.cu.ru)                   | chunk-OIDSCTRK.js                                   |
| `https://static.centraluniversity.ru/documents/social/cv_CU.pdf`                                             | CV template (design school)                     | chunk-2ZEADH6T.js                                   |
| `https://static.centraluniversity.ru/documents/social/portfolio_CU.pdf`                                      | Portfolio guide                                 | chunk-2ZEADH6T.js                                   |

### Tinkoff/T-Bank Services

| URL                                                      | Purpose                        | Source                               |
|----------------------------------------------------------|--------------------------------|--------------------------------------|
| `https://api-statist.tinkoff.ru`                         | Tinkoff analytics (production) | chunk-23VGFXOH.js, chunk-SIQ3MUTR.js |
| `https://api-statist.dev-tcsgroup.io` (base64-encoded)   | Tinkoff analytics (test)       | chunk-23VGFXOH.js                    |
| `https://statist-web-sdk.t-static.ru/v{major}/client.js` | Statist SDK loader             | chunk-23VGFXOH.js, chunk-SIQ3MUTR.js |
| `https://cobrowsing.tbank.ru/cdn`                        | Cobrowsing (production)        | chunk-KUQINTYY.js                    |
| `https://cobrowsing-qa.tcsbank.ru/static/customer`       | Cobrowsing (QA)                | chunk-KUQINTYY.js                    |
| `https://widget.teletype.app/init.js`                    | Teletype chat widget           | chunk-V2W4HQVK.js                    |
| `https://cfg.tbank.ru`                                   | Remote config (production)     | chunk-NXLFTGU7.js                    |
| `https://cfg-stage.dev-tcsgroup.io`                      | Remote config (dev)            | chunk-NXLFTGU7.js                    |

### Video Platform (T-Bank Internal)

| URL                                                                                                    | Purpose                           | Source            |
|--------------------------------------------------------------------------------------------------------|-----------------------------------|-------------------|
| `https://vp-content-api.t-pulse.ru/v1/`                                                                | Video platform content API (prod) | chunk-4SKQR4GO.js |
| `https://vp-content-api-http.vp-stage-public.internal.ya-ruc1-b-cloud-test-wl1.dev.k8s.tcsbank.ru/v1/` | Video platform (dev)              | chunk-4SKQR4GO.js |
| `https://vp-feedback-api.tbank.ru/v1/`                                                                 | Video platform feedback (prod)    | chunk-4SKQR4GO.js |
| `https://vp-feedback-http.vp-stage-public.internal.ya-ruc1-b-cloud-test-wl1.dev.k8s.tcsbank.ru/v1/`    | Video platform feedback (dev)     | chunk-4SKQR4GO.js |

### Angular / Third-Party

| URL                                                                               | Purpose                         | Source                               |
|-----------------------------------------------------------------------------------|---------------------------------|--------------------------------------|
| `https://angular.dev/best-practices/security#preventing-cross-site-scripting-xss` | Angular security docs reference | chunk-KNF5L6KI.js, chunk-E4YLUPE4.js |
| `https://github.com/zloirock/core-js/...`                                         | Core.js license reference       | polyfills                            |

---

## 7. Feature Flag / Permission Names

### LMS Permissions (learn app -- from dedicated chunk files):

**Course permissions** (chunk-JYGWQKMQ.js):

- `lms_add_course`
- `lms_view_course_to_learn`
- `lms_view_course_to_manage`
- `lms_edit_course`
- `lms_delete_course`
- `lms_view_course_events`
- `lms_publish_course`
- `lms_return_to_draft_course`
- `lms_edit_course_skill_level`
- `lms-tasks-manage-course_group`
- `lms-tasks-view-course_group`

**Theme permissions** (chunk-YBIXENKV.js):

- `lms_order_course_themes`
- `lms_add_theme`
- `lms_view_theme`
- `lms_edit_theme`
- `lms_delete_theme`
- `lms_view_theme_events`
- `lms_publish_theme`
- `lms_return_to_draft_theme`

**Longread permissions** (chunk-YBIXENKV.js):

- `lms_order_theme_longreads`
- `lms_add_longread`
- `lms_view_longread`
- `lms_edit_longread`
- `lms_preview_longread`
- `lms_delete_longread`
- `lms_view_longread_events`
- `lms_publish_longread`
- `lms_return_to_draft_longread`

**Material permissions** (chunk-T4STSLR4.js):

- `lms_order_longread_materials`
- `lms_add_material`
- `lms_view_material`
- `lms_edit_material`
- `lms_delete_material`
- `lms-tasks-publish-material`
- `lms-tasks-return_to_draft-material`
- `lms-tasks-view_draft-material`

**Task permissions** (chunk-MWYOKMK6.js):

- `lms_view_all_tasks`
- `lms_view_task`
- `lms_start_task`
- `lms_submit_task`
- `lms_reject_task`
- `lms_evaluate_task`
- `lms_grant_extra_score_for_task`
- `lms_refuse_extra_score_for_task`
- `lms_view_task_events`
- `lms_attach_reviewer_to_task`
- `lms-tasks-prolong_deadline-task`
- `lms-tasks-view_late_days-student`
- `lms-tasks-prolong_late_days-task`
- `lms-tasks-cancel_late_days-task`

**Performance permissions** (chunk-XHD4IVZT.js):

- `lms_view_performance_to_manage`
- `lms_view_performance_to_learn`
- `lms-tasks-upload_scores-performance` (chunk-2NQCAL5L.js)

**Sidebar permissions** (chunk-ORCZSRVH.js):

- `lms_view_sidebar_tasks_to_evaluate`
- `lms_view_sidebar_tasks_to_solve`
- `lms_view_sidebar_courses_to_manage`
- `lms_view_sidebar_courses_to_learn`

**Activity permissions** (chunk-4YQIACAT.js):

- `lms_add_course_activity`
- `lms_edit_course_activity`
- `lms_delete_course_activity`

### Hub/Root App Roles (chunk-N3DZQN6I.js):

- `default_teacher`
- `default_student`
- `default_enrollee`
- `lms_assistant`
- `default_pre_student`
- `default_staff`
- `lms_view_app`
- `auth_view_time`
- `ai_assistant-view-app`
- `admin_view_app`
- `admin_view_job`
- `admin_edit_user`
- `default_view_dev_feature`
- `interview_scheduler_view_interviewer`
- `interview_scheduler_edit_interviewer`
- `interview_scheduler_view_settings`
- `interview_scheduler_edit_settings`
- `interview_scheduler_view_timeslot`
- `interview_scheduler_edit_timeslot`
- `interview_scheduler_view_booking`
- `interview_scheduler_edit_booking`
- `interview_scheduler_view_candidate`
- `university_hub_view_enrollment_form`
- `university_hub_edit_enrollment_form`
- `university_hub_mmis_migration`
- `use_student_hub`

---

## 8. Micro-frontend / Module Federation References

**No loadRemoteModule, remoteEntry, or federation references found** in any files.

The architecture is NOT micro-frontend based. It is a monolithic Angular application split into two
separately-built apps:

- **Root app** (hub) at `https://my.centraluniversity.ru/`
- **Learn app** (LMS) at `https://my.centraluniversity.ru/learn/`

Both use Angular lazy loading with dynamic `import()` for code splitting (chunk files), but NOT
Module Federation.

---

## 9. Environment / Config Keys

### Injection Tokens (Angular DI tokens found):

| Token                                            | File                                 | Purpose                                   |
|--------------------------------------------------|--------------------------------------|-------------------------------------------|
| `LMS_BASE_API_URL`                               | chunk-NJXQAJX7.js                    | LMS API base URL (/api/micro-lms)         |
| `BASE_API_URL`                                   | chunk-XZDI5NMD.js                    | Same (/api/micro-lms)                     |
| `NEWS_API_BASE_URL`                              | chunk-EFZ55JZJ.js                    | News API (/api/event-builder/public/news) |
| `API_ANALYTICS_API_URL`                          | chunk-OIDSCTRK.js                    | Analytics base (/api/analytics)           |
| `TELEGRAM_SUPPORT_URL`                           | chunk-XZTVS2TN.js                    | Telegram bot link                         |
| `STUDENT_HANDBOOK_URL_TOKEN`                     | chunk-OIDSCTRK.js                    | Student handbook link                     |
| `LMS_APP_CONFIGURATION`                          | chunk-GD2ZCBMN.js                    | LMS config object                         |
| `HUB_APP_CONFIGURATION`                          | chunk-7E42AYBV.js                    | Hub config object                         |
| `FILES_UPLOADER`                                 | chunk-SYQADTYA.js                    | File upload service                       |
| `STUDENT_TASKS_FILTER_CACHE_KEY_TOKEN`           | chunk-XVW65H4W.js                    | Task filter cache key                     |
| `COURSE_ID_PROVIDER`                             | chunk-QWTWUWX3.js                    | Course ID provider                        |
| `THEME_ID_PROVIDER`                              | chunk-MYM3JJOJ.js                    | Theme ID provider                         |
| `ERROR_LOGGER_SERVICE`                           | chunk-KG6GO4K5.js, chunk-JZZFOMYH.js | Error logging                             |
| `COBROWSING_APP_ID`                              | chunk-KUQINTYY.js                    | Cobrowsing app ID                         |
| `COBROWSING_UNIT_ID`                             | chunk-KUQINTYY.js                    | Cobrowsing unit ID                        |
| `COBROWSING_ENVIRONMENT`                         | chunk-KUQINTYY.js                    | Cobrowsing env (prod/qa)                  |
| `COBROWSING_USER_ID`                             | chunk-KUQINTYY.js                    | Cobrowsing user ID                        |
| `NOTIFICATION_IN_APP_CONFIG_TOKEN`               | chunk-KUQINTYY.js                    | Notification categories config            |
| `USER_ROLES_TOKEN`                               | chunk-N3DZQN6I.js                    | User roles array                          |
| `LMS_PYTHON_COURSE_URL_TOKEN`                    | chunk-KJDK5QNK.js                    | Python course URL                         |
| `MAIN_CAMPUS_BUILDING_ON_MAP_LINK`               | chunk-FCAL6NCE.js                    | Campus map link                           |
| `NEWS_CHANNEL_URL`                               | chunk-6BRE6SRC.js                    | News channel URL                          |
| `GRANTS_APPLY_POLL_URL_TOKEN`                    | chunk-SYO7BFKQ.js                    | Grants poll URL                           |
| `EDU_URL_TOKEN`                                  | chunk-LMNSNSVG.js                    | Education URL                             |
| `LMS_URL_TOKEN`                                  | chunk-MUVZZKJU.js                    | LMS URL                                   |
| `EVENT_VIEW_ROUTE_BASE_COMMANDS`                 | chunk-4JXHGNUF.js                    | Event view route commands                 |
| `EVENTS_FEATURE_EVENT_DETAILS_CONFIG`            | chunk-3YUESOAR.js                    | Event details config                      |
| `HUB_EVENTS_FEATURE_EVENT_VIEW_NEWS_CHANNEL_URL` | chunk-3YUESOAR.js                    | Event view news URL                       |
| `PROFILE_INFO_CONFIG_TOKEN`                      | chunk-JWZ2APJP.js                    | Profile info config                       |
| `PROFILE_SUPPORTED_ISO_COUNTRY_CODES_TOKEN`      | chunk-JWZ2APJP.js                    | Supported country codes                   |
| `GRANT_CONTEST_CONFIG_TOKEN`                     | chunk-7TDT6UKZ.js                    | Grant contest config                      |
| `EDU_BACHELOR_ENROLLMENT_COURSE_ID_TOKEN`        | chunk-NNLUD57V.js                    | Bachelor enrollment course ID             |

---

## 10. Component Selectors

### CU-prefixed components (from root-pretty/chunk-OIDSCTRK.js and other files):

- `cu-language-sheet`
- `cu-navtab`
- `cu-notification-action`
- `cu-late-days-balance`
- `cu-breadcrumbs`
- `cu-navigation-link`
- `cu-layout-footer`
- `cu-mobile-menu-toggle`
- `cu-user-actions`
- `cu-user-info`
- `cu-sidebar`
- `cu-no-access`
- `cu-not-found`
- `cu-referral-form-page`
- `cu-app-preloader` (DOM element, not component)
- `cu-wuid` (localStorage key)
- `cu-navtab_expanded` (CSS class)
- `cu-container` (CSS class)
- `cu-hub` (analytics client name)

### CU-prefixed components (from learn-pretty files):

- `cu-audio-file`
- `cu-longread-material-audio`
- `cu-student-task-info`
- `cu-task-fallback`
- `cu-student-task-timer`
- `cu-task-comments`
- `cu-task-history`
- `cu-student-task-coding-form`
- `cu-student-task-coding-solution`
- `cu-student-task-info-block`
- `cu-student-task-preview`
- `cu-student-task-questions`
- `cu-student-task-coding`
- `cu-student-task-tabs`
- `cu-student-task-questions-player`
- `cu-student-task-questions-player-navigation`
- `cu-task-questions-nav`
- `cu-attempts-select`
- `cu-uploaded-files-list`
- `cu-tooltip`
- `cu-task-layout`
- `cu-task-heading`
- `cu-student-task-questions-item`
- `cu-student-task-questions-item-input`
- `cu-task-questions-item-single-choice`
- `cu-task-questions-item-multiple-choice`
- `cu-task-no-answer`
- `cu-task-questions-item-auto-evaluation-input`
- `cu-image-carousel`
- `cu-lms-editor-view`
- `cu-lms-editor`
- `cu-textarea-resize-wrapper`
- `cu-input-files`
- `cu-skill-level-view`
- `cu-video-player`

### Hub-prefixed references:

- `hub-onboarding` (Angular provider scope key)
- `hub-grants-2026-referrals-feature-referral-form-page` (text in template)

### Informer component:

- `cu-informer-feature-widget`
- `cu-hub-informer-feature-case-list`
- `cu-teletype-widget`
- `informer-widget-element` (web component / custom element)
- `informer-case-list-element` (web component)

### Design Case components:

- `cu-design-case-apply-widget`
- `cu-design-case-materials`
- `cu-design-case-solution-upload`
- `cu-design-case-start-task`
- `cu-design-case-header`
- `cu-how-to-solve-case`
- `cu-telegram-community-widget`
- `cu-master-grants-cv`
- `cu-linear-stepper`

---

## Summary: Delta from api-endpoints.md

### NEW Endpoints Not Previously Documented:

1. **`GET /api/informer/token`** -- chunk-V2W4HQVK.js (root)
    - Gets authentication token for the Informer (support/case management) system
    - Part of the Mayak (Lighthouse) support widget integration

2. **`POST /api/hub/competitions-cases-solutions-files/me`** (upload via FormData)
    - The existing doc only shows GET and DELETE; the POST upload variant was missing

3. **`POST /api/hub/master-grant-competition-cv-files/me`** (upload via FormData)
    - The existing doc only shows GET; the POST upload variant was missing

### NEW External Services Not Previously Documented:

4. **Cobrowsing service** (T-Bank):
    - `https://cobrowsing.tbank.ru/cdn` (prod)
    - `https://cobrowsing-qa.tcsbank.ru/static/customer` (QA)
    - Used for co-browsing / screen sharing support

5. **Teletype Chat Widget**:
    - `https://widget.teletype.app/init.js` -- embedded chat/support widget

6. **Video Platform** (T-Bank internal):
    - `https://vp-content-api.t-pulse.ru/v1/` (content API, prod)
    - `https://vp-feedback-api.tbank.ru/v1/` (feedback API, prod)

7. **Remote Config**:
    - `https://cfg.tbank.ru` (prod)
    - `https://cfg-stage.dev-tcsgroup.io` (dev)

8. **Student Handbook**:
    - `https://note.cu.ru/space/dff5a22f.../article/db3f475e...`

9. **Static Files**:
    - `https://static.centraluniversity.ru/documents/social/cv_CU.pdf`
    - `https://static.centraluniversity.ru/documents/social/portfolio_CU.pdf`

### NEW Route Paths Not Previously Documented:

10. **Full route tree** documented above for both root and learn apps
11. **Admission routes**: `/admission/bachelor`, `/admission/master`, `/admission/dpo-master`
12. **Informer case list**: `/case-list`
13. **Grants 2026**: `/grants-2026`

### Confirmed: No Hidden/Admin Pages

- No `/admin/` URL paths exist (the admin API is at `/api/admin/configurations/`)
- No hidden debug pages or backdoor routes found
- No Module Federation / micro-frontend architecture

### Confirmed: No Flipt API Endpoint

- The Flipt feature flag provider URL is configured as `https://my.centraluniversity.ru` but no
  explicit `/api/flipt/` endpoint was found in the code. Feature flags appear to be resolved through
  the configuration service (`/api/admin/configurations/`).
