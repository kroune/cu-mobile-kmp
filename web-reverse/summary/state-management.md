# State Management -- CU LMS Website

Extracted from minified JavaScript source files of `https://my.centraluniversity.ru`.
Angular 21.1.4 apps using a mix of RxJS observables, Angular Signals, and service-based caching.

---

## 1. Overall Architecture

The website uses **no centralized store** (no NgRx, no Akita). State management relies on:

1. **Service-level BehaviorSubjects with `shareReplay`** -- most common pattern
2. **Angular Signals** (`signal()`, `computed()`, `toSignal()`) -- in newer components
3. **Generic AsyncState container** -- reusable wrapper for async operations
4. **Tree state manager** -- for course sidebar navigation
5. **Component-scoped facades** -- orchestrate multiple services per feature

---

## 2. Service-Level Caching (shareReplay)

### Pattern

Source: `learn/chunk-H2KAKJ6J.js` (SessionService), `learn/chunk-DPBB7AEG.js` (LateDaysService), `root/chunk-ROFNVQNW.js`

The most common state pattern wraps an HTTP call in a shared, replay-cached observable:

```typescript
// SessionService
this.session$ = this.http.get("/api/account/me").pipe(
  catchError((error) => error.status === 401 ? of(null) : throwError(error)),
  shareReplay({ bufferSize: 1, refCount: false })
);

this.userToken$ = this.session$.pipe(filter(Boolean));
this.userId$    = this.userToken$.pipe(map(t => t.sub), distinctUntilChanged());
this.roles$     = this.userToken$.pipe(map(t => t.roles), shareReplay(1));
```

Key behaviors:
- `bufferSize: 1` -- caches the latest value
- `refCount: false` -- keeps the cache alive even when all subscribers disconnect
- Derived observables chain from the cached source
- No explicit invalidation in most cases (page reload is the reset)

### Services Using This Pattern

| Service | Cached Observable | Source |
|---------|-------------------|--------|
| `SessionService` | `session$`, `roles$`, `userId$` | `learn/chunk-H2KAKJ6J.js`, `root/chunk-ROFNVQNW.js` |
| `LateDaysService` | `lateDaysBalance$` | `learn/chunk-DPBB7AEG.js` |
| `CalendarEventsService` | `registrationState$`, `isTimetableRegistrationAvailable$` | `learn/chunk-CSB6UCKE.js` |
| `NotificationStateService` | `hasUnreadAnyCategory$`, `notificationCategoryStatus$` | `learn/chunk-HTCSOUNZ.js`, `root/chunk-KUQINTYY.js` |
| `StudentService` | `currentStudent$` | `root/chunk-QHDVAQXE.js` |
| `EnrolleeService` | `getCurrent$()` | `root/chunk-2RNYUVXP.js` |

### Invalidation / Reload

The `LateDaysService` has explicit reload:
```typescript
class LateDaysService {
  private trigger$ = new BehaviorSubject<void>(undefined);

  lateDaysBalance$ = this.trigger$.pipe(
    switchMap(() => this.studentApi.getMe$()),
    map(student => student.lateDaysBalance),
    shareReplay({ bufferSize: 1, refCount: true })
  );

  reload(): void {
    this.trigger$.next(undefined);
  }
}
```

---

## 3. Angular Signals

### Pattern

Source: `learn/chunk-23VGFXOH.js`, `learn/chunk-PODCHSSJ.js`, `learn/chunk-HV4RE2NY.js`

Newer components use Angular's signal-based reactivity:

```typescript
// Minified symbols:
de(value)        -> signal(value)           // writable signal
me(() => expr)   -> computed(() => expr)    // derived signal
_t(obs$)         -> toSignal(obs$)          // observable -> signal
Vu(() => expr)   -> linkedSignal(...)       // linked signal (Angular 19+)
L.required()     -> input.required()        // required signal input
L()              -> input()                 // optional signal input
at()             -> output()                // signal-based output
```

### BreakpointService

Source: `learn/chunk-HV4RE2NY.js`

