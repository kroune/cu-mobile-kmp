# Data Models -- CU LMS Website

Extracted from minified JavaScript source files of `https://my.centraluniversity.ru`.

---

## 1. Auth & Session

### UserToken (Session)

Source: `root/chunk-ROFNVQNW.js`, `learn/chunk-H2KAKJ6J.js`

Retrieved from `GET /api/account/me`:

```typescript
interface UserToken {
  sub: string;           // userId (ssoId)
  name: string;          // display name
  email: string;
  phone_number: string;
  locale: string;        // "ru" | "en"
  roles: string[];       // realm roles
  groups: string[];
  resource_access?: {
    "realm-management"?: {
      roles: string[];
    };
  };
}
```

### User Roles

Source: `root/chunk-N3DZQN6I.js`, `learn/chunk-L6URXEQJ.js`

```typescript
enum UserRole {
  Teacher              = "default_teacher",
  Student              = "default_student",
  Enrollee             = "default_enrollee",
  Assistant            = "lms_assistant",
  PreStudent           = "default_pre_student",
  Staff                = "default_staff",
  LmsViewApp           = "lms_view_app",
  AuthViewTime         = "auth_view_time",
  AIAssistantViewApp   = "ai_assistant-view-app",
  AdminViewApp         = "admin_view_app",
  AdminViewJob         = "admin_view_job",
  AdminEditUser        = "admin_edit_user",
  ViewDevFeature       = "default_view_dev_feature",
  UseStudentHub        = "use_student_hub",
  // + interview scheduler roles, enrollment form roles, HubMmisMigration
}
```

---

## 2. Task System

### Task Object

Source: `learn/chunk-PODCHSSJ.js` (student task UI)

```typescript
interface Task {
  id: string;
  state: TaskState;
  deadline: string;                // ISO date
  startedAt: string | null;        // coding task start time
  attemptStartedAt: string | null; // quiz attempt start time
  score: number | null;
  extraScore: number | null;
  scoreSkillLevel: SkillLevel | null;
  currentAttemptId: string | null;
  evaluatedAttemptId: string | null;
  lastAttemptId: string | null;
  quizSessionId: string | null;
  lateDays: number;                // applied late days count
  isLateDaysEnabled: boolean;
  reviewer: unknown;               // presence-checked

  solution: Solution | null;
  exercise: Exercise;
  course: { id: string; name: string };
  theme: { name: string };
}
```

### TaskState

Source: `learn/chunk-KED2J7FW.js`

```typescript
enum TaskStatus {
  Backlog    = "backlog",
  InProgress = "inProgress",
  Review     = "review",
  Evaluated  = "evaluated",
  Failed     = "failed"
}
```

Color mapping:
- Backlog -> SupportNeutral
- InProgress -> SupportCategorical12Pale
- Review -> SupportCategorical13Pale
- Evaluated -> PositivePale
- Failed -> NegativePale

### Solution

```typescript
interface Solution {
  solutionUrl: string | null;
  attachments: ContentFile[];
  answers: Answer[] | null;    // for quiz tasks
}
```

### Exercise

Source: `learn/chunk-PODCHSSJ.js`, `learn/chunk-N4N6VP3I.js`

```typescript
interface Exercise {
  id: string;
  name: string;
  type: ExerciseType;
  timer: string | null;        // duration string "HH:MM:SS"
  maxScore: number;
  activity: { name: string; weight: number; isLateDaysEnabled: boolean } | null;
  attachments: ContentFile[];
  questions: Question[];       // for quiz exercises
  settings: ExerciseSettings;
  content: ExerciseContent;
}

enum ExerciseType {
  Coding    = "coding",
  Questions = "questions"
}

interface ExerciseSettings {
  attemptsLimit: number | null;            // null = unlimited
  evaluationStrategy: EvaluationStrategy;
}

enum EvaluationStrategy {
  Last = "Last",
  Best = "Best"
}

interface ExerciseContent {
  description: string | null;
  estimation: {
    startDate: string | null;
    timer: string | null;
    maxScore: number;
  };
  attachments: ContentFile[];
  taskId: string | null;
}
```

