# Learn App Vendor Chunks -- Detailed Analysis

Analysis of 3 bundle files from the CU LMS Angular 21.1.4 "learn" application:

| File                    | Size   | Lines | Content Type                                         |
|-------------------------|--------|-------|------------------------------------------------------|
| `chunk-23VGFXOH.js`     | 353 KB | 4     | Application component + vendor code (minified ESM)   |
| `scripts-V24VZB7E.js`   | 330 KB | 287   | Third-party rendering libraries (KaTeX + PrismJS)    |
| `polyfills-T4AEJMKQ.js` | 21 KB  | 2     | Core-js polyfills + Russian locale + $localize setup |

---

## 1. Bundled Libraries / Frameworks

### polyfills-T4AEJMKQ.js

- **Core-js 3.48.0** -- Comprehensive ES polyfills (Object.defineProperty, Symbol, Promise, etc.)
    - Copyright: "2013-2025 Denis Pushkarev (zloirock.ru), 2025-2026 CoreJS Company (core-js.io)"
    - Source: `https://github.com/zloirock/core-js`
- **Angular CLDR locale data for Russian (`ru`)** -- full locale registration including:
    - Day names (abbreviated, wide): ПН, ВТ, СР, ЧТ, ПТ, СБ, ВС
    - Month names (abbreviated, wide): янв., февр., март, апр., ...
    - Era designations: до н.э., н.э., до Рождества Христова, от Рождества Христова
    - Date formats: `dd.MM.y`, `d MMM y 'г.'`, `d MMMM y 'г.'`, `EEEE, d MMMM y 'г.'`
    - Time formats: `HH:mm`, `HH:mm:ss`, `HH:mm:ss z`, `HH:mm:ss zzzz`
    - Number formatting: comma for decimal separator, non-breaking space for grouping
    - Currency: RUB (₽) -- российский рубль
    - Period of day labels: полн., полд., утра, дня, веч., ночи
    - Period boundaries: 04:00-12:00 (утро), 12:00-18:00 (день), 18:00-22:00 (вечер), 22:00-04:00 (
      ночь)
- **Navigator.connection polyfill**: Guards against `navigator.connection === null`
- **$localize setup**: `(globalThis.$localize ??= {}).locale = "ru"` -- Angular i18n locale binding

### scripts-V24VZB7E.js

- **KaTeX** (~260 KB of the 330 KB file) -- Full LaTeX math rendering engine
    - UMD module export: `exports.katex`
    - Output modes: `htmlAndMathml`, `html`, `mathml`
    - Config options: `displayMode`, `throwOnError`, `errorColor`, `macros`, `strict`, `trust`,
      `maxSize`, `maxExpand`
    - Full LaTeX parser: `ParseError`, token types (`mathord`, `textord`, `atom`), style system (
      Display/Text/Script/Scriptscript)
    - Unicode support: Latin, Cyrillic, Armenian, Brahmic, Georgian, CJK, Hangul blocks
    - SVG path data for math symbols (arrows, braces, integrals, etc.)
    - Supports: fractions, enclose/cancel, colorbox, fcolorbox, phase diagrams
- **PrismJS** (~70 KB) -- Syntax highlighting engine
    - MIT license, by Lea Verou
    - Bundled language grammars:
        - **CSS** (including atrule, property, function, selector)
        - **C** (with macros, preprocessor directives)
        - **C++** (with templates, concepts, modules, raw strings)
        - **Go** (with builtins like `make`, `cap`, `len`)
        - **Java** (with annotations, generics, imports, triple-quoted strings)
        - **JavaScript** (with regex, template strings, hashbang, modules)
        - **JSON** (with property/string distinction)
        - **Python** (with f-strings, decorators, triple-quoted strings)
        - **SQL** (with extensive keyword list)
        - **Kotlin** (with string interpolation, annotations, labels)
        - **TypeScript** (with decorators, generic functions, keyof, readonly)
        - **YAML** (with scalars, datetime, anchors/aliases)
        - Aliases: `js` = JavaScript, `py` = Python, `ts` = TypeScript, `yml` = YAML, `kt`/`kts` =
          Kotlin

### chunk-23VGFXOH.js -- Embedded Libraries

