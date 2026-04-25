# Паттерны архитектуры

## MVI (Model-View-Intent)

Каждый экран состоит из трёх частей:

1. **`XxxComponent.kt`** — интерфейс с `State`, `Intent`, `Effect` и `stateFlow`/`effects`
2. **`DefaultXxxComponent.kt`** — реализация
3. **`XxxScreen.kt`** — Compose UI, потребляющий state и отправляющий intent'ы

Для сложных экранов UI может быть разбит на несколько файлов.

---

## ContentState\<T\> — прогрессивная загрузка

Sealed interface в `presentation/common/ContentState.kt`: `Loading`, `Success<T>`, `Error`.

Заменяет `isLoading: Boolean` + `error: String?` + plain data в State. Каждое поле данных в State оборачивается в `ContentState<T>` для отдельной обработки загрузки/ошибок каждой секции.

**Правила:**
- Важная ошибка → полноэкранный `ErrorContent`; второстепенная → `ActionErrorBar`
- При refresh: все ContentState сбрасываются в `Loading` → скелетоны → данные заполняются прогрессивно
- Никаких `isLoading`/`isRefreshing` boolean'ов — выводи из ContentState:
  `val isContentLoading get() = tasks.isLoading && courses.isLoading`
- Расширения: `dataOrNull`, `isLoading`, `isError`, `isSuccess`, `errorOrNull`
- Параллельная загрузка: компоненты запускают отдельные корутины на каждый API-вызов, каждая обновляет свой ContentState независимо

---

## Одноразовые эффекты (Effects)

Компоненты с мутациями предоставляют `val effects: Flow<Effect>` для одноразовых событий (ошибки, тосты).

- Реализация: `Channel<Effect>(Channel.BUFFERED)` + `receiveAsFlow()` в Default-компонентах
- UI собирает эффекты в `LaunchedEffect(Unit)` в локальный `mutableStateOf`
- `ActionErrorBar` (в `CommonStates.kt`) показывает временные ошибки с кнопкой dismiss
- Паттерн: `_effects.trySend(Effect.ShowError("message"))` в компоненте → собирается в screen-composable

---

## Навигация (Decompose)

- **ChildPages** — нижние табы (сохраняют состояние при переключении)
- **ChildStack** — detail-навигация (CourseDetail, Longread, Profile, Notifications, CoursePerformance)
- **ChildItems** — longread-материалы в LazyColumn. Экспериментальный API (`@ExperimentalDecomposeApi`).
  Использует `MaterialConfig` (serializable sealed interface) как ключ, `LongreadItem` (sealed wrapper) как child.
  `CodingMaterialComponent` — полноценный MVI; простые материалы (Markdown, File, Image) — лёгкие обёртки.

### Longread-материалы: детали

- **material как constructor-val**: `LongreadMaterial` — неизменяемые данные, фиксированные при создании. Остаётся `val` в конструкторе, НЕ в State (иначе раздувает каждый `state.copy()`).
- **ExternalUpdate канал**: Родитель рассылает события (смена поискового запроса) через `MutableSharedFlow<ExternalUpdate>(replay=1)`. Дети собирают и сохраняют в своём state.
- **Поиск в longread**: `DefaultLongreadComponent` предварительно извлекает plain text из HTML каждого материала (`buildPlainTextIndex` → `Map<String, String>`, на `dispatchers.default`). Нажатия клавиш идут в `MutableStateFlow<String>`, debounce 180ms; затем подсчёт совпадений на `dispatchers.default`, и только потом `ExternalUpdate.SearchQuery` детям. `LongreadSearchHandler` — чистый state-мутатор.

### `navigation.push()` — требует `@OptIn(DelicateDecomposeApi::class)`

---

## DI (Koin)

- `di/Koin.kt` — общие модули (core, network, data, repository)
- `androidMain/di/AndroidKoin.kt` — Android-специфичные привязки + `createRootComponent()`
- `iosMain/di/IosKoin.kt` — iOS-специфичные привязки
- `iosMain/MainViewController.kt` — `createRootComponent()` для iOS
- `coreModule` предоставляет singleton `AppDispatchers`

### Ленивые зависимости Main

`DefaultRootComponent` принимает `mainDependenciesFactory: () -> MainDependencies` (не готовый бандл).
Хранится за `by lazy(mainDependenciesFactory)` — 13 репозиториев/сервисов создаются из Koin только при первом переходе на `Config.Main`. Неавторизованные запуски (splash → Login) не инстанцируют синглтоны основного потока.

---

## Корутин-скоупы

Используй `componentScope()` (в `presentation/common/ComponentScope.kt`) в каждом `Default*Component`:

```kotlin
private val scope = componentScope()
```

Оборачивает essenty `lifecycle.coroutineScope(Dispatchers.Main.immediate + SupervisorJob())`.
**Никогда** не создавай `CoroutineScope` + `onDestroy` вручную.

---

## Инжекция диспатчеров (AppDispatchers)

`util/AppDispatchers.kt` — класс с полями `io`, `default`, `main`. Singleton в Koin (`coreModule`).

- Репозитории с сетью/DataStore/файлами наследуют `CookieAwareRepository(authLocal, dispatchers)` — `withCookie {}` уже оборачивает в `withContext(dispatchers.io)`.
- CPU-bound вычисления в компонентах (фильтрация задач, агрегация оценок, подсчёт совпадений) — на `dispatchers.default`.
- Room `queryDispatcher` уже использует `Dispatchers.IO` — не оборачивай повторно.
- `MainDependencies.dispatchers` передаётся через `TabChildFactory`/`DetailChildFactory` всем компонентам.

---

## Ленивая загрузка табов (ChildPages)

`DefaultMainComponent` подключает табы через `ChildPages`. Неактивные табы в `Status.CREATED` — конструкторы и `init {}` выполняются при старте приложения.

**Правило:** не вызывай сетевые функции из `init {}` в таб-компонентах. Используй:

```kotlin
init {
    lifecycle.doOnStart(isOneTime = true) {
        loadData()
    }
}
```

`doOnStart(isOneTime = true)` — срабатывает только при первом `CREATED → STARTED` (первое открытие таба). Повторные переходы не перезагружают — пользователь обновляет через pull-to-refresh.

Дешёвые локальные наблюдатели (DataStore flows) можно оставить в `init {}`.
Detail-экраны (profile, longread и т.д.) создаются при навигации, поэтому `init { load() }` для них допустим.

---

## Утилиты форматирования

- Все даты/время/размеры: `presentation/common/FormatUtils.kt`
- Дедлайны: `presentation/common/DeadlineFormat.kt` — `parseDeadlineInstant`, `isOverdue`, `formatDeadlineTime` ("HH:mm"), `formatDeadlineDayShortMonth` ("5 апр")
- Русские месяцы: `russianMonthsShort` ("янв", "фев"), `russianMonthsFull` ("января", "февраля") — импортируй, не дублируй
- Внутренний хелпер `parseIsoDateTime(iso)` обрабатывает все форматы (с offset, с Z, только дата → конец дня). Используй через публичные `format*`/`parseDeadlineInstant`
- Только `kotlinx-datetime`, никаких JVM-only API
- `DateTimeProvider` в `commonMain` для "сегодня" и date-to-millis
