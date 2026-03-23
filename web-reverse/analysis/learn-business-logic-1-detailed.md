# Learn Business Logic Analysis (Batch 1) - Detailed

Source files analyzed:

- `chunk-PODCHSSJ.js` (690 lines) -- **Primary**: Student task UI, services, state management, form
  validation
- `chunk-KNF5L6KI.js` (2704 lines) -- Angular 21 core runtime (DI, signals, effects, change
  detection)
- `chunk-4SKQR4GO.js` (1574 lines) -- Exercise preview component, statist analytics SDK, video
  player pool
- `chunk-NXLFTGU7.js` (1305 lines) -- Video player engine (TCS/Tinkoff video), OpenFeature feature
  flags, SHA3

---

## 1. API Endpoints

### Task Service (class `xa`, file: PODCHSSJ line 124-128)

The service injects `ve` (HttpClient) and `Le` (base URL injection token -- presumably resolves to
something like `/api/student-hub` or `/api/lms`).

| Method                        | HTTP    | URL Pattern                         | Body            |
|-------------------------------|---------|-------------------------------------|-----------------|
| `startTask$(taskId)`          | **PUT** | `${baseUrl}/tasks/${taskId}/start`  | `{}` (empty)    |
| `submitTask$(taskId, answer)` | **PUT** | `${baseUrl}/tasks/${taskId}/submit` | `answer` object |

### Feedback/Polls Service (class `ka`, file: PODCHSSJ line 119-122)

| Method                                 | HTTP     | URL Pattern                                  | Body             |
|----------------------------------------|----------|----------------------------------------------|------------------|
| `postFeedback$(data)`                  | **POST** | `${baseUrl}/polls`                           | Feedback payload |
| `checkIfPosted$(entityType, entityId)` | **GET**  | `${baseUrl}/polls/${entityType}/${entityId}` | --               |

### Attempt Service (class `ti`, accessed via `Ge` injection, PODCHSSJ line 145-154)

The attempt service delegates to an injected `Ge` service (not fully defined in these chunks), which
provides:

| Method                                   | HTTP        | URL Pattern                  | Notes                 |
|------------------------------------------|-------------|------------------------------|-----------------------|
| `startAttempt$(sessionId)`               | (delegated) | Quiz session start           | Returns `{attemptId}` |
| `submitAttempt$(attemptId, answer)`      | (delegated) | Submit single attempt answer |                       |
| `completeAttempt$(sessionId, attemptId)` | (delegated) | Finish attempt               |                       |

### Late Days Service (injected as `Io`, PODCHSSJ line 542)

| Method                                                                          | Notes                                              |
|---------------------------------------------------------------------------------|----------------------------------------------------|
| `updateLateDaysBalance$({taskId, exerciseName, courseName, taskType}, options)` | Prolong task deadline using late days              |
| `resetTaskProlongation$(taskId)`                                                | Cancel a previously applied late-days prolongation |

### Hub API (from HTCSOUNZ, cross-reference)

| Method       | HTTP    | URL Pattern                                    |
|--------------|---------|------------------------------------------------|
| Avatar       | **GET** | `/api/hub/avatars/me`                          |
| Grants       | **GET** | `/api/hub/grants/me`                           |
| Active grant | **GET** | `/api/hub/grants/me/${grantId}/${type}/active` |

### Analytics/Statist (chunk-4SKQR4GO)

| Endpoint     | Method           | URL                                                     |
|--------------|------------------|---------------------------------------------------------|
| Push events  | **POST**         | `https://api-statist.tinkoff.ru/gateway/v1/events`      |
| Dev push     | **POST**         | `https://api-statist.dev-tcsgroup.io/gateway/v1/events` |
| Content API  | via `_request()` | Configurable per environment                            |
| Feedback API | **POST**         | `${feedbackApiUrl}/claim`                               |

---

## 2. Data Model Shapes

### Task Object (inferred from template bindings and logic)

