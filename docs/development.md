# Development Guide

## Requirements

- JDK 17
- Android Studio with Android SDK 36, or equivalent command-line tools
- An emulator or device running Android 5.0/API 21 or later

Set `ANDROID_HOME`, or create an untracked `local.properties`:

```properties
sdk.dir=/absolute/path/to/Android/sdk
```

## Understand the Module Names

The new application lives in `guesser/` and is Gradle project `:guesser`.
The legacy source remains in `app/`, but `settings.gradle` registers that
directory as `:guesser_v1`; there is no `:app` project.

List the configured projects with:

```bash
./gradlew projects
```

## Build and Test

Primary revamp:

```bash
./gradlew :guesser:assembleDebug
./gradlew :guesser:lintDebug
./gradlew :gameplay:testDebugUnitTest
./gradlew :domain:test
```

Guesser V1:

```bash
./gradlew :guesser_v1:assembleDebug
./gradlew :guesser_v1:testDebugUnitTest
./gradlew :guesser_v1:lintDebug
```

Combined local verification:

```bash
./gradlew --no-daemon \
  :guesser:assembleDebug \
  :guesser:lintDebug \
  :gameplay:testDebugUnitTest \
  :domain:test \
  :guesser_v1:assembleDebug \
  :guesser_v1:testDebugUnitTest \
  :guesser_v1:lintDebug
```

Connected Android tests require a running emulator or device and can be
selected per application:

```bash
./gradlew :guesser:connectedDebugAndroidTest
./gradlew :guesser_v1:connectedDebugAndroidTest
```

## Install and Launch

Revamp:

```bash
./gradlew :guesser:installDebug
adb shell am start -n io.keval.apps.guesser/.MainActivity
```

V1:

```bash
./gradlew :guesser_v1:installDebug
adb shell am start -n com.thekeval.guesser/.MainActivity
```

The different application IDs allow both builds to be installed together.

## Changing the Revamp

### Domain and Data

- Put platform-independent contracts, models, and use cases in `:domain`.
- Implement repository contracts in `:data`.
- Assemble concrete dependencies only at the `:guesser` application boundary.
- Keep secrets and domain decisions out of navigation route strings and logs.
- Add Kotlin/JVM tests for pure logic before wiring UI.

### Gameplay UI

- Use a stateful `*Route` composable to create/collect state and a stateless
  `*Screen` composable to render it.
- Keep destination route names in `gameplay/navigation/GameplayRoutes.kt`.
- Put shared feature UI in `gameplay/common`; put cross-feature theme tokens in
  `:core-ui`.
- Put player-visible production copy in resources. Existing placeholder copy
  is temporary, not a convention to extend.
- Preserve lifecycle-aware collection and accessible control semantics.

### State and Persistence

The current friend secret is process-memory only. Before introducing
persistence, define:

- whether a secret survives process death;
- when it is consumed or cleared;
- whether it may be backed up;
- how sensitive values are excluded from logs and navigation arguments.

Do not use the existing DataStore dependency as an implicit design decision;
it is not used by production code today.

### Artwork

- Preserve original supplied files in `design_assets/raw_assets`.
- Put runtime-ready copies in the owning module's Android resources.
- Avoid runtime dependencies on source-design directories.
- Maintain content descriptions for interactive image controls; decorative
  images should remain hidden from accessibility semantics.

## Changing Guesser V1

V1 changes belong under `app/` and use Gradle prefix `:guesser_v1`.

For a V1 rules change:

1. Update `GameRules`.
2. Update `GameRulesTest`.
3. Update `GameViewModel` if state transitions change.
4. Update V1 string resources, its Rules screen, and `docs/gameplay.md`.
5. Verify App and Friend modes.

Do not duplicate new V1 logic in `GameFragment`, and do not make the revamp
depend on the V1 application module.

## Test Scope

- Use `:domain` JVM tests for new pure game rules and use cases.
- Use `:gameplay` JVM tests for ViewModel state transitions and validation.
- Use Compose UI/instrumentation tests for semantics, rendering, input, and
  navigation.
- Use V1 JVM tests for its existing `GameRules` and ViewModel behavior.
- Test compact screens because the artwork-driven Home screen scrolls and
  responds to the IME.

## Documentation Maintenance

Keep these documents synchronized:

- behavior and implementation status: `docs/gameplay.md`;
- modules, dependencies, state, or navigation: `docs/architecture.md`;
- code-level runtime flow: `docs/implementation.md`;
- completed phases and future direction: `docs/revamp.md`;
- setup and commands: this guide and the root `README.md`.

Always state whether behavior belongs to the revamp or V1.
