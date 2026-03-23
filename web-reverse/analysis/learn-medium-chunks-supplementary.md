# Supplementary Analysis: Learn App Medium-Size Chunks

Analysis date: 2026-03-22
Companion to: `learn-medium-chunks-detailed.md`

---

## 1. Cross-Chunk Export/Import Map

This section maps how these 8 chunks interconnect and what they export to the broader application.

### chunk-UFTP2EDV.js Exports (used by app code)

```
a -> tuiNativeValidator directive
b -> tuiValidator directive
c -> H (handler options DI token)
d -> De (handler options directive)
e -> Li (disabled-item validator)
f -> ke (tuiLabel directive)
g -> A (textfield options DI token)
h -> Wn (textfield options directive)
i -> Rn (select-like directive)
j -> Me (auxiliary provider factory)
k -> Pn (textfield dropdown directive)
l -> kt (textfield store token)
m -> et (single textfield component)
n -> ri (multi textfield component)
o -> Nn (textfield helper)
p -> si (textfield helper)
q -> Qn (auxiliary finder)
r -> Un (icon-end helper)
s -> jn (input type="text" setter)
```

### chunk-K4EFWGMV.js Exports

Full CDK drag-and-drop API including:

- `CdkDrag`, `CdkDropList`, `CdkDragHandle`, `CdkDragPreview`, `CdkDragPlaceholder`
- `DragRef`, `DropListRef`
- `moveItemInArray`, `transferArrayItem`
- `DragDropModule`

### chunk-TIM5J7LT.js Exports (54 exports)

```
a -> $t (ValueTransformerBase)
b -> _r (ValueTransformer provider factory)
c -> Rn (default transformer)
d -> Hn (ControlValueAccessor base)
e -> Ar (ControlValueAccessor provider)
f -> Vi (ContentProjectionHost)
g -> Gt (PortalService)
h -> ji (portal provider factory)
i -> Ui (PortalItem class)
j -> X (ActiveZone directive)
k -> lr (iOS text scale detector)
l -> Yt (Obscured directive)
m -> fr (hint containment check)
n -> jt (options merge utility)
o -> mr (breakpoint comparison)
p -> An (animation duration: 300ms base)
q -> vr (animation value factory)
r -> Fn (animation duration calculator)
s -> R (PositionAccessor base)
t -> q (DropdownAccessor base)
u -> De (accessor resolution utility)
v -> xi (position accessor provider)
w -> _i (dropdown accessor provider)
x -> Ai (position driver provider)
y -> Fi (dropdown driver provider)
z -> Pi (vehicle provider)
A -> yt (Driver base)
B -> ye (driver provider factory)
C -> Ri (DriverConnector directive)
D -> Li (scrollbar options value)
E -> Ho (scrollbar options provider)
F -> bn (ScrollControls component)
G -> ki (Scrollbar component)
H -> Lo (ScrollIntoView directive)
I -> ko (ScrollRef directive)
J -> No (Scrollable directive)
K -> Xo (BreakpointService)
L -> we (PositionService)
M -> Ni (WebkitViewportCorrection)
N -> er (viewportWidth helper)
O -> Mn (word-boundary range expander)
P -> ao (event plugins provider)
Q -> Zn (dropdown component token)
R -> Gi (root dropdown portal service)
S -> Un (dropdown options override provider)
T -> Vn (dropdown options directive)
U -> wt (Dropdown directive)
V -> $n (Dropdown component)
W -> Kt (DropdownOpen directive)
X -> _s (DropdownManual directive)
Y -> As (DropdownOpenChange directive)
Z -> Fs (DropdownSided directive)
_ -> Ps (dropdown binding factory)
$ -> Rs (dropdown enabled binding)
aa -> Hs (dropdown open signal)
ba -> Ls (dropdown limit-width=fixed)
ca -> ks (dropdown limit-width=auto)
da -> Ns (Dropdowns host component)
ea -> Ws (dropdown host wrapper directive)
```

### chunk-24K4LTF5.js Exports (referenced from UFTP2EDV imports)

```
F -> jt (unknown, possibly FormBuilder)
G -> $t (query helpers)
I -> qt (Validators.nullValidator)
K -> Yt (default disabledItemHandler = () => false)
O -> Zt (default identityMatcher)
a -> Qt (noop function)
d -> ut (signal/writable factory)
da -> Xt (identity function x => x)
ha -> Jt (inject helper)
i -> L (NG_VALIDATORS token)
j -> Ut (Validators class)
l -> ct (computed signal factory)
qa -> g (provide helper: creates provider)
ta -> ft (factory function)
va -> Kt (style component loader)
```

