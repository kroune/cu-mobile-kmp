# Learn Business Logic Analysis (Batch 1) - Supplementary

Additional findings from `chunk-NXLFTGU7.js`, `chunk-KNF5L6KI.js`, and `chunk-4SKQR4GO.js` that are not directly LMS business logic but are relevant infrastructure.

---

## 1. Statist Analytics SDK (chunk-4SKQR4GO, lines 626-679)

### Architecture

The CU LMS uses a **Tinkoff Statist SDK** (version `0.4.179`) for client-side analytics. This is a general-purpose event tracking system.

### Configuration

```
Production endpoint: https://api-statist.tinkoff.ru/gateway/v1/events
Dev endpoint:        https://api-statist.dev-tcsgroup.io/gateway/v1/events
  (stored as base64: aHR0cHM6Ly9hcGktc3RhdGlzdC5kZXYtdGNzZ3JvdXAuaW8=)
```

### Transport Behavior

- **Batching**: default batch size 100 events, push interval 5 seconds
- **Push timeout**: 30 seconds
- **Saved messages**: up to 1000 events stored in localStorage (`__statist_saved_messages`)
- **JSON size limit**: 32KB triggers immediate flush
- **Gzip compression**: enabled by default (can be disabled via `_statist_enable_gzip` localStorage flag)
- **Offline resilience**: saves to localStorage on `pagehide` event, reloads on page return
- **Outdated messages**: events older than 60 days are discarded
- **Beacon API**: used for sending when page is being unloaded (`navigator.sendBeacon`)

### Session Management

- Session ID stored in cookies
- Session timeout: 30 minutes (1800000ms) of inactivity
- Session start time tracked in cookies
- Device ID: generated from `wuid` (WebView UID) or random 32-char hex

### Event Structure

```
{
  project: string,
  name: string,
  parameters: object,
  eventParameters: {
    clientEventTimestamp: number,
    clientUploadTimestamp: number,
    sequence: number
  },
  clientParameters: {
    sessionId: string (UUID v4),
    deviceId: string,
    sessionStartTime: number,
    screenWidth: number,
    screenHeight: number,
    sdkVersion: "0.4.179",
    codegenVersion: string,
    pageUrl: string,
    referrer: string,
    // UTM parameters if collection enabled:
    utm_source, session_utm_source, collectionMethod, cookieDate
  }
}
```

### Error Handling

- HTTP 5xx or network errors: events saved to localStorage for retry
- HTTP 4xx: error details parsed from response JSON
- Error events emitted via internal event emitter

---

## 2. Feedback/Claims API (chunk-4SKQR4GO, lines 803-814)

### API Client (class `Mu`)

```
Environment-based URLs:
  Content API: configured per env (Su.dev / Su.prod)
  Feedback API: configured per env (Tu.dev / Tu.prod)

Methods:
  getContent(params) -> POST to contentApiUrl with auth headers
  sendFeedback(params) -> POST to feedbackApiUrl + "/claim"
  sendClaim(params) -> POST to feedbackApiUrl + "/claim"

Headers include:
  meta: from config provider
  timestamp: Yt() function
  Custom auth header generation via jr()

Retry logic:
  On failure, retries with incremental delay
  Up to configured max retries
```

---

## 3. Video Player System (chunk-NXLFTGU7, lines 1038-1305)

### Player Architecture

The video player is a full-featured media player (TCS/Tinkoff video platform):

**Player types:**
- HTML5 native player
- Dash protocol player
- HLS protocol player

**Core subsystems:**
- `StateProvider`: Tracks player state (isPlaying, isBuffering, isReady, isEnded, etc.)
- `EventBus`: Central event system for player lifecycle
- `SourceController`: Manages video stream URLs and refresh tokens
- `BufferController`: Buffer management
- `SubtitleController`: Subtitle handling
- `FullscreenController`: Fullscreen mode
- `PictureInPictureController`: PiP support
- `LoadController`: Content loading orchestration
- `QualityController`: Quality selection (auto, 480p, 720p, 1080p, 4K)
- `ThumbnailsController`: Video thumbnails/preview
- `ViewportController`: Viewport/resize handling

