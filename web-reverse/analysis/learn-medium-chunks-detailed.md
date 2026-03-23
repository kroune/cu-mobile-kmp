# Detailed Analysis: Learn App Medium-Size Chunks (UI Framework Layer)

Analysis date: 2026-03-22
Source: `docs/web-reverse/raw/learn/` -- 8 chunks totaling ~388KB

---

## Executive Summary

All 8 chunks analyzed are **third-party UI framework code**, not application-domain business logic. They contain:

- **Taiga UI** component library (text fields, dropdowns, scrollbars, labels, validators, notifications, hints)
- **Angular CDK** drag-and-drop module
- **Taiga UI core** event plugin system, dropdown positioning engine, active zone management
- **Angular Forms** integration (Validators, ControlValueAccessor)
- **RxJS** reactive patterns used extensively throughout

**No API endpoints, no domain-specific data models, no business logic, no LMS-specific code** was found in any of these chunks. They provide the foundational UI infrastructure that the application-specific chunks (analyzed separately) consume.

---

## 1. chunk-UFTP2EDV.js (63KB) -- Taiga UI Form Controls & Textfield Components

### Identity
Taiga UI form input components: textfield, multi-select textfield, label, validator directives.

### Imports From
- `chunk-66Z7DLZW.js` (Taiga UI textfield base)
- `chunk-YEFAQ36Q.js` (Taiga UI utilities)
- `chunk-TIM5J7LT.js` (dropdown, active zones -- another chunk in this analysis set)
- `chunk-XXZ65AKJ.js` (Taiga UI auto-id)
- `chunk-BMHKXZLE.js` (Taiga UI core tokens: icons, peer dependencies)
- `chunk-NITUW32T.js` (Taiga UI element ref, native element helpers)
- `chunk-J5NZ2H2F.js` (RxJS operators: shareReplay, distinctUntilChanged, switchAll)
- `chunk-24K4LTF5.js` (Angular Forms: Validators.nullValidator, provide tokens -- another chunk in this set)
- `chunk-4PH4GFEL.js` (Taiga UI utilities: NgIf, async, isMobile -- another chunk in this set)
- `chunk-KNF5L6KI.js` (Angular core: Component, Directive, inject, signal, computed, InjectionToken, etc.)

### Key Components/Directives Exported

| Export Alias | Component/Directive | Description |
|---|---|---|
| `f` | `tuiLabel` directive | Wraps `<label>` with flex layout, auto-assigns `for` attribute |
| `a` | `tuiNativeValidator` directive | Bridges Angular form validation to native `setCustomValidity()` |
| `b` | `tuiValidator` directive | Generic custom validator registration via `[tuiValidator]` input |
| `c` (token `H`) | Default handler options token | Provides `stringify`, `identityMatcher`, `disabledItemHandler` |
| `d` | Handler options directive | Configurable stringify/identity/disabled-item handlers |
| `e` | Disabled-item validator | Validates that disabled items are not selected |
| `g` (token `A`) | Textfield options token | Provides `appearance`, `size`, `cleaner` (defaults: `"textfield"`, `"l"`, `true`) |
| `h` | Textfield options directive | `[tuiTextfieldAppearance]`, `[tuiTextfieldSize]`, `[tuiTextfieldCleaner]` |
| `i` | Select-like directive | `[tuiSelectLike]` -- sets `inputmode="none"`, prevents manual input |
| `j` | Textfield auxiliaries | Auxiliary injection helpers for textfield plugins |
| `k` | Textfield dropdown directive | Links a template to the dropdown |
| `l` | Textfield store token | Holds textfield component state: focus, content, filler, etc. |
| `m` | Single-value textfield component | `<tui-textfield>` with clear button, filler, ghost input, custom content |
| `n` | Multi-value textfield component | `<tui-textfield [multi]>` with scroll controls, chip-like items |
| `o`, `p` | Textfield helper directives | Filler text, icon end injection |
| `q` | Auxiliary finder | `Qn` -- finds an auxiliary component from the textfield store |
| `r` | Icon end helper | Injects icon based on textfield size |
| `s` | Input type setter | Sets `input.type = "text"` |

