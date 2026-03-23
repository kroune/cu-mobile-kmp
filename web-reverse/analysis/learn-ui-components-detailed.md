# CU Learn App -- UI Components Detailed Analysis

Source: Angular 21.1.4 app at `learn.centraluniversity.ru`
Files analysed:

- `chunk-BF6EGGKK.js` (96KB) -- Course editor: themes, longreads, publish states, inline editing
- `chunk-5URS2G45.js` (83KB) -- Taiga UI input-tag, multi-select, CU filter & search components
- `chunk-7AZ6L3C2.js` (95KB) -- date-fns ISO parser, phone metadata, CU form validators
- `chunk-6KSXS3ZZ.js` (82KB) -- Angular Router module
- `chunk-BMHKXZLE.js` (99KB) -- Taiga UI core: appearance, icons, buttons, date/time primitives

---

## 1. Component Hierarchy (chunk-BF6EGGKK.js)

### 1.1 `cu-course-edit` -- Course Editor (top-level)

**Selector:** `cu-course-edit`
**Inputs:** `courseId`
**Key state properties:**

- `courseOverview` -- loaded course data
- `isLoadingCourseOverview` -- loading spinner flag
- `themesItems` -- list of themes in the course
- `themeCreating`, `longreadCreating`, `handoutCreating` -- flags for inline creation forms
- `queryParamsForScrolling` -- scroll-to-element support

**Child component factories:**

- `courseActionFactories` -- course-level actions (edit, delete, publish/unpublish)
- `themeActionFactories` -- theme-level actions
- `longreadActionFactories` -- lesson-level actions
- `handoutActionFactories` -- handout-level actions

**Layout CSS (from styles):**

```css
:host { display: block }
.course-name { display: flex; gap: 0.25rem; margin-bottom: 1.25rem; align-items: center }
.course-name__text { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; height: 3rem }
.course-name__state { margin-left: auto }
.loader-wrapper { margin: 1.5rem 0 }
.longread-item { margin: 0 1rem; box-sizing: border-box }
.add-longread-item { margin: 0.75rem 1rem 0 }
.add-longread-button { position: relative; margin-left: 1rem }
.add-longread-button:not(:first-child) { margin-top: 0.75rem }
hr { color: var(--border); margin: 0.75rem 1rem }
.add-handout-button { margin: 0.5rem 0 1rem 1rem }
.add-theme-item { margin-top: 1rem }
.add-theme-button { margin-top: 0.75rem }
.theme-list { display: flex; flex-direction: column; gap: 1rem }
.longread-list { display: flex; flex-direction: column }
```

**Drag-and-drop** (CDK DragDrop):

```css
.cdk-drag { border-radius: var(--radius-l); overflow: hidden; list-style-type: none }
.cdk-drag-preview { box-shadow: var(--shadow-hover) }
.cdk-drag-placeholder { opacity: 0.3 }
.cdk-drag-handle { --cu-icon-size: var(--cu-icon-size-l); cursor: move; color: var(--text-tertiary); margin-right: 0.75rem }
```

**Behavior:**

- Course name is displayed with ellipsis truncation
- Theme list and longread list are sortable via CDK DragDrop
- Error on reorder: "Возникла ошибка во время сохранения порядка тем/уроков. Пожалуйста попробуйте
  еще раз"
- Reload mechanism after CRUD operations via `this.reload()`

### 1.2 `cu-theme-item` -- Theme Row

**Selector:** `cu-theme-item`
**Inputs:** `courseOverview`, `theme`, `rowIndex`, `actionFactories`
**Outputs:** `expandTransitionEnd`, `updated`

**Key state:**

- `expanded` -- collapse/expand toggle
- `editName`, `editPublishDate` -- inline editing flags
- `hovered` -- hover highlight
- `mergedTheme`, `editedTheme` -- computed merged model

**Layout CSS:**

```css
:host { display: block; width: 100% }
```

**UI labels (Russian):**

- Collapse: "Свернуть"
- Expand: "Развернуть"

### 1.3 `cu-longread-item` -- Lesson/Longread Row

**Selector:** `cu-longread-item`
**Inputs:** `theme`, `longread`, `rowIndex`, `actionFactories`
**Outputs:** `updated`

**Key state:**

