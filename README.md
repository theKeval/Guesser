# Guesser

Guesser is a small Android logic game about deducing a hidden three-character
digit code. Every code uses three distinct digits; after each guess, the game
reports how many digits are in the right position (`R`) and how many belong in
the code but are in the wrong position (`W`).

The project began as a 2021 Android prototype and was modernized in 2026 with
current Android tooling, extracted game rules, explicit game state, refreshed
Material UI, a custom keypad, sound and vibration feedback, and improved tests.

## Gameplay

- The secret and every guess contain exactly three distinct digits.
- `R` means a digit is correct and in the correct position.
- `W` means a digit occurs in the secret but in a different position.
- `SMYLE` means the guess shares no digits with the secret.
- `Winner` means all three digits and positions match.
- There is no guess limit, timer, score, or daily puzzle.
- A leading zero is valid: values are treated as three-character digit codes,
  not as integers.

Example: for secret `245`, guess `325` produces `1R, 1W`.

See [Gameplay and rules](docs/gameplay.md) for the complete rules, round flow,
game modes, and edge cases.

## Modes

- **App mode** generates and hides a random secret.
- **Friend mode** lets one player enter and hide the secret before passing the
  device to the seeker.

Revealing the secret abandons the current round. Resetting clears the guess
history and returns the selected mode to its initial state.

## Implementation at a Glance

Guesser is a single-activity, fragment-based Android application:

```text
MainActivity + Navigation
          |
     GameFragment  <---- UserPreferences
          |
    GameViewModel
          |
      GameRules
          |
 GuessModel -> GuessesAdapter
```

`GameRules` is the pure rules engine. `GameViewModel` owns the current secret,
guess history, and round state. `GameFragment` renders state and coordinates
input, dialogs, the custom keypad, sound, vibration, animation, insets, and
list scrolling.

See:

- [Documentation index](docs/README.md)
- [Architecture](docs/architecture.md)
- [Implementation guide](docs/implementation.md)
- [Revamp history and roadmap](docs/revamp.md)
- [Development guide](docs/development.md)

## Technology

- Kotlin 2.2
- Android Gradle Plugin 9.0.1 and Gradle 9.1
- Java 17
- Compile/target SDK 36; minimum SDK 21
- AndroidX, Material 3, Navigation Component, LiveData, and ViewModel
- XML layouts with generated view/data binding
- JUnit 4 unit tests

The app is local-only. It has no backend, account system, analytics, database,
or network dependency. `SharedPreferences` stores only sound and vibration
choices.

## Build

Prerequisites:

1. JDK 17
2. Android SDK 36
3. `ANDROID_HOME` configured, or `sdk.dir` set in an untracked
   `local.properties`

From the repository root:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Install and launch on a connected emulator or device:

```bash
./gradlew installDebug
adb shell am start -n com.thekeval.guesser/.MainActivity
```

## Project Layout

```text
app/src/main/java/com/thekeval/guesser/
├── MainActivity.kt          # Activity, drawer, and navigation host
├── adapters/                # Guess-history RecyclerView binding
├── data/                    # Local feedback preferences
├── domain/                  # Pure validation, generation, and scoring rules
├── model/                   # Guess display model
├── ui/                      # Game, Rules, About, and Settings fragments
└── viewmodel/               # Round state and user actions

app/src/main/res/
├── layout/                  # Screen and row layouts
├── navigation/              # Fragment destinations
├── raw/                     # Key, result, and win sound effects
└── values*/                 # Copy, colors, dimensions, and themes
```

## License

Licensed under the [Apache License 2.0](LICENSE).
