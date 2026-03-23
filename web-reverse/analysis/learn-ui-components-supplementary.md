# CU Learn App -- UI Components Supplementary Analysis

Supplements `learn-ui-components-detailed.md` with raw extracted data and cross-references.

---

## 1. Full Component Selector Map

### chunk-BF6EGGKK.js (Course Editor Module)

| Selector                          | Type      | Purpose                                       |
|-----------------------------------|-----------|-----------------------------------------------|
| `cu-input-date-time-group`        | Component | Date + Time paired input                      |
| `cu-date-time-form`               | Component | Date/time inline edit form with submit/cancel |
| `cu-name-form`                    | Component | Name inline edit form with submit/cancel      |
| `cu-publish-state-icon`           | Component | Published/draft status icon                   |
| `cu-longread-create`              | Component | Lesson/handout creation form                  |
| `cu-longread-publish-date-update` | Component | Longread publish date editor                  |
| `cu-longread-update`              | Component | Longread name editor                          |
| `cu-longread-item`                | Component | Longread row in theme list                    |
| `cu-theme-create`                 | Component | Theme creation form                           |
| `cu-theme-publish-date-update`    | Component | Theme publish date editor                     |
| `cu-theme-update`                 | Component | Theme name editor                             |
| `cu-theme-item`                   | Component | Expandable theme row                          |
| `cu-course-edit`                  | Component | Top-level course editor                       |
| `tui-input-date`                  | Taiga UI  | Date picker input                             |
| `tui-input-time`                  | Taiga UI  | Time picker input                             |

### chunk-5URS2G45.js (Filter/Search Module)

| Selector                         | Type      | Purpose                                              |
|----------------------------------|-----------|------------------------------------------------------|
| `cu-expandable-search-input`     | Component | Animated expandable search bar                       |
| `cu-multiselect-searchable-list` | Component | Searchable checkbox list in dropdown                 |
| `cu-multiselect-filter`          | Component | Filter dropdown with multiselect, used in list views |
| `tui-arrow`                      | Taiga UI  | Dropdown arrow indicator                             |
| `tui-input`                      | Taiga UI  | Text input                                           |
| `tui-tag`                        | Taiga UI  | Tag/chip display                                     |
| `tui-input-tag`                  | Taiga UI  | Tag input with add/remove                            |
| `tui-multi-select-option`        | Taiga UI  | Option in multiselect                                |
| `tui-multi-select`               | Taiga UI  | Multiselect dropdown                                 |

### chunk-6KSXS3ZZ.js (Angular Router)

| Selector        | Type    | Purpose              |
|-----------------|---------|----------------------|
| `router-outlet` | Angular | Route view container |

---

## 2. Component Input/Output Cross-Reference

### BF6EGGKK -- Inputs

```
cu-input-date-time-group:
  size, datePlaceholder, timePlaceholder, min, labelDate, labelTime

cu-date-time-form:
  initialValue, min, loading

cu-name-form:
  loading, placeholder, nameForm

cu-publish-state-icon:
  published

cu-longread-create:
  themeId, longreadType, placeholder

cu-longread-publish-date-update:
  initialValue, minPublishDate

cu-longread-update:
  initialValue, placeholder

cu-longread-item:
  theme, longread, rowIndex, actionFactories

cu-theme-create:
  courseId

cu-theme-update:
  initialValue

cu-theme-publish-date-update:
  initialValue

cu-theme-item:
  courseOverview, theme, rowIndex, actionFactories

cu-course-edit:
  courseId
```

### BF6EGGKK -- Outputs

```
cu-date-time-form:     submitted
cu-name-form:          submitted
cu-longread-create:    created
cu-longread-update:    updated
cu-longread-publish-date-update: updated
cu-longread-item:      updated
cu-theme-create:       created
cu-theme-update:       updated
cu-theme-publish-date-update:  updated
cu-theme-item:         expandTransitionEnd, updated
```

### 5URS2G45 -- Inputs

