# Аутентификация

## Основной поток

1. Запуск → Android native splash (`androidx.core:core-splashscreen`)
2. Root-навигация стартует в `SplashChild`, пока `DefaultRootComponent.checkSavedAuth()` проверяет cookie локально (`hasCookie()`)
3. Cookie есть → `SplashChild` → `MainChild`, валидация в фоне
4. Cookie нет → `SplashChild` → `LoginChild`
5. Фоновая валидация провалилась → редирект в `LoginChild`
6. Логин → `WebViewLoginChild` (или нативный auth flow)
7. WebView загружает `https://my.centraluniversity.ru`
8. При каждой загрузке страницы проверяет cookies на `bff.cookie`
9. Найден → сохраняет через `AuthRepository` → валидирует через `GET /student-hub/students/me` → `MainChild`
10. Логаут → очистка cookie → `LoginChild`
11. Android native splash: убирается через `setKeepOnScreenCondition` — виден пока active child = `SplashChild`

---

## Точки входа (LoginComponent)

`LoginComponent.AuthStep` — четыре ветки:

- **Email / Password / Otp** — нативный Keycloak-поток через `AuthApiService`
- **BffCookie** — прямая вставка значения `bff.cookie`. Обходит Keycloak, вызывает `authRepository.saveCookie()` + `validateCookie()`. Для тестирования и ручного восстановления.
- Intent **FallbackToWebView** — переход к полному WebView-логину

Ошибки — одноразовые события: `LoginComponent.Effect.ShowError` через буферизованный Channel, собирается в `LoginScreen` в локальный `mutableStateOf<String?>`, очищается при смене шага.

---

## SMS OTP автозаполнение

`presentation/auth/sms/SmsCodeObserver` — `expect @Composable`, доставляет обнаруженный OTP-код в callback.

**Android**: SMS User Consent API (`play-services-auth-api-phone`). Без runtime-разрешений; система показывает one-tap consent dialog, доставляет SMS через ActivityResult. Receiver зарегистрирован с `SmsRetriever.SEND_PERMISSION` через `ContextCompat.registerReceiver` (RECEIVER_EXPORTED на API 33+).

**iOS**: нет API для чтения SMS → fallback на clipboard. Слушает `UIPasteboardChangedNotification` и `UIApplicationDidBecomeActiveNotification`, извлекает код из строки clipboard.

Использование:
- Нативный OTP-шаг (`OtpStepContent`): `SmsCodeObserver` → заполняет `otpCode` через intent → авто-отправка
- WebView (`PlatformWebView.android.kt`): `SmsCodeObserver` → пишет в Android clipboard → подсказка клавиатуры для вставки в поле WebView

---

## Автозаполнение форм

### Android WebView
`PlatformWebView.android.kt`: `importantForAutofill = IMPORTANT_FOR_AUTOFILL_YES` и `settings.saveFormData = true` — Google/менеджер паролей может предложить сохранённые учётные данные.

### Нативные поля
`LoginStepContent.kt` — `OutlinedTextField` с `Modifier.contentType(...)`:
- Email → `ContentType.EmailAddress`
- Password → `ContentType.Password`
- OTP → `ContentType.SmsOtpCode`

`AuthTextField` принимает `contentType: ContentType?` — передавай из каждого step-composable при добавлении новых полей.