- **React 19.2.4** -- Full React production runtime bundled inside
    - `pe.version = "19.2.4"`
    - Includes: Component, PureComponent, StrictMode, Suspense, Fragment, Profiler, Activity
    - All hooks: useState, useEffect, useCallback, useMemo, useRef, useContext, useReducer,
      useDeferredValue, useTransition, useId, useActionState, useOptimistic, useSyncExternalStore,
      useInsertionEffect, useLayoutEffect, useImperativeHandle
    - `__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE` internal field
    - `__COMPILER_RUNTIME` with `useMemoCache`
    - `pe.cache`, `pe.cacheSignal`, `pe.use`, `pe.startTransition`
    - Children utilities: `map`, `forEach`, `count`, `toArray`, `only`
    - createElement, createRef, createContext, forwardRef, cloneElement, isValidElement, lazy, memo
- **Lodash-style utilities** (tree-shaken/custom build, not full lodash) -- various utility
  functions:
    - `forEach` (array iteration with early termination)
    - `assignValue` (Object property assignment with `__proto__` safety)
    - `baseAssignValue` (using `defineProperty` when available)
    - `copyObject` (shallow copy with custom handler support)
    - `isEqual` type checking
    - Deep clone support (`cloneDeep` via `(0, Sd.default)(e)` and `(0, Pd.default)(e)`)
- **tus-js-client** -- TUS resumable upload protocol implementation
    - Upload creation: `POST` to endpoint with `Upload-Length`, `Upload-Metadata` headers
    - Upload resumption: `HEAD` to check `Upload-Offset`
    - Parallel uploads: `Upload-Concat: partial` / `Upload-Concat: final;...`
    - `Upload-Defer-Length` for deferred length uploads
    - `Upload-Complete: ?0` for protocol variant `la` (likely `ietf-draft`)
    - Error handling with retry logic: `retryDelays` option, `_retryAttempt` counter
    - Fingerprint-based URL storage for resume after page reload
    - Chunk-based upload with progress events
    - Supports two protocols: `ws` (standard tus) and `la` (IETF draft)
- **UTM/Analytics tracking library** -- Cookie-based marketing attribution
    - Cookie management: `utm_source`, `utm_medium`, `utm_campaign`, `utm_content`, `utm_term`,
      `utm_date_set`, `sid`, `wm`
    - Context cookies: `ctx_source`, `ctx_position`, `ctx_block`
    - Session storage: `session_utm_source`
    - Cookie categories: `CookieCategoryBit.Marketing`, `CookieCategoryBit.Statistics`,
      `CookieCategoryBit.Essential`
    - User consent vault with category-based allow/deny
    - UTM marker resolution from: query params, hash params, referrer, parent_url
    - Internal domain detection for referrer handling
    - Cookie size limits: utm_source=255, utm_term=600, utm_content=600, utm_campaign=255, etc.
    - Visitor new/return tracking: `s_nr` cookie with 1-year expiry (31536000s)
    - Cookie expiry: 60 days (5184000s) default for UTM cookies
- **Maskito** -- Input masking library (referenced via `maskitoOptions`, `maskito` directive)
- **Taiga UI** -- UI component library used throughout
    - Components: `tuiButton`, `tuiIconButton`, `tuiHint`, `tuiTextfield`, `tuiSwitch`, `tui-error`,
      `tui-icon`, `tui-textfield`, `tui-data-list-wrapper`, `tui-notification`, `tui-loader`
    - Pipes: `tuiFieldError`, `tuiMapper`
    - Directives: `tuiLabel`, `tuiTextfieldSize`, `tuiTextfieldCleaner`
    - Styles: Uses CSS custom properties with `--tui-` prefix (e.g., `--tui-duration`,
      `--tui-text-primary`, `--tui-service-autofill-background`)
- **Angular CDK** -- Component Dev Kit
    - Drag and Drop: `cdkDrag`, `cdkDragHandle`, `cdkDragPlaceholder`, `cdkDropList`,
      `cdkDropListDropped`, `cdkDropListAutoScrollStep`, `cdkDropListLockAxis`,
      `cdkDropListDisabled`, `moveItemInArray` (via `Un()`)
- **Angular Reactive Forms**
    - `FormGroup` (as `Pe`), `FormControl` (as `W`), `FormArray`, `Validators` (as `$e`)
    - `NG_VALUE_ACCESSOR` (as `Kr`)
    - `ngModel`, `ngModelChange`, `formControl`, `formGroup`, `formArrayName`

