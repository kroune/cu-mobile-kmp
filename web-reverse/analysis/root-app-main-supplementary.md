# Root App -- Supplementary Analysis

Covers chunk file mapping, secondary services, UI utilities, and miscellaneous
infrastructure not in the main analysis.

---

## 1. Chunk File Mapping

| Chunk                   | Size  | Contents                                                             |
|-------------------------|-------|----------------------------------------------------------------------|
| `main-NI5Y7GEG.js`      | ~3KB  | Entry point, locale registration, bootstrap                          |
| `chunk-OIDSCTRK.js`     | 133KB | appConfig, routes, Sentry config, providers, layout components       |
| `chunk-SCK4OUJI.js`     | 292KB | Markdown-it (full parser), markup rendering                          |
| `chunk-H2IG3LXV.js`     | 270KB | Angular CDK, TaigaUI components library                              |
| `chunk-E4YLUPE4.js`     | 210KB | Angular core framework (compiler, DI, zones, change detection)       |
| `chunk-7HYSEYNL.js`     | ~35KB | Angular HttpClient (full), XSRF interceptor                          |
| `chunk-STCJX36N.js`     | ~20KB | RxJS operators, Angular common utilities                             |
| `chunk-X7AG73LV.js`     | ~2KB  | Object spread helpers (`__spreadValues`, `__spreadProps`, `__async`) |
| `chunk-NOMFNW4V.js`     | ~15KB | Cookie/URL utilities, alert/notification components, TUI helpers     |
| `chunk-OJLRDDE5.js`     | ~3KB  | RxJS operator helpers (shareReplay, toSignal, catchError wrappers)   |
| `chunk-F4UDLC3X.js`     | ~1KB  | Route path enum (R)                                                  |
| `chunk-ROFNVQNW.js`     | ~4KB  | Auth session service (UserToken, session$, userId$, roles$)          |
| `chunk-BSYKMDXA.js`     | ~5KB  | Auth navigation service (sign-in, sign-up, logout, callback)         |
| `chunk-44YKIWJR.js`     | ~8KB  | Events API service + polling service                                 |
| `chunk-EFZ55JZJ.js`     | ~1KB  | News API service                                                     |
| `chunk-2NELPLXO.js`     | ~1KB  | Admissions state API service                                         |
| `chunk-QHDVAQXE.js`     | ~3KB  | Student service (currentStudent$, education levels)                  |
| `chunk-2RNYUVXP.js`     | ~5KB  | Enrollee service (profile CRUD, onboarding)                          |
| `chunk-H2UV6AAT.js`     | ~2KB  | Grants API service                                                   |
| `chunk-VMZTUSWJ.js`     | ~3KB  | Admin configurations service                                         |
| `chunk-KUQINTYY.js`     | ~20KB | Notifications, Service Worker, Push, Cobrowsing                      |
| `chunk-WNGRPCQW.js`     | ~25KB | Perfume.js performance monitoring                                    |
| `chunk-FGZGDGP3.js`     | ~8KB  | UTM tracking service                                                 |
| `chunk-N3DZQN6I.js`     | ~3KB  | User roles enum, RolesService, access control                        |
| `chunk-ISH5QSO5.js`     | ~1KB  | Events sub-routes definition                                         |
| `chunk-6BRE6SRC.js`     | ~1KB  | EventPaths constant, NEWS_CHANNEL_URL token                          |
| `chunk-LDIE5KPJ.js`     | ~3KB  | Polymorpheus, MutationObserver directive, animation utils            |
| `chunk-U6ZV2IWF.js`     | ~50KB | DOMPurify (HTML sanitizer)                                           |
| `chunk-TJTDBR6Z.js`     | ~30KB | AppComponent (root), layout, header                                  |
| `chunk-OD35FLTN.js`     | ~45KB | Angular Router (full implementation)                                 |
| `chunk-SIQ3MUTR.js`     | ~30KB | Tinkoff Statist client                                               |
| `chunk-LRUFFHY6.js`     | ~1KB  | @tinkoff/cobrowsing package metadata (v5.0.0)                        |
| `chunk-WZZUWG4A.js`     | ~3KB  | TaigaUI core utilities                                               |
| `chunk-YDHQNC7H.js`     | ~3KB  | TaigaUI date/time classes                                            |
| `chunk-OPJZQU5M.js`     | ~1KB  | TUI pipes (tuiFallbackSrc, tuiInitials)                              |
| `chunk-YEHSEGZY.js`     | ~3KB  | Confirm dialog component                                             |
| `chunk-PJSN4G6F.js`     | ~3KB  | TUI dialog service                                                   |
| `chunk-BHFGHFHJ.js`     | ~5KB  | Angular platform-browser bootstrap                                   |
| `polyfills-LXTXMF5S.js` | ~3KB  | Zone.js polyfills                                                    |

