# Implementation Guide

## Rules Engine

`GameRules` is the smallest and most reusable part of the app.

### Validation

`isValidUniqueThreeDigitNumber` checks length, digit-only content, and set
cardinality. It deliberately operates on a `String`, which preserves leading
zeros.

### Secret Generation

`generateSecretNumber` shuffles the ten decimal digits, takes the first three,
and joins them. Sampling without replacement guarantees uniqueness without a
retry loop. A `Random` argument can be supplied for deterministic tests.

### Remark Calculation

`buildRemark` calculates:

1. `right`: equal characters at equal indexes.
2. `overlap`: guessed characters contained anywhere in the secret.
3. `wrong`: `overlap - right`.

This is correct under the enforced unique-digit invariant. If repeated digits
are ever allowed, the overlap algorithm must change before validation changes,
or repeated values could be overcounted.

`buildRemark` assumes both inputs already satisfy the three-character contract;
it does not validate lengths itself. Generated secrets and Friend-mode secrets
are validated before play, and `submitGuess` validates every guess.

## Round State

`GameViewModel` exposes two read-only `LiveData` streams:

- `guesses: LiveData<List<GuessModel>>`
- `gameState: LiveData<GameState>`

The fragment sends commands rather than mutating those streams.

### Starting

- `startAutoGame()` generates a secret, clears guesses, and enters `STARTED`.
- `startFriendGame(secret)` validates first. On success it stores the secret,
  clears guesses, enters `STARTED`, and returns `true`.

### Guessing

`submitGuess` rejects calls unless the state is `STARTED` and the guess is
valid. A valid guess is scored, appended with a monotonically increasing ID,
and returned to the UI. A `Winner` remark also transitions the state to
`WON`.

### Ending and Resetting

- `revealAndAbandon()` transitions only `STARTED` to `ABANDONED`.
- `resetGame()` clears guesses and enters `NOT_STARTED`.
- The old secret remains in memory after reset but is hidden and replaced by
  the next start action.

## Game Screen

`GameFragment` performs setup in this order:

1. Inflate binding and obtain the fragment-scoped ViewModel.
2. Disable the system keyboard for the seeker field.
3. load four short effects into `SoundPool`.
4. Capture baseline padding and margins.
5. Attach the guess adapter.
6. Configure inset and scroll behavior.
7. Attach control and keypad listeners.
8. Observe ViewModel streams.
9. Render the selected mode and current state.

### Rendering

`renderModeOnly` toggles Auto/Hide controls, secret-field editability, and
mode-label colors. `renderState` is the central state-to-view mapping for
status copy, control enablement, secret masking, guess input, and keypad
visibility.

The secret is not removed from the text field when hidden. A full-size overlay
view covers it. Reveal and win remove that overlay.

### Guess History

The ViewModel stores guesses in submission order. The fragment reverses the
list before passing it to `GuessesAdapter`, so the newest result appears at
position zero. The adapter uses stable model IDs for identity and full data
class equality for content changes.

### Custom Keypad

Digit listeners reject input after three characters and reject a digit already
present in the current entry. Rejected keys use the wrong-result feedback.
Delete, clear, and keypad-hide actions operate directly on the seeker field.

The system input method is explicitly hidden whenever the custom keypad opens.
Inset listeners and computed list heights keep the active controls visible on
different screen sizes.

## Device Feedback

Four bundled effects are loaded:

| Resource | Trigger |
| --- | --- |
| `sfx_key.wav` | Digit, delete, clear, or keypad-hide tap |
| `sfx_right.wav` | Non-winning remark containing at least one `R` |
| `sfx_wrong.wav` | `W`-only/`SMYLE` result or rejected keypad digit |
| `sfx_win.wav` | Winning guess |

Result sounds and keypad sounds have separate preferences. Vibration is also
independent. The implementation handles the vibrator API split at Android 12
and the one-shot effect split at Android 8.

Visual feedback includes key scaling, input scaling for an `R`, a horizontal
shake for other results, status/card animation on win, and a generated
36-piece confetti burst.

## Preferences

`UserPreferences` uses a private `SharedPreferences` file. New installs default
all feedback to enabled. If category-specific sound keys do not exist, reads
fall back to the legacy aggregate `sound_enabled` key, preserving an older
preference value during migration.

## Non-Game Screens

- Rules and About are static fragments whose text comes from string resources.
- Settings uses view binding and writes preference changes immediately.
- MainActivity wires the drawer and Navigation Component.

## Testing

Current JVM tests cover:

- valid unique code acceptance;
- duplicate and non-digit rejection;
- winning, mixed `R`/`W`, `W`-only, and `SMYLE` remarks.

The instrumented test is still the generated application-context smoke test.
ViewModel transitions, random-generation invariants, UI flows, preferences,
restoration, and accessibility are not yet covered.
