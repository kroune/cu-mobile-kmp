# Root App - New Chunks Detailed Analysis

Analysis of 9 role-gated JavaScript chunks from the CU LMS root app that were not loaded during
normal browser navigation.

---

## 1. Dashboard (chunk-FPXM5OVY.js + chunk-NNLUD57V.js)

**Size**: 71KB (component) + 1KB (routes)
**Export**: `EnrolleeDashboardComponent`

### Route Structure (chunk-NNLUD57V.js)

```
/ (DashboardComponent shell)
  ├── "" [canMatch: Enrollee role] → EnrolleeDashboardComponent (chunk-FPXM5OVY.js)
  └── "**" → redirect to LMS app URL (if user has LmsViewApp access)
```

Key: Dashboard is **enrollee-only**. Students with `LmsViewApp` access are redirected to the LMS app
URL entirely. The route uses `EDU_BACHELOR_ENROLLMENT_COURSE_ID_TOKEN` provider.

### Dashboard Widgets & Layout

The `EnrolleeDashboardComponent` (`<cu-enrollee-dashboard>`) shows conditional content based on
enrollee state:

1. **Loading skeleton** (tui-skeleton) while data loads
2. **Grant banners** (conditional on enrollment type):
    - Master enrollees see one of:
        - `<cu-master-odo-winner-banner>` (if `hasPreGrant`)
        - `<cu-master-grant-competition-banner>` (if `isEnrolleeOnThirdCourseOrMore`)
        - `<cu-master-enrollee-on-second-course-or-less>` (otherwise)
    - Non-master enrollees see `<cu-main-page-grant-banner>`
3. **Events block**: `<cu-my-events-block>` (if `hasEventsReadAccess`)
4. **Master document submission banner**: `<cu-master-document-submission-banner>` (master enrollees
   only)

### Angular Component Selectors

| Selector                             | Purpose                                                  |
|--------------------------------------|----------------------------------------------------------|
| `cu-enrollee-dashboard`              | Main dashboard container                                 |
| `cu-main-page-grant-banner`          | Primary grant banner with IT/Design school state machine |
| `cu-main-page-banner`                | Wrapper banner component                                 |
| `cu-prompt-apply-widget`             | "Apply to grant competition" call-to-action              |
| `cu-dpo-master-admission-banner`     | Combined master admission banner                         |
| `cu-dpo-master-enrollment-banner`    | Master enrollment CTA                                    |
| `cu-dpo-master-grant-banner`         | Master grant CTA                                         |
| `cu-master-grant-competition-banner` | Master grant competition                                 |
| `cu-master-odo-winner-banner`        | ODO winner notification                                  |
| `cu-row-banner`                      | Generic row banner layout                                |
| `cu-my-events-block`                 | Events listing widget                                    |

### API Endpoints

```
POST /api/hub/competitions/bachelor-2024/waitlist/me         — Add to bachelor waitlist
GET  /api/hub/competitions/bachelor-2024/waitlist/me/exists   — Check bachelor waitlist status
POST /api/hub/competitions/master-2025/waitlist/me            — Add to master waitlist (body: {educationalProgram})
GET  /api/hub/competitions/master-2025/waitlist/me/exists     — Check master waitlist status
```

### Grant Banner State Machine

The `<cu-main-page-grant-banner>` component implements a state machine with 10 states based on IT
and Design grant participation:

```
enum BannerState {
  SchoolChild,
  ItGrantAndDesignGrant,
  ItGrantAndDesignForm,
  ItGrantNoDesignForm,
  DesignGrantAndItForm,
  DesignGrantNoItForm,
  ItFormAndDesignForm,
  ItFormNoDesignForm,
  DesignFormNoItForm,
  NoItNoDesign
}
```

State is computed from:

- `isSchoolChildAndNoGrant` (from Wt() — school child check)
- `hasItGrant` / `hasDesignGrant` (from stage facades)
- `isItGrantParticipant` / `isDesignGrantParticipant`

