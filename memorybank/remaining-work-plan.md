# Remaining Work Plan — CuMobile KMP

## Created: 2026-02-23

## Priority Order

### Batch 1: UI Polish (High Priority)
1. [x] Theme refinement — fix task state colors to match Flutter exactly
2. [x] Login page polish — add instructions, version, proper button styling
3. [x] Pull-to-refresh on Home, Tasks, Courses, Longread, Notifications, Files screens
4. [x] Loading/error/empty states — ensure consistent use across all screens

### Batch 2: Missing Features (Medium Priority)
5. [ ] Late days dialog with stepper (currently just sends fixed request)
6. [ ] Upload system for file attachments (solutions + comments)
7. [ ] Content search & highlighting in longreads
8. [ ] Schedule/Calendar integration (ICS parser + day view)

### Batch 3: Low Priority Features
9. [ ] Course reordering & archiving (drag-and-drop, local persistence)
10. [ ] File rename templates
11. [ ] In-app update checker
12. [ ] Document scanner (platform-specific, most complex)

### Batch 4: Code Quality
13. [ ] Model refactoring: move data/model/ to standalone model/ package
14. [ ] Rename *Response types to non-API-specific names

## Status
- Currently implementing: Batch 1