### chunk-4PH4GFEL.js Exports (referenced from other imports)

```
j -> rt (RxJS operator, likely switchMap)
k -> dt (RxJS operator, likely mergeMap)
p -> st (async pipe or similar)
z -> Nt (isMobile / platform detection)
```

---

## 2. Taiga UI Component Hierarchy

Based on the analyzed chunks, the Taiga UI component hierarchy used in the LMS is:

```
tui-root (from other chunks)
  +-- tui-dropdowns (portal host for dropdowns) [chunk-TIM5J7LT]
  +-- tui-dialogs (portal host for dialogs) [chunk-DNFIPLFW]
  +-- tui-hints (portal host for hints) [chunk-GKYTE6DP]
  |
  +-- [application content]
       +-- tui-textfield [chunk-UFTP2EDV]
       |     +-- <input> / <select> / <textarea>
       |     +-- tui-icon
       |     +-- tui-dropdown [chunk-TIM5J7LT]
       |           +-- tui-scrollbar [chunk-TIM5J7LT]
       |                 +-- tui-scroll-controls [chunk-TIM5J7LT]
       |
       +-- label[tuiLabel] [chunk-UFTP2EDV]
       +-- [tuiActiveZone] [chunk-TIM5J7LT]
       +-- tui-scrollbar [chunk-TIM5J7LT]
       +-- [cdkDrag] [chunk-K4EFWGMV]
       +-- [cdkDropList] [chunk-K4EFWGMV]
```

---

## 3. UI Behavior Patterns

### 3.1 Dropdown Open/Close State Machine

```
States: CLOSED, OPEN
Triggers:
  CLOSED -> OPEN:
    - Click on non-editable trigger
    - ArrowDown/ArrowUp key on trigger
    - Programmatic: tuiDropdownOpen = true
    - Driver signal: driver.next(true)

  OPEN -> CLOSED:
    - Escape key (or CloseWatcher API)
    - Click outside active zone
    - Active zone becomes inactive
    - Trigger becomes obscured (e.g., dialog opens on top)
    - Programmatic: tuiDropdownOpen = false
    - Driver signal: driver.next(false)

Side effects on close:
  - Return focus to trigger if was focused
  - Emit tuiDropdownOpenChange(false)
```

### 3.2 Form Control Invalid State Display

```
Conditions for showing invalid state:
  1. pseudoInvalid !== null -> use pseudoInvalid value directly
  2. pseudoInvalid === null (auto mode):
     a. Control must be interactive (not disabled, not readOnly)
     b. Control must be touched
     c. Control status must be "INVALID"

Visual indicators:
  - CSS class: tui-invalid (on tui-textfield)
  - Native: setCustomValidity(message) on <input>
  - Mode signal: "invalid" | "valid" | "readonly"
```

### 3.3 Drag-and-Drop Sequence

```
1. mousedown/touchstart on [cdkDrag] element
2. Clone element as drag preview
3. Apply preview styles (fixed position, opacity: 0 initially)
4. Disable touch-action, user-select on body
5. Track mousemove/touchmove
6. Update preview position via translate3d
7. Check collision with [cdkDropList] containers
8. On drop: fire cdkDropListDropped event with { previousIndex, currentIndex, previousContainer, container }
9. Clean up: remove preview, restore styles
```

### 3.4 Scrollbar Thumb Calculation

```
thumbSize = max(clientSize / scrollSize * clientSize, MIN_THUMB_SIZE=24px)
thumbPosition = scrollPosition / (scrollSize - clientSize) * (clientSize - thumbSize)

Refresh triggers:
  - Resize observer on scrollable element
  - Scroll events (debounced at 100ms)
  - Content changes (via zone-aware ResizeObserver)
```

### 3.5 Long-Press (Longtap) Detection

```
Parameters:
  - Duration: 700ms
  - Max movement: 15px (Euclidean distance)

Sequence:
  1. touchstart -> record position, start 700ms timer
  2. touchmove -> if distance > 15px, cancel
  3. Timer fires -> dispatch custom "longtap" event
  4. touchend/touchcancel -> cancel timer

iOS specific: Uses touch events instead of contextmenu
Non-iOS: Listens to contextmenu.prevent.stop
```

---

## 4. CSS Design Token Reference

Tokens used across these chunks (for theme mapping in Compose):