For states `ItFormNoDesignForm` and `ItGrantNoDesignForm` → prompts for Design school
For states `DesignFormNoItForm` and `DesignGrantNoItForm` → prompts for IT school

### Key Instance Properties

- `isMasterEnrollee` — boolean signal from enrollee service
- `isEnrolleeOnThirdCourseOrMore` — boolean signal for course year check
- `hasEventsReadAccess` — role-based events visibility
- `hasPreGrant` — pre-grant track eligibility
- `eventsPath` — router URL to Events section
- `waitlistModal` — modal for waitlist registration
- `educationalProgramCtrl` / `educationalProgramList` — for master program selection in waitlist

### Navigation Links

From dashboard, users can navigate to:

- `/admission` (Admission page)
- `/master-grants` → Contest subpage
- `/events` (Events section)

---

## 2. Onboarding (chunk-65PL4UYO.js)

**Size**: 1.7MB (mostly tsparticles/confetti library embedded)
**Export**: `OnboardingComponent`

### Structure

The onboarding is a full-screen multi-step wizard shown to new enrollees. It takes up the full
viewport (`min-height: 100vh`) with centered content (max-width 32.25rem).

~95% of the file is the **tsparticles** / ngx-confetti library used for the success animation. The
actual onboarding component is at the very end of the file.

### Onboarding Form Fields

```typescript
formGroup: {
  studyDegreeType: FormControl<StudyDegreeType>   // Bachelor | Master | None
  lastStudyDegreeType: FormControl<LastStudyDegreeType>  // School | College | University | None
  graduationYear: FormControl<number | null>        // 1981..now+11
  city: FormControl<string>                         // required
  middleName: FormControl<string>                   // optional, max 50 chars, ru/en name validation
  telegram: FormControl<string>                     // required, telegram username validation
  livesAbroad: FormControl<boolean>                 // default false
}
```

### Study Degree Types

```typescript
enum StudyDegreeType {
  None,
  Bachelor,  // "В бакалавриат" — after 11th grade, college, or transfer from another university
  Master     // "В магистратуру" — on final bachelor years or already has bachelor diploma
}

enum LastStudyDegreeType {
  None,
  School,
  College,
  University
}
```

### Onboarding Flow

1. **Step 1**: Select study degree type (Bachelor / Master) using card-select UI
    - Once selected and saved, the study degree type control is disabled
    - If Master is selected, lastStudyDegreeType auto-sets to University and School/College are
      disabled

2. **Step 2**: Fill in details form:
    - City + "lives abroad" checkbox (with city autocomplete)
    - Middle name (отчество) — optional with hint "skip if you don't have one"
    - Telegram username — with @-prefix mask, required
    - Last study degree type (School/College/University radio buttons)
    - Graduation year (min year, max year picker)

3. **Success screen**: Confetti animation + "Спасибо!" title + "Давай продолжим в личном кабинете"
   subtitle

### UI Text Labels (decoded from Unicode)

| Field                   | Label                                                                                                                                          |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Title                   | "Добро пожаловать!"                                                                                                                            |
| Subtitle                | "Добавь немного деталей, чтобы продолжить работу"                                                                                              |
| Study degree input      | "Я поступаю"                                                                                                                                   |
| Bachelor card           | "В бакалавриат" — "Если поступаешь после 11 класса, колледжа или переходишь из другого вуза"                                                   |
| Master card             | "В магистратуру" — "Если учишься на последних курсах бакалавриата или уже получил диплом бакалавра"                                            |
| Middle name             | "Отчество" — hint: "Если у тебя нет отчества, просто пропусти это поле"                                                                        |
| Last study degree       | "Последнее место учебы"                                                                                                                        |
| Graduation year         | "Год окончания" / "Укажи год выпуска"                                                                                                          |
| Graduation year tooltip | "Укажи год получения высшего образования, либо планируемый год, если еще учишься в вузе; если его нет — выбери бакалавриат на предыдущем шаге" |
| Telegram                | "Введи свой ник без @"                                                                                                                         |
| Success title           | "Спасибо!"                                                                                                                                     |
| Success subtitle        | "Давай продолжим в личном кабинете"                                                                                                            |

