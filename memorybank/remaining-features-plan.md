# Remaining Features Implementation Plan

## Last Updated: 2026-02-26

## Feature Priority Order

### 1. Pull-to-Refresh (Phase 11) — HIGH
- Add `pullToRefresh` to: HomeScreen, TasksScreen, CoursesScreen, FilesScreen, NotificationsScreen, ProfileScreen, CourseDetailScreen, LongreadScreen, CoursePerformanceScreen
- Use Material3 `pullToRefresh` modifier
- Wire `Refresh` intent to each component (most already have it)
- Status: NOT STARTED

### 2. Course Reordering — MEDIUM  
- DataStore persistence of custom course order
- Edit mode button in Courses tab
- Drag-and-drop via LazyColumn reorder
- Archive/unarchive toggle per course  
- Status: NOT STARTED

### 3. Schedule/Calendar Integration — MEDIUM
- ICS parser (pure Kotlin, shared code)
- RRULE expansion (DAILY/WEEKLY/MONTHLY/YEARLY), EXDATE handling
- Local file cache of parsed calendar
- Calendar URL configuration in Profile screen
- Day view UI in Home tab with time grid
- ClassData model + extraction from VEVENT
- Status: NOT STARTED

### 4. File Upload System — HIGH
- expect/actual file picker (Android: ActivityResultContract, iOS: UIDocumentPickerViewController)
- Upload flow: getUploadLink → presigned URL → PUT
- Progress tracking
- Attachment management in longread task section (solutions + comments)
- PendingAttachment state tracking
- Status: NOT STARTED

### 5. Content Search in Longreads — MEDIUM
- Search bar in longread screen
- Case-insensitive search in markdown/HTML content
- Match highlighting
- Navigation between matches
- Status: NOT STARTED

### 6. Avatar Upload — MEDIUM
- expect/actual image picker (Android: photo picker, iOS: PHPickerViewController)
- POST /hub/avatars/me multipart upload
- Camera + gallery options
- Size limit validation (<8MB)
- Status: NOT STARTED

### 7. File Rename Templates — LOW
- DataStore-based template storage
- FileRenameRule: course, activity, extension, target name
- Settings page for managing templates
- Apply templates on file download
- Status: NOT STARTED

### 8. In-App Update Checker — LOW
- GitHub releases API check
- Version comparison
- Update dialog with download link
- Status: NOT STARTED

### 9. Document Scanner — LOW (deferred, high complexity)
- Camera/gallery capture (expect/actual)
- Multi-page support with reorder
- Image rotation/cropping
- PDF generation
- Status: NOT STARTED

### 10. Dark Theme Refinement & Loading States — MEDIUM
- Review all screens for theme consistency with Flutter reference
- Proper Material3 theme setup
- Loading states improvements
- Status: NOT STARTED

### 11. Model Package Restructuring — LOW
- Move from data/model/ to standalone model/ package
- Rename *Response types
- Status: NOT STARTED

### 12. Unit/Integration Tests — LOW
- Repository tests with mock API
- Component logic tests
- Status: NOT STARTED