```
Task {
  id: string
  state: TaskState            // "inProgress" | "backlog" | "evaluated" | "failed" | etc.
  deadline: string (ISO date)
  startedAt: string | null    // when the task was started (for coding tasks)
  attemptStartedAt: string | null  // when the current quiz attempt started
  score: number | null
  extraScore: number | null
  scoreSkillLevel: SkillLevel | null
  currentAttemptId: string | null  // active quiz attempt ID
  evaluatedAttemptId: string | null
  lastAttemptId: string | null
  quizSessionId: string | null
  reviewer: unknown           // checked for presence via `Tt(task.reviewer)`

  solution: Solution | null {
    solutionUrl: string | null
    attachments: ContentFile[]
    answers: Answer[] | null   // for quiz-type tasks
  }

  exercise: Exercise {
    id: string
    name: string
    type: ExerciseMaterialType  // "Questions" | "Coding"
    timer: string | null        // duration string parseable by `Qt.fromString()`
    maxScore: number
    activity: { name: string } | null
    attachments: ContentFile[]

    questions: Question[]       // for quiz exercises

    settings: {
      attemptsLimit: number | null   // null = unlimited
      evaluationStrategy: EvaluationStrategy  // "Last" | "Best"
    }

    content: {
      description: string | null
      estimation: {
        startDate: string | null   // when exercise becomes available
        timer: string | null
        maxScore: number
      }
      attachments: ContentFile[]
      taskId: string | null
    }
  }

  course: {
    id: string
    name: string
  }

  theme: {
    name: string
  }
}
```

### Question Object

```
Question {
  id: string
  type: QuestionType          // "OpenText" | "SingleChoice" | "MultipleChoice" | "NumberMatch" | "StringMatch"
  score: number               // max score for this question
  content: {
    description: string | null
  }
  recommendation: string | null  // shown after auto-evaluation
}
```

### Answer Object

```
Answer {
  question: { id: string }
  // value varies by question type
}
```

### Solution DTO Mapping (class `ae`, PODCHSSJ line 157-158)

```
// mapFromDto(dto) -> form value
{
  solutionUrl: string | null
  attachments: ContentFile[] | null   // mapped via ue.mapFromContent
}

// mapToDto(formValue) -> API payload
{
  solutionUrl?: string
  attachments: ContentDTO[]           // mapped via ue.mapToContent
}
```

### Evaluation Feedback Payload (PODCHSSJ line 188)

```
EvaluationFeedback {
  score: "bad" | "neutral" | "good"
  details: "evaluationError" | "unclearComments" | "tooLongWaiting" | "lowScore" | "other" | null
  comment: string | null
  entityId: string               // task ID
  entityType: "task"
}
```

### ContentFile (mapped by `ue.mapFromContent` / `ue.mapToContent`)

```
ContentFile {
  name: string
  // other fields from content mapping utility
}
```

---

## 3. Enums / Constants

### TaskState (referenced as `G`)

| Value          | Key                      |
|----------------|--------------------------|
| `G.InProgress` | Active, being worked on  |
| `G.Backlog`    | Not yet started / queued |
| `G.Evaluated`  | Graded/scored            |
| `G.Failed`     | Failed                   |

Additional states likely exist (Submitted, Pending) but are not directly referenced in these chunks.

### ExerciseMaterialType (referenced as `ft`)

| Value          | Key                         |
|----------------|-----------------------------|
| `ft.Questions` | Quiz/test exercise          |
| `ft.Coding`    | Coding/file-upload exercise |

### QuestionType (referenced as `yt`)

| Value               | Key                |
|---------------------|--------------------|
| `yt.OpenText`       | Free text answer   |
| `yt.SingleChoice`   | Radio button       |
| `yt.MultipleChoice` | Checkbox           |
| `yt.NumberMatch`    | Numeric answer     |
| `yt.StringMatch`    | Exact string match |

### EvaluationStrategy (referenced as `St`)

| Value     | Key  | Description                |
|-----------|------|----------------------------|
| `St.Last` | Last | Last attempt result counts |
| `St.Best` | Best | Best attempt result counts |

### EvaluationFeedbackScore (referenced as `ut`)

| Value       |
|-------------|
| `"bad"`     |
| `"neutral"` |
| `"good"`    |

### EvaluationFeedbackDetails (referenced as `gt`)