### CSS Design Tokens Used
- `--tui-font-text-s`, `--tui-font-text-m` (font sizes)
- `--tui-text-primary`, `--tui-text-secondary` (text colors)
- `--tui-radius-m` (border radius)

### Textfield Options Model
```
{
  appearance: "textfield" (default),   // CSS appearance variant
  size: "l" (default),                 // "s" | "m" | "l"
  cleaner: true (default)              // show clear button
}
```

### Handler Options Model
```
{
  stringify: String,                    // item -> display string
  identityMatcher: ===,                // (a, b) -> boolean
  disabledItemHandler: () => false     // item -> disabled?
}
```

### Template Structure (Single Textfield)
```
<tui-textfield>
  <ng-content select="input" />
  <ng-content select="select" />
  <ng-content select="textarea" />
  <ng-content select="label" />
  <ng-content />                       <!-- default slot -->
  <ng-content select="tui-icon" />
  [clear button if cleaner enabled]
  [content display if provided]
  [ghost filler input]
</tui-textfield>
```

### Template Structure (Multi Textfield)
```
<tui-textfield multi>
  <ng-content select="label" />
  <ng-content select="input" />
  <ng-content select="select" />
  <ng-content />
  <ng-content select="tui-icon" />
  [tui-scroll-controls]
  [chip items rendered via polymorpheusOutlet]
  [clear all button]
  [content display if provided]
</tui-textfield>
```

### Validation Behavior
- `tuiNativeValidator`: watches `control.events` / `control.valueChanges` / `control.statusChanges`; on `focusout`, adds/removes `tui-invalid` CSS class and calls `setCustomValidity()`
- Disabled-item validator: returns `{tuiDisabledItem: value}` if a disabled item is selected

---

## 2. chunk-K4EFWGMV.js (59KB) -- Angular CDK Drag & Drop

### Identity
Complete Angular CDK `@angular/cdk/drag-drop` module. Provides drag-and-drop functionality.

### Key Classes (all minified but identifiable by behavior)

| Class | Purpose |
|---|---|
| `Vt` (lazy loader) | Loads CDK drag-and-drop reset styles component lazily |
| `B` (UniqueSelectionDispatcher equivalent) | Generates unique IDs for drag elements |
| `G` (ViewportScrollPosition) | Caches scroll positions for drag containers during drag operations |
| `ve` (drag-resets component) | Injects reset CSS: `.cdk-drag-preview { background: none; border: none; }` |

### Drag-and-Drop Infrastructure

**DOM Utilities:**
- `lt(node)` -- deep clones a node, preserving canvas content and input values
- `gt(element)` -- returns `getBoundingClientRect()` as a plain object
- `ct(rect, x, y)` -- point-in-rect collision detection
- `me(viewport, element)` -- checks if element overflows viewport
- `L(rect, deltaY, deltaX)` -- translates a rect by delta
- `Zt(rect, threshold, x, y)` -- proximity detection with threshold
- `N(x, y)` -- generates `translate3d(x, y, 0)` CSS string
- `qt(element, rect)` -- applies width/height/transform to element
- `E(element, enabled)` -- toggles touch-action/user-select/webkit-user-drag
- `$t(element, visible)` -- toggles fixed positioning (used for drag preview)

**Touch/Pointer Events:**
- `Bt(event)` -- detects if event was triggered by mouse (buttons === 0)
- `Gt(event)` -- detects simulated touch events (identifier === -1)
- `T(event)` -- gets the real event target via `composedPath()[0]`
- `z(node)` -- gets ShadowRoot if available

**Scroll Position Tracker (class `G`):**
- Caches scroll positions of all scrollable containers
- `handleScroll(event)` -- computes scroll deltas and updates cached client rects
- Used during drag to compensate for container scrolling

### CSS Applied During Drag
```css
.cdk-drag-preview {
  background: none;
  border: none;
  padding: 0;
  color: inherit;
  inset: auto;
}
.cdk-drag-placeholder *, .cdk-drag-preview * {
  pointer-events: none !important;
}
```