### Onboarding Facade

Injected as `onboardingFacade` (provided at component level). Key signals:

- `isOnboardingFinished` — determines whether to show success screen or form
- `currentEnrollee` — pre-fills form from existing data
- `currentEnrollee$` — observable for patching form
- `performOnboarding(data)` — submits the form

### Angular Selectors

| Selector                       | Purpose                              |
|--------------------------------|--------------------------------------|
| `ng-component`                 | Onboarding root (no custom selector) |
| `cu-card-select`               | Study degree type card selection     |
| `cu-segmented-view`            | Multi-step navigation                |
| `cu-confetti` / `ngx-confetti` | Confetti animation wrapper           |

---

## 3. Grants / Bachelor Competition (chunk-6KXFKLYL.js)

**Size**: 173KB
**Exports**: `GrantContestComponent`, `GrantContestWidgetComponent`, `GrantRefusedComponent`,
`GrantWidgets`, `StageMapperConfig`, `UnknownContestStatusComponent`

### ~50% of file is Russian date-fns locale

Contains full `date-fns/locale/ru` implementation for date formatting.

### Competition Stage Types (enum `d`)

```typescript
enum CompetitionStageType {
  New,                           // Initial/registration
  NewSkipContest,                // Skips contest phase
  Contest,                       // Online contest (math/programming/business)
  FormVerification,              // Application form under review
  NeedFix,                       // Application needs correction
  BusinessGameInvited,           // Invited to business game
  BusinessGameEnrolleeConfirmed, // Confirmed business game attendance
  InterviewInvited,              // Invited to interview
  InterviewEnrolleeConfirmed,    // Confirmed interview
  Commission,                    // Under commission review
  GrantReceived,                 // Grant awarded!
  Succeeded,                     // Overall success
  Unknown                        // Error/unknown state
}
```

### Stage Track Types

```typescript
enum StageType {
  Simple,   // Standard track
  Fast,     // Fast track (skip contest)
  Special   // Special track
}
```

### Grant Widgets (enum `ve`)

```typescript
enum GrantWidgets {
  OnlineContest,   // Math/programming online contest
  BusinessGame,    // Business game stage
  Application,     // Application form
  Interview        // Interview stage
}
```

### Grant Contest Component (`<cu-grant-contest>`)

Main competition page with:

- **Progress indicator**: Steps visualization (`<cu-grant-progress>`)
- **Stage widgets**: Grid layout (2-column) of current and upcoming stages
- **Increase grant details**: List of grant increase applications
- **Polls**: Embedded polls widget via `<cu-polls-frame>`
- **Modals**: Contest rules, Business game rules, Interview preparation
- **Error messages**: Form check errors displayed inline

### Key Business Logic

- Auto-refresh: When stage is `BusinessGameInvited`, polls every 8 seconds (retry at 80s) until
  `BusinessGameEnrolleeConfirmed`
- Interview info loaded when stage is `InterviewEnrolleeConfirmed`
- Error message loaded when stage is `NeedFix`
- External links: `https://t.me/interview_cu_bot` for interview registration

### Grant Increase Status Types

```typescript
type IncreaseGrantStatus = "check" | "increase" | "new" | "rejected" | "review"
```

| Status   | Russian Label               |
|----------|-----------------------------|
| check    | "На проверке"               |
| increase | "Грант повышен"             |
| new      | "Отправлена"                |
| rejected | "Рассмотрено без повышения" |
| review   | "Возвращено на доработку"   |

### Grant Refused Component (`<cu-grant-refused>`)

Shown when enrollee loses the competition:

- Title: "Ошибки — путь к победе" (Mistakes are the path to victory)
- Text: "Сейчас мы не готовы предложить тебе грант. Ты можешь поступить без гранта или попробовать
  свои силы в следующем году. Не сдавайся!"