| Key               | Russian text                                    |
|-------------------|-------------------------------------------------|
| `EvaluationError` | Ошибки в проверке                               |
| `UnclearComments` | Непонятные комментарии                          |
| `TooLongWaiting`  | Слишком долгое ожидание                         |
| `LowScore`        | Неверная оценка -- работа лучше, чем её оценили |
| `Other`           | Другое                                          |

### SkillLevel (referenced as `Fn`)

Has at least a `None` value. Used for `scoreSkillLevel` display.

### ButtonAppearance (referenced as `wt`)

| Key                   | Usage                |
|-----------------------|----------------------|
| `Primary`             | Main action buttons  |
| `Secondary`           | Secondary actions    |
| `Tertiary`            | Low-emphasis actions |
| `TertiaryNoPadding`   | Back navigation      |
| `TertiaryDestructive` | Cancel/reset actions |

---

## 4. Business Logic: Task State Machine

### State Determination Functions (PODCHSSJ lines 112-117)

```
isQuestions(task)     = task.exercise.type === "Questions"
getStartTime(task)   = isQuestions(task) ? task.attemptStartedAt : task.startedAt

isTimerExpired(task):
  startTime = getStartTime(task)
  if (startTime == null || task.exercise.timer == null) return null
  endTime = new Date(startTime) + parseTimer(task.exercise.timer)
  return endTime <= Date.now()

isTaskInProgress(task) = task.state === InProgress && isTimerExpired(task) !== true
isTaskScored(task)     = task.state === Failed || task.state === Evaluated || task.score != null
isTaskStarted(task)    = getStartTime(task) != null

isTaskSubmittable(task) = state is InProgress or Backlog AND timer not expired
  Specifically: [InProgress, Backlog].includes(task.state) && isTimerExpired(task) !== true

hasRemainingAttempts(task) = isQuestions(task) && task.state === InProgress && !task.currentAttemptId
  (quiz task that has been started but current attempt finished -- can start new attempt)

isTimerRunning(task) = isTaskInProgress(task) && task.exercise.timer != null
```

### Task Lifecycle Flow

```
1. BACKLOG (task not started)
   -> User clicks "Start" -> PUT /tasks/{id}/start
      -> If quiz with timer: shows confirmation dialog first
      -> Response: { quizSessionId } for quiz tasks
      -> If quiz: auto-starts first attempt via startAttempt$(quizSessionId)

2. IN_PROGRESS (task started)
   For Coding tasks:
     -> User fills form (solutionUrl + attachments)
     -> PUT /tasks/{id}/submit with solution payload
     -> Can change answer before deadline
     -> After deadline: auto-submitted for review

   For Quiz tasks:
     -> Each answer auto-saved (debounced 500ms) via submitTask
     -> User clicks "Complete test" -> completeAttempt$(attemptId, sessionId)
     -> Confirmation dialog: "You have unanswered questions" or "Are you sure?"
     -> Timer expiry: auto-submits, shows "Time expired" dialog
     -> If attempts remain: can start new attempt

3. EVALUATED / FAILED (task scored)
   -> Score displayed: score/maxScore
   -> Extra score displayed if present
   -> Skill level badge shown if not None
   -> Evaluation feedback button shown if:
       - user has polling role
       - task just transitioned to Evaluated
       - reviewer is present
       - task is coding OR quiz with Open/Single/Multiple choice questions
```

### Timer Logic (class `ii`, PODCHSSJ line 160-172)

```
setTask(task):
  if task has no timer, or timer not running, or (quiz and no current attempt):
    clear timer
    return

  endTime = new Date(getStartTime(task)) + parseTimer(task.exercise.timer)
  start countdown to endTime

Timer tick:
  Calculates remaining milliseconds
  Ticks every ~1 second (with drift correction)

Timer expiry (left$ === 0):
  Shows modal dialog: "Time expired, we saved and submitted all answers"
  For students: "Return to lesson"
  For non-students: "Return to selection"
  Dialog is non-closeable (isNoAbort: true)

Timer indicator colors:
  > 40% remaining: green (categorical-04)
  > 10% remaining: yellow (warning)
  <= 10% remaining: red (negative)
  Blinking animation at 1.2s interval
```