### Question

Source: `learn/chunk-PODCHSSJ.js`

```typescript
interface Question {
  id: string;
  type: QuestionType;
  score: number;
  content: { description: string | null };
  recommendation: string | null;  // shown after auto-evaluation
}

enum QuestionType {
  OpenText       = "OpenText",
  SingleChoice   = "SingleChoice",
  MultipleChoice = "MultipleChoice",
  NumberMatch    = "NumberMatch",
  StringMatch    = "StringMatch"
}
```

### Evaluation Feedback

Source: `learn/chunk-PODCHSSJ.js`

```typescript
interface EvaluationFeedback {
  score: "bad" | "neutral" | "good";
  details: EvaluationFeedbackDetail | null;
  comment: string | null;
  entityId: string;          // task ID
  entityType: "task";
}

enum EvaluationFeedbackDetail {
  EvaluationError  = "evaluationError",
  UnclearComments  = "unclearComments",
  TooLongWaiting   = "tooLongWaiting",
  LowScore         = "lowScore",
  Other            = "other"
}
```

### Late Days Models

Source: `learn/chunk-DPBB7AEG.js`

```typescript
interface Student {
  lateDaysBalance: number;
  // ...other fields
}

// PUT /tasks/{id}/late-days-prolong body:
interface LateDaysProlongRequest {
  lateDays: number;            // 1..availableDays
}

// Response:
interface LateDaysProlongResult {
  lateDaysUsed: number;
}

// PUT /tasks/{id}/late-days-cancel response:
interface LateDaysCancelResult {
  lateDaysCancelled: number;
}
```

---

## 3. Course System

### Course

Source: `learn/chunk-FY752YIR.js`, `learn/chunk-SQDJS5LQ.js`

```typescript
interface Course {
  id: string;
  name: string;
  state: CourseState;         // "Draft" | "Published"
  // Additional fields in overview endpoint
}

interface CourseOverview {
  state: CourseState;
  themes: Theme[];
  // includeExercises param adds exercise data
}
```

### Theme

Source: `learn/chunk-OLC3OSQO.js`, `learn/chunk-BF6EGGKK.js`

```typescript
interface Theme {
  id: string;
  name: string;
  state: State;               // "Draft" | "Published"
  publishDate?: string;       // ISO datetime (deferred publish)
  longreads: Longread[];
}
```

### Longread

Source: `learn/chunk-OLC3OSQO.js`, `learn/chunk-BF6EGGKK.js`

```typescript
interface Longread {
  id: string;
  name: string;
  type: LongreadType;
  state: State;               // "Draft" | "Published"
  publishDate?: string;       // ISO datetime
  theme: { id: string };
}

enum LongreadType {
  Common  = "Common",         // visible to students ("Урок")
  Handout = "Handout"         // teacher/assistant material, hidden from students
}
```

### MarkStatus (Course Progress)

Source: `learn/chunk-Y2I2TH22.js`

```typescript
enum MarkStatus {
  Passed     = "passed",
  Pristine   = "pristine",
  Blocked    = "blocked",
  InProgress = "inProgress"
}
```

---

## 4. Material System

### Material

Source: `learn/chunk-RDZTKWJR.js`, `learn/chunk-KZ4SBTZ3.js`, `learn/chunk-4SKQR4GO.js`

```typescript
interface LongreadMaterial {
  id: string;
  discriminator: MaterialDiscriminator;
  state: MaterialState;
  name?: string;
  publishDate?: string;
  publishedAt?: string;
  content?: MaterialContent;
}

type MaterialDiscriminator =
  | "Coding"
  | "Questions"
  | "Markdown"
  | "VideoPlatform"
  | "File"
  | "Audio"
  | "Video"
  | "Image";

enum MaterialState {
  Draft     = "Draft",
  Published = "Published"
}

interface MaterialContent {
  value?: string;              // HTML for Markdown type
  isTuiEditor?: boolean;
  file?: FileContent;
  url?: string;                // for VideoPlatform
  description?: string;
  timecodes?: Timecode[];
  imageScale?: number;
  attachments?: ContentFile[];
  state?: string;              // for exercises
  questions?: Question[];      // for Questions type
}

interface Timecode {
  time: string;                // "HH:mm:ss"
  description: string;
}
```

