# Implementation Guide

## Revamp Startup

The new application starts through four small composition layers:

1. Android creates `GuesserApplication`.
2. Its lazy `AppContainer` creates repository implementations and use cases.
3. `MainActivity` enables edge-to-edge rendering and applies `GuesserTheme`.
4. `GuesserApp` creates a `NavHostController` and calls `GuesserNavHost`.

The application layer depends on all four support modules and is the only
place that assembles concrete `:data` repositories with `:domain` use cases.

## Manual Dependency Container

`RepositoryModule` constructs:

- `DefaultAppInfoRepository`, which returns `"Welcome"`;
- `InMemoryGameSessionRepository`, which holds one nullable friend secret.

`AppContainer` exposes:

- `GetWelcomeMessageUseCase`;
- `SaveFriendSecretUseCase`;
- `GetFriendSecretUseCase`.

These abstractions establish replaceable boundaries but are intentionally
minimal. The welcome result is currently discarded, and the session
repository has no clear/consume operation beyond saving `null`.

## Home State and Validation

`HomeUiState` contains:

```text
playerMode: SINGLE | DOUBLE
friendSecretNumber: String
secretValidationMessage: String?
```

`HomeViewModel` owns a `MutableStateFlow` internally and exposes an immutable
`StateFlow`.

### Mode Changes

Selecting Single Player clears the friend secret and any validation message.
Selecting Double Player preserves a previously entered secret and clears the
message.

### Secret Input

`onFriendSecretNumberChanged` filters input to ASCII `0`-`9` (`isCodeDigit`) and keeps
at most three characters. Empty input shows no live error. Non-empty values
are validated with duplicate detection before length detection, so `11`
reports `Digits must be unique`.

### Starting

`canStartGame` always accepts Single Player and clears validation. Double
Player calls the same validator used during typing and accepts only three
unique digits.

`HomeRoute` invokes the outer Start callback only after `canStartGame` returns
true.

## Home Compose UI

`HomeScreen` is stateless: it renders `HomeUiState` and emits callbacks.
`HomeRoute` is the stateful boundary that creates the ViewModel and collects
state with `collectAsStateWithLifecycle`.

Notable implementation details:

- `BoxWithConstraints` drives width fractions, tablet caps, and proportional
  down-scaling for short-height screens;
- `verticalScroll`, status/navigation bar padding, and `imePadding` support
  compact layouts;
- player mode, Start, and secondary buttons are drawn in Compose with gradients
  and rounded-pill borders (only background/title remain image assets);
- the mode selector uses an animated selected segment and two `selectable`
  radio-button regions;
- the friend secret uses `BasicTextField`, numeric-password keyboard options,
  and `PasswordVisualTransformation`;
- buttons collect press state and scale/darken to pressed feedback without a
  ripple;
- accessibility descriptions for home controls come from string resources.

## Session Handoff

The home destination does not pass a secret in a navigation route:

```mermaid
sequenceDiagram
    participant Home as HomeRoute
    participant Nav as GuesserNavHost
    participant Save as SaveFriendSecretUseCase
    participant Repo as InMemoryGameSessionRepository
    participant Game as GameRoute

    Home->>Nav: Valid HomeUiState
    Nav->>Save: friend secret or null
    Save->>Repo: saveFriendSecret(value)
    Nav->>Game: navigate("game")
    Game->>Repo: via GetFriendSecretUseCase
    Repo-->>Game: String? secret
```

Single Player stores `null`; Double Player stores the validated secret.
`GameRoute` passes the retrieved value into `GameViewModel`, which uses it as
the round secret (or generates one when absent) and never displays it until
the round is won. The repository is process-memory only.

## Game Screen

- `GuessRules` (`:domain`, package `domain.game`) validates codes, generates
  unique secrets, and evaluates a guess into per-digit `DigitFeedback`
  (`EXACT`, `CLOSE`, `ABSENT`) plus the V1 remark string.
- `GameViewModel` owns the secret, submitted guesses, current input, and dialog
  flags. These are stored in `SavedStateHandle`, so they survive rotation,
  process death, and navigating to Help and back. It exposes `GameUiState` as
  a `StateFlow`.
- `GameRoute` collects state, handles system back with `BackHandler`, and
  delegates to a stateless `GameScreen`.
- `GameScreen` sizes everything from `BoxWithConstraints`. Tile size is the
  smaller of a width fit and a height fit that keeps at least five rows
  visible. Keypad keys scale with width and height, and content is capped at
  520 dp on tablets.
- `GameComponents` provides the `WoodPanel` (gold frame over a cropped wood
  texture), `DigitTile` with a staggered flip reveal, `RemarkBox`, `KeypadKey`,
  and `GuesserDialogFrame`. These reuse the Home palette.
- `game_board_wood` and `game_keypad_wood` are textures cropped from the home
  background and drawn with `ContentScale.Crop`, so they never stretch.

## Navigation and Placeholder Screens

`GuesserNavHost` declares Home, Game, Tutorial, Gameplay, and About routes
with horizontal slide transitions. Tutorial, Gameplay, and About use
`GuesserScreenScaffold` and `GuesserBackButton`.

Game exits by popping back to Home. Its help button navigates to Gameplay
(`launchSingleTop`) and leaves Game on the back stack. Tutorial, Gameplay, and
About delegate to a common `PlaceholderScreen` with temporary hardcoded text.

## Theme and Assets

`:core-ui` defines `GuesserTheme`, colors, and default Material typography.
Android 12+ dynamic color is enabled unless a caller opts out. The wood-screen
foreground color is explicit white so placeholder content remains legible
regardless of the dynamic scheme.

Source artwork lives in `design_assets/raw_assets`. Runtime-sized copies live
in `gameplay/src/main/res/drawable-nodpi` for the background, title, and the
two Game wood textures. Buttons, tiles, and keys are Compose-drawn, so they
stay crisp across screen sizes and densities.

## Revamp Tests

- `HomeViewModelTest` covers duplicate-message consistency, empty-on-type
  versus required-on-start behavior, and valid Double Player setup.
- `GetWelcomeMessageUseCaseTest` verifies repository delegation.
- `GuessRulesTest` covers validation, generation, and the full remark table.
- `GameViewModelTest` covers input guards, scoring, winning, exit
  confirmation, play-again, and `SavedStateHandle` restoration.

There are no current Compose UI, navigation, repository, or instrumentation
tests.

## Guesser V1 Implementation

The complete game remains under `app/` as `:guesser_v1`:

- `GameRules` validates codes, generates unique secrets, and produces remarks.
- `GameViewModel` owns the secret, guess history, and four-state round machine.
- `GameFragment` handles rendering, dialogs, custom keypad input, layout,
  sound, vibration, animations, and confetti.
- `GuessesAdapter` renders newest-first immutable guess rows.
- `UserPreferences` persists result sound, keypad sound, and vibration options.

V1's scoring computes right-position matches, total overlap, and then
wrong-position matches as `overlap - right`. That implementation assumes both
inputs already satisfy the unique-three-digit contract.

V1 is not a shared library and is not imported by the revamp. Reusing its
rules requires intentionally porting the behavior into the new domain model,
not depending on `:guesser_v1`.