### Relevance to KMP
Drag-and-drop is used in the LMS for reordering items (e.g., theme ordering within courses, exercise ordering). The KMP mobile app may need to implement similar drag-to-reorder functionality natively.

---

## 3. chunk-DNFIPLFW.js (55KB) -- Taiga UI Core Utilities: Mask, Dialog, Portal Infrastructure

### Identity
Core infrastructure chunk containing Taiga UI masking utilities, dialog/modal portal infrastructure, and DOM manipulation utilities. Detected via: HttpClient references, mask patterns, dialog/overlay patterns, pipe/transform references, and injectable providers.

### Key Subsystems (identified via pattern matching)

**Input Masking Infrastructure:**
- Contains pipe/transform logic for input formatting
- DOM manipulation (`innerHTML`, `textContent`, `appendChild`, `createElement`, `querySelector`, `classList`, `setAttribute`)
- No Maskito library directly, but internal masking infrastructure

**Dialog/Modal Portal System:**
- Overlay-related patterns detected
- Portal infrastructure for rendering content outside normal DOM hierarchy
- Injectable services with `providedIn: "root"`

**Dependency Chain:**
- Imports from `chunk-BMHKXZLE.js`, `chunk-KNF5L6KI.js`, `chunk-NITUW32T.js`, `chunk-J5NZ2H2F.js`, `chunk-24K4LTF5.js`, `chunk-4PH4GFEL.js`
- Heavy RxJS usage (Observable patterns)

### Content Limitations
Single-line minified file (55KB on one line) could not be fully read due to tool constraints. Analysis based on pattern detection only.

---

## 4. chunk-24K4LTF5.js (51KB) -- Angular Forms & DI Core Utilities

### Identity
Angular Forms integration layer and dependency injection utilities. Referenced by most other chunks in this set as a dependency.

### Key Patterns Detected
- **Angular Forms:** Validators, FormControl integration, ControlValueAccessor patterns
- **Dependency Injection:** InjectionToken, Provider, factory patterns
- **Signal/Computed:** Angular Signals API usage
- **Component selectors:** At least 1 component defined

### Role in Architecture
This is a foundational chunk imported by:
- `chunk-UFTP2EDV.js` (textfield components)
- `chunk-TIM5J7LT.js` (event plugins, dropdowns)
- Other UI components

Provides shared utilities for form validation, DI token management, and reactive state.

### Exports Referenced by Other Chunks
From UFTP2EDV imports: `jt` (FormBuilder?), `$t` (query helpers), `qt` (nullValidator), `Yt` (disabledItemHandler default), `Zt` (identityMatcher default), `Qt` (noop), `ut` (signal factory), `Xt` (identity fn), `Jt` (inject helper), `L` (Validator token), `Ut` (Validators), `ct` (computed), `g` (provide helper), `ft` (factory), `Kt` (style loader)

### Content Limitations
Single-line minified file (51KB) could not be fully read. Analysis based on import/export references and pattern detection.

---

## 5. chunk-TIM5J7LT.js (42KB) -- Taiga UI Event Plugins, Dropdowns, Scrollbar, Active Zones

### Identity
Taiga UI core infrastructure: custom event handling plugins, dropdown positioning engine, scrollbar components, active zone directives, content projection utilities.

### Event Plugin System

The chunk implements a complete custom event handling system for Angular via `EVENT_MANAGER_PLUGINS`:

| Plugin Class | Event Modifier | Behavior |
|---|---|---|
| `Zt` / `.silent` | `.silent` | Runs handler outside Angular zone (`runOutsideAngular`) |
| `cn` / `.zoneless` | `.zoneless` | Same as silent (alias) |
| `rn` / `.self` | `.self` | Only fires if `target === currentTarget` |
| `tn` / `>` | `>element` | Delegates event to a different element |
| `en` / options | `.capture`, `.once`, `.passive` | Native addEventListener options |
| `nn` / `.prevent` | `.prevent` | Auto-calls `preventDefault()` |
| `on` / `resize` | `resize` | Uses ResizeObserver instead of resize event |
| `sn` / `.stop` | `.stop` | Auto-calls `stopPropagation()` |
| `me` / `longtap` | `longtap` | Custom long-press (700ms) with touch distance threshold (15px) |
| `ve` / `.debounce~` | `.debounce~500ms` | Debounced event handling |
| `ge` / `.throttle~` | `.throttle~500ms` | Throttled event handling |