```
cu-expandable-search-input:
  disableSearch, after, searchPlaceholder, size

cu-multiselect-searchable-list:
  stringify, searchRequester, sortedItems

cu-multiselect-filter:
  controlName, items, placeholder, selectedPrefix, icon,
  selectedValueFormat, selectedValueVisibleCount,
  identityMatcher, stringify, searchRequester,
  dropdownAppearance, optionContent, isLoading
```

### 5URS2G45 -- Outputs

```
cu-expandable-search-input: searchChange
cu-multiselect-searchable-list: (no direct outputs, uses data-binding)
cu-multiselect-filter: search
```

---

## 3. Full Russian UI Text Map

### Form Controls

| Context                | Russian Text                                   |
|------------------------|------------------------------------------------|
| Date placeholder       | "ДД.ММ.ГГГГ"                                   |
| Future time validation | "Укажите будущее время"                        |
| Save button            | "Сохранить"                                    |
| Close button           | "Закрыть"                                      |
| Required field error   | "Поле обязательное"                            |
| Max length error       | "Длина поля не может быть больше {N} символов" |
| Submit/Send            | "Отправить"                                    |
| Cancel                 | "Отменить"                                     |

### Course Management

| Context                   | Russian Text                                                             |
|---------------------------|--------------------------------------------------------------------------|
| Delete                    | "Удалить"                                                                |
| Edit                      | "Редактировать"                                                          |
| Activities                | "Активности"                                                             |
| Publish                   | "Опубликовать"                                                           |
| Unpublish                 | "Снять с публикации"                                                     |
| Reports                   | "Ведомость"                                                              |
| Not published             | "Не опубликован"                                                         |
| Published                 | "Опубликован"                                                            |
| Set deferred publish date | "Установить дату отложенной публикации"                                  |
| Course visibility note    | "Курс станет видимым для студентов вместе с первой опубликованной темой" |

### Deletion Confirmations

| Entity  | Title                                                | Body                                                                                    |
|---------|------------------------------------------------------|-----------------------------------------------------------------------------------------|
| Course  | "Удалить курс?"                                      | "При удалении курса также удаляются все его темы, уроки и материалы"                    |
| Theme   | "Удалить тему?"                                      | "При удалении темы также удаляются все её уроки и материалы"                            |
| Lesson  | "Удалить урок?"                                      | "При удалении урока также удаляются все материалы"                                      |
| Handout | "Удалить материал для преподавателей и ассистентов?" | "При удалении материала для преподавателей и ассистентов также удаляются все материалы" |

### Publish Confirmations

| Entity | Title                | Body                                                                                                                              |
|--------|----------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| Course | "Опубликовать курс?" | "Если опубликовать курс сейчас, то станут видны все темы и уроки. Также студентам назначатся задания и начнется отсчет дедлайнов" |
| Theme  | "Опубликовать тему?" | "Если опубликовать тему сейчас, то станут видны все уроки. Также студентам назначатся задания и начнется отсчет дедлайнов"        |
| Lesson | "Опубликовать урок?" | "Если опубликовать урок сейчас, то материалы станут доступны, студентам назначатся задания и начнется отсчет дедлайнов"           |

### Unpublish Confirmations

| Entity | Title                        | Body                                                                                                                                                                         |
|--------|------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Course | "Перевести курс в черновик?" | "Если перевести курс в черновик, то все темы и уроки также будут переведены в этот статус, и студенты больше не смогут их видеть. Все назначенные задания станут недоступны" |
| Theme  | "Перевести тему в черновик?" | "Если перевести тему в черновик, то все уроки также будут переведены в этот статус, и студенты больше не смогут их видеть. Все назначенные задания станут недоступны"        |
| Lesson | "Перевести урок в черновик?" | "Если перевести урок в черновик, то все опубликованные материалы также будут сняты с публикации и станут недоступны"                                                         |

### Success/Error Toasts

