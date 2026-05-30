# Guesser

Guesser is a fast logic game where players deduce a hidden 3-digit number.

This project has been modernized with current Android tooling, refreshed UI, cleaner game logic, and updated docs.

## Gameplay

- Secret is always a 3-digit number with unique digits.
- Guesses must also be 3 unique digits.
- Feedback per guess:
  - `R` (Right): digit exists and is in the correct position.
  - `W` (Wrong position): digit exists but in a different position.
  - `SMYLE`: no guessed digits exist in the secret.
  - `Winner`: all 3 digits are correct and in the correct position (`3R`).

### Example

- Secret: `245`
- Guess: `325`
- Result: `1R, 1W`

## Modes

- **App mode**: app auto-generates and hides the secret.
- **Friend mode**: one player enters the secret, other player guesses.

## Tech Stack

- Kotlin
- Android Gradle Plugin `9.0.1`
- Gradle `9.1.0`
- Android SDK `36` (target/compile)
- Min SDK `21`
- AndroidX + Material 3
- Navigation Component
- Lifecycle (LiveData + ViewModel)

## Project Structure

- `app/src/main/java/com/thekeval/guesser/domain` - core game rules
- `app/src/main/java/com/thekeval/guesser/viewmodel` - game state and actions
- `app/src/main/java/com/thekeval/guesser/ui` - fragments and activity
- `app/src/main/java/com/thekeval/guesser/adapters` - guesses list adapter

## Build and Run

```bash
cd /Users/keval/data/projects/Guesser
./gradlew assembleDebug
./gradlew installDebug
```

To launch from command line after install:

```bash
/Users/keval/Library/Android/sdk/platform-tools/adb shell am start -n com.thekeval.guesser/.MainActivity
```

## Tests

```bash
cd /Users/keval/data/projects/Guesser
./gradlew testDebugUnitTest
```

Unit tests cover remark generation and validation in `GameRules`.