- CTA: "Поступить без гранта" → navigates to `/admission/bachelor`

### Unknown Contest Status Component

Error page with:

- "Кажется, страница уехала в отпуск. Обратись в поддержку, чтобы вернуть ее на рабочее место"
- CTA: "Обратиться в поддержку" → telegram bot URL

### Angular Selectors

| Selector                          | Purpose                              |
|-----------------------------------|--------------------------------------|
| `cu-grant-contest`                | Main competition page                |
| `cu-grant-contest-widget`         | Small grant status widget            |
| `cu-grant-refused`                | Competition failed page              |
| `cu-grant-progress`               | Steps progress indicator             |
| `cu-grant-widget`                 | Generic grant widget                 |
| `cu-grant-approval-status`        | Approval status badge                |
| `cu-increase-grant-detail`        | Grant increase application accordion |
| `cu-increase-grant-detail-list`   | List of increase applications        |
| `cu-increase-grant-status`        | Increase status badge                |
| `cu-increase-grant-competition`   | Increase competition section         |
| `cu-contest-banner-status`        | Contest step status banner           |
| `cu-contest-rules`                | Contest rules modal content          |
| `cu-business-game-rules`          | Business game rules modal content    |
| `cu-how-to-prepare-to-interview`  | Interview preparation modal content  |
| `cu-join-grant-holders-community` | Telegram community CTA               |

---

## 4. Contest Design (chunk-HFMDPRBI.js)

**Size**: 142KB
**Export**: `ContestDesignComponent`

Design school competition page — specialized version of the grant contest for the Design track.

### Angular Selectors

| Selector                              | Purpose                          |
|---------------------------------------|----------------------------------|
| `cu-contest-design`                   | Main design competition page     |
| `cu-design-competition-steps`         | Competition progress steps       |
| `cu-design-case-rules`                | Case assignment rules modal      |
| `cu-design-case-presentation-rules`   | Case presentation rules modal    |
| `cu-design-case-presentation-helper`  | Presentation preparation tips    |
| `cu-design-testing-rules`             | Testing/contest rules            |
| `cu-design-interview-helper`          | Interview preparation for design |
| `cu-grant-design-contest-2026-widget` | 2026 competition widget          |

### Key Properties

- `showTestingRulesModal`, `showCaseRulesModal`, `showCasePresentationRulesModal`,
  `showCasePresentationHelperModal`, `showInterviewHelperModal` — modal visibility signals
- `drawerWidth` — responsive drawer width
- `isPristineStage()` / `isGrantReceivedStage()` — stage state checks

### Design Competition Stages

The design track has additional stages compared to IT:

1. Application form
2. Testing (design testing)
3. Case (design case assignment)
4. Case presentation
5. Interview
6. ODO (some evaluation stage)
7. Grant received

### SVG Backgrounds

Extensive themed SVG backgrounds for each stage:

- `form-banner-on-dark.svg`, `testing-banner-on-dark.svg`
- `case-banner.svg`, `case-banner-on-dark.svg`
- `case-presentation-banner.svg`, `case-presentation-banner-on-dark.svg`
- `interview-banner.svg`, `interview-banner-on-dark.svg`
- `xp-game-banner.svg`, `admission-banner.svg`, `odo-banner.svg`

---

## 5. Contest IT (chunk-PYS4WOGO.js)

**Size**: 112KB
**Export**: `ContestItComponent`

IT school competition page — specialized grant contest for the IT/Technology track.

### Angular Selectors

| Selector                          | Purpose                                  |
|-----------------------------------|------------------------------------------|
| `cu-contest-it`                   | Main IT competition page                 |
| `cu-it-competition-steps`         | IT competition progress steps            |
| `cu-contest-rules`                | Contest rules modal (shared with grants) |
| `cu-business-game-rules`          | Business game rules modal                |
| `cu-interview-helper`             | IT interview preparation                 |
| `cu-grant-it-contest-2026-widget` | 2026 IT competition widget               |

### IT Competition Stages