```typescript
class BreakpointService {
  breakpoint = signal<Breakpoint | null>(null);    // from ResizeObserver
  isMobile = computed(() => this.breakpoint() === "mobile");
  isTabletOrLess = computed(() => this.isEqualToOrLess("tablet"));
  isTabletOrWider = computed(() => this.isEqualToOrWider("tablet"));
  isDesktopSmallOrLess = computed(() => this.isEqualToOrLess("desktopSmall"));
  isDesktopSmallOrWider = computed(() => this.isEqualToOrWider("desktopSmall"));
}
```

Breakpoint thresholds: Mobile=600px, Tablet=900px, DesktopSmall=1200px, DesktopLarge=1620px

### Component-Level Signals

In the longread editor and student task components, signals manage local UI state:

```typescript
// LongreadEditorComponent
materialWrappers = signal<MaterialWrapper[]>([]);
isReordering = signal(false);
isDragging = signal(false);

// StudentTaskComponent
task = toSignal(this.taskLoader.result$);
isLoading = computed(() => this.startTaskState().isLoading || this.submitTaskState().isLoading);
```

---

## 4. Generic AsyncState Container

Source: `learn/chunk-VITV7FAW.js`

A reusable state machine for any async operation:

```typescript
class AsyncState<TArgs, TResult> {
  result$: Observable<TResult | null>;
  error$: Observable<Error | null>;
  isLoading$: Observable<boolean>;
  state$: Observable<{ result: TResult | null; error: Error | null; isLoading: boolean }>;
  loadingEnd$: Observable<StateSnapshot>;
  firstLoadingEnd$: Observable<StateSnapshot>;

  next(args: TArgs, force?: boolean): this;  // trigger load
  setResult(result: TResult): this;          // manually set result
  reload(): this;                            // re-execute with last args
  reset(): this;                             // clear state
  getIsLoadingSync(): boolean;
  getResultSync(): TResult | null;
  getLatestArgsSync(): TArgs;
}
```

Features:
- `retryInterval` for automatic retry on error
- `isNextArgsEqualFn` for deduplication (defaults to deep-equal)
- Auto-cleanup on Angular destroy
- Used for task loading, course loading, and other data fetching

---

## 5. Action State Pattern

Source: `learn/chunk-PODCHSSJ.js`

For one-shot mutations (start task, submit answer, complete attempt):

```typescript
class ActionState {
  constructor(config: { action: (params) => Observable });

  state$: BehaviorSubject<{ isLoading: boolean; error: Error | null; data: any }>;
  loadingEnd$: Observable<void>;

  next(params, resetError?): void;   // trigger the action
}
```

Used in TaskFacade:
```typescript
this.startTaskState = new ActionState({
  action: (taskId) => this.taskApi.startTask$(taskId)
});

this.submitTaskState = new ActionState({
  action: ({taskId, answer}) => this.taskApi.submitTask$(taskId, answer)
});
```

---

## 6. Tree State Manager

Source: `learn/chunk-7ADN7E2A.js`

For the course sidebar with expandable themes/longreads:

```typescript
class TreeStateManager {
  storage$: Observable<Map<string, TreeItem>>;

  getItemByKey$(key: string): Observable<TreeItem>;
  loadChildren(config: { key: string; force: boolean }): this;
  init(): void;
  reset(): void;
}
```

Used by `CourseTreeManager` for sidebar navigation. Lazy-loads children (themes, longreads) on expand.

---

## 7. Notification Polling

Source: `root/chunk-KUQINTYY.js`, `learn/chunk-HTCSOUNZ.js`

### Stats Polling

```typescript
class NotificationStateService {
  private trigger$ = new BehaviorSubject<void>(undefined);

  checkNotificationStats$ = this.trigger$.pipe(
    switchMap(() => timer(0, 300000)),   // poll every 5 minutes
    switchMap(() => forkJoin([this.config$, this.api.stats$()])),
    map(([config, stats]) => filterCategories(config, stats)),
    shareReplay({ refCount: true, bufferSize: 1 })
  );

  hasUnreadAnyCategory$ = this.checkNotificationStats$.pipe(
    map(categories => categories.some(c => c.hasUnread))
  );

  retryNotificationStats(): void {
    this.trigger$.next(undefined);
  }
}
```

### Notification Sidebar

```typescript
class NotificationSidebarService {
  isOpen$ = new BehaviorSubject<boolean>(false);

  open(): void  { this.isOpen$.next(true); }
  close(): void { this.isOpen$.next(false); }
}
```