- `hovered` -- hover highlight
- `editName`, `editPublishDate` -- inline editing flags
- `mergedLongread`, `editedLongread` -- computed merged model
- `longReadState` -- publish state
- `lmsCoursesPaths` -- navigation paths

**Layout CSS:**

```css
:host {
  display: flex; align-items: center; min-height: 3rem;
  background-color: #fff; border-radius: var(--radius-l);
}
:host:hover { background-color: var(--background-alt) }
.publish-state-icon { margin-right: 0.5rem }
.name {
  text-overflow: ellipsis; white-space: nowrap; overflow: hidden;
  margin-right: auto; font: var(--font-text-l); color: var(--accent);
}
.name:hover { color: var(--accent-hover) !important }
.actions { margin-right: 0.5rem }
.date-time-chips { cursor: pointer; margin-right: 0.5rem }
```

### 1.4 `cu-longread-create` -- Lesson/Handout Creation Form

**Selector:** `cu-longread-create`
**Inputs:** `themeId`, `longreadType`, `placeholder`
**Outputs:** `created`

**State:** `loading`, `nameForm`
**Longread types:** `Type.Common` (lesson), `Type.Handout` (teacher/assistant material)

**Success messages:**

- Common: "Урок создан"
- Handout: "Материал для преподавателей и ассистентов создан"

**Error message:** "Не удалось создать {урок|материал для преподавателей и ассистентов}"

### 1.5 `cu-theme-create` -- Theme Creation Form

**Selector:** `cu-theme-create`
**Inputs:** `courseId`
**Outputs:** `created`

**State:** `loading`, `nameForm`

**Placeholder:** "Тема 1. Описание..."
**Success:** "Тема создана"
**Error:** "Не удалось создать тему. {error.message}"

### 1.6 `cu-longread-update` -- Lesson Name Update

**Selector:** `cu-longread-update`
**Inputs:** `initialValue`, `placeholder`
**Outputs:** `updated`
**State:** `loading`, `nameForm`

### 1.7 `cu-longread-publish-date-update` -- Publish Date Editor

**Selector:** `cu-longread-publish-date-update`
**Inputs:** `initialValue`, `minPublishDate`
**Outputs:** `updated`

### 1.8 `cu-theme-update` -- Theme Name Update

**Selector:** `cu-theme-update`
**Inputs:** `initialValue`
**Outputs:** `updated`

### 1.9 `cu-theme-publish-date-update` -- Theme Publish Date Editor

**Selector:** `cu-theme-publish-date-update`
**Inputs:** `initialValue`
**Outputs:** `updated`

---

## 2. Shared Form Components (chunk-BF6EGGKK.js)

### 2.1 `cu-date-time-form` -- Date+Time Inline Form

**Selector:** `cu-date-time-form`
**Inputs:** `initialValue`, `min`, `loading`
**Outputs:** `submitted`

**Contains `cu-input-date-time-group` child with:**

- Date input (width: 10rem) with placeholder "ДД.ММ.ГГГГ"
- Time input (width: 7rem)

**Layout:**

```css
:host { display: block }
.date-time-form { display: flex }
.date-time-form__inputs { display: flex; gap: 0.25rem }
.date-time-form__input-date { width: 10rem }
.date-time-form__input-time { width: 7.5rem }
.date-time-form__submit-button { color: var(--positive) !important }
.date-time-form__cancel-button { color: var(--negative) !important }
.input-date-time-group { display: grid; grid-template-columns: 10rem 7rem; gap: 0.25rem }
```

**Validation:** "Укажите будущее время" (shown when past date/time entered)
**Behavior:**

- On submit: calls `getRawValue()`, converts to ISO string via `.toISOString()`
- Cancel and submit buttons with check/X icons
- `showFormValidationErrors` flag controls error display

### 2.2 `cu-name-form` -- Name Inline Edit Form

**Selector:** `cu-name-form`
**Inputs:** `loading`, `placeholder`, `nameForm`
**Outputs:** `submitted`

**Layout:**

```css
:host { display: block }
.name-form { display: flex }
.name-form__input-wrapper { width: 100%; margin-right: 0.5rem }
.name-form__input { width: 100% }
.name-form__submit-button { color: var(--positive) !important }
.name-form__cancel-button { color: var(--negative) !important }
```

**Validation messages:**

- Required: "Поле обязательное"
- Max length: "Длина поля не может быть больше {maxLength} символов"

