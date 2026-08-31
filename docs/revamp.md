# Revamp History and Roadmap

## Project Eras

### 2021: Original Prototype

The original application established the hidden-number game, App and Friend
modes, `R`/`W`/`SMYLE` scoring, XML navigation, and a RecyclerView guess log.
Most interaction and state logic lived in `GameFragment`, and the build used
Android 30-era plugins and dependencies.

### April 2026: Compatibility Pass

Commit `c6fc98b` updated the original project for modern Gradle and Android
tooling and removed obsolete build configuration.

### May 2026: V1 Gameplay and UX Modernization

Commit `5b7cecd` substantially improved the original application:

- extracted pure `GameRules`;
- centralized round state in `GameViewModel`;
- modernized the guess model and adapter;
- refreshed Material layouts, themes, copy, and navigation;
- added a custom keypad, sound, vibration, animation, and confetti;
- added feedback settings and focused rule tests.

This version remains in `app/` and is now registered as `:guesser_v1`.

### August 2026: Documentation Baseline

PR #2 added the first comprehensive gameplay, architecture, implementation,
development, and roadmap documentation. Those documents originally described
the V1 application as the repository's only app.

### August 2026: Separate Modular Compose Revamp

PR #3, merged as `9288a48`, changed the project boundary rather than
incrementally rewriting V1. It:

- preserved `app/` as the independent `:guesser_v1` application;
- introduced the new `:guesser` Compose application with application ID
  `io.keval.apps.guesser`;
- added `:gameplay`, `:domain`, `:data`, and `:core-ui`;
- implemented the artwork-driven Home screen and player-mode selection;
- added masked, validated Double Player secret setup;
- handed friend secrets to Game through an in-memory repository and use cases;
- added animated navigation and shared secondary-screen scaffolding;
- preserved original design artwork separately from runtime drawables;
- added Home ViewModel and domain use-case tests;
- improved secret privacy, selector accessibility, validation consistency, and
  wood-background contrast during review.

## Current Baseline

The primary revamp is a scalable application foundation, not yet a complete
game. Its Home screen and setup handoff are implemented. Game, Tutorial,
Gameplay, and About are placeholders.

The repository therefore has two valid but different baselines:

| Concern | Revamp `:guesser` | V1 `:guesser_v1` |
| --- | --- | --- |
| UI toolkit | Jetpack Compose | XML Views |
| Architecture | Multi-module, contracts/use cases, manual container | Single app module, MVVM-style |
| Home/setup | Implemented | Implemented within game screen |
| Complete guessing loop | Not implemented | Implemented |
| Rules/scoring engine | Not implemented | Implemented |
| Feedback/settings | Not implemented | Implemented |
| Session persistence | In memory only | Round in memory; feedback preferences persisted |
| Package | `io.keval.apps.guesser` | `com.thekeval.guesser` |

## Current Risks and Gaps

1. **No revamp game domain yet.** The new `:domain` module has session and app
   info contracts but no secret generator, scoring model, round state, or
   guess history.
2. **Session-only handoff.** A friend secret is lost on process death and has no
   explicit consume lifecycle.
3. **Scaffold abstractions are ahead of use.** The welcome use case result is
   discarded, and DataStore is declared but unused.
4. **Temporary copy.** Secondary screens use hardcoded placeholder text rather
   than finalized resource-backed content.
5. **Limited test depth.** ViewModel validation and one use case are covered,
   but Compose behavior, navigation, repository lifetime, and future gameplay
   have no tests.
6. **Parallel app maintenance.** Build and documentation changes must avoid
   accidentally coupling or confusing V1 with the revamp.
7. **No automated delivery pipeline.** CI, signing, release automation, and
   store publishing remain undocumented/unimplemented.

## Recommended Next Phases

These phases describe a technically coherent path, not committed product
scope.

### Phase 1: Establish the Revamp Game Domain

- Port the established rules into pure `:domain` models and use cases without
  importing V1.
- Model player mode, round status, secret, guess, and structured `R`/`W`
  results explicitly.
- Test leading zeroes, unique-digit validation, generation, the full scoring
  table, and invalid transitions.
- Define when the friend secret is consumed or cleared.

### Phase 2: Implement the Compose Game Loop

- Replace `GameScreen` with a stateful route and stateless screen.
- Support Single Player generation and Double Player setup from the session.
- Add guess input, history, reveal, reset, win, and back-navigation semantics.
- Keep display strings separate from structured scoring decisions.
- Add ViewModel and Compose UI tests for both modes.

### Phase 3: Finish Product Content and Settings

- Replace Tutorial, Gameplay, and About placeholders with resource-backed
  final content.
- Decide which V1 sound, vibration, motion, and accessibility behaviors belong
  in the revamp.
- Introduce settings storage only after its required lifetime and migration
  behavior are defined.

### Phase 4: Expand Product Capabilities Deliberately

- Decide whether rounds should survive configuration, process death, or full
  restarts.
- Evaluate profiles, scoreboards, statistics, and Google Play Games against
  concrete product requirements.
- Add CI, versioning, release notes, signing, and store metadata.

## Revamp Guardrails

- Keep `:guesser_v1` independent; do not use the application module as a
  library.
- Preserve established rules unless a product decision intentionally changes
  them.
- Represent secrets as strings so leading zeroes remain valid.
- Keep domain decisions free of Compose and Android dependencies.
- Pass sensitive setup through typed state/repositories, not navigation route
  text or logs.
- Distinguish implemented behavior from intended behavior in code, UI copy,
  tests, and documentation.
