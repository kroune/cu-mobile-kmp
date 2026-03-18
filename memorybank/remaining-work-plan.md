# Remaining Work — CuMobile KMP

> Last updated: 2026-03-18

## Pending Features (by priority)

### High Priority
- [x] **Late days dialog with stepper** — DONE: `ProlongLateDays(days: Int)` intent; stepper dialog shows current/new deadline, balance remaining; `formatDeadlinePlusDays()` helper in FormatUtils
- [ ] **File upload system** — expect/actual file picker (Android: ActivityResultContract, iOS: UIDocumentPickerViewController); upload flow: `getUploadLink` → presigned URL → PUT; progress tracking; attach to solutions + comments in `LongreadTaskSection`. **Start in new session (large feature)**

### Medium Priority
- [ ] **Content search in longreads** — search bar in `LongreadScreen`; case-insensitive search in markdown content; match highlighting; navigation between matches
- [ ] **Avatar upload** — expect/actual image picker (Android: photo picker, iOS: PHPickerViewController); POST `/hub/avatars/me` multipart; camera + gallery; size validation <8MB

### Low Priority
- [ ] **In-app update checker** — check GitHub releases API; version compare; update dialog with download link
- [ ] **Document scanner** — camera/gallery capture (expect/actual); multi-page with reorder; rotation/cropping; PDF generation (high complexity, deferred)
- [ ] **Model package restructuring** — move `data/model/` to standalone `model/` package; rename `*Response` types

## Already Done (for reference)
- ✅ Pull-to-refresh on all screens (Phase 11)
- ✅ Dark theme refinement (Phase 11)
- ✅ Unit tests for FormatUtils + Theme (Phase 11)
- ✅ Course reordering (DataStore + edit mode with Up/Down)
- ✅ File rename templates (DataStore + settings UI)
- ✅ Schedule/Calendar integration (ICS parser + day view on Home)
- ✅ kotlinx-datetime migration