### 2.3 `cu-publish-state-icon` -- Publish State Badge

**Selector:** `cu-publish-state-icon`
**Inputs:** `published`

**Visual states:**

- Published: positive badge (green check icon)
- Unpublished: accent-on-dark-hover background, text-tertiary color (minus icon)

### 2.4 `cu-input-date-time-group` -- Date+Time Input Group

**Selector:** `cu-input-date-time-group`
**Inputs:** `size`, `datePlaceholder`, `timePlaceholder`, `min`, `labelDate`, `labelTime`

**Layout:**

```css
:host {
  display: flex; justify-content: space-between;
  gap: var(--cu-input-date-time-gap, 0.5rem);
}
.input-label { width: 100%; flex: 1; justify-content: flex-end }
```

---

## 3. Publish State Machine & Action Factories

### 3.1 Enums

**State enum (Se/State):** `Draft`, `Published`
**LongreadState (Le):** `Draft`, `Published`
**LongreadType (W/Type):** `Common` (lessons), `Handout` (teacher materials)
**Action enum (Re):** `Delete`, `Edit`, `Publish`, `Return` (unpublish)
**Appearance:** `Tertiary`
**Role:** `Order`, `Add`
**Navigation (Tn):** `Reports`
**Student filter (Si):** `Students`, `Archived`, `Actual`
**DragItem (Gi):** `Longread`, `Theme`
**Paths:** `Themes`, `Long`

### 3.2 Course Actions

| Action     | Label (RU)           | Confirmation Dialog                                                                                                 |
|------------|----------------------|---------------------------------------------------------------------------------------------------------------------|
| Edit       | "Редактировать"      | --                                                                                                                  |
| Activities | "Активности"         | --                                                                                                                  |
| Publish    | "Опубликовать"       | "Опубликовать курс?" + warning about visible themes/lessons and deadline activation                                 |
| Unpublish  | "Снять с публикации" | "Перевести курс в черновик?" + warning about themes/lessons becoming invisible and assignments becoming unavailable |
| Delete     | "Удалить"            | "Удалить курс?" + "При удалении курса также удаляются все его темы, уроки и материалы"                              |

### 3.3 Theme Actions

| Action               | Label (RU)                              | Confirmation Dialog                                                              |
|----------------------|-----------------------------------------|----------------------------------------------------------------------------------|
| Edit                 | "Редактировать тему"                    | --                                                                               |
| Publish              | "Опубликовать"                          | "Опубликовать тему?" + warning about lessons visibility and assignment deadlines |
| Set deferred publish | "Установить дату отложенной публикации" | --                                                                               |
| Unpublish            | "Снять с публикации"                    | "Перевести тему в черновик?" + warning about lessons/assignments                 |
| Delete               | "Удалить тему"                          | "Удалить тему?" + "При удалении темы также удаляются все её уроки и материалы"   |

### 3.4 Lesson/Longread Actions

| Action                              | Label (RU)                                                | Confirmation Dialog                                                                                                                            |
|-------------------------------------|-----------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Edit lesson                         | "Редактировать урок"                                      | --                                                                                                                                             |
| Edit handout                        | "Редактировать материал для преподавателей и ассистентов" | --                                                                                                                                             |
| Publish                             | "Опубликовать"                                            | "Опубликовать урок?" + warning about materials becoming available and deadline countdown                                                       |
| Publish error (theme not published) | --                                                        | "Невозможно опубликовать урок, так как тема не опубликована"                                                                                   |
| Set deferred publish                | "Установить дату отложенной публикации"                   | --                                                                                                                                             |
| Unpublish                           | "Снять с публикации"                                      | "Перевести урок в черновик?" + warning about materials being unpublished                                                                       |
| Delete lesson                       | "Удалить урок"                                            | "Удалить урок?" + "При удалении урока также удаляются все материалы"                                                                           |
| Delete handout                      | "Удалить материал для преподавателей и ассистентов"       | "Удалить материал для преподавателей и ассистентов?" + "При удалении материала для преподавателей и ассистентов также удаляются все материалы" |

### 3.5 Success/Error Toast Messages