### Material Action Categories

Source: `learn/chunk-23VGFXOH.js`

```typescript
enum MaterialActionCategory {
  Action   = "action",        // "Действие"
  Content  = "content",       // "Контент"
  Exercise = "exercise"       // "Задача"
}
```

### Content Material Types

Subset of discriminators for non-exercise content:
```
Audio, File, Image, Video, VideoPlatform, Markdown
```

### Video States

Source: `learn/chunk-KZ4SBTZ3.js`

```typescript
enum VideoState {
  Unspecified    = "unspecified",
  Empty          = "empty",
  Uploaded       = "uploaded",
  Transcoding    = "transcoding",
  Viewable       = "viewable",
  Ready          = "ready",
  PartiallyReady = "partiallyReady",
  Error          = "error"
}
```

---

## 5. Pagination

Source: `learn/chunk-WPLOIEQW.js`

### API Request Format

```typescript
interface PaginationApiRequest {
  params: {
    offset: number;              // pageNumber * pageSize
    limit: number;               // pageSize
    sortProperty?: string;
    sortDirection?: string;
    [filterKey: string]: any;
  };
}
```

### API Response Format

```typescript
interface PaginatedResponse<T> {
  items: T[];
  paging: {
    offset: number;
    limit: number;
    totalCount: number;
  };
}
```

---

## 6. Events System

Source: `root/chunk-44YKIWJR.js`, `root/chunk-3AKKBUTX.js`, `root/chunk-N2T4BRFD.js`

### Event

```typescript
interface Event {
  id: string;
  slug: string;
  title: string;
  description: string | null;
  startDate: string;             // ISO datetime
  endDate: string | null;
  location: EventLocation | null;
  format: string | null;         // "online", "offline"
  tags: string[] | null;
  previewImageUrl: string | null;
  broadcastUrl: string | null;
  lmsCourseUri: string | null;
  registrationUrl: string | null;
  isCurrentUserApplied: boolean;
  eventTicketRegistration: EventTicketRegistration | null;
}

interface EventLocation {
  city: string | null;
}

interface EventTicketRegistration {
  status: EventTicketRegistrationStatus;
  isPossibleToRegister: boolean;
}

enum EventTicketRegistrationStatus {
  None                                   = "None",
  ReadyToRegister                        = "ReadyToRegister",
  TicketsLimitHasBeenReached             = "TicketsLimitHasBeenReached",
  TicketsLimitHasBeenReachedTryOnline    = "TicketsLimitHasBeenReachedTryOnline",
  RegistrationEndDateHasBeenReached      = "RegistrationEndDateHasBeenReached"
}
```

Event action types: `"apply"`, `"broadcast"`, `"record"`, `"force-applying"`

### Events List Request

```typescript
interface EventsListRequest {
  paging: {
    limit: number;               // default 24
    offset: number;
    sorting: Array<{
      by: string;                // "startDate"
      isAsc: boolean;
    }>;
  };
  filter: {
    usePersonalSuggestionFilter: boolean;
    showOnlyMine: boolean;
    endDateGreaterThanOrEqualTo?: string;  // ISO datetime
    endDateLessThanOrEqualTo?: string;
  };
}
```

### Event Calendar File

```typescript
interface EventCalendarFile {
  fileName: string;              // from Content-Disposition header
  content: Blob;
}
```

---

## 7. Notification System

Source: `root/chunk-KUQINTYY.js`, `learn/chunk-HTCSOUNZ.js`

### In-App Notification

