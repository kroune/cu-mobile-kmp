# CuMobile KMP — Обзор архитектуры

## Проект

KMP-переписка Flutter LMS-приложения для Центрального Университета (ЦУ).
Оригинальное Flutter-приложение: `/home/olowo/StudioProjects/lms-mobile`.
Реверс веб-приложения: `web-reverse/` (анализ API, моделей, поведения сайта).

- Платформы: Android + iOS (тестируем на Android-эмуляторе, Linux-хост)
- Максимум общего кода в `shared/`

**Android applicationId**: `com.thirdparty.cumobile` (совпадает с Flutter для бесшовного обновления), debug-постфикс для отладочных сборок
**iOS bundleId**: `ru.spacedreamer.centraluniversity` (совпадает с Flutter)
**Kotlin-пакет**: `io.github.kroune.cumobile`
**Base URL**: `https://my.centraluniversity.ru/api/`

---

## Стек технологий

| Библиотека | Версия | Назначение |
|---|---|---|
| Compose Multiplatform | 1.11.0-alpha02 | UI (shared) |
| Decompose | 3.5.0 | Навигация (ChildPages + ChildStack + ChildItems) |
| Koin | — | DI |
| Ktor | — | HTTP-клиент |
| kotlinx-serialization | — | JSON |
| DataStore Preferences | — | Key-value хранилище |
| Room | 2.8.4 | Структурированное хранилище (SQLite) |
| essenty-lifecycle-coroutines | 2.5.0 | Автоотменяемые корутин-скоупы |
| kotlin-logging (oshai) | 8.0.01 | Логирование в catch-блоках |
| kotlinx-datetime | — | Форматирование дат |
| Ksoup (fleeksoft) | 0.2.6 | Парсинг HTML (KMP-порт Jsoup) |
| ComposeMediaPlayer | 0.8.7 | Видео/аудио (KMP) |
| play-services-auth-api-phone | 18.2.0 | SMS User Consent API (только Android) |

---

## Структура модулей

```
CuMobile/
├── androidApp/                    # Android-оболочка
│   └── src/main/
│       ├── AndroidManifest.xml    # android:name=".AndroidApplication"
│       ├── MainActivity.kt        # createRootComponent() из DI; testTagsAsResourceId
│       └── AndroidApplication.kt  # Инициализация Koin
│   └── src/release/generated/baselineProfiles/  # baseline-prof.txt
├── baselineprofile/               # Генератор baseline profiles (UiAutomator)
├── baseline-profile-tags/         # KMP-библиотека с testTag-константами
├── iosApp/                        # iOS-оболочка (Swift)
│   └── iOSApp.swift               # MainViewControllerKt.createRootComponent()
└── shared/
    └── src/
        ├── commonMain/kotlin/io/github/kroune/cumobile/
        │   ├── data/
        │   │   ├── local/         # DataStore, AuthLocalDataSource, FileStorage
        │   │   │   └── db/        # Room: AppDatabase, Entity, DAO, миграции
        │   │   ├── model/         # ~37 @Serializable DTO
        │   │   ├── network/       # HttpClientFactory, ApiService (25 эндпоинтов)
        │   │   └── repository/    # 8 реализаций + CookieAwareRepository
        │   ├── di/                # Koin.kt (модули: core, network, data, repository)
        │   ├── domain/
        │   │   └── repository/    # 8 интерфейсов репозиториев
        │   └── presentation/
        │       ├── auth/          # LoginComponent, LoginScreen, LoginStepContent
        │       │   ├── sms/       # SmsCodeObserver (expect) — OTP-автозаполнение
        │       │   └── webview/   # WebViewLoginComponent, PlatformWebView (expect/actual)
        │       ├── common/        # Theme, TopBar, TaskCard, CourseCard, FormatUtils
        │       ├── courses/       # CoursesComponent + detail/CourseDetailComponent
        │       ├── files/         # FilesComponent (файловый менеджер)
        │       ├── home/          # HomeComponent (дедлайны + расписание + курсы)
        │       ├── longread/      # LongreadComponent (ChildItems), Screen, SearchHandler
        │       │   ├── component/ # MaterialConfig, LongreadItem, CodingMaterialComponent
        │       │   ├── htmlrender/ # HTML→Compose (Ksoup → HtmlBlock → composable)
        │       │   └── ui/        # LongreadScreen, CommentsTab, InfoTab, SolutionTab
        │       ├── main/          # MainComponent (ChildPages табы + ChildStack детали)
        │       ├── notifications/ # NotificationsComponent
        │       ├── performance/   # CoursePerformanceComponent (2 вкладки)
        │       ├── profile/       # ProfileComponent
        │       ├── root/          # RootComponent, RootScreen
        │       ├── scanner/       # ScannerComponent (сканер документов + PDF)
        │       └── tasks/         # TasksComponent (полный MVI с фильтрацией)
        ├── androidMain/           # AndroidKoin, DataStorePath, PlatformWebView, FileStorage
        └── iosMain/               # IosKoin, DataStorePath, PlatformWebView, FileStorage, MainViewController
```

---

## Ключевые паттерны (кратко)

> Детали каждого паттерна — в `architecture/patterns.md`

**MVI** — каждый экран: `XxxComponent` (интерфейс с State/Intent/Effect) → `DefaultXxxComponent` (реализация) → `XxxScreen` (Compose UI).

**ContentState\<T\>** — sealed interface (`Loading`, `Success<T>`, `Error`) для per-section загрузки. Никаких `isLoading: Boolean` — всё выводится из ContentState.

**Навигация (Decompose)** — ChildPages для табов, ChildStack для detail-экранов, ChildItems для longread-материалов.

**DI (Koin)** — общие модули в `di/Koin.kt`, платформенные в `androidMain/di/` и `iosMain/di/`.

**Репозитории** — все наследуют `CookieAwareRepository`, предоставляющий `withCookie {}`. ApiService никогда не бросает исключений — возвращает null/false.

**Скоупы** — `componentScope()` в каждом компоненте. Никогда не создавать CoroutineScope вручную.

**Диспатчеры** — `AppDispatchers` инжектится через конструктор. Прямые ссылки на `Dispatchers.IO/Default` запрещены (detekt `InjectDispatcher`).

**Форматирование дат** — `FormatUtils.kt` и `DeadlineFormat.kt`. Русские названия месяцев через `russianMonthsShort`/`russianMonthsFull`. Использовать `kotlinx-datetime`.

---

## Детальная документация

| Файл | Содержимое |
|------|-----------|
| `architecture/patterns.md` | MVI, ContentState, Effects, навигация, DI, ленивая загрузка табов, скоупы, диспатчеры, форматирование |
| `architecture/data-layer.md` | Репозитории, API-эндпоинты, модели данных, task state machine, late days, фильтрация задач |
| `architecture/ui.md` | Тема (тёмная/светлая), иконки, превью, detekt |
| `architecture/auth.md` | Поток аутентификации, OTP, WebView, автозаполнение |
| `architecture/baseline-profile.md` | Baseline profiles, генерация, верификация |