| Operation             | Success (RU)                                       | Error (RU)                                                                |
|-----------------------|----------------------------------------------------|---------------------------------------------------------------------------|
| Delete course         | "Курс удален"                                      | "Возникла ошибка при удалении курса"                                      |
| Publish course        | "Курс опубликован"                                 | "Возникла ошибка в процессе публикации курса"                             |
| Unpublish course      | "Курс переведен в черновик"                        | "Возникла ошибка при изменении статуса курса"                             |
| Delete theme          | "Тема удалена"                                     | "Возникла ошибка при удалении темы"                                       |
| Publish theme         | "Тема опубликована" / "Тема и курс опубликованы"   | "Возникла ошибка в процессе публикации темы"                              |
| Unpublish theme       | "Тема переведена в черновик"                       | "Возникла ошибка при изменении статуса темы"                              |
| Delete lesson         | "Урок удален"                                      | "Возникла ошибка при удалении урока"                                      |
| Publish lesson        | "Урок опубликован"                                 | "Возникла ошибка в процессе публикации урока"                             |
| Unpublish lesson      | "Урок переведен в черновик"                        | "Возникла ошибка при изменении статуса урока"                             |
| Delete handout        | "Материал для преподавателей и ассистентов удален" | "Возникла ошибка при удалении материала для преподавателей и ассистентов" |
| Create lesson         | "Урок создан"                                      | "Не удалось создать урок"                                                 |
| Create handout        | "Материал для преподавателей и ассистентов создан" | "Не удалось создать материал для преподавателей и ассистентов"            |
| Create theme          | "Тема создана"                                     | "Не удалось создать тему. {error}"                                        |
| Update longread       | --                                                 | "Не удалось обновить лонгрид. {error}"                                    |
| Update theme          | "Тема отредактирована"                             | "Не удалось обновить тему. {error}"                                       |
| Publish date changed  | "Дата отложенной публикации изменена"              | --                                                                        |
| Publish date set      | "Дата отложенной публикации установлена"           | --                                                                        |
| Publish date removed  | "Дата отложенной публикации удалена"               | --                                                                        |
| Reorder themes error  | --                                                 | "Возникла ошибка во время сохранения порядка тем"                         |
| Reorder lessons error | --                                                 | "Возникла ошибка во время сохранения порядка уроков"                      |

### 3.6 Course Publish Note

When course is in draft state, a tooltip/note is shown:
"Курс станет видимым для студентов вместе с первой опубликованной темой"

Publish state labels:

- "Не опубликован"
- "Опубликован"

---

## 4. Filter & Search Components (chunk-5URS2G45.js)

### 4.1 `cu-expandable-search-input` -- Expandable Search

**Selector:** `cu-expandable-search-input`
**Inputs:** `disableSearch`, `after`, `searchPlaceholder`, `size`
**Outputs:** `searchChange`

**Layout:**

```css
:host { display: flex; justify-content: space-between; gap: 1rem }
.search-input__in { position: relative; display: flex; gap: 1rem; flex: 1 1 auto; overflow: hidden }
.search-input__content { display: flex; gap: 0.5rem; flex: 1 1 auto; align-items: center; flex-wrap: wrap }
.search {
  position: absolute; inset: 0 0 0 100%; overflow: hidden;
  transition: left 0.3s ease-in-out;
}
.search.search_show { left: var(--cu-expandable-search-input-visible-left, 0) }
```

**Behavior:**

- Search panel slides in from right via CSS transition
- Close button positioned at top-right (top: 0.525rem, right: 0.5rem)
- Labels: "Открыть поиск" / "Закрыть поиск"

### 4.2 `cu-multiselect-searchable-list` -- Searchable Dropdown List

**Selector:** `cu-multiselect-searchable-list`
**Inputs:** `stringify`, `searchRequester`, `sortedItems`

**Layout:**

```css
:host {
  display: flex; flex-direction: column; height: 100%;
  --scrollable-content-height: 16.35rem;
}
.scroll-bar { height: var(--scrollable-content-height); width: 22.5rem; scrollbar-width: none }
.search-control { --t-height: 2.5rem; margin: 0.25rem; flex-shrink: 0 }
.empty-content {
  display: flex; width: 100%; height: calc(var(--scrollable-content-height) - 1rem);
  flex-direction: column; align-items: center; justify-content: center; gap: 0.75rem;
}
.empty-content__image { --empty-content-image-size: 4.575rem; width/height: var(--empty-content-image-size) }
.empty-content__text { width: 10rem; text-align: center }
```

