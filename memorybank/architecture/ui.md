# UI-конвенции

## Тема (тёмная + светлая)

- `AppColorScheme` — data class в `Theme.kt`: все цвета (тема-зависимые + семантические)
- `DarkAppColors` / `LightAppColors` — экземпляры; семантические цвета (состояния задач, оценки, категории) одинаковые в обеих темах
- `LocalAppColors` — `staticCompositionLocalOf`; `AppTheme.colors` — `@Composable @ReadOnlyComposable` accessor
- `CuMobileTheme(darkTheme)` — обёртка `CompositionLocalProvider` + `MaterialTheme`; используется в `App()` и `@Preview`
- Тёмный акцент: `#00E676`; Светлый акцент: `#007B32`
- Все экраны используют `AppTheme.colors.xxx` (camelCase), никогда хардкод-цвета
- Функции `taskStateColor()`, `courseCategoryColor()`, `gradeColor()` — `@Composable` (читают из `AppTheme.colors`)

## Иконки

Material Icons Extended: `androidx.compose.material:material-icons-extended:1.11.0-alpha02`

## Превью

Каждая `@Composable` screen-функция должна иметь `@Preview` (тёмный + светлый вариант).

Паттерн:
- `XxxScreen(component)` — не содержит UI-логики, делегирует в `XxxScreenContent(state, onIntent, ...)`
- `XxxScreenContent` — `internal`; превью вызывают эту функцию с моковым состоянием
- Общие компоненты (TopBar, TaskCard) — превью оборачивают в `CuMobileTheme` + `Box(background)`
- Import: `import androidx.compose.ui.tooling.preview.Preview`
- Превью обычно в отдельном файле `XxxScreenPreviews.kt` (detekt не будет ругаться на `MagicNumber`)

## Detekt

- Запуск: `./gradlew detektAll` — исправляй нарушения, никогда не подавляй
- 6 "compiler errors" в detektMainAndroid — false positives (cross-module resolution), игнорируй
- `MagicNumber` с `ignoreAnnotated: ["Composable"]` — числа внутри `@Composable` функций (dp/sp/alpha) не требуют выноса в константы
- Выноси числа в константы только когда они несут неочевидный семантический смысл (`MillisPerHour`, `UrgencyRedHours`)