**Player States:**

```
State {
  isPlaying: boolean
  isPlayRequested: boolean
  isBuffering: boolean
  isReady: boolean
  isLoaded: boolean
  isEnded: boolean
  isBusy: boolean
}
```

**Key Events (enum `F`):**
- Play, Playing, Pause, Canplay, CanplayThrough, LoadStart
- QualityChangeRendered, ReInit, Cleared, StateUpdated
- EmptyBuffer, Error, PlayerCreated, Init

**Error Handling:**
```
ErrorSeverity:
  AutoRecoverable -> auto-recover, emit error event
  Normal -> if playing, wait for empty buffer then emit; otherwise emit immediately
```

**Quality Resolution:**
```
getQualityLabel(height):
  >= 2160 -> "4K"
  otherwise -> "{height}p"
```

### Video Stream & Thumbnails

```
SourceController:
  getVideoStream() -> fetches video URL for configured protocol (dash/hls)
  refreshToken -> auto-refreshes video URL before expiration
  thumbnails -> fetched if feature flag enabled
```

### Player Pool (chunk-4SKQR4GO, line 846)

```
PlayerPool:
  Maximum pool size: configurable (Hu constant)
  create(config) -> creates or reuses player instance
  release(player) -> returns to pool if not full, else destroys
  destroy() -> destroys all pooled players
  extend(extensions) -> add player extensions
```

---

## 4. OpenFeature Feature Flags (chunk-NXLFTGU7, lines 273-365)

Two implementations found (likely client-side and server-side wrappers):

### Client API

```
OpenFeatureClient:
  getBooleanValue(key, defaultValue, context) -> Promise<boolean>
  getStringValue(key, defaultValue, context) -> Promise<string>
  getNumberValue(key, defaultValue, context) -> Promise<number>
  getObjectValue(key, defaultValue, context) -> Promise<object>

Provider interface:
  metadata: { name: string }
  status: "NOT_READY" | undefined
  initialize(context) -> Promise
  onClose() -> Promise
  resolveBooleanEvaluation(key, default, context) -> { value, variant, reason }
```

### Resolution Reasons

```
StandardResolutionReasons:
  CACHED -> resolved from cache
  UNKNOWN -> default/fallback
```

### Caching

```
Cache client:
  Uses SHA256 hash of context for cache keys
  Format: "{flagKey}_{sha256(context)}"
  Supports get/set operations
  On cache miss: fetches and stores result
```

### Known Feature Flag

```
zr.Thumbnails -> Controls video thumbnail generation
```

---

## 5. Angular Core Patterns (chunk-KNF5L6KI)

### Signal-based Reactivity (Angular 21)

This chunk contains the Angular core runtime. Key patterns used in the LMS:

**Signals:**
- `signal(initialValue)` -- writable signal
- `computed(() => expr)` -- derived signal
- `effect(() => { ... })` -- side-effect runner

**Signal Utilities (from PODCHSSJ usage):**
- `x.required()` -- required input signal
- `x(defaultValue)` -- optional input signal with default
- `xe(initialValue)` -- writable signal (model input)
- `S(() => ...)` -- computed signal
- `se(initialValue)` -- writable signal
- `H()` -- output event emitter

**Resource API (line 2459):**
```
httpResource / resource pattern:
  state: { extRequest, status, previousStatus, stream }
  status transitions: "idle" -> "loading" -> "loaded" | "error"
  Supports pending tasks tracking
```

### Dependency Injection Patterns

```
T(token) -> inject(token)
K -> Injector
et -> DestroyRef
Le -> Base URL token (InjectionToken)
ve -> HttpClient
```

### Lifecycle Management

```
U() -> takeUntilDestroyed (auto-unsubscribe on component destroy)
ot(() => { ... }) -> effect with auto-cleanup
Y(observable, options) -> toSignal (convert Observable to Signal)
```