**Empty state:** Shows image + "По вашему запросу ничего не найдено"
**Search:** "Поиск" placeholder

### 4.3 `cu-multiselect-filter` -- Filter Dropdown

**Selector:** `cu-multiselect-filter`
**Inputs:** `controlName`, `items`, `placeholder`, `selectedPrefix`, `icon`, `selectedValueFormat`,
`selectedValueVisibleCount`, `identityMatcher`, `stringify`, `searchRequester`,
`dropdownAppearance`, `optionContent`, `isLoading`
**Outputs:** `search`

**CSS states:**

- Default: secondary text, transparent background, border
- Hover: `--cu-filter-background: var(--neutral-hover)`,
  `--cu-filter-color: var(--text-primary-hover)`
- Focused: `--cu-filter-background: var(--neutral-pressed)`
- Completed (has selection): `--cu-filter-color: var(--accent)`,
  `--cu-filter-border-color: var(--accent)`
- Completed+hover: accent-hover colors
- Completed+focused: accent-pressed colors

**Clear button:** small X button (0.875rem) next to selected value
**Labels:** "Очистить" (Clear)

---

## 5. Form Validators (chunk-7AZ6L3C2.js)

### 5.1 Date/Time Parsing (date-fns `parseISO` equivalent)

Parses ISO 8601 strings with:

- Date: `YYYY`, `YYYY-MM`, `YYYY-MM-DD`, `YYYY-Www`, `YYYY-Www-d`, `YYYY-DDD`
- Time: `HH`, `HH:MM`, `HH:MM:SS`, `HH:MM:SS.sss`
- Timezone: `Z`, `+HH`, `+HH:MM`, `-HH`, `-HH:MM`
- Separator: `T` or space

### 5.2 CU Custom Validators

Exported as a `J` object with these validators:

| Validator                  | Key                                 | Validation Rule                                                                                                                             | Error Message (RU) |
|----------------------------|-------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|--------------------|
| `allFilesAreUploaded`      | `rejected` / `uploading`            | Checks all files have completed upload                                                                                                      | --                 |
| `capitalized`              | `capitalized`                       | First letter must be uppercase                                                                                                              | --                 |
| `cuStaffEmailWithTBank`    | --                                  | Email format for CU staff                                                                                                                   | --                 |
| `dateRange`                | `dateRange`                         | Start date <= End date with configurable comparison                                                                                         | --                 |
| `dependentOnRequiredField` | `dependentOnRequiredFieldValidator` | Field required only if a dependent field has a value                                                                                        | --                 |
| `email`                    | `email`                             | Valid email format                                                                                                                          | --                 |
| `fileAcceptType`           | `invalidFileType`                   | File extension matches accepted types                                                                                                       | --                 |
| `fileSizeMax`              | `maxSize`                           | File size <= limit in MiB; error: "Размер одного из файлов превышает лимит {limit}MiB"                                                      | --                 |
| `ifValuePresent`           | --                                  | Conditional validator: runs inner validator only if value is non-empty                                                                      | --                 |
| `inn`                      | `inn`                               | Russian INN (tax ID) validation via checksum algorithm                                                                                      | --                 |
| `json`                     | `invalidJson`                       | Valid JSON string                                                                                                                           | --                 |
| `max`                      | `max`                               | Numeric maximum                                                                                                                             | --                 |
| `maxDate`                  | `maxDate`                           | Date before maximum                                                                                                                         | --                 |
| `maxLength`                | `maxLength`                         | String max length                                                                                                                           | --                 |
| `min`                      | `min`                               | Numeric minimum                                                                                                                             | --                 |
| `minArrayLength`           | `minArrayLength`                    | Array minimum length                                                                                                                        | --                 |
| `minDate`                  | `minDate`                           | Date after minimum                                                                                                                          | --                 |
| `minLength`                | `minLength`                         | String min length                                                                                                                           | --                 |
| `minValidArrayLength`      | `minValidArrayLength`               | Minimum count of valid items in array                                                                                                       | --                 |
| `moreThanNumber`           | `moreThanNumber`                    | Number > threshold                                                                                                                          | --                 |
| `notValue`                 | `notValue`                          | Value must not equal specific value                                                                                                         | --                 |
| `numberRange`              | `numberRange`                       | Number within range                                                                                                                         | --                 |
| `oneLangForFields`         | `theSameLanguage`                   | "Язык в связанных полях должен быть один" (Language in related fields must be the same)                                                     | --                 |
| `oneLangForFieldsControl`  | --                                  | Per-control version of above                                                                                                                | --                 |
| `pattern`                  | `pattern`                           | Regex pattern match                                                                                                                         | --                 |
| `phoneNumber`              | `invalidPhoneNumber`                | Uses libphonenumber-js metadata for international phone validation                                                                          | --                 |
| `precisionValidator`       | `precision`                         | "Ответ не соответствует формату {format}" (Answer does not match format 0/0.00/etc.)                                                        | --                 |
| `required`                 | `required`                          | Non-empty value required                                                                                                                    | --                 |
| `restricted`               | `restricted`                        | Value matches a blacklisted predicate                                                                                                       | --                 |
| `ruOrEnName`               | --                                  | Name in Russian or English only; Russian: "только кириллические буквы, пробел и -"; English: "только латинские буквы, пробел, апостроф и -" | --                 |
| `snils`                    | `snils`                             | Russian SNILS (social security) validation with checksum + uniqueness check (no >2 consecutive same digits)                                 | --                 |
| `specificValue`            | `specificValue`                     | Must equal a specific value                                                                                                                 | --                 |
| `telegram`                 | `telegram`                          | Telegram username: `^[a-zA-Z0-9_]{5,32}$`                                                                                                   | --                 |
| `url`                      | `url`                               | Valid URL with proper hostname                                                                                                              | --                 |