```typescript
interface InAppNotification {
  id: number;
  notificationId: string;
  title: string;
  description: string;
  createdAt: string;             // ISO datetime
  category: NotificationCategory;
  groupingKey: string;
  icon: NotificationIconType;
  startDate?: string;
  endDate?: string;
  previewImageUri?: string;
  link?: NotificationLink;
}

interface GroupedNotification extends InAppNotification {
  links: NotificationLink[];
  unionCount: number;
  collapsed: boolean;
}

interface NotificationLink {
  uri: string;
  label: string;
  target?: "Blank" | "Self";
}

enum NotificationCategory {
  Education = 1,
  Other     = 2
}

enum NotificationCategoryName {
  Education = "Education",
  Others    = "Others"
}

enum NotificationIconType {
  Education   = "Education",
  News        = "News",
  ServiceDesk = "ServiceDesk"
}
```

### Notification Stats

```typescript
interface NotificationStats {
  categories: NotificationCategoryStat[];
}

interface NotificationCategoryStat {
  category: string;
  hasUnread: boolean;
}
```

---

## 8. Profile & Enrollment

### Enrollee Profile

Source: `root/chunk-2RNYUVXP.js`

```typescript
interface EnrolleeProfile {
  firstName: string | null;
  lastName: string | null;
  middleName: string | null;
  phone: string | null;            // stored without "+", displayed with "+"
  birthdate: string | null;        // "YYYY-MM-DD"
  email: string | null;
  graduationYear: number | null;
  studyDegreeType: StudyDegreeType;
  lastStudyDegreeType: LastStudyDegreeType;
  city: string | null;
  citizenship: string | null;
  educationPlaceName: string | null;
  foreignEducationPlace: boolean;
  snils: string | null;            // "XXX-XXX-XXX-XX"
  telegram: string | null;
  livesAbroad: boolean;
  // computed:
  fullName: string;                // first + last + middle name
}

enum StudyDegreeType { Bachelor = "Bachelor", Master = "Master", None = "None" }
enum LastStudyDegreeType { School = "School", University = "University", None = "None" }
```

### Student

Source: `root/chunk-QHDVAQXE.js`

```typescript
interface Student {
  educationLevel: EducationLevel;
  lateDaysBalance: number;
  // ...other fields
}

enum EducationLevel {
  None      = "None",
  Bachelor  = "Bachelor",
  Master    = "Master",
  Dpo       = "Dpo",
  DpoMaster = "DpoMaster"
}
```

---

## 9. Grants

Source: `root/chunk-H2UV6AAT.js`

```typescript
interface Grant {
  status: GrantStatus;
  // ...additional fields in lazy-loaded chunks
}

enum GrantStatus {
  None           = "None",
  Denied         = "Denied",
  NotConfirmed   = "NotConfirmed",
  Confirmed      = "Confirmed",
  ConfirmedIfTop = "ConfirmedIfTop"
}
```

Grant types: `"InformationTechnology"`, `"Design"`

---

## 10. Calendar / Timetable

Source: `learn/chunk-CSB6UCKE.js`, `learn/chunk-HYIXHTRH.js`

```typescript
enum TimetableRegistrationStatus {
  Opened    = "opened",
  Scheduled = "scheduled",
  Closed    = "closed"
}

interface RegistrationConfig {
  openDate?: string;             // ISO datetime
  closeDate?: string;            // ISO datetime
}

interface RegistrationState {
  status: TimetableRegistrationStatus;
  openDate: string | null;
  closeDate: string | null;
}
```

---

## 11. Performance / Gradebook

Source: `learn/chunk-P4BJN2XN.js`, `learn/chunk-2NQCAL5L.js`

```typescript
interface Subject {
  id: string;
  // ...other fields
}

// GET /gradebook/{subjectId}/{semesterId} response
interface GradebookItem {
  // ...fields in lazy-loaded chunk
}

// GET /courses/{id}/performance response
interface StudentPerformanceResponse {
  items: PerformanceItem[];
  paging: PaginationInfo;
}

// POST /performance/courses/{id}/jobs response
interface PerformanceJob {
  jobId: string;
}
```

---

## 12. Admin Configuration

Source: `root/chunk-VMZTUSWJ.js`

```typescript
interface Configuration {
  key: string;
  value: string;
  valueType: ConfigValueType;
}

enum ConfigValueType {
  None     = "None",
  Object   = "Object",
  Boolean  = "Boolean",
  Int      = "Int",
  Double   = "Double",
  DateOnly = "DateOnly",
  DateTime = "DateTime",
  Text     = "Text"
}
```