---

## 8. Events List Caching

Source: `root/chunk-3AKKBUTX.js`

The events list maintains separate caches for each tab/filter combination:

```
Cache keys:
  active-all       -- active tab, all events
  active-mine      -- active tab, user's events only
  archived-all     -- archive tab, all events
  archived-mine    -- archive tab, user's events only
```

Each cache stores fetched items. New pages append to existing cache.

### Events Polling Strategy

Source: `root/chunk-44YKIWJR.js`, `root/chunk-3AKKBUTX.js`

Post-onboarding optimization: if user just completed onboarding (< 5 seconds ago):
- Active tab: 6 requests at 1-second intervals
- Other tabs: 2 requests at 5-second intervals
- Normal mode: single request

Event detail has retry polling:
- Polls every 3 seconds, up to 5 retries
- Stops on first successful (non-undefined) result
- On 5th retry with HTTP error: throws

---

## 9. Task Facade (Component-Scoped State)

Source: `learn/chunk-PODCHSSJ.js`

The `TaskFacade` orchestrates all task-related state at the component level:

```typescript
class TaskFacade {
  // Data loading
  task: Signal<Task | null>;               // from AsyncState loader
  isTaskLoadingError: Signal<boolean>;
  isLoading: Signal<boolean>;              // combined loading state

  // Timer
  timeLeft: Signal<number | null>;         // ms remaining from timer service

  // Permissions
  hasSubmitRole: Signal<boolean>;

  // Attempts (quiz)
  attemptsList: Signal<Attempt[]>;

  // Mutation states
  startTaskState: ActionState;
  submitTaskState: ActionState;
  completeAttemptState: ActionState;

  // UI state
  closedEndViewTaskId: Signal<string | null>;
  evaluationFeedbackPosted: Signal<boolean>;
  showEvaluationFeedbackButton: Signal<boolean>;  // computed from multiple conditions
}
```

---

## 10. Quiz Answer Auto-Save

Source: `learn/chunk-PODCHSSJ.js`

```typescript
// In StudentTaskQuestionsComponent:
static saveDebounceTime = 500;  // ms

formControl.valueChanges.pipe(
  filter(() => isTaskSubmittable(this.task)),
  debounceTime(500),
  map(value => ({
    taskId: this.task.id,
    answer: { value, questionId: this.question.id, sessionId: this.sessionId, type: this.question.type },
    attemptId: this.currentAttemptId
  }))
).subscribe(data => this.facade.submitTask(data));
```

---

## 11. Session Management

### Cookie-Based Authentication

Source: `learn/chunk-H2KAKJ6J.js`, `root/chunk-ROFNVQNW.js`

- Session is cookie-based (httpOnly cookies set by API gateway)
- No explicit token storage in JavaScript
- `GET /api/account/me` returns user token if session cookies are valid
- On 401: `session$` emits `null`, `isAuthenticated$` emits `false`
- `UnauthorizedInterceptor` catches 401 and redirects to Keycloak sign-in
- Logout navigates to `/api/account/signout` which clears server-side session

### XSRF Protection

Source: `root/chunk-7HYSEYNL.js`, `learn/chunk-HTCSOUNZ.js`

- Cookie name: `XSRF-TOKEN`
- Header name: `X-XSRF-TOKEN`
- Skipped for GET/HEAD requests and cross-origin requests
- Automatic: Angular HttpClient reads cookie and injects header

### Route Preservation

Source: `root/chunk-OIDSCTRK.js`

- Before sign-in redirect: save current route URL
- After authentication: navigate to saved route
- Scoped storage: `"hub"` for main app, `"hub-onboarding"` for onboarding

---

## 12. HTTP Interceptor Chain

Source: `learn/chunk-HTCSOUNZ.js`, `root/chunk-OIDSCTRK.js`

### Learn App Interceptors