### 5.3 Phone Number Metadata

Full libphonenumber metadata is embedded for all countries (including Russia `+7`). Contains
per-country:

- `version`, `codes` (calling codes), `countries` (detailed format patterns), `nonGeographic`
  entries.

---

## 6. Taiga UI Components Used (chunk-BMHKXZLE.js)

### 6.1 Core Directives

- `tuiAppearance` -- visual appearance system with data-attributes: `data-appearance`, `data-state`,
  `data-focus`, `data-mode`
- `tuiIcons` -- icon rendering via CSS masks (SVG) or fonts, with `iconStart`/`iconEnd` support
- `tuiButton` / `tuiIconButton` -- button sizes: `xs`, `s`, `m`, `l` (default)
- `tuiTransitioned` -- transition management

### 6.2 Date/Time Primitives

From the Taiga UI date/time system:

- **TuiDay** (`oe` class): immutable date with `year`, `month`, `day`
    - Formats: DMY (DD.MM.YYYY), MDY, YMD
    - Separator: `.` (default for Russian locale)
    - JSON format: `YYYY-MM-DD`
    - `dayBefore()`, `daySame()`, `dayAfter()` comparisons
    - `append({year, month, day})` for date arithmetic

- **TuiMonth** (`L` class): `year` + `month`
    - `monthBefore()`, `monthSame()`, `monthAfter()`

- **TuiTime** (`Ni` class): `hours`, `minutes`, `seconds`, `ms`
    - `shift({hours, minutes, seconds, ms})`
    - String format: `HH:MM` or `HH:MM:SS` or `HH:MM:SS.MSS`
    - AM/PM support (12-hour parsing)

- **TuiDayRange** (`Mi` class): `from` + `to` (TuiDay pair)
- **TuiMonthRange** (`Zt`): `from` + `to` (TuiMonth pair)

### 6.3 Date Format Configuration

```javascript
// Default Russian date format
{ mode: "DMY", separator: "." }
```

Locale tokens:

- `months`, `close`, `back`, `clear`, `nothingFoundMessage`, `defaultErrorMessage`, `spinTexts`,
  `shortWeekDays`
- Week start: Monday (1)

### 6.4 Design Tokens (CSS Variables)

```css
--tui-duration: 0.3s          /* transition duration */
--tui-radius-xs/s/m/l         /* border radii */
--tui-height-xs/s/m/l         /* component heights */
--tui-border-focus             /* focus outline color */
--tui-border-normal            /* normal border */
--tui-disabled-opacity         /* disabled state opacity */
--tui-padding-s: 0.5rem       /* small padding */
--tui-font-text-m              /* medium text */
--tui-font-text-s              /* small text */
--tui-font-text-ui-s           /* small UI text */
```

CU custom tokens:

```css
--cu-icon-size / --cu-icon-size-l / --cu-icon-size-xs
--cu-input-date-time-gap: 0.5rem
--cu-label-font-color
--cu-input-text-left-padding-s
--cu-filter-color / --cu-filter-background / --cu-filter-border-color / --cu-filter-arrow-color
--cu-button-width / --cu-button-height / --cu-button-disabled-opacity
--radius-l                     /* CU's large radius */
--border / --border-hover
--accent / --accent-hover / --accent-pressed
--text-secondary / --text-tertiary / --text-primary-hover
--neutral-hover / --neutral-pressed
--positive / --negative
--background / --background-alt
--shadow-hover
--font-text-l
--accent-on-dark-hover
```

---

## 7. Navigation & Routing (chunk-6KSXS3ZZ.js)

This is the standard Angular Router v21 with:

- `RouterOutlet`, `RouterLink`, `RouterLinkActive` components
- Route configuration with lazy loading (`loadComponent`)
- Query params and fragment support
- Navigation events pipeline: `NavigationStart` -> `RouteConfigLoadStart` -> `RoutesRecognized` ->
  `GuardsCheckStart` -> `ActivationStart` -> `NavigationEnd`
- Error handling: `NavigationError`, `NavigationCancel`
- Scroll position restoration
- `urlUpdateStrategy` and `canceledNavigationResolution` options
- Route reuse strategy support

**Key route structure** (from inputs):

- `routerLink`, `queryParams`, `fragment`, `queryParamsHandling`, `state`, `info`, `relativeTo`
- `preserveFragment`, `skipLocationChange`, `replaceUrl`

---

## 8. Loading & Error State Patterns

### 8.1 Loading States

- `isLoadingCourseOverview` -- shows loader wrapper (1.5rem margin) while course data loads
- `loading` signal on each form component (create/update) -- disables/enables form controls
- `themeCreating`, `longreadCreating`, `handoutCreating` -- shows inline creation forms
- Taiga UI `showLoader` / `._loading` class with overlay pattern

### 8.2 Error Handling Pattern

All mutations follow the same RxJS pattern:

```
action$.pipe(
  // ... API call
  catchError(error => {
    showErrorNotification(errorMessage);
    return EMPTY;
  }),
  takeUntilDestroyed(destroyRef)
).subscribe(() => {
  showSuccessNotification(successMessage);
  this.updated.emit(value);  // or this.created.emit(true)
});
```

### 8.3 Form Validation Display

- `showFormValidationErrors` flag controls when validation errors are visible
- `markAsTouched()` and `markAsPristine()` for form state management
- Disabled state toggled via `disable()` / `enable()` during loading

---

## 9. Data Models (Inferred from Component Properties)

### 9.1 Course Overview

```
CourseOverview {
  state: State (Draft | Published)
  themes: Theme[]
}
```

### 9.2 Theme

```
Theme {
  name: string
  state: State (Draft | Published)
  publishDate?: Date (ISO string)
  longreads: Longread[]
}
```

### 9.3 Longread

```
Longread {
  name: string
  type: LongreadType (Common | Handout)
  state: State (Draft | Published)
  publishDate?: Date (ISO string)
  theme: ThemeReference
}
```

### 9.4 DateTime Form Value

```
DateTimeFormValue {
  date: TuiDay      // DD.MM.YYYY in Russian locale
  time: TuiTime     // HH:MM
}
// Submitted as ISO string via toISOString()
```

---

## 10. Handout vs Lesson Distinction

The app distinguishes two types of longreads:

- **Common (Type.Common)** = "Урок" (Lesson) -- visible to students
- **Handout (Type.Handout)** = "Материал для преподавателей и ассистентов" (Teacher/Assistant
  Material) -- hidden from students

Teacher material section:

- Placeholder: "Новый материал для преподавателей и ассистентов"
- Description: "Здесь лежат скрытые материалы, которые не видны студентам"
- Separate add button and CRUD operations
- Same state machine (Draft/Published) but independent from lesson publishing

**Add buttons in course editor:**

- "Добавить урок" (Add lesson)
- "Материал для преподавателей и ассистентов" (Teacher/assistant material)
- "Добавить тему" (Add theme)

### Navigation

- `Paths.Themes` -- themes route
- `Paths.Long` -- longreads route
- `Tn.Reports` -- reports/activities (ведомость) navigation