| Operation                | Success                                            | Error                                                                               |
|--------------------------|----------------------------------------------------|-------------------------------------------------------------------------------------|
| Delete course            | "Курс удален"                                      | "Возникла ошибка при удалении курса"                                                |
| Publish course           | "Курс опубликован"                                 | "Возникла ошибка в процессе публикации курса"                                       |
| Draft course             | "Курс переведен в черновик"                        | "Возникла ошибка при изменении статуса курса"                                       |
| Delete theme             | "Тема удалена"                                     | "Возникла ошибка при удалении темы"                                                 |
| Publish theme (solo)     | "Тема опубликована"                                | "Возникла ошибка в процессе публикации темы"                                        |
| Publish theme+course     | "Тема и курс опубликованы"                         | --                                                                                  |
| Draft theme              | "Тема переведена в черновик"                       | "Возникла ошибка при изменении статуса темы"                                        |
| Create theme             | "Тема создана"                                     | "Не удалось создать тему. {msg}"                                                    |
| Update theme             | "Тема отредактирована"                             | "Не удалось обновить тему. {msg}"                                                   |
| Delete lesson            | "Урок удален"                                      | "Возникла ошибка при удалении урока"                                                |
| Publish lesson           | "Урок опубликован"                                 | "Возникла ошибка в процессе публикации урока"                                       |
| Lesson unpublished theme | --                                                 | "Невозможно опубликовать урок, так как тема не опубликована"                        |
| Draft lesson             | "Урок переведен в черновик"                        | "Возникла ошибка при изменении статуса урока"                                       |
| Create lesson            | "Урок создан"                                      | "Не удалось создать урок"                                                           |
| Create handout           | "Материал для преподавателей и ассистентов создан" | "Не удалось создать материал для преподавателей и ассистентов"                      |
| Delete handout           | "Материал для преподавателей и ассистентов удален" | "Возникла ошибка при удалении материала для преподавателей и ассистентов"           |
| Update longread          | --                                                 | "Не удалось обновить лонгрид. {msg}"                                                |
| Deferred date set        | "Дата отложенной публикации установлена"           | --                                                                                  |
| Deferred date changed    | "Дата отложенной публикации изменена"              | --                                                                                  |
| Deferred date removed    | "Дата отложенной публикации удалена"               | --                                                                                  |
| Reorder themes           | --                                                 | "Возникла ошибка во время сохранения порядка тем. Пожалуйста попробуйте еще раз"    |
| Reorder lessons          | --                                                 | "Возникла ошибка во время сохранения порядка уроков. Пожалуйста попробуйте еще раз" |

### Filter/Search Labels

| Context            | Russian Text                          |
|--------------------|---------------------------------------|
| Open search        | "Открыть поиск"                       |
| Close search       | "Закрыть поиск"                       |
| Nothing found      | "По вашему запросу ничего не найдено" |
| Search placeholder | "Поиск"                               |
| Clear              | "Очистить"                            |

### Validation Messages

| Validator                  | Russian Message                                                                      |
|----------------------------|--------------------------------------------------------------------------------------|
| Russian name invalid chars | "Введен недопустимый символ, разрешены только кириллические буквы, пробел и -"       |
| English name invalid chars | "Введен недопустимый символ, разрешены только латинские буквы, пробел, апостроф и -" |
| File too large             | "Размер одного из файлов превышает лимит {N}MiB"                                     |
| Same language required     | "Язык в связанных полях должен быть один"                                            |
| Precision format           | "Ответ не соответствует формату {format}"                                            |

### Placeholder Templates

| Context                     | Text                                                        |
|-----------------------------|-------------------------------------------------------------|
| Theme creation              | "Тема 1. Описание..."                                       |
| Lesson creation             | "Урок 1. Описание..."                                       |
| New handout                 | "Новый материал для преподавателей и ассистентов"           |
| Handout section description | "Здесь лежат скрытые материалы, которые не видны студентам" |

---

## 4. Chunk Dependency Graph