1. Application form
2. Online contest (math + programming)
3. Business game
4. Interview
5. Grant received

### Key Properties

- `currentStepIndex`, `steps()` — progress tracking
- `widgets()` — dynamic widget configuration
- `isPristineStage()` / `isGrantReceivedStage()` — stage checks
- `showBusinessGameRulesModal`, `showContestRulesModal`, `showInterviewModal`
- `devRole` — ViewDevFeature role for dev-mode state switching

### SVG Backgrounds

- `get-base-grant.svg`, `get-base-grant-on-dark.svg`
- `business-game.svg`, `business-game-on-dark.svg`
- `form-banner.svg`, `form-banner-on-light.svg`
- `contest-banner.svg`
- `xp-game-banner.svg`, `admission-banner.svg`
- `interview-banner.svg`, `interview-banner-on-dark.svg`

---

## 6. Master Grants Contest (chunk-ZE753EYD.js)

**Size**: 104KB
**Exports**: `MasterGrantsContestPageComponent`

Master's degree grant competition page with a different flow than bachelor competitions.

### Angular Selectors

| Selector                                               | Purpose                    |
|--------------------------------------------------------|----------------------------|
| `cu-master-grants-contest-page`                        | Main master grants page    |
| `cu-master-grants-feature-grant-widget`                | Grant value/pricing widget |
| `cu-master-grants-past-steps`                          | Completed steps display    |
| `cu-contest-step`                                      | Individual step card       |
| `cu-master-contest-rules`                              | Rules for master contest   |
| `cu-master-contest-rules-unknown-education-program`    | Rules when program unknown |
| `cu-master-telegram-community-widget`                  | Telegram community CTA     |
| `cu-master-design-telegram-community-widget`           | Design-specific Telegram   |
| `cu-master-interview-helper`                           | Interview prep for masters |
| `cu-master-interview-design-program-helper`            | Design interview prep      |
| `cu-master-interview-unknown-education-program-helper` | Generic interview prep     |

### Grant Widget Data

The `<cu-master-grants-feature-grant-widget>` shows:

- **Grant value**: Displayed as percentage (e.g., "60%") with animated counter
- **Semester price**: Cost of study per semester
- **Duration**: "4 семестра" (4 semesters)
- **Info text**: "Прими грант в течение двух недель"
- **Hint text**: Optional additional information

### Contest Rules Content

For IT directions:

- "Для решения задач по математике у тебя будет 1,5 часа. На задание по программированию — 3 часа.
  Решать эти задачи можно на любом языке программирования. Главное, чтобы решение прошло все тесты."

For Design/Business directions:

- "Для решения задач по математике у тебя будет 1,5 часа. На решение бизнес-кейса даются сутки."

Common: "Если не получится набрать проходной балл с первого раза, можешь попробовать свои силы в
следующей волне. Всего в этом году их пять."

### Master Competition Steps

Steps displayed with themed cover images:

- `application` (application-B5B4MVRR.svg / application-dark-42UCSI2X.svg)
- `contest` (contest-4I34ZJB3.svg / contest-dark-5HHOKVZP.svg)
- `interview` (interview-64JWSIHR.svg / interview-dark-SSEEFSHJ.svg)
- `summary` (summary-SHHTNW6D.svg)

Each step has dark mode variants with `--cu-contest-step-bg: var(--background-alt-dark)`.

### Telegram Community Widget

- Title: "Подпишись на телеграм-канал магистратуры ЦУ"
- Description: "Внутри анонсы мероприятий и важных апдейтов, истории студентов магистратуры,
  полезная информация о поступлении и университете"
- CTA: "Вступить в сообщество" → bot URL

### Key Properties

- `isDesignProgram()`, `isUnknownEducationProgram()`, `isProduct()` — program type signals
- `grantValue()`, `semesterPrice()` — financial data
- `stages` — configured step list
- `contestId`, `eduContestId`, `eduProductContestId` — contest identifiers
- `skipContest` — flag for special track without contest
- `showPolls` / `pollsFacade` — embedded polls support