Parsing: Objects from JSON, Booleans from string, Doubles/Ints with comma->dot replacement.

---

## 13. File Upload / Content

Source: `learn/chunk-QCPI75QD.js`, `learn/chunk-PODCHSSJ.js`

### Upload Link

```typescript
interface UploadLinkResponse {
  uploadUrl: string;             // presigned S3 URL
}
// Request params: directory, filename, contentType
// Upload: PUT to presigned URL with Content-Disposition, Content-Type, x-amz-meta-version headers
```

### Coding Task File Constraints

```
Accepted extensions: pptx, pdf, xlsx, csv, py, zip, gz, ipynb, jpeg, jpg, png,
                     mp4, sql, html, docx, md, mp3, wav, lean, tex, txt
Max file size: 1,048,576,000 bytes (1 GB)
Max file count: 5
```

### Video Upload (TUS protocol)

Source: `learn/chunk-23VGFXOH.js`

```typescript
// createVideoMaterial$ response:
interface VideoUploadInfo {
  materialId: string;
  uploadMediaId: string;
  uploadClientId: string;
  uploadToken: string;
  urls: string[];                // TUS server URLs
}
```

---

## 14. Task Event Types

Source: `learn/chunk-FBVLZLM6.js`

18 event types covering the full task lifecycle:

```typescript
enum TaskEventType {
  ExerciseEstimated       = "ExerciseEstimated",
  TaskSubmitted           = "TaskSubmitted",
  TaskEvaluated           = "TaskEvaluated",
  TaskRejected            = "TaskRejected",
  TaskFailed              = "TaskFailed",
  TaskReset               = "TaskReset",
  TaskExtraScoreGranted   = "TaskExtraScoreGranted",
  MaxScoreChanged         = "MaxScoreChanged",
  AssistantAssigned       = "AssistantAssigned",
  ReviewerAssigned        = "ReviewerAssigned",
  TaskProlonged           = "TaskProlonged",
  SolutionAttached        = "SolutionAttached",
  TaskLateDaysReset       = "TaskLateDaysReset",
  TaskLateDaysCancelled   = "TaskLateDaysCancelled",
  TaskLateDaysProlong     = "TaskLateDaysProlong",
  TaskStarted             = "TaskStarted",
  ExerciseMaxScoreChanged = "ExerciseMaxScoreChanged",
  ExerciseDeadlineChanged = "ExerciseDeadlineChanged"
}
```

---

## 15. Task Comment Model

Source: `learn/chunk-FBVLZLM6.js`

```typescript
interface TaskComment {
  id: string;
  text: string;
  attachments: ContentFile[];
  author: {
    name: string;
    initials: string;
  };
  createdAt: string;           // ISO datetime
  isEditable: boolean;
  isDeletable: boolean;
}

enum MessageState {
  Sent    = "Sent",
  Deleted = "Deleted"
}

enum ShowAuthor {
  Disabled     = "Disabled",
  LastMessage   = "LastMessage",
  EveryMessage  = "EveryMessage"
}

enum FileAppearance {
  Neutral = "Neutral",
  Success = "Success",
  Accent  = "Accent"
}

// Role constants for comment permissions
enum TaskCommentRoles {
  ViewTaskComment = "lms_view_task_comment",
  AddTaskComment  = "lms_add_task_comment",
  EditTaskComment = "lms_edit_task_comment"
}
```

---

## 16. Quiz Attempt Models

Source: `learn/chunk-FBVLZLM6.js`

```typescript
enum QuestionResult {
  Unknown        = "Unknown",
  Unanswered     = "Unanswered",
  Review         = "Review",
  Fail           = "Fail",
  Success        = "Success",
  PartialSuccess = "PartialSuccess"
}

enum AttemptStrategy {
  Last = "Last",
  Best = "Best"
}
```

Quiz question types (5 total, see section 2 `QuestionType` enum):
- `SingleChoice` -- radio buttons, one correct answer
- `MultipleChoice` -- checkboxes, multiple correct answers
- `StringMatch` -- exact string input
- `NumberMatch` -- numeric input with precision validation
- `OpenText` -- free text input