---

## 2. API Endpoint Patterns and Service Methods

### Material CRUD Operations (from `xe` service / material state management)

These are the RxJS-based service methods called throughout the component. The actual HTTP calls are
delegated to injected services (`ui` being the primary material API service):

```
createMaterial$(data)           -- Create a new material with {longreadId, type, viewContent?}
readMaterial$(materialId)       -- Read/fetch a single material by ID
updateMaterial$(materialId, data) -- Update material metadata
deleteMaterial$(materialId)     -- Delete a material
reorderMaterials$(longreadId, materialIds[]) -- Reorder materials in a longread
```

### Video Platform Operations

```
createVideoMaterial$(longreadId, {name, description, type: VideoPlatform})
trackVideoMaterialInPlatform$(materialId)  -- Poll video processing status
updateVideoPlatformMaterialTimecodes$(materialId, timecodesText)
```

### Video Upload Flow (via tus-js-client)

```
new Xs().upload(file, {
  uploadMediaId: response.uploadMediaId,
  uploadClientId: response.uploadClientId,
  uploadToken: response.uploadToken,
  serverUrls: response.urls,
  mimeTypes: Ro.map(String)   -- Allowed MIME types from constant
})
```

### Other Service Calls Referenced

```
courseMembers$      -- Observable of course members
courseGroups$       -- Observable of course groups
longread$          -- Observable of current longread
theme$             -- Observable of current theme
longreadId$        -- Observable of current longread ID
themeId$           -- Observable of current theme ID
courseId$           -- Observable of current course ID
reviewers$         -- Observable of reviewers
getActivities$(id) -- Get activities for a course
id$                -- Observable of current entity ID
```

### Cookie/Storage API Patterns

```
cu.lms.longread-material-copy -- localStorage key for copied material (24h expiry)
setItem(key, value, {expires})
getItem(key)
removeItem(key)
```

---

## 3. Data Models / Serialization

### Material Discriminator Types (`Ze` enum)

```
Ze.Audio         -- Audio material
Ze.Coding        -- Coding exercise
Ze.File          -- File attachment
Ze.Image         -- Image material
Ze.Markdown      -- Markdown/rich text content
Ze.Questions     -- Question-based exercise
Ze.Video         -- Video material
Ze.VideoPlatform -- Video platform integration
```

### Content Material Types (`Tt` enum)

Subset of above representing "content" (non-exercise) types:

```
Tt.Audio, Tt.File, Tt.Image, Tt.Video, Tt.VideoPlatform, Tt.Markdown
```

### Material State (`ft` enum)

```
ft.Draft
ft.Published
```

### Longread State (`Gt` enum)

Referenced as `longReadState = Gt`

### Longread Type (`Vt` enum)

```
Vt.Common -- Common longread type (supports Content, Exercise, and Action blocks)
```

### Material Action Categories (`Kt`)

```
Kt.Action   -> "action"     -- Label: "Действие"
Kt.Content  -> "content"    -- Label: "Контент"
Kt.Exercise -> "exercise"   -- Label: "Задача"
```

### Video Upload States (`yt` enum)

```
yt.Empty         -- Initial/uploading
yt.Transcoding   -- Processing on server
yt.Viewable      -- Partially ready
yt.Ready         -- Fully processed
yt.Error         -- Upload/processing error
```

### Video Upload Error Types (`Yn`)

```
Yn.UploadInPlatformError
Yn.PlatformProcessingError
```

### Material Editing State (`Pt` enum)

```
Pt.NotEditing
Pt.Editing
Pt.Invalid
Pt.PublishDateEditing
```

### Create Video Step (`li` enum)

```
li.Metadata    -- Step 1: name, description, file upload
li.Timecodes   -- Step 2: set timecodes
```

### Material Copy/Paste Data Flow

Serialization for copy: Material object stored in cookie storage (`cu.lms.longread-material-copy`)
Deserialization for paste:

- Coding/Questions: Clone via deep clone, create new server-side material ID
- Markdown: Deep clone, strip IDs from headings via `el()` DOM parser, JSON.stringify content