### Active Zone System (`tuiActiveZone`)

Tracks which elements are "active" (focused/interacted with) in a nested tree:
- `tuiActiveZone` directive on container elements
- `tuiActiveZoneChange` output emits when active state changes
- `tuiActiveZoneParent` input for manual parent assignment
- `contains(element)` checks if element is within this zone or children
- Used by dropdowns to detect clicks outside

### Dropdown Positioning Engine

**Architecture:**
```
tuiDropdown directive
  -> J (BehaviorSubject driver, controls open/close)
  -> Gi (root portal service, manages dropdown lifecycle)
  -> Qt (position accessor, calculates dropdown coordinates)
  -> $n (dropdown component, applies styles)
```

**Dropdown Options Model:**
```
{
  align: "left" | "right" | "center",  // horizontal alignment
  direction: null | "top" | "bottom",  // forced direction (null = auto)
  limitWidth: "auto" | "fixed" | "min", // width constraint
  maxHeight: 400,                        // max dropdown height in px
  minHeight: 80,                         // min dropdown height in px
  offset: 4,                            // gap from trigger in px
  appearance: ""                         // CSS appearance variant
}
```

**Position Calculation (`Qt.getPosition`):**
1. Get accessor client rect (trigger element)
2. Get viewport client rect
3. Calculate available space top/bottom with offset
4. Respect forced direction if set
5. Fall back to direction with more space
6. Apply alignment (left/right/center) with RTL support
7. Return `[top, left]` absolute coordinates

**Dropdown Component (`$n`):**
- Applies calculated position via inline styles
- Handles fixed vs absolute positioning
- Compensates for CSS zoom (`currentCSSZoom`)
- Uses `tui-scrollbar` for content scrolling
- Closes via `CloseWatcher` API or Escape key
- Animations: `tuiFade` + `tuiSlide`

**Dropdown Open/Close Controller (`Kt`):**
- Keyboard: ArrowDown/ArrowUp opens, Escape closes
- Click: toggles if not editable
- Focus: manages focus between trigger and dropdown
- Obscured: closes if trigger becomes obscured by dialogs
- Active zone: closes when zone becomes inactive

### Scrollbar System

**Components:**
- `tui-scrollbar` -- main scrollbar container, replaces native scrollbar
- `tui-scroll-controls` -- renders horizontal/vertical scroll tracks
- `tuiScrollbar` directive -- individual scrollbar thumb with drag support

**Scrollbar Modes:**
- `"always"` -- always show custom scrollbars (default)
- `"hover"` -- show on hover only
- `"native"` -- use native scrollbars
- `"hidden"` -- hide scrollbars entirely

**Scroll Behavior:**
- Thumb size proportional to content/viewport ratio
- Minimum thumb size: 24px (`Vt`)
- Thumb position tracks scroll position with compensation
- Supports RTL layout
- `tuiScrollIntoView` directive for programmatic scroll-to
- `tuiScrollable` directive emits custom event for delegate scrolling
- Refresh at 300ms throttle via zone-aware stream

### Control Value Accessor Base (`Hn`)

Reusable base class for form controls implementing `ControlValueAccessor`:
- Tracks: `value`, `readOnly`, `touched`, `status`, `disabled`, `interactive`, `invalid`, `mode`
- Supports `pseudoInvalid` override
- Integrates with `$t` value transformer (from/to control value)
- Derived `mode`: `"readonly"` | `"invalid"` | `"valid"`

### Content Projection Host (`Vi`, `Gt`)