---

## 17. Payer Types (Admission Forms)

Source: `root/chunk-EEZOTMIH.js`

```typescript
enum PayerType {
  Self                   = "Self",
  Another                = "Another",
  Organization           = "Organization",
  IndividualEntrepreneur = "IndividualEntrepreneur"
}

enum IdentityDocumentType {
  RussianPassport = "RussianPassport"
}
```

Each payer type has different required form fields:
- **Self/Another**: passport details, personal info
- **Organization**: full company details, banking info, manager info
- **IndividualEntrepreneur**: OGRNIP, INN, banking details, legal/mailing address

---

## 18. Activity Types (Grant Forms)

Source: `root/chunk-OO7VWEVR.js`, `root/chunk-TOVOQLRA.js`

```typescript
enum ActivityType {
  VsOSH             = "vsosh",              // All-Russian School Olympiad
  RsOSH             = "rsosh",              // Russian School Olympiad
  DesignCompetition  = "designCompetition",  // Design competitions
  Other              = "other"               // Other activities
}

enum ActivityStatus {
  Approved = "Approved",
  Declined = "Declined",
  NeedFix  = "NeedFix"
}
```

Activity form fields per type:
- All types share: `id`, `userActivityId`, `type`, `status`, `year`, `class`, `criteriaId`, `files`
- **VsOSH/RsOSH**: + `activity`, `activityProfileId`, `activityProfileName`
- **DesignCompetition/Other**: + `activity` (name only)

File upload for activities: accepts `.jpeg, .jpg, .png, .pdf`, max 5MB per file.

---

## 19. Grant Increase Status

Source: `root/chunk-TYXSATCA.js`

```typescript
enum IncreaseGrantStatus {
  New      = "new",       // "Sent" — application submitted
  Check    = "check",     // "Under review" — being reviewed
  Review   = "review",    // "Returned for revision" — needs fixes
  Increase = "increase",  // "Grant increased" — approved
  Rejected = "rejected"   // "Reviewed without increase" — denied
}
```

Grant widget model:
```typescript
interface GrantWidget {
  appearance: "it" | "design";
  baseGrantValue: number;        // percentage from admissions/competitions/olympiads/EGE
  additionalGrantValue: number;  // percentage from special events and XP Game
  totalGrantValue: number;       // capped at 100%
  discountedPrice: number;
  isTenClassSchoolchild: boolean; // additional grant starts from 11th grade
}
```

---

## 20. File Upload Constraints

Source: `learn/chunk-FBVLZLM6.js` (task attachments), `learn/chunk-PODCHSSJ.js` (coding tasks)

### Task Attachments (Comments/Messages)
```
Max file size: 512 MB (536,870,912 bytes)
Accepted extensions: .pdf, .png, .jpg, .jpeg, .pptx, .xlsx, .py, .zip, .gz, .ipynb,
                     .mp4, .mp3, .sql, .html, .md, .docx, .lean
```

### Coding Task Solutions
```
Max file size: 1 GB (1,048,576,000 bytes)
Accepted extensions: pptx, pdf, xlsx, csv, py, zip, gz, ipynb, jpeg, jpg, png,
                     mp4, sql, html, docx, md, mp3, wav, lean, tex, txt
Max file count: 5
```

### Activity Proof Documents
```
Max file size: 5 MB
Accepted extensions: .jpeg, .jpg, .png, .pdf
```

### Content Mapping
```typescript
enum MediaType {
  Image = "Image",
  Video = "Video",
  Audio = "Audio",
  File  = "File"
}

// mapFromContent: { name, filename, size, type, version }
// mapToContent:   { version, filename, name, length, mediaType }
```

---

## 21. Scheduled Event Format

Source: `root/chunk-TYXSATCA.js`

```typescript
enum ScheduledEventFormat {
  Online      = "Online",
  Offline     = "Offline",
  OfflineBank = "OfflineBank",   // at T-Bank office
  OfflineCu   = "OfflineCu"     // at CU campus
}
```