---

## 6. Markdown/Rich Text Editor (chunk-PODCHSSJ, lines 192-327)

### Editor Configuration (cu-lms-editor)

**Available tools** (filtered from default Tui Editor tools):
- Excludes: Anchor, Image, Code, Color, Hilite, CellColor
- Includes: Undo, Quote, Redo, Link, Attach, Sub, Sup, TeX, HR, Clear, GroupAdd/Remove, DetailsAdd/Remove, TextAlign variants, Lists, Indent/Outdent, FontSize, InsertTable, TableCellMerge/Split, AddRowTable, Bold/Italic/Underline/Strike, TextColor, Highlight

**Code Block Languages:**
```
Bash, C, C++, CSS, Go, HTML, Java, JavaScript, JSON, Kotlin, Python, SQL, TypeScript, YAML
```

**Custom Background Colors (for rich text):**
22 named colors including: Negative, Neutral, Positive, Expert blue/orange, Illusion, Mauve, Optimistic lime/yellow, Sure blue/tiny -- each with pale (24% opacity) and medium (7A% opacity) variants.

**Custom Text Colors:**
15 named colors including: Accent, Secondary, Additional, Negative, Primary, Positive, and same brand colors as backgrounds.

---

## 7. Click Context Resolution (chunk-4SKQR4GO, lines 171-175)

### LMS Click Context Factory (class `Fn`)

Builds analytics context by resolving from multiple sources:

```
getClickContext$(injector, overrides?):
  1. Course: from CourseOverviewService or overrides -> { courseId, courseName }
  2. Theme: from ThemeService or overrides -> { themeId, themeName }
  3. Longread: from LongreadService or overrides -> { longreadId, longreadName }

  Combined via combineLatest, emits once (take(1))
  Returns merged object of all context fields
```

---

## 8. Exercise Preview Component Details (chunk-4SKQR4GO, lines 138-170)

### Deadline Chip Logic

```
if deadline is null: return null (no chip shown)

if deadline passed:
  status="error", text="Дедлайн прошел"

if days remaining > 0:
  status="success", text="Осталось N дней" (with plural forms)

if hours remaining > 0 (less than 1 day):
  status="success", text="Осталось N часов"

if less than 1 hour:
  status="success", text="Осталось меньше часа"
```

### Open Date Chip Logic

```
if startDate is set:
  if hours until open > 0:
    "Откроется через N часов"
  else:
    "Откроется {date} в {time}"
else:
  "Откроется в скором времени"
```

### Timer Display in Preview

```
if timer is set and deadline not passed:
  "На решение дается [N ч] M мин"
  For non-quiz exercises: also shows "1 попытка"
```

### Attempts Display in Preview

```
if quiz exercise:
  if attemptsLimit is null: "Неограниченное количество попыток"
  else: "Всего N попыток" (with plural forms)
```

---

## 9. Solution Upload Flow for Coding Tasks

### Detailed Form Submission (cu-student-task-coding, PODCHSSJ lines 473-478)

```
1. User fills form:
   - solutionUrl: URL input with validation
   - attachments: file upload (max 5 files, 1GB each)
   - Cross-validation: at least one must be provided

2. On submit click:
   a. Send analytics click event
   b. If form invalid -> show validation errors, return
   c. Map form to DTO:
      - solutionUrl (if present)
      - attachments mapped via ue.mapToContent
   d. Emit submitTask with:
      {
        taskId: task.id,
        answer: mappedDTO,
        successMessage: "Ты можешь изменить его до {deadline}",
        loadEndCallback: () => {
          changingAnswer.set(false)
          form.enable()
        }
      }
   e. Mark form as pristine
   f. Disable form (while submitting)

3. After submission:
   - Solution shown in read-only view (cu-student-task-coding-solution)
   - "Изменить решение" button available until deadline
   - Hint: "Можно менять решение до дедлайна, после -- отправим его на проверку"
```

---

## 10. Quiz Task Navigation