---

## 2. Role-Based Access Control

### 2.1 RolesService (`chunk-N3DZQN6I.js`)

```typescript
class RolesService {
  hasAccess$(roles, options?): Observable<boolean>
  hasRole$(role): Observable<boolean>
  hasRoles$(...roles): Observable<boolean[]>
  filterEntitiesByAccess$(entities, options?): Observable<Entity[]>
}
```

Options:

```typescript
interface AccessOptions {
  strategy: "every" | "some" | "noOne";
  defaultValue: boolean;  // default: false
}
```

### 2.2 Route Guards

The route tree uses several guard patterns:

- `Ea` -- top-level authentication guard
- `ln(role)` / `ln([roles])` -- role-based canMatch guard
- `Rs` -- onboarding-needed check
- `Es([observables])` -- observable-based availability check
- `Qr` -- events visibility guard
- `zs` -- news visibility guard
- `ks` -- grants-2026 visibility guard
- `Jt` -- grants availability guard
- `Vi` -- master grants availability guard

### 2.3 Key Role Checks in Routes

| Route             | Required Role             |
|-------------------|---------------------------|
| Onboarding branch | `Enrollee`                |
| Admission (form)  | `PreStudent` or `Student` |
| Grants 2026       | guarded by `ks`           |
| News              | guarded by `zs`           |
| Events            | guarded by `Qr`           |

---

## 3. Service Worker & App Updates

**Registration:** `ngsw-worker.js`

- Enabled only when not in dev server (`!oo()`)
- Strategy: `registerWhenStable:30000` (wait up to 30s for app to stabilize)

**SwUpdate service:**

- `versionUpdates` -- Observable of version events
- `unrecoverable` -- Observable of unrecoverable state
- `checkForUpdate()` -- forces update check
- `activateUpdate()` -- activates pending update

**SwPush service:**

- `messages` -- push message data
- `notificationClicks` / `notificationCloses` -- notification interaction events
- `requestSubscription({serverPublicKey})` -- VAPID push subscription
- `unsubscribe()` -- removes push subscription

---

## 4. UI Infrastructure

### 4.1 Alert/Toast System (`chunk-NOMFNW4V.js`)

TaigaUI-based notification system:

```typescript
interface AlertOptions {
  autoClose: number;     // default: 3000ms
  label: string;
  closeable: boolean;    // default: true
  data: any;
  appearance: string;    // "success", "error", "info", "warning", etc.
}
```

Usage helper:

```typescript
function showAlertOnResult(injector, options?) {
  // pipe operator that shows success/error alerts
  // success: appearance "success"
  // error: appearance "error"
}
```

### 4.2 Confirm Dialog (`chunk-YEHSEGZY.js`)

```typescript
interface ConfirmDialogData {
  label: string;
  content: PolymorpheusContent;
  contentContext?: any;
  confirmText?: string;      // default: "Да"
  abortText?: string;        // default: "Нет"
  confirmAppearance?: string;
  confirmIconStart?: string;
  buttonSize?: string;       // default: "m"
  maxWidth?: string;         // default: "27rem"
  minWidth?: string;
  isNoAbort?: boolean;
  isConfirmDisabled?: () => boolean;
  labelStyle?: string;
}
```

Responsive: uses bottom sheet on mobile, dialog on desktop.

### 4.3 Language Switcher (`chunk-OIDSCTRK.js`)

```typescript
class LanguageService {
  isEn: boolean;
  language: "ru" | "en";
  locale: "en-US" | "ru-RU";

  requestChange(lang): void;       // fire-and-forget
  requestChange$(lang): Observable; // returns observable
}
```

Language options display:

```
{ code: "ru", flag: "ru", name: "Рус" }
{ code: "en", flag: "gb", name: "Eng" }
```

### 4.4 Mobile Menu

```typescript
interface MobileMenuState {
  isMobileMenuOpened$: Observable<boolean>;
  toggleMobileMenu(): void;
}
```

Sidebar items are constructed based on screen size:

- Desktop: header items only
- Mobile: header items + profile + logout + language switch

---

## 5. Enrollee Error Handling Details

### 5.1 InconsistentUserData Error Messages

| Condition                | Message                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| email + phone duplicated | "У тебя уже есть аккаунт с этими email {email} и телефоном {phone}. Если что -- обращайся в поддержку, мы поможем разобраться." |
| phone duplicated only    | "У тебя уже есть аккаунт с этим телефоном {phone}. Если что -- обращайся в поддержку, мы поможем разобраться."                  |
| email duplicated only    | "У тебя уже есть аккаунт с этим email {email}. Если что -- обращайся в поддержку, мы поможем разобраться."                      |
| other error              | "Что-то пошло не так. Пожалуйста, попробуй обновить страницу."                                                                  |