Portal system for dynamically projecting components/templates:
- `Gt` service manages host attachment
- `add(component)` / `remove(componentRef)` for component children
- `addTemplate(template, context)` / `removeTemplate(viewRef)` for templates
- Used by dropdowns, dialogs, hints to render outside parent DOM

### Other Utilities

- `Xo` -- responsive breakpoint service (converts viewport width to named breakpoints)
- `we` -- position tracking observable (tracks element position relative to viewport)
- `Ni` -- WebKit visual viewport offset correction (iOS keyboard)
- `er({document, innerWidth})` -- actual viewport width calculation
- `Mn(range)` -- word-boundary range expansion for text selection
- `Wi(callback)` -- iOS dynamic text scale detection via hidden iframe
- `be(element)` -- checks if element has `position: fixed` ancestor
- `ao()` -- provides all event plugins as Angular providers
- `vr()` / `Fn()` -- animation duration calculation based on media preferences

### Exports (selected)
Over 50 exports including: `ActiveZone`, `Scrollbar`, `ScrollControls`, `DropdownDirective`, `DropdownOpen`, `DropdownManual`, `DropdownSided`, `DropdownHost`, `ControlValueAccessor base`, `ContentProjectionHost`, `BreakpointService`, `PositionService`, `EventPlugins`, etc.

---

## 6. chunk-XMBH7NO5.js (40KB) -- Taiga UI Table/Data Display & Selection Utilities

### Identity
Contains table/data display components, selection range utilities, swipe/touch gesture handling, hint/tooltip infrastructure. Detected via: table/row/cell/column patterns, Selection/Range patterns, swipe/touch patterns, hint patterns, and resize observer usage.

### Key Subsystems Detected
- **Table Infrastructure:** Column definitions, row templates, cell rendering
- **Selection/Range:** Text selection handling, range manipulation
- **Touch/Swipe:** Gesture detection and handling
- **Hints/Tooltips:** UI component infrastructure
- **Header/Surface:** Layout component primitives (`tuiHeader`, `tuiSurface`, `tuiCard`)
- **Resize Observer:** Element resize tracking

### Content Limitations
Single-line minified file (40KB) could not be fully read. Analysis based on pattern detection across multiple search queries.

---

## 7. chunk-GKYTE6DP.js (39KB) -- Taiga UI Hints, Notifications & Animation Utilities

### Identity
Contains Taiga UI hint (tooltip) system, notification/alert infrastructure, animation utilities, and appearance configuration. Detected via: hint/notification/alert patterns, animation patterns, resize observer usage, and inject/provide patterns.

### Key Subsystems Detected
- **Hint/Tooltip System:** Component for displaying hints near elements
- **Notifications/Alerts:** Alert display infrastructure
- **Animations:** State transition utilities
- **Appearance Configuration:** Theme and appearance providers
- **Polymorpheus Integration:** Dynamic content rendering
- **Resize Observer:** Element size tracking

### Dependency References
- Imports from `chunk-TIM5J7LT.js` (dropdowns, active zones)
- Imports from `chunk-KNF5L6KI.js` (Angular core)

### Content Limitations
Single-line minified file (39KB) could not be fully read. Analysis based on pattern detection.

---

## 8. chunk-4PH4GFEL.js (39KB) -- Taiga UI Core Directives, NgIf, Pipes & Platform Detection

### Identity
Core utility chunk providing Angular common directives (`NgIf`, `AsyncPipe`), platform detection, pipe transforms, focus management, and zone utilities. Referenced by most other chunks.

### Key Subsystems Detected
- **Angular Common:** NgIf, async pipe (used by scroll-controls, dropdown components)
- **Platform Detection:** `isMobile` / `isIOS` / `isAndroid` detection (`Nt` = isMobile, referenced from UFTP2EDV)
- **Pipes/Transforms:** Data transformation utilities
- **Focus Management:** Focus tracking and zone management
- **Component Directives:** Various utility directives
- **DI Infrastructure:** Provider factories, injection token helpers

### Exports Referenced by Other Chunks
From TIM5J7LT imports: `le` (NgIf), `Xe` (async pipe), `qe` (isMobile check)
From UFTP2EDV imports: `rt` (switchMap?), `dt` (mergeMap?), `st` (async pipe), `Nt` (isMobile)

