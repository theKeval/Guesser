# Gameplay and Implementation Status

## Product Rule Set

Guesser is a deduction game in which a seeker identifies a hidden
three-character digit code. Each guess returns positional information that
narrows the remaining possibilities.

These rules are fully implemented in Guesser V1. They are the established
product contract for the revamp, but the revamp Game screen does not implement
them yet.

### Valid Codes

A secret or guess is valid when it:

1. contains exactly three characters;
2. contains only characters recognized as digits by Kotlin's `Char.isDigit`;
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

The new `:guesser` application currently implements round setup, not the round
itself.

### Single Player

1. Single Player is selected by default.
2. Tapping **Start** clears any previously held friend secret.
3. Navigation opens the Game destination.
4. The destination displays a Single Player placeholder.

No random secret is generated and no guesses can be submitted yet.

### Double Player

1. Selecting Double Player reveals a masked friend-secret field.
2. Input is filtered to digit characters and truncated to three characters.
3. Duplicate digits show `Digits must be unique`.
4. A non-empty short value shows `Enter exactly 3 digits`.
5. An empty field remains quiet while typing but shows the length message when
   Start is pressed.
6. A three-unique-digit value is saved to an in-memory repository.
7. Navigation opens the Game destination, which acknowledges Double Player
   setup without displaying the secret.

The secret is not placed in a route argument, log, or visible placeholder
copy. It remains available through `GetFriendSecretUseCase` only while the
application process and `AppContainer` live.

### Other Destinations

Tutorial, Gameplay, and About are navigable placeholder screens. They share
the wood background and back control, but they do not contain final product
content.

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

The revamp has not carried these feedback systems forward yet.

## Scope Not Yet Implemented in the Revamp

The Compose app currently has no secret generation, scoring engine, guesses,
round state machine, win/reveal/reset flow, attempt history, sound, haptics,
settings, persistence, profiles, scoreboard, Play Games integration, timer,
hints, or daily puzzle.
