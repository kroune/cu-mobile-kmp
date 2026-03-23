# Analysis of Large Learn App JS Chunks

## File-by-file Analysis

---

### 1. chunk-FBVLZLM6.js (1.8MB, 7895 lines)

**Type:** HYBRID -- Vendor library (Prism.js/highlight.js, ~70%) + Major LMS feature bundle (
task/quiz/comments, ~30%)

**Vendor Content (lines 73-1400 approx):**

- **Prism.js** -- full syntax highlighting library with all languages (C, C++, C#, Java, Python,
  Kotlin, Haskell, Scala, SQL, etc.)
- Used for code display in exercises (likely coding exercises and longread content)

**LMS Application Code (lines 1400-7895):**

**API Endpoints (all relative to `/api/micro-lms`):**

| Endpoint                          | Method | Purpose                        |
|-----------------------------------|--------|--------------------------------|
| `/quizzes/attempts`               | POST   | Start new quiz attempt         |
| `/quizzes/attempts/{id}`          | GET    | Get single attempt             |
| `/quizzes/sessions/{id}/attempts` | GET    | Get all attempts for a session |
| `/quizzes/attempts/{id}/submit`   | POST   | Submit answers for an attempt  |
| `/quizzes/attempts/{id}/complete` | POST   | Complete an attempt            |
| `/quizzes/{id}/questions`         | GET    | Get quiz questions             |
| `/tasks/{id}`                     | GET    | Get task by ID                 |
| `/tasks/{id}/events`              | GET    | Get task history events        |
| `/comments`                       | POST   | Create comment                 |
| `/comments/{id}`                  | PUT    | Update comment                 |
| `/comments/{id}`                  | DELETE | Delete comment                 |
| `/{entity}/{entityId}/comments`   | GET    | Get comments for entity        |

**Data Models:**

```
QuestionType = {
  SingleChoice, MultipleChoice, StringMatch,
  NumberMatch, OpenText
}

TaskState = {
  Backlog, InProgress, Review, Evaluated, Failed
}

QuestionResult = {
  Unknown, Unanswered, Review, Fail, Success, PartialSuccess
}

AttemptStrategy = { Last, Best }

MessageState = { Sent, Deleted }

ShowAuthor = { Disabled, LastMessage, EveryMessage }

FileAppearance = { Neutral, Success, Accent }

TaskCommentRoles = {
  ViewTaskComment: "lms_view_task_comment",
  AddTaskComment: "lms_add_task_comment",
  EditTaskComment: "lms_edit_task_comment"
}

TaskEvent types = {
  ExerciseEstimated, TaskSubmitted, TaskEvaluated,
  TaskRejected, TaskFailed, TaskReset,
  TaskExtraScoreGranted, MaxScoreChanged,
  AssistantAssigned, ReviewerAssigned, TaskProlonged,
  SolutionAttached, TaskLateDaysReset,
  TaskLateDaysCancelled, TaskLateDaysProlong,
  TaskStarted, ExerciseMaxScoreChanged,
  ExerciseDeadlineChanged
}
```

**Content/Attachment mapping:**

```
gn.mapFromContent(content) -> { name, filename, size, type, version }
gn.mapToContent(file) -> { version, filename, name, length, mediaType }

MediaType: Image, Video, Audio, File
```

**Business Logic (KMP-relevant):**

1. **Quiz attempt flow:** startAttempt -> submitAttempt -> completeAttempt. Questions have
   configurable order. Multiple question types supported.
2. **Task lifecycle states:** Backlog -> InProgress -> Review -> Evaluated/Failed. Events tracked
   with full history.
3. **Task comments/messaging system:** Full CRUD with attachments, edit/delete capabilities,
   participant tracking. Messages grouped by date.
4. **Score calculation:** Supports extraScore, precision validators for NumberMatch questions, score
   rounding.
5. **Late Days system:** Students can transfer deadlines using "Late Days" -- cancel, prolong,
   reset.
6. **File attachments:** Accepted types:
   `.pdf, .png, .jpg, .jpeg, .pptx, .xlsx, .py, .zip, .gz, .ipynb, .mp4, .mp3, .sql, .html, .md, .docx, .lean`.
   Max size: 512MB.