---

## 5. Component Architecture

### Component Hierarchy

```
cu-student-task (T4 -- main page component)
├── cu-student-task-preview (Ja -- task overview card before opening)
│   ├── Task tags (state chip, late days option)
│   ├── Task info (deadline, timer, score, attempts)
│   ├── Start/Continue button
│   └── Late days actions (prolong / reset)
│
├── [When task opened]:
│   ├── cu-student-task-questions (Za -- quiz task view)
│   │   ├── cu-task-layout
│   │   ├── cu-student-task-questions-player (Ya)
│   │   │   └── cu-student-task-questions-item (Xa)
│   │   │       ├── cu-lms-editor-view (question description)
│   │   │       ├── cu-image-carousel (question attachments)
│   │   │       ├── cu-task-questions-item-single-choice
│   │   │       ├── cu-task-questions-item-multiple-choice
│   │   │       ├── cu-task-questions-item-input (NumberMatch/StringMatch/OpenText)
│   │   │       └── cu-task-no-answer (fallback)
│   │   ├── cu-student-task-questions-player-navigation ($a)
│   │   ├── cu-student-task-timer (oi)
│   │   ├── cu-attempts-select
│   │   ├── cu-student-task-info-block (ni)
│   │   └── cu-task-questions-nav
│   │
│   ├── cu-student-task-coding (Ha -- coding task view)
│   │   ├── cu-task-coding-overview
│   │   ├── cu-student-task-coding-form (Ua)
│   │   │   ├── URL input (tui-textfield)
│   │   │   ├── cu-files (file upload, max 5 files, 1GB each)
│   │   │   └── Submit button
│   │   ├── cu-student-task-coding-solution (Ga -- read-only view)
│   │   ├── cu-student-task-timer (oi)
│   │   └── cu-student-task-info-block (ni)
│   │
│   └── cu-task-fallback (Na -- unknown exercise type)
│
└── cu-student-task-tabs (ja)
    ├── Tab: "Решение" (Solution) -- cu-task-history
    ├── Tab: "Комментарии" (Comments) -- cu-task-comments
    └── Tab: "Информация" (Information) -- cu-student-task-info
```

### Exercise Preview Component (cu-exercise-preview, chunk-4SKQR4GO line 138-170)

Shows exercise metadata in lesson/longread context:

- Open date chip (countdown or date)
- Deadline chip (days/hours remaining, or "Deadline passed")
- Timer chip ("На решение дается X мин")
- Attempts chip ("Unlimited" or "N attempts total")
- Description (markdown)
- Attachments list

---

## 6. Services & State Management

### TaskFacade (class `bi`, PODCHSSJ line 173-191)

Central orchestrator service. Provided at component level (`providedIn` component providers).

**Injected dependencies:**

- `K` -- Injector
- `He` -- Task data loader (manages HTTP requests and caching)
- `xa` -- Task API service (start/submit)
- `ii` -- Timer service
- `ka` -- Polls/feedback API service
- `Ht` -- Role/permissions service
- `ti` -- Attempt management service

**Signals (reactive state):**

- `task` -- current task data (from loader)
- `isTaskLoadingError` -- boolean signal
- `isLoading` -- combined loading state
- `timeLeft` -- from timer service
- `hasSubmitRole` -- whether user can submit
- `attemptsList` -- list of attempts for quiz
- `startTaskState` / `submitTaskState` / `completeAttemptState` -- loading states
- `closedEndViewTaskId` -- tracks which task's "end view" is open
- `evaluationFeedbackPosted` -- whether feedback poll was already submitted
- `showEvaluationFeedbackButton` -- computed from multiple conditions

**Key methods:**

- `loadTask(taskId)` -- loads task data
- `reloadTask()` -- refreshes task data
- `startTask(task)` -- starts task, chains with attempt start for quizzes
- `submitTask(data)` -- submits solution/answer
- `completeAttempt(data)` -- finishes quiz attempt
- `postEvaluationFeedback$(feedback, taskId)` -- sends feedback poll

### Attempt Service (class `ti`, PODCHSSJ line 145-154)

Manages quiz attempt lifecycle using `Wt` (action state wrapper pattern).

