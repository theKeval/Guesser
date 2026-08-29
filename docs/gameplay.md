# Gameplay and Rules

## Objective

The seeker tries to identify a hidden code by submitting guesses and using the
feedback from each attempt. A round ends when the seeker finds the code or
chooses to reveal it.

The UI calls the value a "3-digit number," but the implementation treats it as
a string of three digits. Consequently, `012` is valid and must remain intact
as three characters.

## Roles and Terms

- **Secret**: the hidden three-digit code.
- **Picker**: the app or friend that chooses the secret.
- **Seeker**: the player entering guesses.
- **Guess**: one proposed three-digit code.
- **Remark**: the result shown beside a guess.

## Valid Codes

Both secrets and guesses must satisfy all of these conditions:

1. Exactly three characters.
2. Every character is recognized as a digit by Kotlin's `Char.isDigit`.
3. All three digits are different.

Valid examples include `358`, `907`, and `012`. Invalid examples include `33`,
`331`, and `3a1`.

## Feedback

Each digit is evaluated against the secret:

- Count one `R` when the digit and its position both match.
- Count one `W` when the digit occurs in the secret at another position.
- Because repeated digits are forbidden, each overlap is counted once.

The displayed remark is selected in this order:

| Condition | Remark |
| --- | --- |
| Three positions match | `Winner` |
| No digits overlap | `SMYLE` |
| Only right-position matches exist | `<count>R` |
| Only wrong-position matches exist | `<count>W` |
| Both kinds exist | `<right>R, <wrong>W` |

`SMYLE` expands to "Simply Make Your Life Easy." The in-app rules use the
phrase "3R means Winner," while the implementation displays the word
`Winner` rather than the literal string `3R`.

### Examples

| Secret | Guess | Result | Explanation |
| --- | --- | --- | --- |
| `245` | `325` | `1R, 1W` | `5` is placed correctly; `2` is present elsewhere |
| `245` | `542` | `1R, 2W` | `4` is placed correctly; `5` and `2` are misplaced |
| `358` | `147` | `SMYLE` | No digit appears in the secret |
| `245` | `245` | `Winner` | Every digit and position matches |

## Game Modes

### App Mode

1. The player taps **Auto**.
2. The app shuffles digits `0` through `9`, takes three, stores that secret,
   and covers the secret field.
3. The seeker enters guesses with the custom keypad and taps **Check**.
4. Tapping **Show** asks for confirmation. Confirming reveals the secret and
   abandons the round.

### Friend Mode

1. The player switches to **Friend** mode.
2. The picker enters a valid secret and taps **Hide**.
3. The secret field is covered, and the device can be passed to the seeker.
4. The seeker plays the same guessing flow as App mode.
5. Tapping **Show** confirms and then abandons the round.

Switching modes during an active round requires confirmation and resets the
round. Switching before a round begins resets immediately.

## Round States

| State | Meaning | Available actions |
| --- | --- | --- |
| `NOT_STARTED` | No active secret | Generate or enter a secret; switch mode |
| `STARTED` | Secret is hidden and guesses are accepted | Check guesses, reveal, reset, or request a mode change |
| `WON` | The latest guess matched the secret | Review the revealed secret and guess history; reset |
| `ABANDONED` | The player revealed the secret | Review the revealed secret and guess history; reset |

The game has unlimited guesses. It does not currently track attempts as a
score, impose a timer, provide hints, or choose a shared daily secret.

## Input and Feedback

- The seeker uses an on-screen numeric keypad.
- The keypad refuses a fourth digit and refuses a duplicate digit.
- **Delete** removes the last digit; **Clear** removes all entered digits.
- Invalid submissions show a dialog and clear the entry.
- Guess history is displayed newest first.
- A winning guess triggers a win sound, vibration, status animation, and
  confetti.
- A guess containing at least one `R` uses the positive-result effect.
- Other non-winning remarks use the wrong-result effect.
- Settings independently control guess-result sounds, keypad sounds, and
  vibration. All three default to enabled.

## Reset and Reveal Semantics

Reset clears the guess list and returns to `NOT_STARTED`. It does not switch
the selected mode. Starting the next round replaces the previous secret.

Reveal changes an active round to `ABANDONED`; it does not silently start a
new round. The player must tap **Reset** before playing again.