---

## 7. Admission Form (chunk-VOKE7BVC.js)

**Size**: 101KB
**Export**: `HubAdmissionFutureStudentApplicationFormPageFeatureComponent`

Multi-step enrollment application form for future students.

### API Endpoints

```
GET  /api/event-builder/admissions/{admissionType}/forms/future-student/me
     → Returns: { serverData, currentStep, form, isFormCompleted }

POST /api/event-builder/admissions/{admissionType}/forms/future-student/me/draft/{step}
     → Saves draft for a specific step

POST /api/event-builder/admissions/{admissionType}/forms/future-student/me/submit
     → Final form submission

GET  /api/event-builder/admissions/forms/future-student/dictionaries
     → Returns all lookup dictionaries

POST /api/event-builder/admissions/forms/future-student/files/upload
     → File upload (photos, documents) with optional isPhoto query param
```

`admissionType` can be `"Bachelor2025"` or a masters variant.

### Form Steps

The form has 4 main steps rendered as a stepper:

| Step | Key              | Description                                                       |
|------|------------------|-------------------------------------------------------------------|
| 1    | `common`         | General information (personal data, contact person, education)    |
| 2    | `dormitory`      | Dormitory/housing preferences                                     |
| 3    | `scholarship`    | Scholarship/financial information (INN, tax residency, bank card) |
| 4    | `militaryRecord` | Military registration documents (males only)                      |

### Form Title & Subtitle

- Title: "Анкета абитуриента"
- Subtitle: "Анкету можно заполнить за несколько раз, информация сохраняется автоматически после
  перехода к следующему блоку"
- Success alert: "Анкета отправлена"

### Common Step (Step 1) Fields

Personal identification:

- `sex` — gender
- `birthdate` — date of birth
- `photo` — photo upload
- `identityDocumentFiles` — identity document scans
- `educationDiploma` — diploma details
    - `series`, `number`, `issueDate`, `issuedBy`
    - `diplomaFile` — diploma scan
    - `doesDiplomaReceivedUnderDifferentName` — name change flag
    - `changeOfNameDocumentFile` — name change document

Contact person subgroup:

- `isContactPersonIndicated` — checkbox to enable contact person section
- `contactPerson`:
    - `kindshipType` — relationship type (select from dictionary)
    - `lastName`, `firstName`, `middleName`
    - `phone` — international phone input
    - `email`

Current university subgroup:

- `isCurrentlyStudyingInUniversity` — checkbox
- `currentUniversity`:
    - `universityName`
    - `degreeType` — education level (from dictionary)
    - `speciality` — field of study
    - `course` — current course year (from dictionary)
    - `studyType` — form of study (from dictionary)

Work:

- `isCurrentlyWorking`
- `work.placeOfWork`

### Dormitory Step (Step 2) Fields

- Dormitory selection/preference (from dictionary)
- Residence address
- Registration address
- `residenceAddressStartDate`, `registrationAddressStartDate`

### Scholarship Step (Step 3) Fields

- `inn` — INN (tax ID) with mask
- `isRFTaxResident` — Russian Federation tax resident checkbox
- `cardMirRequisitesDocumentFile` — MIR bank card requisites document
    - Hint: "Для получения стипендии понадобится номер счета карты МИР. Его можно скачать в
      приложении твоего банка"

### Military Record Step (Step 4) Fields

- `isSubjectToMilitaryRegistration` — registration obligation
- `militaryRegistrationDocumentType` — document type (from dictionary)
- Document type-specific groups:
    - **Conscription certificate**: `conscriptionCertificate`
    - **Electronic conscription certificate**: `electronicConscriptionCertificate`
    - **Military identificator**: `militaryIdentificator` with subfields:
        - `militaryFitnessCategory`, `militaryProfile`, `militaryRank`
        - `militaryReserveCategory`, `militaryCommissariatName`
        - `vusCode`, `speciality`
        - `canFitnessCategoryBeImplicit`, `shouldHaveMilitaryCategoryDProof`
        - `healthCertificateFile`, `categoryDMilitaryRegistrationDocument`
    - **Reserve officer**: `reserveOfficerMilitaryIdentificator`
    - **Replacement certificate**: `replacementCertificateForm`
