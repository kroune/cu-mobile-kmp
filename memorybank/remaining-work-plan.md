# Remaining Work — CuMobile KMP

> Last updated: 2026-03-19 (file upload done)

## Pending Features (by priority)

### High Priority
- [x] **Late days dialog with stepper** — DONE: `ProlongLateDays(days: Int)` intent; stepper dialog shows current/new deadline, balance remaining; `formatDeadlinePlusDays()` helper in FormatUtils
- [x] **File upload system** — DONE: `FilePicker` expect/actual (Android: `ActivityResultContracts.OpenDocument`, iOS: `UIDocumentPickerViewController`); `ContentApiService.uploadFileToUrl()` PUT to presigned URL; `ContentRepository.uploadFile()` orchestration; `PendingAttachment`/`UploadStatus` state tracking; `AttachButton` + `PendingAttachmentChip` composables in SolutionTab + CommentsTab; `material-icons-extended:1.7.3` added

### Medium Priority
- [x] **Content search in longreads** — DONE: search bar in `LongreadScreen`; case-insensitive search in markdown content; match highlighting with `AnnotatedString`; prev/next navigation between matches; match counter; toggle via search icon in top bar
- [x] **Avatar upload** — DONE: `ProfileApiService.uploadAvatar()` POST multipart to `/student-hub/avatars/me`; `ProfileRepository.uploadAvatar()`; `UploadAvatar(PickedFile)` intent; reuses `FilePicker` from file upload feature; green "+" button on avatar in ProfileScreen; `isUploadingAvatar` state with spinner

### Low Priority
- [x] **In-app update checker** — DONE: `UpdateChecker` fetches GitHub releases API; `isNewerVersion()` semantic version compare; update dialog in `MainScreen`; `UpdateInfo`/`GithubRelease` data models; unit tests for version comparison
- [ ] **Document scanner** — camera/gallery capture (expect/actual); multi-page with reorder; rotation/cropping; PDF generation (high complexity, deferred)
- [ ] **Model package restructuring** — move `data/model/` to standalone `model/` package; rename `*Response` types

### Known Remaining Issues
- [x] **Files tab: `onOpenFile`** — DONE: `FileOpener` interface + `AndroidFileOpener` (FileProvider + Intent.ACTION_VIEW); iOS stub (`IosFileOpener`). FileProvider configured in AndroidManifest with `file_paths.xml`.
- [ ] **Light theme** — `AppColors` is hardcoded dark. All 15+ screen files use `AppColors` directly. Migration to `CompositionLocal`-based dynamic colors requires touching every screen. **Start in new session (large feature, >15 files).**
- [ ] **`@Preview` functions missing** — 10+ screens need preview composables with mock component implementations. **Start in new session (large feature, >10 files).**

## Already Done (for reference)
- ✅ Pull-to-refresh on all screens (Phase 11)
- ✅ Dark theme refinement (Phase 11)
- ✅ Unit tests for FormatUtils + Theme (Phase 11)
- ✅ Course reordering (DataStore + edit mode with Up/Down)
- ✅ File rename templates (DataStore + settings UI)
- ✅ Schedule/Calendar integration (ICS parser + day view on Home)
- ✅ kotlinx-datetime migration
- ✅ Bug fix audit (2026-03-19): isOverdue, notification links, calendar entry point, performance TopBar, pull-to-refresh data loss, empty states, delete confirmations
