# Guesser Documentation

This directory describes the behavior and structure of the current Android
application. Where the historical prototype and the current implementation
differ, the current source code is authoritative.

| Document | Purpose |
| --- | --- |
| [Gameplay and rules](gameplay.md) | Player-facing rules, modes, feedback, round flow, and edge cases |
| [Architecture](architecture.md) | Components, ownership boundaries, state, dependencies, and data flow |
| [Implementation guide](implementation.md) | Code-level walkthrough of starting, guessing, rendering, and feedback |
| [Revamp history and roadmap](revamp.md) | What changed from the 2021 prototype, current gaps, and recommended next phases |
| [Development guide](development.md) | Local setup, build commands, tests, and safe change patterns |

## Source of Truth

The same concepts appear in several places:

| Concern | Authoritative source |
| --- | --- |
| Validation, secret generation, and remarks | `domain/GameRules.kt` |
| Secret, guesses, and round state | `viewmodel/GameViewModel.kt` |
| Interaction and device feedback | `ui/GameFragment.kt` |
| Player-facing rules text | `res/values/strings.xml` |
| Screen structure | `res/layout/*.xml` and `res/navigation/navigation.xml` |
| Feedback preferences | `data/UserPreferences.kt` |

When behavior changes, update the authoritative source, its tests, the
player-facing strings, and the relevant document together.
