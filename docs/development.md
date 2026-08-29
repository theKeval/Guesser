# Development Guide

## Requirements

- JDK 17
- Android Studio with Android SDK 36, or equivalent command-line tools
- An emulator or device running Android 5.0/API 21 or later

Set `ANDROID_HOME`, or create an untracked `local.properties`:

```properties
sdk.dir=/absolute/path/to/Android/sdk
```

## Common Commands

Run these from the repository root:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew installDebug
```

Launch an installed debug build:

```bash
adb shell am start -n com.thekeval.guesser/.MainActivity
```

Run connected instrumentation tests:

```bash
./gradlew connectedDebugAndroidTest
```

The last command requires a running emulator or connected device.

## Changing Gameplay

For a rules change:

1. Update `GameRules` first.
2. Add or update focused tests in `GameRulesTest`.
3. Update `GameViewModel` only if the action or state transition changes.
4. Update `strings.xml`, the Rules screen, and `docs/gameplay.md`.
5. Verify both App and Friend mode use the same resulting behavior.

Avoid duplicating scoring or validation in the fragment. UI validation may
improve feedback, but `GameViewModel` and `GameRules` must remain the final
guard.

## Changing State

The state machine currently has four values: `NOT_STARTED`, `STARTED`, `WON`,
and `ABANDONED`. When adding a state or transition:

- define which commands are valid in that state;
- update every branch in `GameFragment.renderState`;
- cover accepted and rejected transitions in unit tests;
- document reset, reveal, and mode-switch behavior.

## Changing Feedback

Result effects are selected in `GameFragment` and preferences are stored by
`UserPreferences`. Keep guess-result and keypad channels independent. If a
new feedback type is persisted, provide a safe default and consider migration
from existing preference keys.

Always preserve a non-audio, non-haptic way to understand the result.

## Resource and UI Conventions

- Put player-visible copy in `res/values/strings.xml`.
- Use theme colors and dimensions rather than duplicating literals where
  reuse is meaningful.
- Maintain day and night theme behavior.
- Keep new keypad controls accessible by label and focus order.
- Test compact screens because the game combines a scroll view, guess list,
  and bottom keypad.

## Test Scope

Use JVM tests for pure rules and ViewModel behavior. Use instrumentation tests
only for Android-specific behavior such as navigation, dialogs, preferences,
view state, accessibility, and device feedback integration.

Before submitting a gameplay change, the minimum local check is:

```bash
./gradlew testDebugUnitTest assembleDebug
```

## Documentation Maintenance

The implementation is the final authority. When it changes, keep these
documents aligned:

- rules or round flow: `docs/gameplay.md`;
- ownership or dependencies: `docs/architecture.md`;
- code-level behavior: `docs/implementation.md`;
- completed or planned modernization: `docs/revamp.md`;
- setup or commands: this guide and the root `README.md`.