| CSS Variable                   | Usage                               | Suggested Compose Mapping                       |
|--------------------------------|-------------------------------------|-------------------------------------------------|
| `--tui-font-text-s`            | Small text (labels, captions)       | `MaterialTheme.typography.bodySmall`            |
| `--tui-font-text-m`            | Medium text (body, inputs)          | `MaterialTheme.typography.bodyMedium`           |
| `--tui-text-primary`           | Primary text color                  | `MaterialTheme.colorScheme.onSurface`           |
| `--tui-text-secondary`         | Secondary text color                | `MaterialTheme.colorScheme.onSurfaceVariant`    |
| `--tui-radius-m`               | Medium border radius                | `MaterialTheme.shapes.medium`                   |
| `--tui-shadow-medium`          | Dropdown/elevated shadow            | `Elevation 4-8dp`                               |
| `--tui-background-elevation-3` | Elevated surface bg                 | `MaterialTheme.colorScheme.surface` + elevation |
| `--tui-border-normal`          | Default border color                | `MaterialTheme.colorScheme.outline`             |
| `--tui-duration`               | Transition duration (default 300ms) | Animation spec duration                         |

---

## 5. Numeric Constants Reference

| Constant                      | Value | Context                          |
|-------------------------------|-------|----------------------------------|
| Dropdown offset               | 4px   | Gap between trigger and dropdown |
| Dropdown maxHeight            | 400px | Maximum dropdown height          |
| Dropdown minHeight            | 80px  | Minimum dropdown height          |
| Dropdown max-width margin     | 16px  | Inset from viewport edge         |
| Scrollbar min thumb           | 24px  | Minimum scrollbar thumb size     |
| Scrollbar refresh interval    | 300ms | Scroll position check throttle   |
| Long-press duration           | 700ms | Time to trigger longtap          |
| Long-press movement threshold | 15px  | Max finger movement              |
| Animation base duration       | 300ms | Base animation timing            |
| Debounce scroll styles        | 100ms | Scroll position debounce         |

---

## 6. Angular Patterns to Compose Equivalents

| Angular Pattern                    | These Chunks                 | Compose Equivalent                                     |
|------------------------------------|------------------------------|--------------------------------------------------------|
| `@Input()` bindings                | All chunks                   | `@Composable` parameters                               |
| `InjectionToken` + `provide`       | UFTP2EDV, TIM5J7LT, 24K4LTF5 | `CompositionLocal`                                     |
| `BehaviorSubject` + `subscribe`    | TIM5J7LT                     | `MutableStateFlow` + `collectAsState()`                |
| `signal()` / `computed()`          | 24K4LTF5, UFTP2EDV           | `mutableStateOf()` / `derivedStateOf()`                |
| `ContentChild` / `ContentChildren` | UFTP2EDV                     | Compose slot APIs                                      |
| `ControlValueAccessor`             | TIM5J7LT, UFTP2EDV           | Custom `TextFieldValue` state holder                   |
| `HostBinding` / `HostListener`     | All chunks                   | `Modifier` extensions                                  |
| `ngOnDestroy` cleanup              | All chunks                   | `DisposableEffect` / `LaunchedEffect`                  |
| `EventManagerPlugin`               | TIM5J7LT                     | `Modifier.pointerInput` / `Modifier.combinedClickable` |
| `ResizeObserver`                   | TIM5J7LT, XMBH7NO5, GKYTE6DP | `onSizeChanged` modifier                               |
| `ViewContainerRef` portal          | TIM5J7LT, DNFIPLFW           | Compose `Popup` / `Dialog`                             |
| `NgIf` / `NgFor`                   | 4PH4GFEL (re-exported)       | Kotlin `if` / `forEach` in `@Composable`               |
| CDK DragDrop                       | K4EFWGMV                     | `Modifier.draggable` + `LazyColumn` reorder            |

---

## 7. Summary of Findings for Mobile App Development

### What these chunks tell us:

1. The web app uses a sophisticated UI component library (Taiga UI) with custom form controls,
   dropdowns, scrollbars, and interaction patterns. The mobile app should use Material 3
   equivalents.
2. Drag-and-drop is a first-class interaction pattern -- the mobile app needs reorder support.
3. Form validation follows a specific pattern (touched + invalid) that should be preserved.
4. The dropdown positioning engine handles viewport boundaries intelligently -- mobile popups/bottom
   sheets should do the same.
5. Long-press (700ms) and swipe gestures are supported -- mobile should have similar gesture
   handling.

### What these chunks do NOT contain:

- No API endpoints
- No domain models (courses, students, grades, etc.)
- No business logic
- No authentication
- No routing/navigation
- No state management (Ngrx/etc.)

All domain-specific logic is in other chunks (see `all-files-api-scan.md` for API analysis).