```javascript
// Clone for coding/questions:
Td(original, newMaterialId) -> {
  ...cloneDeep(original),
  id: newMaterialId,
  state: Draft,
  publishDate: removed,
  publishedAt: removed,
  content.attachments: [],
  content.state: Draft,
  questions[].attachments: [],  // for Questions type
  questions[].id: deleted
}

// Clone for markdown:
Dd(original) -> {
  ...cloneDeep(original),
  state: Draft,
  publishDate: removed,
  publishedAt: removed,
  content: { value: stripHeadingIds(html), isTuiEditor: true }
}
```

### Form Data Structures

Video material form:

```typescript
{
  name: string          // Required, validated: "Добавьте название видео"
  description: string   // Optional
  videoFile: File|null  // Required for new, validated: "Загрузите файл"
  timecodes: string     // Format: "MM:SS description\nMM:SS description"
}
```

Question input form:

```typescript
{
  type: QuestionInputAnswerType  // Dropdown with type options
  correctAnswer: FormGroup       // Auto-evaluation form
  recommendation: FormControl    // Пояснение (explanation)
}
```

Question choice options form:

```typescript
{
  options: FormArray<{
    value: FormControl<string>
    isCorrect: FormControl<boolean>
    recommendation: FormControl<string>
  }>
  areOptionsShuffled: FormControl<boolean>
}
```

---

## 4. HTTP Client Configuration

No direct `HttpClient` configuration found in these specific chunks. The HTTP calls are abstracted
behind injected Angular services (using the DI pattern). The actual HTTP interceptors, base URL
configuration, and authentication headers are in other chunks (likely the main bundle or shared
infrastructure chunks like `chunk-KNF5L6KI.js` or `chunk-24K4LTF5.js`).

Key observations:

- All API calls use RxJS Observable patterns (`pipe()`, `subscribe()`, `switchMap` as `je()`)
- Error handling via `takeUntilDestroyed` pattern (`fe(c(this, t))`)
- Loading state management via signal-based approach (`de()` = `signal()`, `me()` = `computed()`)

---

## 5. Date/Time Handling

### Russian Date Formatting (from polyfills)

```
Short:  dd.MM.y           -> "22.03.2026"
Medium: d MMM y 'г.'      -> "22 мар. 2026 г."
Long:   d MMMM y 'г.'     -> "22 марта 2026 г."
Full:   EEEE, d MMMM y 'г.' -> "воскресенье, 22 марта 2026 г."

Time:
Short:  HH:mm             -> "14:30"
Medium: HH:mm:ss          -> "14:30:45"
Long:   HH:mm:ss z        -> "14:30:45 MSK"
Full:   HH:mm:ss zzzz     -> "14:30:45 Moscow Standard Time"

Combined: "{1}, {0}"      -> "22 мар. 2026 г., 14:30"
```

### Date Expiration Check

```javascript
function zc(e) { return +Yu(e) < Date.now() }
// Yu likely maps to a Date parsing function
```

### Minimum Publish Date Logic

```javascript
static getMinPublishDate(publishDate) {
  let now = new Date();
  return Xu(
    publishDate ? Wu([new Date(publishDate), now]) : now,
    Ai.Moscow  // Moscow timezone
  )[0];
}
// Wu = max of dates, Xu = timezone conversion to Moscow
```

### Timecode Formatting

```javascript
// Parse: "MM:SS description" per line
// Serialize: timecodes.map(t => `${t.time} ${t.description}`).join("\n")
```

---

## 6. Form Handling Patterns

### Angular Reactive Forms Usage

The chunk uses Angular Reactive Forms extensively with these patterns:

1. **Signal-based form creation**: Forms created as computed signals from input data
2. **Custom validators**: `$e.required("error message")` -- validators with custom messages
3. **ControlValueAccessor**: `cu-input-tag` component implements CVA for custom tag input
4. **Maskito integration**: Input masking via `[maskito]` directive binding to `MaskitoOptions`
5. **TuiFieldError pipe**: Error display via `tui-error` component with async pipe
6. **Form validation on submit**: `On(this.form)` -- likely `markAllAsTouched`

### Form Components

- `cu-input-tag` -- Multi-value tag input with CVA, deduplication, validation per tag
- `cu-question-input-configurator` -- Question type selector with auto-evaluation toggle
- `cu-question-choice-options-configurator` -- Drag-and-drop option list with correct/incorrect
  marking
