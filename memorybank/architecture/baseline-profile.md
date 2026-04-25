# Baseline Profile (Android)

## Структура модулей

- `:baselineprofile` (com.android.test + `androidx.baselineprofile`) — генерирует профили
- `:baseline-profile-tags` — testTag-константы, общие между генератором и UI
- `:androidApp` — потребитель: `androidx.baselineprofile` + `androidx.profileinstaller` (для API 28-30)
  - `baselineProfile { saveInSrc = true; mergeIntoMain = true }`
  - MainActivity: `Box.semantics { testTagsAsResourceId = true }` для UiAutomator

## TestTag-якоря (в BaselineTestTags)

4 таба нижней навигации, 3 якоря login/BffCookie, первая карточка курса, первая карточка задачи. Поверхность минимальная.

## Генераторы (3 шт., от стабильного к полному)

1. **StartupBaselineProfileGenerator** — только холодный старт. Без testTag — всегда проходит.
2. **UnauthUiBaselineProfileGenerator** — экран логина (Email → BffCookie шаг).
3. **LoggedInTourBaselineProfileGenerator** — требует `bffCookie` instrumentation arg (`Assume.assumeTrue`); полный логин + обход табов + один курс.

## GMD

`pixel6Api31` (AOSP system image) в `:baselineprofile`. Использует локально закэшированный `android-31/default` образ.

## Генерация

```bash
./gradlew :androidApp:generateBaselineProfile
# Для logged-in тура:
./gradlew :androidApp:generateBaselineProfile \
  -Pandroid.testInstrumentationRunnerArguments.bffCookie=$BFF_COOKIE
```

Результат коммитится в `androidApp/src/main/generated/baselineProfiles/baseline-prof.txt` и автоматически включается в `assembleRelease`.

## Верификация

```bash
./scripts/verify-baseline-profile.sh <path-to-apk>
```

Проверяет наличие `assets/dexopt/baseline.prof{,m}` в APK/AAB.

## CI

CI не регенерирует профили (нет эмулятора). Закоммиченный txt используется как source-of-truth для каждой release-сборки.