| Order | Interceptor | Behavior |
|-------|-------------|----------|
| 1 | `ServiceUnavailableInterceptor` | 503 -> redirect to `/maintenance` |
| 2 | `UnauthorizedInterceptor` | 401 -> redirect to Keycloak sign-in |
| 3 | `StudentHubRewriteInterceptor` | If `UseStudentHub` role, rewrite `api/hub/` -> `api/student-hub/` (29 URL patterns) |
| 4 | `HttpErrorResponseSanitizer` | Strip hostname from error messages for privacy |
| 5 | `XSRF Interceptor` | Add `X-XSRF-TOKEN` header from cookie |

### Root App Interceptors

| Order | Interceptor | Behavior |
|-------|-------------|----------|
| 1 | `CircuitBreakerInterceptor` | Fetch blocked URL patterns from config, return 503 for blocked requests. Retry with exponential backoff (300s base, 1.1x factor, max 1200s) |
| 2 | `StudentHubRewriteInterceptor` | Same as learn app |
| 3 | `InconsistentUserDataInterceptor` | Catch `title === "InconsistentUserData"`, redirect to `/error` with details |
| 4 | `HttpErrorResponseSanitizer` | Same as learn app |
| 5 | `XSRF Interceptor` | Same as learn app |

---

## 13. Error Handling Flow

Source: `learn/chunk-PODCHSSJ.js`, `learn/chunk-HTCSOUNZ.js`

### Toast/Alert Pattern

```typescript
operation$.pipe(
  handleHttpResult(injector, {
    successMessage: "Success text",
    successAlertOptions: { label: "Label", icon: "icon" },
    errorMessage: (error) => extractBadRequestDetail(error) ?? "Fallback error message",
    errorAlertOptions: { label: "Error" }
  })
)
```

Alert configuration:
```typescript
interface AlertOptions {
  autoClose: 3000;           // ms (default)
  label: string;
  closeable: true;           // default
  appearance: "success" | "error" | "info" | "warning";
}
```

### Service-Level catchError

```typescript
// Fallback to empty/default values:
getRegistrationState$().pipe(catchError(() => of({})))
stats$().pipe(catchError(() => of({ categories: [] })))
session$.pipe(catchError(err => err.status === 401 ? of(null) : throwError(err)))
```

### Sentry-Compatible Error Reporting

Source: `root/chunk-OIDSCTRK.js`

- DSN: `https://university_frontend_errorhub@error-hub.tinkoff.ru/2`
- Client: `micro-sentry.javascript.angular`
- Throttle: 1000ms between identical errors
- Sets `user.id` from session
- Enabled in: Develop, Preprod, Prod environments

---

## 14. Feature Flag Evaluation

### Flipt (Learn App)

Source: `learn-dynamical-config.json`, multiple learn chunks

- Provider URL: `https://my.centraluniversity.ru`
- Used for conditional rendering via `isEnabled` checks
- Present in: `chunk-PBADBHDX.js`, `chunk-DNFIPLFW.js`, `chunk-3CU6GOFU.js`, `chunk-NXLFTGU7.js`, `chunk-23VGFXOH.js`

### Role-Based Feature Flags

Source: `learn/chunk-ORCZSRVH.js`

Sidebar visibility controlled by Keycloak realm roles, not Flipt:
- `lms_view_sidebar_tasks_to_evaluate`
- `lms_view_sidebar_tasks_to_solve`
- `lms_view_sidebar_courses_to_manage`
- `lms_view_sidebar_courses_to_learn`

### Admin Configurations (Root App)

Source: `root/chunk-VMZTUSWJ.js`

Key-value configuration store at `/api/admin/configurations/{key}` used for runtime feature toggling (e.g., cobrowsing URL patterns).

### Server-Driven Availability

Observable-based guards check server state before enabling routes:
- `getIsAdmissionAvailableOnce$()` for admission route
- Grant availability checks for `/grants`, `/master-grants`

---

## 15. Lifecycle / Cleanup Patterns

### takeUntilDestroyed

All RxJS subscriptions in components use Angular's `takeUntilDestroyed()`:

```typescript
this.someObservable$.pipe(
  takeUntilDestroyed(this.destroyRef)
).subscribe(value => { ... });
```

This auto-unsubscribes when the component is destroyed.

### Service Scoping

- Most services: `providedIn: "root"` (singleton, shared across app)
- `TaskFacade`: provided at component level (new instance per task page)
- `RouteStorageService`: scoped via `provide("hub")` / `provide("hub-onboarding")`