### 5.2 Not Found Page

```typescript
class NotFoundComponent {
  // For enrollees: link "На главную" -> "/"
  // For non-enrollees: link "В обучение" -> lmsUrl
}
```

Title: "Ничего не найдено!"
Body: "Запрашиваемый ресурс удален или у вас недостаточно прав для его просмотра"

---

## 6. Link Click Tracking

`cuTrackClick` directive on `<a>` elements:

- Tracks `mousedown` events (not click)
- Only fires on primary button (button === 0)
- Skips if ctrl/shift/alt/meta held
- Sends: `elementId` (or `innerText`) + `href` to `LINK_CLICK_LISTENER_TOKEN`

---

## 7. Events Detail -- Polling Strategy

The events service supports aggressive polling for fresh data:

```typescript
class EventFacade {
  getEvent$(slug) {
    // Polls every 3000ms, up to 5 retries
    // Stops on first non-undefined result
    // On 5th retry with HttpError: throws
  }

  list$(request, options?) {
    // If options.requestsCount > 1:
    //   Polls at options.intervalTime
    //   Stops after requestsCount requests
    //   Returns first non-empty result, or last result
    // Otherwise: single request
  }
}
```

---

## 8. Route Save/Restore on Auth

`ft` class (StartupService in appConfig):

```typescript
class StartupService {
  start$() {
    // When authenticated: navigate to last saved route
    // Before sign-in redirect: save current route
    return Observable.of(true);
  }
}
```

`Ke` (RouteStorageService):

- `saveCurrentRoute()` -- persists current URL
- `saveRoute(url)` -- persists specific URL
- `navigateToLastRoute()` -- restores saved route
- Provider scoped: `Ke.provide("hub")` for main, `Ke.provide("hub-onboarding")` for onboarding

---

## 9. Input Masks

Defined in `chunk-NOMFNW4V.js`:

| Name        | Pattern                  | Example          |
|-------------|--------------------------|------------------|
| SNILS       | `XXX-XXX-XXX-XX`         | `123-456-789-01` |
| INN 10      | `XXXXXXXXXX`             | `1234567890`     |
| INN 12      | `XXXXXXXXXXXX`           | `123456789012`   |
| KPP         | `XXX-XXX`                | `123-456`        |
| Postal code | `XXXXXX`                 | `123456`         |
| Year        | `XXXX`                   | `2025`           |
| Telegram    | `/^[a-zA-Z0-9_]{0,32}$/` | `my_username`    |

---

## 10. File Download Utility

```typescript
// Extracts filename from Content-Disposition header
function getFileName(headers): string | undefined {
  // Tries: filename*=UTF-8''encoded_name
  // Falls back to: filename="name" or filename=name
}
```

---

## 11. Third-Party Integrations Summary

| Integration            | Purpose                             | Config Source                            |
|------------------------|-------------------------------------|------------------------------------------|
| Keycloak               | OAuth2 authentication               | `authUrl` from dynamic config            |
| Error Hub (Tinkoff)    | Error reporting (Sentry-compatible) | DSN hardcoded: `error-hub.tinkoff.ru`    |
| Tinkoff Statist        | Analytics event tracking            | `statistEndpointUrl` from dynamic config |
| @tinkoff/cobrowsing    | Co-browsing support sessions        | CDN: `cobrowsing.tbank.ru`               |
| Perfume.js             | Web performance metrics             | Built-in, reports via Statist            |
| Teletype               | Chat/messaging widget               | `teletypeId` from dynamic config         |
| TaigaUI                | Component library                   | Bundled                                  |
| DOMPurify              | HTML sanitization                   | Bundled v3.1.7                           |
| Markdown-it            | Markdown rendering                  | Bundled                                  |
| Angular Service Worker | Offline/push notifications          | `ngsw-worker.js`                         |

---

## 12. Identified Feature Flags

No Flipt integration was found in the root app. Feature visibility is controlled
through:

1. **Role-based guards** -- routes shown/hidden based on user roles
2. **Server-driven availability** -- e.g. `getIsAdmissionAvailableOnce$()` checks
   server state before enabling admission route
3. **Admin configurations** -- `/api/admin/configurations/:key` provides key-value
   config that can toggle features (including `cobrowsing-*-recorded-urls` for
   cobrowsing activation)
4. **Dynamic config** -- `configuration.json` provides environment-specific settings
5. **Environment check** -- Sentry enabled only in `[Develop, Preprod, Prod]`