- `cu-exercise-coding-configure` -- Coding exercise configuration
- `cu-exercise-questions-configure` -- Questions exercise configuration
- `cu-lms-editor` -- Rich text editor component (referenced, defined elsewhere)

---

## 7. Rich Text / Content Editor Components

### Editor References in This Chunk

- `cu-lms-editor` -- Referenced as a dependency for editing option text in question configurators
    - Input: `control` (FormControl), `placeholder`
    - Used for: editing variant text in question options, recommendation text
- `cu-longread-material-editor` -- Material inline editor
    - Input: `material` (the material object)
- Content uses `isTuiEditor: true` flag on markdown content to indicate Taiga UI editor usage

### Material Viewer vs Editor Pattern

```
material__editor  -- Editing mode (padding: 3.5rem 0)
material__viewer  -- View mode
material__viewer_padded -- View with padding (3.5rem 0)
```

### Content Value Processing for Paste

```javascript
function Sv(value) {
  let { pseudoDocument, foundElements } = el(value, "h1[id], h2[id]");
  foundElements.forEach(n => n.setAttribute("id", Oo())); // Generate new UUIDs
  return pseudoDocument.body.innerHTML;
}
```

Uses DOM parsing (`el()`) to find headings with IDs and regenerate them -- implies content is stored
as HTML with heading anchors for navigation.

---

## 8. Component Architecture (chunk-23VGFXOH.js)

### Exported Component

**`LongreadEditorComponent`** (selector: `cu-longread-editor`) -- The main and only export

### Angular Modern Features Used

- **Signals**: `signal()` (as `de()`), `computed()` (as `me()`), `effect()` (as `Ut()`)
- **Signal-based inputs**: `input.required()` (as `L.required()`)
- **Signal-based outputs**: `output()` (as `at()`)
- **`toSignal()`**: Converting observables to signals (as `_t()`)
- **`inject()`**: Function-based dependency injection (as `E()`)
- **`viewChild()`**: Signal-based view queries (as `Mn()`)
- **View transitions**: `@defer`, `@if`, `@for` control flow
- **OnPush change detection**: `changeDetection: 0`

### Key Dependencies (Angular DI)

The component injects 27+ services including:

- Router, ActivatedRoute
- Course store, Theme store, Longread store
- Material management services (CRUD, reorder)
- Various action factories (publish date, copy, delete, move, etc.)
- Dialog service (for page-size modals)
- Breadcrumb service (optional)

### Russian UI Text Found

| Key                       | Text                                             |
|---------------------------|--------------------------------------------------|
| Back button               | "К списку всех тем"                              |
| Add block above           | "Добавить блок выше"                             |
| Add block below           | "Добавить блок ниже"                             |
| Not published             | "Не опубликован"                                 |
| Published                 | "Опубликован"                                    |
| Block                     | "Блок"                                           |
| Move                      | "Переместить"                                    |
| Delete confirmation       | "Вы действительно хотите удалить этот материал?" |
| Video loading             | "Загружаем видео"                                |
| Video processing          | "Обрабатываем видео"                             |
| Video partially ready     | "Видео частично готово"                          |
| Failed to upload          | "К сожалению, не удалось загрузить видео"        |
| Try different format      | "Попробуй загрузить видео другого формата"       |
| Back                      | "Назад"                                          |
| Retry                     | "Повторить"                                      |
| Choose new file           | "Выбрать новый файл"                             |
| Assign students           | "Назначить студентов"                            |
| Auto-check answer         | "Автопроверка ответа"                            |
| Explanation               | "Пояснение"                                      |
| Add variant               | "Добавить вариант"                               |
| Shuffle option            | "Перемешивание вариантов"                        |
| Answer type               | "Тип ответа"                                     |
| Coding task               | "Задание с вложениями"                           |
| Test                      | "Тест"                                           |
| Search                    | "Поиск"                                          |
| Students & groups heading | "Учащиеся, вольнослушатели и группы"             |
| Assign                    | "Назначить"                                      |
| Save                      | "Сохранить"                                      |
| Add video name            | "Добавьте название видео"                        |
| Upload file               | "Загрузите файл"                                 |
| Enter variant             | "Введите вариант ответа"                         |
| Change order              | "Изменить порядок"                               |
| Correct answer hint       | "Данный ответ будет считаться правильным"        |
| Wrong answer hint         | "Данный ответ будет считаться ошибочным"         |