**State streams:**

- `completeAttemptState` / `startAttemptState` -- `{ isLoading, error }`
- `completeAttemptLoadingEnd$` / `startAttemptLoadingEnd$` -- signals when operations complete

**Error handling for completeAttempt:**

- If error is `InvalidState`: "Время истекло, ваши ответы были сохранены и отправлены на проверку.
  Обновите страницу."
- Otherwise: "При отправке решения произошла ошибка!"

### Timer Service (class `ii`, PODCHSSJ line 160-172)

Uses a `Ze` class (custom countdown timer with BehaviorSubject pattern).

- Emits milliseconds remaining via `left$`
- Handles timer end with modal dialog
- Different "return" text based on student role

### Action State Pattern (`Wt` class)

Reusable pattern for managing async operations:

```
Wt({action: (params) => Observable})
  .state$ -- BehaviorSubject with { isLoading, error, data }
  .loadingEnd$ -- emits when operation completes
  .next(params, resetError?) -- triggers the action
```

---

## 7. Form Validation

### Coding Task Form (function `wa`, PODCHSSJ line 157)

```
FormGroup {
  solutionUrl: FormControl<string | null>
    validators: [urlValidator(allowEmpty=true)]

  attachments: FormControl<File[] | null>
    validators: [
      Sa -- checks no files are in error state (yo)
      maxLength(5, "Ошибка, лимит - 5 файлов для загрузки")
    ]
}

Cross-field validator (ya):
  - At least one of solutionUrl or attachments must be provided
  - Error message: "Необходима ссылка на решение или прикрепленный файл"
```

### Evaluation Feedback Form (PODCHSSJ line 201)

```
FormGroup {
  score: FormControl<"bad"|"neutral"|"good" | null>
    validators: [required("Оцени проверку")]
    nonNullable: true

  details: FormControl<string | null>
    validators: [required("Выбери причину")]
    initially disabled
    nonNullable: true

  comment: FormControl<string | null>
    initially disabled
    nonNullable: true
}

Dynamic behavior on score change:
  If score is "bad" or "neutral":
    -> Enable details dropdown
    -> If details === "Other": enable comment + add required validator
    -> Else: disable comment
  If score is "good":
    -> Disable details
    -> Enable comment (optional, no required validator)
```

### Quiz Answer Auto-save (Za class, PODCHSSJ line 631-635)

```
Debounce: 500ms (static property saveDebounceTime)

On any form control value change:
  - Filter: only when task is submittable
  - Debounce 500ms
  - Emit: { taskId, answer: {value, questionId, sessionId, type}, attemptId }
  - Calls submitTask endpoint
```

---

## 8. File Upload Constraints

### Coding Task

```
Accepted formats: pptx, pdf, xlsx, csv, py, zip, gz, ipynb, jpeg, jpg, png,
                  mp4, sql, html, docx, md, mp3, wav, lean, tex, txt
Max file size: 1,048,576,000 bytes (1 GB)
Max file count: 5
```

### Supported File Type Categories

```
Documents: pptx, pdf, xlsx, csv, py, zip, gz, ipynb, sql, html, docx, md, lean, tex, txt
Images: jpeg, jpg, png
Video MIME types: video/3gpp2, video/3gpp, video/x-ms-asf, video/avi, video/dv,
                  video/x-flv, video/mp4, video/mpeg, video/mpg, video/x-ms-wmv,
                  video/x-matroska, video/webm, video/ogg, video/quicktime,
                  video/vnd.dlna.mpeg-tts
Audio: mp3, wav
```

---

## 9. Role-Based Access Control

### Permissions Referenced

| Permission Key                                   | Usage                                                                  |
|--------------------------------------------------|------------------------------------------------------------------------|
| `ze.SubmitTask`                                  | Controls whether "Submit" / "Save solution" buttons are shown          |
| `fo.Student`                                     | Determines "return to lesson" vs "return to selection" in timer dialog |
| `xi.SubmitPoll` (`lms-polls-submit-poll_answer`) | Required for evaluation feedback                                       |
| `xi.ViewPoll` (`lms-polls-view-poll`)            | Required for evaluation feedback                                       |
| `ze.ViewTaskEvents`                              | Controls visibility of "History" tab                                   |
| `Eo.ViewTaskComment`                             | Controls visibility of "Comments" tab                                  |