- `militaryRegistrationDocuments` — uploaded document files
- Disabilities information:
    - `disabilities`, `hasChildren`, `isMarried`, `isRaisingChild`

### Dictionaries

All lookup values come from `/api/event-builder/admissions/forms/future-student/dictionaries`:

```
KindshipType         — relationship types for contact person
DegreeTypes          — education levels
Courses              — course years
StudyTypes           — study forms (full-time, part-time, etc.)
Dormitories          — available dormitories
MilitaryRegistrationDocumentTypes
MilitaryFitnessCategories
MilitaryReserveCategories
MilitaryRanks
MilitaryProfiles
```

### Form Behavior

- Auto-save: Each step auto-saves as draft when navigating to next step
- Read-only mode: Form becomes read-only when `isFormCompleted` or `isFutureStudentFormCompleted`
- File uploads: Go through `/api/event-builder/admissions/forms/future-student/files/upload`
- File size limits: `maxFileSizeInBytes` and `maxSmallFileSizeInBytes` thresholds
- Retry on dictionary load failure with 5-second interval

### Angular Selectors

| Selector                                                                           | Purpose                     |
|------------------------------------------------------------------------------------|-----------------------------|
| `cu-hub-admission-future-student-application-form-page-feature`                    | Root form page              |
| `cu-hub-admission-future-student-application-form-page-form-common-group`          | Common info step            |
| `cu-hub-admission-future-student-application-form-page-form-dormitory-group`       | Dormitory step              |
| `cu-hub-admission-future-student-application-form-page-form-scholarship-group`     | Scholarship step            |
| `cu-hub-admission-future-student-application-form-page-form-military-record-group` | Military step               |
| `cu-hub-admission-fsafp-form-common-group-contact-person-group`                    | Contact person sub-group    |
| `cu-hub-admission-fsafp-form-common-group-current-university-group`                | University sub-group        |
| `cu-form-military-record-conscription-certificate-group`                           | Conscription cert sub-group |
| `cu-form-military-record-electronic-conscription-certificate-group`                | E-conscription sub-group    |
| `cu-form-military-record-military-identificator-group`                             | Military ID sub-group       |

---

## 8. Profile Routes (chunk-XYNDJCEW.js)

**Size**: 5KB
**Exports**: `profileRoutes`, `experienceModificationRoutes`

### Route Structure

```
/ [canMatch: Student role]
  ├── experience/add-experience → StudentExperienceRootComponent
  │   └── "" → StudentExperienceAddComponent
  │   title: "Добавить достижения"
  │   hint: "Поделись успехами за время учебы в университете"
  │
  ├── experience/edit-experience/:id → StudentExperienceRootComponent
  │   └── "" → StudentExperienceEditComponent
  │   title: "Изменить достижение"
  │
  ├── experience/edit-bio → StudentExperienceRootComponent
  │   └── "" → StudentEditBioComponent
  │   title: "Обо мне"
  │   hint: "Напиши пару слов о себе. Расскажи о своих интересах, хобби, активностях"
  │
  └── "" → StudentProfileComponent
      ├── "" → redirect to generalInfoTab
      ├── {generalInfoTab} → StudentGeneralInfoComponent
      └── {experienceTab} → StudentExperienceTabComponent [providers: experience services]

/ [canMatch: Enrollee role]
  ├── "" → redirect to generalInfoTab
  ├── {generalInfoTab} → EnrolleeProfileComponent
  │   └── "" → EnrolleeInfoComponent
  └── {certificatesTab} → EnrolleeProfileComponent [canActivate: showEnrolleeCertificatesTab config]
      └── "" → CertificatesTabComponent
```

### Key Components (lazy-loaded from other chunks)