7. **Task timer:** Tasks can have time limits (`taskTimerExpiresAt`).

**Angular Components:**

- `cu-uploaded-files-list` -- file display with presigned URLs
- `cu-images-preview` / `cu-image-carousel` -- image viewer with carousel
- `cu-message` / `cu-message-list` -- chat message display
- `cu-message-input` / `cu-message-attachments-trigger` -- message composer
- `cu-task-comments` -- complete comments widget
- `cu-task-history` -- event history timeline
- `cu-task-heading` -- task title with score
- `cu-task-coding-overview` / `cu-task-coding-solution` -- coding exercise UI
- `cu-task-no-answer` -- empty answer placeholder
- `cu-lms-editor-view` -- rich content viewer

---

### 2. chunk-DUQY2F3Y.js (819KB, 5784 lines)

**Type:** VENDOR LIBRARY -- dash.js (MPEG-DASH video streaming player)

**Contents:**

- Complete dash.js media player implementation
- Adaptive bitrate streaming (ABR) logic
- Buffer management, segment scheduling
- CMCD/CMSD telemetry support (Common Media Client Data)
- Content steering support
- DRM handling infrastructure
- Manifest parsing (MPD)
- Gap jumping and seek handling

**No API endpoints, no CU-specific data models.**

**KMP Relevance:** LOW. This is for video streaming within the LMS (likely for video
lessons/materials). The KMP app would use native video players (ExoPlayer on Android, AVPlayer on
iOS) rather than reimplementing DASH. The important takeaway is that the LMS supports DASH video
content.

---

### 3. chunk-E4QVR5ON.js (291KB, 5443 lines)

**Type:** VENDOR LIBRARY -- TUI Editor (Tiptap-based rich text editor)

**Contents:**

- TUI Editor component suite -- Angular wrapper around Tiptap/ProseMirror
- Editor toolbar tools: bold, italic, underline, strikethrough, code, lists, tables, images, links,
  anchors, headings, colors, alignment, undo/redo, subscript/superscript, quote, horizontal rule,
  attachments, TeX
- Extension loader system with lazy-loaded editor features (table, image, video, audio, iframe,
  tabs, details, jump anchors, file links, background colors)
- Font customization support
- Image upload/preview integration

**Angular Components (exported):**

- `tui-editor-socket` -- rich content display
- Multiple toolbar tools (`tuiUndoTool`, `tuiBoldTool`, etc.)
- `tui-editor-toolbar` -- toolbar container
- Color picker components
- Editor wrapper components

**No API endpoints. No CU-specific data models.**

**KMP Relevance:** LOW-MEDIUM. The KMP app needs to *display* rich HTML content (longread
descriptions, task descriptions) but does not need the editor itself. The `tui-editor-socket`
rendering styles are relevant for matching web content display.

---

### 4. chunk-SOSKVXWE.js (270KB, 3431 lines)

**Type:** VENDOR LIBRARY -- ProseMirror core

**Contents:**

- `prosemirror-model` -- document model (Fragment, Node, Mark, Schema, Slice)
- `prosemirror-transform` -- document transformations (steps, mapping)
- `prosemirror-state` -- editor state management (Transaction, Selection, Plugin)
- `prosemirror-view` -- editor view/DOM rendering
- `prosemirror-commands` -- standard editing commands (join, lift, split, delete)
- `prosemirror-schema-list` -- list handling
- `prosemirror-dropcursor` -- drag & drop cursor
- `prosemirror-gapcursor` -- gap cursor for non-editable nodes
- `prosemirror-history` -- undo/redo history
- Keyboard input handling for all platforms (Mac/Windows/Android/iOS)

**No API endpoints. No CU-specific data models.**

**KMP Relevance:** NONE. This is the underlying rich text editing engine for ProseMirror/Tiptap. Not
needed in the KMP app.

---

### 5. chunk-OAEP3TFI.js (131KB, 1406 lines)

**Type:** VENDOR LIBRARY -- JSZip

