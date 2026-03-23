# Learn Vendor Chunks -- Supplementary Analysis

Supplements the main analysis in `learn-vendor-chunks-detailed.md` with additional observations,
cross-references, and mobile-app implications.

---

## 1. Module Import Graph (chunk-23VGFXOH.js)

The chunk imports from **~120 other chunk files**. Key dependency clusters:

### Core Framework Chunks (high import count)

| Chunk               | Likely Contains                                                                                                                                                                                    |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `chunk-KNF5L6KI.js` | Angular core runtime (largest import -- 60+ symbols including `Component`, `inject`, `signal`, `computed`, `effect`, DI tokens, template instructions `f`, `h`, `T`, `S`, `Q`, `d`, `m`, `Y`, `V`) |
| `chunk-24K4LTF5.js` | Angular forms + RxJS operators (`FormGroup`, `FormControl`, `Validators`, `pipe`, `subscribe`, `switchMap`, `takeUntilDestroyed`, `map`, `filter`, `first`, `tap`)                                 |
| `chunk-BMHKXZLE.js` | Angular common directives (`NgTemplateOutlet`, `AsyncPipe`, ViewChild-related)                                                                                                                     |
| `chunk-DR2JNUWK.js` | RxJS core (`Observable`, `Subject`, `combineLatest`, `of`, `merge`, `forkJoin`, `EMPTY`)                                                                                                           |
| `chunk-4PH4GFEL.js` | Angular router (`Router`, `ActivatedRoute`, `RouterLink`, `routerLink`)                                                                                                                            |
| `chunk-GXSA7CKD.js` | Utility functions (object spread, property access, safe operators)                                                                                                                                 |

### Domain-Specific Chunks

| Chunk               | Likely Contains                                                                        |
|---------------------|----------------------------------------------------------------------------------------|
| `chunk-4SKQR4GO.js` | Material types/discriminators, material service interface                              |
| `chunk-BF6EGGKK.js` | Longread types, states, date utilities (`Wu` = max dates, `Gt`, `Vt`, `ft`)            |
| `chunk-KZ4SBTZ3.js` | Content material types (`Tt`), media type enums                                        |
| `chunk-4EYN7X4K.js` | Lodash-style utilities (`isEqual`, `forEach`, `get`, `set`, etc.)                      |
| `chunk-PODCHSSJ.js` | UI infrastructure (overlay, dialog, notifications)                                     |
| `chunk-EJ3QF465.js` | User/member utilities (`Hn` = user label map, `Gn` = user options map)                 |
| `chunk-X32QAISO.js` | Icon constants (`co` = cuIconArrowLeft, `kl` = cuIconReorder, `fo` = paste icon, etc.) |
| `chunk-QCPI75QD.js` | Router utilities, path generation                                                      |
| `chunk-YSPW2MHC.js` | Longread/course path utilities                                                         |
| `chunk-K4EFWGMV.js` | Array reorder utility (`Un` = moveItemInArray), dialog components                      |
| `chunk-IB3LKSLZ.js` | Action dialog base class (`qn`), drawer appearance constants (`Yt`)                    |
| `chunk-ZICG35BS.js` | Course member store                                                                    |
| `chunk-VVMUT7QD.js` | Cookie/storage service                                                                 |

---

## 2. Signal-Based State Management Pattern

The codebase uses Angular's modern signal-based reactivity extensively (Angular 17+ style):

```
de(value)        -> signal(value)         -- Create writable signal
me(() => expr)   -> computed(() => expr)  -- Create computed signal
_t(obs$)         -> toSignal(obs$)        -- Convert Observable to signal
Vu(() => expr)   -> linkedSignal(...)     -- Linked signal (Angular 19+)
L.required()     -> input.required()      -- Required signal input
L()              -> input()               -- Optional signal input
at()             -> output()              -- Signal-based output
```

State management combines signals with RxJS:

- Service observables (`courseId$`, `longread$`) converted to signals via `toSignal()`
- Local UI state managed with writable signals (`editing`, `dragging`, `loading`)
- Computed derivations for complex state (`materials`, `materialActionGroups`, `coursePath`)
- RxJS `pipe()` chains for async operations with `takeUntilDestroyed()` lifecycle management

---

## 3. Material System Architecture

### Material Wrapper Type

```typescript
interface MaterialWrapper {
  localId: string;     // Client-side UUID (generated via Oo() = uuid)
  material: Material;  // Server-side material object
}
```

### Material Lifecycle

1. **Creation**: User selects material type from action menu -> local wrapper created with empty
   material -> `createMaterial$()` called -> server returns ID -> `readMaterial$()` fetches full
   material -> local wrapper updated
2. **Editing**: Double-click toggles editor -> form binds to material data -> on save,
   `updateMaterial$()` called -> `readMaterial$()` refreshes
3. **Reordering**: CDK drag-and-drop -> `moveItemInArray()` locally -> `reorderMaterials$()` syncs
   server
4. **Copy/Paste**: Material serialized to cookie storage -> on paste, deep clone with new IDs ->
   create on server
5. **Deletion**: Confirmation dialog -> `deleteMaterial$()` if server-side, or just remove locally

### Material Action Groups

The material creation menu is organized by longread type:

- **Common longreads**: Content + Exercise + Action (if clipboard has data)
- **Other longreads**: Content + Action (if markdown clipboard data)

Content options: Markdown, Image (via `np`), Audio (via `tp`), File (via `ip`), VideoPlatform (via
`op`)
Exercise options: Coding (via `ep`), Questions (via `Xc`)
Action options: Paste from clipboard (via `Bo`)

---

## 4. Video Platform Integration Details

### Upload Flow

1. Create video material on server: `createVideoMaterial$()` returns
   `{ materialId, uploadMediaId, uploadClientId, uploadToken, urls }`
2. Upload via tus-js-client to platform URLs with authentication tokens
3. Track processing: `trackVideoMaterialInPlatform$()` polls until `state === Ready`
4. On ready: receive `playbackLink` URL, update UI

### Video Player

- Created via `createVideoPlayer(playbackLink, containerElement)`
- Player destruction on component destroy: `player?.destroy()`
- Supports timecodes: parsed from text format `"MM:SS description"` per line

### TUS Protocol Details (from bundled client)

- Supports parallel uploads with `Upload-Concat: partial` / `Upload-Concat: final`
- Fingerprint-based resume: stores upload URL in URL storage, checks on restart
- Status 423 handling: "upload is currently locked; retry later"
- IETF draft protocol variant with `Upload-Complete: ?0` header
- Configurable retry: `retryDelays` array, automatic retry on transient errors

---

## 5. User Assignment System

### Assignment Dialog

- Tabbed UI: Users tab / Groups tab
- Segmented control for tab switching
- Search field with instant filtering
- Multi-select with toggle behavior
- Submit with pluralization: "Назначен 1 студент" / "Назначено 2 студента" / "Назначено 5 студентов"
- Warning state when no students selected: "Студенты не выбраны"
- Can assign to all course students

### Exercise Estimation

```typescript
{
  activity: {
    id: string;
    name: string;
    weight: number;
    isLateDaysEnabled: boolean;
  }
  startDate: Date;
}
```

Activities filtered based on exercise state and `isLateDaysEnabled` match.
Display format: `"ActivityName, (Вес = weight)"`

---

## 6. Implications for Mobile App (KMP)

### What Needs to Be Implemented

1. **Material types**: All 8 discriminator types need model classes and rendering
2. **Material states**: Draft/Published lifecycle with publish date management
3. **Rich text**: HTML content with heading IDs needs proper rendering (WebView or custom parser)
4. **Code highlighting**: PrismJS supports 12+ languages; mobile needs equivalent (consider
   Highlight.js or native solutions)
5. **Math rendering**: KaTeX LaTeX rendering required; consider WebView-based KaTeX or native
   alternatives