### Access Check Pattern

```typescript
hasRole$(role) -> Observable<boolean>
hasAccess$([permissions], { strategy: "every" }) -> Observable<boolean>
```

---

## 10. Route Guard: canDeactivate

The main `cu-student-task` component implements `canDeactivate`:

```
canDeactivate():
  form = studentTask.taskForm  // accessed via ViewChild
  return !form || form.pristine || form.disabled

  // Prevents navigation away if the form has unsaved changes
```

---

## 11. LMS Click Tracking

Analytics events are sent via `Et` (analytics service, `sendLmsClick`). Each event contains:

```
LmsClickContext {
  courseId, courseName,
  themeId, themeName,
  longreadId, longreadName,
  materialId,
  elementName: string,    // human-readable action name
  elementId: string,      // structured ID like "course_start_exercise_button_{exerciseId}"
  elementType: string     // category like "course_start_exercise_button"
}
```

### Tracked Events

| Action          | elementId pattern                                  | elementType                                   |
|-----------------|----------------------------------------------------|-----------------------------------------------|
| Start exercise  | `course_start_exercise_button_{id}`                | `course_start_exercise_button`                |
| Open exercise   | `course_open_exercise_button_{id}`                 | `course_open_exercise_button`                 |
| Submit solution | `course_send_solution_button_{id}`                 | `course_send_solution_button`                 |
| Change solution | `course_change_solution_button_{id}`               | `course_change_solution_button`               |
| Finish test     | `course_finish_test_button_{id}`                   | `course_finish_test_button`                   |
| Tab click       | `course_task_information_header_{id}`              | `course_task_information_header`              |
| Send comment    | `course_send_comment_exercise_{id}`                | `course_send_comment_exercise`                |
| Delete comment  | `course_delete_comment_exercise_{id}`              | `course_delete_comment_exercise`              |
| Confirm delete  | `course_confirmation_delete_comment_exercise_{id}` | `course_confirmation_delete_comment_exercise` |

---

## 12. Late Days System

### UI Flow (cu-student-task-preview, PODCHSSJ line 517-527)

```
If task has prolong late days permission AND exercise type supports it:
  Show "Перенести дедлайн" (Move deadline) button
Else:
  Show warning icon with hint "В этом задании нельзя применить Late days"

If task already has late days applied (hasCancelLateDays$):
  Show "Отменить перенос" (Cancel postponement) button

Prolong action:
  Call updateLateDaysBalance$({taskId, exerciseName, courseName, taskType}, options)
  On success: reload task

Reset action:
  Call resetTaskProlongation$(taskId)
  On success: reload task
```

### Late Days Balance Display (from HTCSOUNZ cross-reference)

```
cu-late-days-balance component:
  Displays "Late days: {balance}" with calendar icon
  Balance comes from a service's lateDaysBalance$ observable
```

---

## 13. Error Handling Patterns

### Consistent Toast/Alert Pattern (via `Gt` operator)

```
operation$.pipe(
  Gt(injector, {
    successMessage?: string | AlertOptions,
    successAlertOptions?: { label, icon },
    errorMessage: string | ((error) => string)
  })
)
```

### Error Messages (Russian)

| Context               | Message                                                                                  |
|-----------------------|------------------------------------------------------------------------------------------|
| Start task error      | "Ошибка при старте таски"                                                                |
| Save solution error   | "При сохранении решения произошла ошибка"                                                |
| Submit solution error | "При отправке решения произошла ошибка!"                                                 |
| Timer expired (quiz)  | "Время истекло, ваши ответы были сохранены и отправлены на проверку. Обновите страницу." |
| Save answer error     | "При сохранении решения произошла ошибка"                                                |

### Reload Triggers

The task is auto-reloaded after:

1. Attempt completion (completeAttemptLoadingEnd$)
2. Task start (startTaskLoadingEnd$)
3. Timer expiry (timerEnd$)
4. New attempt start (startAttemptLoadingEnd$)
