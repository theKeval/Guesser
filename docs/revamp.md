# Revamp History and Roadmap

## Project Eras

### 2021 Prototype

The first implementation established the core hide-and-seek game, App and
Friend modes, navigation, and a RecyclerView guess log. Most behavior lived in
`GameFragment`; strings were frequently hardcoded, game state was duplicated
between fragment fields and the ViewModel, and rule helpers mixed validation,
generation, and mutable list management.

The build targeted Android 30 and Java 8 and included obsolete or unused
technology such as Kotlin Android synthetics, `kotlin-android-extensions`,
KAPT, Room, legacy lifecycle extensions, and duplicate Navigation
dependencies.

### April 2026 Compatibility Pass

Commit `c6fc98b` made the old project buildable with contemporary tooling. It
updated the Gradle wrapper and Android build configuration, removed obsolete
plugins and dependencies, and adjusted code for the modern Android toolchain.

### May 2026 Gameplay and UX Pass

Commit `5b7cecd` is the major current revamp baseline. It:

- extracted validation, generation, and scoring into `GameRules`;
- moved the secret, guesses, and lifecycle state into `GameViewModel`;
- replaced the mutable legacy guess adapter/model path with immutable
  `GuessModel`, `ListAdapter`, and `DiffUtil`;
- refreshed the Material 3 layouts, themes, copy, and navigation content;
- added a custom numeric keypad and responsive inset/scroll handling;
- added newest-first guess history;
- added sound, vibration, animation, and confetti feedback;
- added persistent feedback settings;
- added focused rule tests and expanded the README.

The revamp preserved the original rules and both game modes while replacing
most of the original implementation.

## Current Baseline

The code is functional and intentionally small. The domain rules are isolated
and unit-testable, but the presentation layer is only partially decomposed.
`GameFragment` remains the integration point for state rendering, dialogs,
keypad policy, audio, haptics, animation, insets, and scroll sizing.

Known characteristics are not necessarily defects:

- rounds and mode are not persisted across process death;
- there is no guess limit, score, timer, daily challenge, hint system, or
  online service;
- the app is portrait-only;
- Rules and About are static resource-backed screens;
- only feedback preferences survive app restarts.

## Risks and Maintenance Gaps

1. **Split state ownership.** Mode lives in the fragment while round state
   lives in the ViewModel, making restoration harder to reason about.
2. **Stringly typed results.** UI behavior detects a positive guess with
   `remark.contains("R")`; structured counts and a result type would be safer
   than parsing display text.
3. **Large UI controller.** `GameFragment` has platform services, layout math,
   animations, and game interactions in one class.
4. **Limited tests.** Rules have basic unit coverage, but state transitions,
   generated-code invariants, preferences, and UI flows do not.
5. **Accessibility and localization.** The custom keypad, dynamic effects, and
   symbolic results need focused TalkBack, reduced-motion, contrast, and
   translation review.
6. **No automated delivery pipeline.** The repository currently contains no
   CI workflow, release automation, or documented signing process.
7. **Legacy compatibility surface.** The aggregate sound preference methods
   remain only as fallback infrastructure, and coroutine dependencies are
   present even though current production code does not launch coroutines.

## Recommended Next Phases

These are recommendations, not committed product scope.

### Phase 1: Lock Down Current Behavior

- Expand `GameRules` tests into a complete result table, including leading
  zero and deterministic generation.
- Add ViewModel tests for every state transition and rejected action.
- Add instrumented flows for both modes, reveal, reset, and settings.
- Add CI that builds and runs JVM tests on pull requests.

### Phase 2: Consolidate State

- Introduce one immutable `GameUiState` containing mode, round state, secret
  visibility, and guesses.
- Replace nullable/string command results with explicit sealed outcomes.
- Decide whether rounds should survive configuration only, process death, or
  full app restarts; then implement that policy deliberately.

### Phase 3: Decompose the Game Screen

- Move audio/haptic behavior behind a feedback controller.
- Isolate custom-keypad input policy and animations.
- Move viewport/inset calculations into a small UI helper or a simpler layout.
- Keep `GameFragment` focused on binding state and forwarding user events.

### Phase 4: Product and Accessibility Work

- Validate TalkBack labels, focus order, touch targets, and symbolic feedback.
- Respect reduced-motion and device haptic capabilities.
- Review whether attempt counts, statistics, difficulty, or daily play fit the
  product before adding storage or backend complexity.
- Establish versioning, release notes, signing, and store metadata.

## Revamp Guardrails

- Preserve three distinct digits and leading-zero behavior unless the game
  rules are intentionally changed.
- Keep scoring pure and covered by JVM tests.
- Do not add persistence implicitly; document exactly when a round ends.
- Keep display copy in resources and avoid making game decisions by parsing
  localized strings.
- Treat App and Friend modes as two secret-selection paths into one shared
  guessing loop.