6. **Video**: Integration with video platform, potentially using native player with `playbackLink`
   URL
7. **Date formatting**: Russian locale with specific patterns (dd.MM.y, etc.) and Moscow timezone
8. **File upload**: TUS protocol for large file uploads; consider tus-android/tus-ios libraries
9. **Drag and drop**: Material reordering UI (may need simplified mobile UX)
10. **Question/Exercise forms**: Complex form validation logic needs careful porting

### What Can Be Simplified for Mobile

1. **Analytics/UTM tracking**: Not needed in mobile app (use Firebase Analytics or similar)
2. **Cookie consent**: Not applicable to mobile
3. **Polyfills**: Not needed -- Kotlin/Swift handle this
4. **Editor**: Mobile may use simplified editor rather than full Taiga UI editor
5. **Copy/paste materials**: Simplify to in-memory clipboard rather than cookie storage

### API Surface Area from This Chunk

The following RxJS-based service methods represent the API surface that the mobile app needs to
call:

```
Materials:
  createMaterial$(data: CreateMaterialRequest) -> Observable<{materialId}>
  readMaterial$(materialId: string) -> Observable<Material>
  updateMaterial$(materialId: string, data: UpdateMaterialRequest) -> Observable<any>
  deleteMaterial$(materialId: string) -> Observable<any>
  reorderMaterials$(longreadId: string, materialIds: string[]) -> Observable<any>

Video:
  createVideoMaterial$(longreadId, metadata) -> Observable<{materialId, uploadMediaId, uploadClientId, uploadToken, urls}>
  trackVideoMaterialInPlatform$(materialId) -> Observable<{state, playbackLink}>
  updateVideoPlatformMaterialTimecodes$(materialId, text) -> Observable<any>

Course:
  courseMembers$ -> Observable<Member[]>
  courseGroups$ -> Observable<Group[]>
  getActivities$(courseId) -> Observable<Activity[]>
  reviewers$ -> Observable<Reviewer[]>
```

---

## 7. Interesting Technical Details

### Heading ID Regeneration on Paste

When pasting markdown content, heading IDs (for table-of-contents anchors) are regenerated:

```javascript
function Sv(htmlValue) {
  let { pseudoDocument, foundElements } = el(htmlValue, "h1[id], h2[id]");
  foundElements.forEach(heading => heading.setAttribute("id", generateUUID()));
  return pseudoDocument.body.innerHTML;
}
```

This prevents duplicate anchor IDs when the same content appears multiple times.

### Minimum Publish Date with Moscow Timezone

```javascript
static getMinPublishDate(parentPublishDate) {
  let now = new Date();
  return convertToTimezone(
    parentPublishDate ? maxDate([new Date(parentPublishDate), now]) : now,
    Timezone.Moscow
  )[0];
}
```

Material publish date must be >= parent's (theme or longread) publish date, all computed in Moscow
timezone.

### React Inside Angular

React 19.2.4 is bundled alongside Angular 21.1.4. This is likely used for a specific component (
possibly a video player or third-party widget) that is rendered within Angular using a wrapper. The
React runtime includes the full production build with all hooks.

### WeakMap-Based Private Fields

The codebase uses ES2022 private class fields compiled to WeakMap pattern:

```javascript
var t = new WeakMap, i = new WeakMap, n = new WeakMap; // etc.
class Mt {
  constructor() {
    v(this, t);  // Initialize private field slot
    x(this, t, inject(SomeService));  // Set private field value
  }
}
// Access: c(this, t) -> WeakMap.get(this)
```

This is the output of TypeScript/esbuild compilation of `#privateField` syntax.

### Content Discrimination Logic

```javascript
// Material is "filled" if:
// - Coding or Questions type: has a name
// - Other types: has an ID (saved on server)
function isMaterialFilled(wrapper) {
  return wrapper.material.discriminator === "coding" ||
         wrapper.material.discriminator === "questions"
    ? !!wrapper.material.name
    : !!wrapper.material.id;
}
```
