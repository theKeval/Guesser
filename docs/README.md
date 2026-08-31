# Guesser Documentation

These documents cover both applications in the repository:

- **Guesser revamp** is the primary `:guesser` Compose application.
- **Guesser V1** is the complete legacy application in `app/`, registered with
  Gradle as `:guesser_v1`.

The revamp currently implements its home, navigation, dependency boundaries,
and Double Player setup handoff. Its actual game and informational
destinations remain placeholders. V1 remains the only complete playable
implementation.

| Document | Purpose |
| --- | --- |
| [Gameplay and implementation status](gameplay.md) | Established rules, current revamp behavior, and V1 round flow |
| [Architecture](architecture.md) | Module graph, runtime wiring, state ownership, navigation, and V1 isolation |
| [Implementation guide](implementation.md) | Code walkthrough for the Compose foundation and legacy game |
| [Revamp history and roadmap](revamp.md) | Project eras, PR #3 scope, current gaps, and recommended next phases |
| [Development guide](development.md) | Setup, module-specific commands, testing, and change conventions |

## Sources of Truth

### Revamp

| Concern | Authoritative source |
| --- | --- |
| Included modules and V1 alias | `settings.gradle` |
| App assembly and dependency wiring | `guesser/src/main/java/io/keval/apps/guesser/` |
| Navigation graph | `guesser/.../GuesserNavHost.kt` |
| Home state and validation | `gameplay/.../home/HomeUiState.kt` and `HomeViewModel.kt` |
| Home Compose UI | `gameplay/.../home/HomeScreen.kt` |
| Session handoff contracts | `domain/.../repository/GameSessionRepository.kt` |
| Session implementation | `data/.../repository/InMemoryGameSessionRepository.kt` |
| Shared theme | `core-ui/.../theme/` |
| Original artwork | `design_assets/raw_assets/` |
| Runtime artwork | `gameplay/src/main/res/drawable-nodpi/` |

### Guesser V1

| Concern | Authoritative source |
| --- | --- |
| Validation, secret generation, and remarks | `app/.../domain/GameRules.kt` |
| Secret, guesses, and round state | `app/.../viewmodel/GameViewModel.kt` |
| Interaction and device feedback | `app/.../ui/GameFragment.kt` |
| Player-facing rules and copy | `app/src/main/res/values/strings.xml` |
| Feedback preferences | `app/.../data/UserPreferences.kt` |

When changing behavior, update the source, tests, player-facing copy, and the
corresponding document together. Do not describe planned revamp behavior as
implemented until it exists in `:guesser` or its supporting modules.
