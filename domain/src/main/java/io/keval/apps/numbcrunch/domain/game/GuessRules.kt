package io.keval.apps.numbcrunch.domain.game

import kotlin.random.Random

const val CODE_LENGTH = 3

enum class DigitFeedback {
    /** Digit is in the secret and in the correct position (`R`). */
    EXACT,

    /** Digit is in the secret but in another position (`W`). */
    CLOSE,

    /** Digit is not in the secret. */
    ABSENT,
}

data class GuessEvaluation(
    val guess: String,
    val digits: List<DigitFeedback>,
    val remark: String,
) {
    val isWin: Boolean get() = digits.all { it == DigitFeedback.EXACT }
}

/**
 * Pure NumbCrunch rules, matching the established V1 scoring and remark format.
 */
object GuessRules {

    const val WINNER_REMARK = "Winner"
    const val SMYLE_REMARK = "SMYLE"

    fun isValidCode(value: String): Boolean {
        return value.length == CODE_LENGTH &&
            value.all(::isCodeDigit) &&
            value.toSet().size == CODE_LENGTH
    }

    fun generateSecret(random: Random = Random.Default): String {
        return (0..9)
            .shuffled(random)
            .take(CODE_LENGTH)
            .joinToString(separator = "")
    }

    fun evaluate(secret: String, guess: String): GuessEvaluation {
        require(isValidCode(secret)) { "Secret must be three unique digits" }
        require(isValidCode(guess)) { "Guess must be three unique digits" }

        val digits = guess.mapIndexed { index, digit ->
            when {
                secret[index] == digit -> DigitFeedback.EXACT
                secret.contains(digit) -> DigitFeedback.CLOSE
                else -> DigitFeedback.ABSENT
            }
        }
        val right = digits.count { it == DigitFeedback.EXACT }
        val wrong = digits.count { it == DigitFeedback.CLOSE }

        val remark = when {
            right == CODE_LENGTH -> WINNER_REMARK
            right + wrong == 0 -> SMYLE_REMARK
            wrong == 0 -> "${right}R"
            right == 0 -> "${wrong}W"
            else -> "${right}R, ${wrong}W"
        }
        return GuessEvaluation(guess = guess, digits = digits, remark = remark)
    }
}

/** Only ASCII `0`-`9` are valid code digits, matching the in-game keypad. */
fun isCodeDigit(char: Char): Boolean = char in '0'..'9'