- `StudentProfileComponent` (chunk-3OTZ76XW.js)
- `StudentGeneralInfoComponent` (chunk-4DJDOUKC.js)
- `StudentExperienceTabComponent` (chunk-UCCVAEQI.js)
- `StudentExperienceRootComponent` (chunk-B6FE3BJX.js)
- `StudentExperienceAddComponent` (chunk-V4BDZGJX.js)
- `StudentExperienceEditComponent` (chunk-SSXSFKSJ.js)
- `StudentEditBioComponent` (chunk-MLPW34RF.js)
- `EnrolleeProfileComponent` (chunk-RA5VEO3V.js)
- `EnrolleeInfoComponent` (chunk-O6WLLJOU.js)
- `CertificatesTabComponent` (chunk-QQWRM2SM.js)

### Config-gated Features

- Enrollee certificates tab is behind `showEnrolleeCertificatesTab` config flag

---

## 9. Summary of Key Findings for KMP App

### What the Dashboard Shows (Enrollees)

The dashboard is **exclusively for enrollees** (prospective students). Current students are
redirected to the LMS app. The dashboard focuses on:

1. Grant competition status/CTA banners (IT and Design schools)
2. Waitlist registration for bachelor/master programs
3. Events listing
4. Master document submission prompts

### What Grants Pages Do

The grant competition is a multi-stage process with 3 specialized competition pages:

- **Bachelor IT** (`ContestItComponent`): Application -> Contest -> Business Game -> Interview ->
  Grant
- **Bachelor Design** (`ContestDesignComponent`): Application -> Testing -> Case -> Case
  Presentation -> Interview -> ODO -> Grant
- **Master** (`MasterGrantsContestPageComponent`): Application -> Contest -> Interview -> Summary

Each has real-time stage tracking, modal rules/help, Telegram community links, and embedded polls.

### What Onboarding Involves

A 2-step wizard collecting:

1. Study degree type (bachelor/master) via card selection
2. Personal details (city, telegram, middle name, last education type, graduation year, lives
   abroad)

Submits to the `onboardingFacade` service. Shows confetti on success.

### What Admission Does

A comprehensive 4-step enrollment application form with:

- Personal data, education history, contact person
- Dormitory preferences
- Financial info (INN, bank card for scholarship)
- Military registration (with 5 document type variants)
- File uploads for documents, photos, diplomas
- Auto-save as draft per step

### API Endpoints Discovered

| Method | Path                                                                        | Purpose                  |
|--------|-----------------------------------------------------------------------------|--------------------------|
| POST   | `/api/hub/competitions/bachelor-2024/waitlist/me`                           | Add to bachelor waitlist |
| GET    | `/api/hub/competitions/bachelor-2024/waitlist/me/exists`                    | Check bachelor waitlist  |
| POST   | `/api/hub/competitions/master-2025/waitlist/me`                             | Add to master waitlist   |
| GET    | `/api/hub/competitions/master-2025/waitlist/me/exists`                      | Check master waitlist    |
| GET    | `/api/event-builder/admissions/{type}/forms/future-student/me`              | Get admission form       |
| POST   | `/api/event-builder/admissions/{type}/forms/future-student/me/draft/{step}` | Save draft               |
| POST   | `/api/event-builder/admissions/{type}/forms/future-student/me/submit`       | Submit form              |
| GET    | `/api/event-builder/admissions/forms/future-student/dictionaries`           | Get dictionaries         |
| POST   | `/api/event-builder/admissions/forms/future-student/files/upload`           | Upload files             |

### Relevance to KMP App

These features are all **enrollee-facing** (pre-admission) and are **not part of the current LMS
student app**. The KMP app currently implements the **student** experience (courses, tasks,
performance, etc.). These chunks represent a different user journey:

- Onboarding flow (first-time enrollee setup)
- Grant competition tracking
- Admission application form
- Enrollee dashboard

If the KMP app ever needs to support enrollees/prospective students, these APIs and flows would need
to be implemented.