```
chunk-BF6EGGKK.js (Course Editor)
  imports from:
    chunk-KTNMAB4T.js
    chunk-XHD4IVZT.js
    chunk-EDVYOVIJ.js
    chunk-K4EFWGMV.js   (form utilities: Fi, Jn, Zn, Kn)
    chunk-PGX77SDS.js
    chunk-CCU76LAC.js
    chunk-PCCRX24G.js
    chunk-OLC3OSQO.js
    chunk-YBIXENKV.js
    chunk-VCDSCRHX.js
    chunk-XUXMCUIT.js
    chunk-BAO5U6GC.js
    chunk-IGQL27KA.js
    chunk-QGKG66VQ.js
    chunk-DFIM3YCL.js
    chunk-2UCY7WTM.js
    chunk-ZICG35BS.js
    chunk-4YQIACAT.js
    chunk-X34B7NWC.js   (gt, Qn)
    chunk-FW6HY3OK.js   (Le -- publish state icon)
    chunk-5JTPJSK6.js
    chunk-TZDR3JE7.js   (pt -- date utility)
    chunk-7AZ6L3C2.js   (ct=parseISO, ft=validators)
    chunk-OZNOOWYM.js   (Ci, yi, Di, Hn, jn)
    chunk-GKYTE6DP.js   (shared utilities: Nn, vi, Ti, Rn, bi, Bn, ht, $n, Ne, xi, Ii, Mi)
    chunk-GTGLXNGW.js
    chunk-ITXQLBJ5.js   (hi, Ft, fi, gi, _i)
    chunk-J6TKB4NW.js
    chunk-DWZTIVYQ.js   (ui, pi -- dialog/prompt)
    chunk-2D7MVTTE.js   (J -- injection)
    chunk-YSPW2MHC.js   (Ji, _t, wi -- forms)
    chunk-XY6YZP74.js   (Y)
    chunk-O6IXYHUU.js   (di)
    chunk-DPUIKIPP.js   (Vn)
    chunk-X32QAISO.js   (gn, dt, ye, mi, ci, hn, lt, pn, fn)
    chunk-YVQMK22W.js   (sn -- CDK DragDrop)
    chunk-QZLBO5EF.js   (li)
    chunk-UFTP2EDV.js   (un, cn, mn, ln)
    chunk-66Z7DLZW.js   (rn, an, Ui, qi)
    chunk-YAU527B5.js   (ge -- icons)
    chunk-BMHKXZLE.js   (TuiDay, TuiTime, etc.)

chunk-5URS2G45.js (Filter/Search)
  imports from:
    chunk-VXVXPLT2.js
    chunk-LSMGJZJT.js
    chunk-DCHB3XME.js
    chunk-5IQTO7XV.js
    chunk-5BEYH3WW.js
    chunk-OZNOOWYM.js
    chunk-2CYYGRR2.js
    chunk-GKYTE6DP.js
    chunk-2WAR6GED.js
    chunk-GTGLXNGW.js
    chunk-ITXQLBJ5.js
    chunk-X32QAISO.js
    chunk-YVQMK22W.js
    chunk-UFTP2EDV.js
    chunk-66Z7DLZW.js
    chunk-YAU527B5.js
    chunk-K3PAG7Z6.js
    chunk-3ZJICZ6G.js
    chunk-IDTOBA75.js
    chunk-2PCIRN5N.js
    chunk-STFZ2HBP.js
    chunk-MWB6F2K6.js
    chunk-TIM5J7LT.js
    chunk-LET2ZNDS.js
    chunk-XXZ65AKJ.js
    chunk-BMHKXZLE.js
    chunk-NITUW32T.js
    chunk-24K4LTF5.js
    chunk-4PH4GFEL.js
    + Angular core chunk
```

---

## 5. CSS Variable System Summary

### CU Application Variables

