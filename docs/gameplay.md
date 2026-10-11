# Gameplay and Implementation Status

## Product Rule Set

Guesser is a deduction game in which a seeker identifies a hidden
three-character digit code. Each guess returns positional information that
narrows the remaining possibilities.

These rules are fully implemented in Guesser V1 and are ported to the revamp
as `GuessRules` in `:domain`, which drives the revamp Game screen.

### Valid Codes

A secret or guess is valid when it:

1. contains exactly three characters;
2. contains only ASCII digits `0`–`9` (the revamp rejects other Unicode
   digits so every valid secret can be typed on the game keypad);
3. contains three different digits.

Codes are strings, not integers, so a leading zero is preserved. Examples:
`358`, `907`, and `012` are valid; `33`, `331`, and `3a1` are invalid.

### Remarks

- Count one `R` when a digit and its position match the secret.
- Count one `W` when a digit is present in the secret at a different position.
- Display `SMYLE` when no guessed digit is present.
- Display `Winner` when all three positions match.

| Secret | Guess | Result | Explanation |
| --- | --- | --- | --- |
| `245` | `325` | `1R, 1W` | `5` is placed correctly; `2` is present elsewhere |
| `245` | `542` | `1R, 2W` | `4` is placed correctly; `5` and `2` are misplaced |
| `358` | `147` | `SMYLE` | No digit appears in the secret |
| `245` | `245` | `Winner` | Every digit and position matches |

`SMYLE` expands to "Simply Make Your Life Easy." V1's in-app rules say "3R
means Winner," while its scoring engine displays the word `Winner`.

## Revamp Behavior Today

The new `:guesser` application implements round setup and a complete
guessing round.

### Single Player

1. Single Player is selected by default.
2. Tapping **Start** clears any previously held friend secret.
3. Navigation opens the Game destination, which generates a random secret of
   three unique digits.

### Double Player

1. Selecting Double Player reveals a masked friend-secret field.
2. Input is filtered to digit characters and truncated to three characters.
3. Duplicate digits show `Digits must be unique`.
4. A non-empty short value shows `Enter exactly 3 digits`.
5. An empty field remains quiet while typing but shows the length message when
   Start is pressed.
6. A three-unique-digit value is saved to an in-memory repository.
7. Navigation opens the Game destination, which uses the friend's secret
   without displaying it.

The secret is not placed in a route argument, log, or visible copy. It
remains available through `GetFriendSecretUseCase` only while the application
process and `AppContainer` live.

### Game Screen

- A header shows a back button, the Guesser title, and a help (`?`) button,
  followed by a Single/Double Player mode badge.
- The guess board lists numbered rows of three digit tiles plus a remark box.
  Each submitted row has a small gold **i** button beside its remark. Tapping
  it opens an iOS-style popup (zoom/fade in, fade/shrink out) with a one-line
  meaning of that remark; tap **GOT IT**, outside the card, or Back to close.
  Guesses are unlimited; the board scrolls and keeps the active row visible.
- The custom keypad has digits 0–9, with **DELETE** bottom-left and **ENTER** bottom-right. Digits already
  in the current guess are disabled, so duplicates are impossible. ENTER is
  enabled only after three digits.
- Empty and typed tiles use the original yellowish wood color. On ENTER, the
  tiles flip in sequence and every analysed tile turns gray: per-digit
  green/orange feedback is disabled to increase difficulty. Deduction uses the V1 remark
  (`1R, 1W`, `SMYLE`, `Winner`), not individual tile colors.
  The original green/orange/gray feedback logic is retained for reuse.
- A `SMYLE` remark box is teal and plays a single heartbeat pulse when it is
  first revealed.
- A winning guess disables the keypad and opens a win dialog showing the
  secret and guess count. Single Player offers **PLAY AGAIN** and **HOME**;
  Double Player offers **HOME**.
- Back (header or system) during an unfinished round asks for confirmation
  before returning Home and discarding the round.
- Help opens the Gameplay destination. Returning resumes the same round,
  because the Game back-stack entry and its `SavedStateHandle` state are kept.

### Other Destinations

- **Tutorial:** on first launch, four swipeable steps introduce the shown
  practice secret `245`, explain a scored clue row, offer an interactive
  practice keypad, and introduce both modes. Progress dots and Back/Next buttons
  provide alternate navigation. Skip, Get Started, or system Back from the
  first step completes onboarding and opens Home; completion is saved across
  restarts. Tutorial remains available from Home. Practice accepts any valid
  guess and displays the latest evaluation; it is independent of real rounds.
  Manual Tutorial Back returns through the steps and then to Home. **PLAY NOW**
  clears any friend secret and replaces Tutorial with a new Single Player game.
- **Gameplay:** a scrollable reference explains code constraints (including
  leading zero), unlimited guesses, tile colors, R/W/SMYLE/Winner, both modes,
  controls, and strategy. Four examples use `GuessRules` against secret `245`.
  Tutorial and Gameplay examples retain per-digit colors for teaching; the
  guide distinguishes these examples from the gray tiles used in real rounds.
  Returning from in-game help preserves the existing Game entry.
- **About:** the family origin story credits Paresh Langalia, the inventor of
  Guesser, alongside developer Keval Langalia, the Compose revamp, installed
  app version, and external links to the public repository and Apache 2.0
  license.

All three use resource-backed copy, safe-area padding, dark wood panels, and
a 520 dp maximum reading width. Example rows expose spoken per-digit feedback
so clue meaning does not depend on color.

## Guesser V1 Behavior

Guesser V1 remains playable and has two equivalent setup modes:

- **App mode** generates and hides a random unique three-digit code.
- **Friend mode** lets one player enter and hide a valid secret before passing
  the device to the seeker.

### V1 Round Flow

1. Start App mode with **Auto**, or enter a Friend-mode secret and tap
   **Hide**.
2. Enter a valid guess with the custom keypad.
3. Tap **Check** to append the guess and its remark to newest-first history.
4. Continue without a guess limit until the result is `Winner`.
5. Optionally tap **Show**; confirmation reveals the secret and abandons the
   round.
6. Tap **Reset** to clear history and return the selected mode to its initial
   state.

Switching modes during an active round requires confirmation and resets that
round.

### V1 States

| State | Meaning |
| --- | --- |
| `NOT_STARTED` | No active secret |
| `STARTED` | Secret is hidden and guesses are accepted |
| `WON` | The latest guess matched the secret |
| `ABANDONED` | The player revealed the secret |

### V1 Feedback

- The keypad refuses a fourth digit and a repeated digit.
- Invalid submissions display a dialog and clear the guess field.
- Winning triggers sound, vibration, status/card animation, and confetti.
- A non-winning result containing `R` uses the positive-result effect.
- Other results use the wrong-result effect.
- Settings independently control result sounds, keypad sounds, and vibration.

The revamp carries forward the keypad duplicate/length guards (as disabled
keys) and a win state, but not sound, vibration settings, reveal, or confetti.

## Scope Not Yet Implemented in the Revamp

The Compose app currently has no secret reveal/abandon flow, sound, settings,
cross-launch persistence, profiles, scoreboard, Play Games integration, timer,
hints, or daily puzzle.