---

## 22. Permission Role Constants

### Course Roles

Source: `learn/chunk-JYGWQKMQ.js`

```
lms_add_course, lms_view_course_to_learn, lms_edit_course,
lms_delete_course, lms_view_course_events, lms_publish_course,
lms_return_to_draft_course, lms_edit_course_skill_level,
lms-tasks-manage-course_group, lms-tasks-view-course_group
```

### Task Roles

Source: `learn/chunk-MWYOKMK6.js`

```
lms_view_all_tasks, lms_view_task, lms_start_task, lms_submit_task,
lms_reject_task, lms_evaluate_task, lms_grant_extra_score_for_task,
lms_refuse_extra_score_for_task, lms_view_task_events,
lms_attach_reviewer_to_task, lms-tasks-prolong_deadline-task,
lms-tasks-view_late_days-student, lms-tasks-prolong_late_days-task,
lms-tasks-cancel_late_days-task
```

### Longread / Theme / Material Roles

Source: `learn/chunk-YBIXENKV.js`, `learn/chunk-T4STSLR4.js`

```
lms_order_theme_longreads, lms_add_longread, lms_view_longread,
lms_edit_longread, lms_preview_longread, lms_delete_longread,
lms_view_longread_events, lms_publish_longread, lms_return_to_draft_longread

lms_order_course_themes, lms_add_theme, lms_view_theme,
lms_edit_theme, lms_delete_theme, lms_view_theme_events,
lms_publish_theme, lms_return_to_draft_theme

lms_order_longread_materials, lms_add_material, lms_view_material,
lms_edit_material, lms_delete_material, lms-tasks-publish-material,
lms-tasks-return_to_draft-material, lms-tasks-view_draft-material
```

### Gradebook Roles

Source: `learn/chunk-ULV6EIU5.js`

```
lms-gradebook-manage-gradebook, lms-gradebook-delete-gradebook_record
```

### Polls Roles

Source: `learn/chunk-PODCHSSJ.js`

```
lms-polls-submit-poll_answer, lms-polls-view-poll
```

### Combined Role Groups

Source: `learn/chunk-EJTNNIJ6.js`

```
ManageCourseRoles   = [ViewCoursesToManage, AddCourse, EditCourse, DeleteCourse, PublishCourse, ReturnToDraftCourse]
ManageThemeRoles    = [ViewCoursesToManage, AddTheme, EditTheme, DeleteTheme, OrderThemes, PublishTheme, ReturnToDraftTheme]
ManageLongreadRoles = [ViewCoursesToManage, AddLongread, EditLongread, DeleteLongread, OrderLongreads, PublishLongread, ReturnToDraftLongread]
ManageMaterialRoles = [ViewCoursesToManage, AddMaterial, EditMaterial, DeleteMaterial, OrderMaterials]
AllManageRoles      = union of above four
ViewLongreadRoles   = [ViewCoursesToLearn, ViewLongread, ViewMaterial]
ViewCourseStructure = [ViewCoursesToLearn, ViewCourseToLearn, ViewTheme]
AllViewRoles        = union of ViewCourseStructure + ViewLongreadRoles
```

---

## 23. Dynamic Config Shape

Source: `learn-dynamical-config.json`, `root/chunk-OIDSCTRK.js`

```typescript
interface DynamicConfig {
  authUrl: string;                       // "https://id.centraluniversity.ru"
  hubAppUrl: string;                     // "https://my.centraluniversity.ru"
  adminAppUrl: string;                   // "https://my.centraluniversity.ru/admin"
  lmsAppUrl: string;
  lmsPythonCourseUrl: string;
  tiMeUrl: string;                       // "https://time.cu.ru"
  statistEndpointUrl: string;            // "https://api-statist.tinkoff.ru"
  fliptProviderUrl: string;              // "https://my.centraluniversity.ru"
  informerScriptUrl: string;
  mainCampusBuildingOnMapLink: string;
  teletypeId: string;
  newsChannelUrl: string;
  env: "develop" | "preprod" | "prod" | "test";
  eduGrantOnlineContestId: string;
}
```