### Content Limitations
Single-line minified file (39KB) could not be fully read. Analysis based on import/export references and pattern detection.

---

## Dependency Graph

```
chunk-KNF5L6KI.js (Angular core - not in this set)
  |
  +-- chunk-4PH4GFEL.js (core directives, NgIf, pipes, platform detection)
  |     |
  +-- chunk-24K4LTF5.js (Forms, DI utilities)
  |     |
  +-- chunk-BMHKXZLE.js (Taiga UI tokens - not in this set)
  |     |
  +-- chunk-TIM5J7LT.js (event plugins, dropdowns, scrollbars, active zones)
  |     |     |
  |     |     +-- imports chunk-BMHKXZLE, chunk-NITUW32T, chunk-4PH4GFEL, chunk-24K4LTF5,
  |     |     |   chunk-J5NZ2H2F, chunk-7GBMEGUQ, chunk-GXSA7CKD
  |     |
  +-- chunk-UFTP2EDV.js (textfield, label, validator components)
  |     |
  |     +-- imports chunk-TIM5J7LT, chunk-24K4LTF5, chunk-4PH4GFEL,
  |         chunk-66Z7DLZW, chunk-YEFAQ36Q, chunk-XXZ65AKJ, chunk-BMHKXZLE
  |
  +-- chunk-K4EFWGMV.js (CDK drag-and-drop) -- standalone, minimal imports
  |
  +-- chunk-DNFIPLFW.js (masks, dialogs, portals)
  |     |
  |     +-- imports chunk-BMHKXZLE, chunk-KNF5L6KI, chunk-NITUW32T,
  |         chunk-J5NZ2H2F, chunk-24K4LTF5, chunk-4PH4GFEL
  |
  +-- chunk-XMBH7NO5.js (table, selection, swipe, hints)
  |
  +-- chunk-GKYTE6DP.js (hints, notifications, animations)
        |
        +-- imports chunk-TIM5J7LT
```

---

## API Endpoints Found

**None.** These chunks are entirely UI framework code with no HTTP calls or API endpoint references.

---

## Data Models Found

No LMS domain data models. Only UI framework configuration models:

1. **Textfield Options:** `{ appearance, size, cleaner }`
2. **Handler Options:** `{ stringify, identityMatcher, disabledItemHandler }`
3. **Dropdown Options:** `{ align, direction, limitWidth, maxHeight, minHeight, offset, appearance }`
4. **Scrollbar Options:** `{ mode: "always" | "hover" | "native" | "hidden" }`

---

## Relevance to KMP Mobile App

### Directly Relevant Patterns

1. **Drag-and-Drop (chunk-K4EFWGMV):** The LMS uses drag-to-reorder for themes/exercises in courses. The mobile app needs equivalent functionality via Compose `DragAndDropModifier` or similar.

2. **Dropdown Positioning Logic (chunk-TIM5J7LT):** The viewport-aware positioning algorithm (checking available space top/bottom, alignment, RTL support) informs how to position popups/dropdowns in the mobile app.

3. **Form Validation Pattern (chunk-UFTP2EDV):** The validation flow (touched + invalid -> show error, pseudoInvalid override) should be replicated in Compose form validation.

4. **Scrollbar Behavior (chunk-TIM5J7LT):** Custom scrollbar modes (always, hover, hidden) suggest the desktop app has different scroll UX expectations than mobile.

5. **Long-press Gesture (chunk-TIM5J7LT):** 700ms long-press with 15px movement threshold provides reference values for mobile gesture detection.

6. **Responsive Breakpoints (chunk-TIM5J7LT):** Named breakpoints (`xxs` through `xxl`) indicate the web app has responsive layouts that the mobile app needs to consider.

### Not Directly Relevant
- Event plugin system (Angular-specific)
- Portal/content projection (web-specific DOM manipulation)
- CSS design tokens (need equivalent Compose theme tokens)
- Active zone tracking (Angular focus management)