### Player Navigation ($a component)

```
Navigation buttons:
  - "Предыдущий вопрос" (Previous) -- shown if questionIndex > 0
  - "Следующий вопрос" (Next) -- shown if questionIndex < totalQuestions - 1
  - "Завершить тест" (Complete test) -- shown:
      a. As secondary button alongside Next (if canSubmit and not last question)
      b. As primary button (if canSubmit and last question)

Mobile: all buttons take full width (flex-basis: 100%)
```

### Question Index Navigation (via cu-task-questions-nav)

```
- Sidebar/secondary section showing all question numbers
- Two-way bound questionIndex
- Allows jumping to any question directly
```

### Attempt Selector

```
When isVisibleAttemptsSelector (function Ko):
  Shows attempt dropdown in secondary section
  Label: "Попытки, засчитывается последняя/лучшая"
  Initial selection: evaluatedAttemptId ?? lastAttemptId
  On change: emits attemptChange -> loads cached task with that attempt's data
```

---

## 11. Evaluation Feedback Flow

### Visibility Conditions

```
showEvaluationFeedbackButton =
  hasPollingRole (both SubmitPoll AND ViewPoll) AND
  closedEndViewTaskId === task.id AND
  task.state === Evaluated AND
  task.reviewer is present AND
  (isCodingTask OR (isQuizTask AND has OpenText/SingleChoice/MultipleChoice questions)) AND
  NOT evaluationFeedbackPosted
```

### Dialog Flow

```
1. User clicks "Оценить" button on task info block
2. Opens dialog with Pa component:
   - Title: "Как тебе проверка?"
   - Content: feedback form
   - Non-dismissible, closeable
   - Size: auto

3. Form:
   - Score: smiley face selection (bad=red, neutral=yellow, good=green)
   - Details: radio list (only for bad/neutral scores)
   - Comment: textarea (for "Other" details or "good" score)

4. On submit:
   - Validates form
   - Calls postEvaluationFeedback$(feedback, taskId)
   - POST /polls with { score, details, comment, entityId, entityType: "task" }
   - On success: sets evaluationFeedbackPosted = true
```

---

## 12. Key Imported Symbols Cross-Reference

This maps minified symbols to their likely meaning based on usage context:

| Symbol | Likely Meaning |
|--------|---------------|
| `G` | TaskState enum |
| `ft` | ExerciseMaterialType enum |
| `yt` | QuestionType enum |
| `St` | EvaluationStrategy enum |
| `ut` | FeedbackScore enum |
| `gt` | FeedbackDetails enum |
| `Fn` | SkillLevel enum |
| `wt` | ButtonAppearance enum |
| `Le` | Base URL InjectionToken |
| `ve` | HttpClient |
| `K` | Injector |
| `et` | DestroyRef |
| `Ht` | RolesService |
| `He` | TaskLoader service |
| `Ge` | AttemptApiService |
| `Et` | AnalyticsService (LMS click tracking) |
| `Io` | LateDaysService |
| `Oe()` | DateFormatConfig factory |
| `Ut` | TaskStateForStudentTexts map |
| `Pe` | StateChipClassMap (state -> appearance) |
| `Z(task)` | isQuestionsTask(task) |
| `je(task)` | isCodingTask(task) |
| `Ue(solution)` | hasSolution(solution) |
| `Yt(state, task)` | isInState(state, task) |
| `Ho(task)` | isDeadlinePassed(task) |
| `Wo(task)` | isTaskWaiting(task) |
| `Jo(task)` | isCloseButtonDisabled condition |
| `Ko(task)` | isVisibleAttemptsSelector(task) |
| `Xo(value)` | isEmpty(value) |
| `Tt(value)` | isPresent/isDefined(value) |
| `_t(task)` | buildLmsClickContext(task) |
| `Wt` | ActionState class (async op wrapper) |
| `Ze` | CountdownTimer class |
| `Lo` | showFormValidationErrors utility |
| `Kt` | openConfirmDialog utility |