```css
/* Colors */
--accent                    /* Primary brand color */
--accent-hover              /* Hover state */
--accent-pressed            /* Pressed/active state */
--accent-on-dark-hover      /* Unpublished icon background */
--positive                  /* Success/confirm green */
--negative                  /* Error/cancel red */
--border                    /* Default border color */
--border-hover              /* Hover border */
--text-secondary            /* Secondary text */
--text-tertiary             /* Tertiary/muted text */
--text-primary-hover        /* Primary text on hover */
--background                /* Page background */
--background-alt            /* Alternate row background */
--neutral-hover             /* Neutral hover state */
--neutral-pressed           /* Neutral pressed state */
--shadow-hover              /* Elevation shadow on drag */

/* Typography */
--font-text-l               /* Large body text */

/* Layout */
--radius-l                  /* Large border radius */

/* Component-specific */
--cu-icon-size
--cu-icon-size-l
--cu-icon-size-xs
--cu-input-date-time-gap: 0.5rem
--cu-label-font-color
--cu-input-text-left-padding-s
--cu-size-1_25
--cu-filter-color
--cu-filter-background
--cu-filter-border-color
--cu-filter-arrow-color
--cu-button-width
--cu-button-height
--cu-button-disabled-opacity
```

### Taiga UI Variables

```css
--tui-duration: 0.3s
--tui-radius-xs / -s / -m / -l
--tui-height-xs / -s / -m / -l
--tui-border-focus
--tui-border-normal
--tui-disabled-opacity
--tui-padding-s
--tui-font-text-m
--tui-font-text-s
--tui-font-text-ui-s
--tui-font-icon
```

---

## 6. Timezone Handling

- Moscow timezone is explicitly used: `Me.Moscow` (referenced multiple times in course editor)
- Date comparisons for publish dates use `moscowToday` computed value
- Min publish date constraint: must be in the future (Moscow time)
- ISO string conversion via `.toISOString()` before API submission

---

## 7. Drag-and-Drop Reordering

Both themes and longreads within themes support drag-and-drop reordering:

**Tracked items:** `Gi.Longread`, `Gi.Theme`
**Tracking functions:** `trackByThemes`, `trackByLongread`

**Error handling:**

- Theme reorder error: "Возникла ошибка во время сохранения порядка тем. Пожалуйста попробуйте еще
  раз"
- Lesson reorder error: "Возникла ошибка во время сохранения порядка уроков. Пожалуйста попробуйте
  еще раз"

**Visual feedback:**

- Dragged item gets `box-shadow: var(--shadow-hover)`
- Placeholder has `opacity: 0.3`
- Handle icon uses `--cu-icon-size-l` with `cursor: move`
- Animation during drag uses Taiga UI's transition duration

---

## 8. Multiselect Filter State Machine (cu-multiselect-filter)

```
States:
  Default    -> border: var(--border), text: var(--text-secondary)
  Hover      -> bg: var(--neutral-hover), text: var(--text-primary-hover), border: var(--border-hover)
  Focused    -> bg: var(--neutral-pressed), same text/border as hover, outline: 1px
  Completed  -> text: var(--accent), border: var(--accent)
  Completed+Hover -> border: var(--accent-hover), text: var(--accent-hover)
  Completed+Focused -> bg: var(--neutral-pressed), text: var(--accent-pressed), outline: 1px
```

The filter supports:

- `stringify` function for displaying items
- `searchRequester` for server-side search
- `identityMatcher` for comparing selected values
- `selectedPrefix` with computed prefix display
- `selectedValueVisibleCount` to limit visible selected items
- `dropdownAppearance` for styling the dropdown
- Clear button per filter value

---

## 9. SNILS Validation Algorithm (from chunk-7AZ6L3C2.js)

Russian SNILS (social security number) validation:

1. Must be 11 digits
2. Checksum: multiply each of first 9 digits by (9 - position), sum them
3. If sum < 100: last 2 digits must equal sum
4. If sum == 100 or 101: last 2 digits must be 00
5. Otherwise: last 2 digits must equal (sum % 101), treating 100 as 0
6. Uniqueness check: no more than 2 consecutive identical digits

---

## 10. INN Validation Algorithm (from chunk-7AZ6L3C2.js)

Russian INN (tax ID) validation uses two coefficient arrays:

```javascript
Z = [7, 2, 4, 10, 3, 5, 9, 4, 6, 8]      // 10-digit INN check
E = [3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8]    // 12-digit INN check
```

Applied as weighted checksum against digits.
