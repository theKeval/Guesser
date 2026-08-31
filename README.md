# Guesser

Guesser is an Android number-deduction game in active redevelopment. The
repository now contains two independent applications:

| Application | Gradle module | Source | Status |
| --- | --- | --- | --- |
| **Guesser revamp** | `:guesser` | `guesser/` | Primary Compose app; home and navigation foundation implemented, gameplay screens still placeholders |
| **Guesser V1** | `:guesser_v1` | `app/` | Preserved, fully playable legacy game |

The revamp does not reuse V1 source or resources. Both applications build
independently from the same Gradle project.

## Current Revamp

The new app uses the package and application ID `io.keval.apps.guesser`. Its
current implemented experience includes:

- a responsive, scrollable, portrait-first Jetpack Compose home screen;
- artwork-backed controls for Single Player and Double Player modes;
- a masked Double Player secret field with three-unique-digit validation;
- in-memory handoff of a valid friend secret to the Game destination;
- animated navigation to Game, Tutorial, Gameplay, and About destinations;
- a shared wood-background scaffold, back control, and Compose theme;
- clean module boundaries for UI, domain contracts, data, and app assembly.

The Game, Tutorial, Gameplay, and About destinations are intentionally
placeholders. The new app does **not yet implement guessing, `R`/`W` scoring,
round state, history, sound, vibration, settings, profiles, or scoreboards**.

## Game Rules

The established Guesser rules, currently playable in Guesser V1 and intended
to guide the revamp, are:

- The secret and every guess contain exactly three distinct digits.
- `R` means a digit is correct and in the correct position.
- `W` means a digit occurs in the secret but in another position.
- `SMYLE` means the guess shares no digits with the secret.
- `Winner` means all three digits and positions match.
- Leading zeroes are valid because codes are represented as strings.

Example: secret `245` and guess `325` produce `1R, 1W`.

See [Gameplay and implementation status](docs/gameplay.md) for the complete
rules and a precise distinction between V1 behavior and revamp behavior.

## Architecture

```text
:guesser (Compose application and navigation)
├── :gameplay (Home and destination UI)
│   ├── :core-ui (Compose theme)
│   └── :domain (repository contracts and use cases)
├── :data (in-memory repository implementations)
│   └── :domain
├── :domain
└── :core-ui

:guesser_v1 (independent legacy XML/View application)
```

| Module | Purpose |
| --- | --- |
| `:guesser` | Application entry point, manual dependency container, and Compose navigation |
| `:gameplay` | Home state/UI and Game, Tutorial, Gameplay, and About destinations |
| `:domain` | Pure Kotlin repository contracts and use cases |
| `:data` | Repository implementations; currently app info and in-memory session data |
| `:core-ui` | Shared Material 3 color and typography foundation |
| `:guesser_v1` | Complete legacy game, physically retained in `app/` |

See the [architecture guide](docs/architecture.md) and
[implementation guide](docs/implementation.md) for the runtime flow.

## Technology

- Kotlin 2.2
- Android Gradle Plugin 9.0.1 and Gradle 9.1
- Java 17
- Compile/target SDK 36; minimum SDK 21
- Jetpack Compose and Navigation Compose for the revamp
- XML Views, Navigation Component, LiveData, and ViewModel for V1
- JUnit 4 tests

The current revamp has no backend, account system, analytics, or network
integration. Double Player secrets are held only in memory and are lost when
the application process ends.

## Build and Test

Prerequisites:

1. JDK 17
2. Android SDK 36
3. `ANDROID_HOME` configured, or `sdk.dir` set in an untracked
   `local.properties`

Build both applications and run their JVM tests:

```bash
./gradlew :guesser:assembleDebug \
  :gameplay:testDebugUnitTest \
  :domain:test \
  :guesser_v1:assembleDebug \
  :guesser_v1:testDebugUnitTest
```

Build only the primary revamp:

```bash
./gradlew :guesser:assembleDebug
```

Install and launch the revamp:

```bash
./gradlew :guesser:installDebug
adb shell am start -n io.keval.apps.guesser/.MainActivity
```

Install and launch V1:

```bash
./gradlew :guesser_v1:installDebug
adb shell am start -n com.thekeval.guesser/.MainActivity
```

## Repository Layout

```text
guesser/          # New Compose application
gameplay/         # Revamp screens, home state, and navigation route names
domain/           # Revamp repository contracts and use cases
data/             # Revamp repository implementations
core-ui/          # Revamp Compose theme
app/              # Guesser V1 source, registered as :guesser_v1
design_assets/    # Original source artwork; not runtime resources
docs/             # Architecture, gameplay, implementation, and revamp docs
```

## Documentation

- [Documentation index](docs/README.md)
- [Gameplay and implementation status](docs/gameplay.md)
- [Architecture](docs/architecture.md)
- [Implementation guide](docs/implementation.md)
- [Revamp history and roadmap](docs/revamp.md)
- [Development guide](docs/development.md)

## License

Licensed under the [Apache License 2.0](LICENSE).