**Contents:**

- Complete JSZip library for creating/reading ZIP files in the browser
- Compression (deflate) support via pako
- Stream-based processing (CRC32, DataWorker, GenericWorker)
- UTF-8 encoding support
- File/folder management within ZIP archives

**No API endpoints. No CU-specific data models.**

**KMP Relevance:** VERY LOW. JSZip is likely used for bulk file downloads or assignment submissions.
The KMP app would use native archive APIs if needed.

---

## Custom Component Selectors Found (across all learn files)

From the comprehensive grep of `"cu-[a-z-]*"` patterns across all learn files:

### Core UI Components

- `cu-tooltip` -- tooltip wrapper
- `cu-table-actions-trigger` -- table row actions

### Task/Exercise Components

- `cu-exercise-status-badge` -- exercise completion status indicator
- `cu-task-comments` -- task comment/messenger widget
- `cu-task-history` -- task event timeline
- `cu-task-heading` -- task title bar with score
- `cu-task-coding-overview` -- coding task description
- `cu-task-coding-solution` -- coding task answer display
- `cu-task-no-answer` -- empty answer state

### Course/Learning Components

- `cu-course-member` -- course member display
- `cu-course-settings-add-users-layout` -- admin: add users layout
- `cu-students-performance-list` -- student performance grid
- `cu-skill-level` -- skill level indicator
- `cu-publish-state-icon` -- published/unpublished indicator
- `cu-lms-editor-view` -- rich text content viewer

### Messaging Components

- `cu-message` -- single message bubble
- `cu-message-list` -- message list container
- `cu-message-input` -- message input field
- `cu-message-attachments-trigger` -- attachment button
- `cu-edited-message` / `cu-message-template` -- message rendering

### File/Media Components

- `cu-uploaded-files-list` -- file list with download
- `cu-images-preview` -- image preview modal
- `cu-image-carousel` -- image carousel viewer

### Navigation/Layout Components

- `cu-navigation-link` -- nav link
- `cu-layout-footer` -- page footer
- `cu-sidebar` -- navigation sidebar
- `cu-notification-action` -- notification action button
- `cu-notification-separator` -- notification divider
- `cu-mobile-menu-toggle` -- mobile hamburger menu
- `cu-user-actions` -- user action buttons
- `cu-user-info` -- user profile display
- `cu-card-icon` / `cu-card-time` -- notification card parts
- `cu-default-action` -- generic action component

### Import/Admin Components

- `cu-import-header` -- import page header
- `cu-import-layout` -- import page layout

---

## Summary of KMP-Relevant Findings

### High Priority (new API endpoints discovered):

1. **Quiz/Attempt API** (chunk-FBVLZLM6):
    - `POST /api/micro-lms/quizzes/attempts` -- start attempt
    - `GET /api/micro-lms/quizzes/attempts/{id}` -- get attempt
    - `GET /api/micro-lms/quizzes/sessions/{id}/attempts` -- list attempts
    - `POST /api/micro-lms/quizzes/attempts/{id}/submit` -- submit answers
    - `POST /api/micro-lms/quizzes/attempts/{id}/complete` -- complete

2. **Task Events API** (chunk-FBVLZLM6):
    - `GET /api/micro-lms/tasks/{id}/events` -- task history

3. **Comments API** (chunk-FBVLZLM6):
    - Full CRUD on `/api/micro-lms/comments`
    - Entity-scoped: `/api/micro-lms/{entity}/{entityId}/comments`

### High Priority (data models):

- 5 question types: SingleChoice, MultipleChoice, StringMatch, NumberMatch, OpenText
- Task states: Backlog, InProgress, Review, Evaluated, Failed
- 18 task event types covering full lifecycle
- Late Days system for deadline extensions
- File attachment model with presigned URL support

### Low Priority (vendor libraries):

- Prism.js for syntax highlighting (native code highlighting in KMP)
- dash.js for video streaming (native players in KMP)
- TUI Editor / ProseMirror for rich text (display-only needed in KMP)
- JSZip for archive handling (native APIs in KMP)
