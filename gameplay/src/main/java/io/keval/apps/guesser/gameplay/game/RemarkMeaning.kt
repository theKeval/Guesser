package io.keval.apps.guesser.gameplay.game

import io.keval.apps.guesser.domain.game.DigitFeedback
import io.keval.apps.guesser.domain.game.GuessEvaluation

/** Plain-language meaning of a guess remark, shown by the remark info popup. */
sealed interface RemarkMeaning {
    data object Winner : RemarkMeaning
    data object Smyle : RemarkMeaning
    data class Counts(val right: Int, val wrong: Int) : RemarkMeaning
}

fun GuessEvaluation.remarkMeaning(): RemarkMeaning {
    val right = digits.count { it == DigitFeedback.EXACT }
    val wrong = digits.count { it == DigitFeedback.CLOSE }
    return when {
        isWin -> RemarkMeaning.Winner
        right + wrong == 0 -> RemarkMeaning.Smyle
        else -> RemarkMeaning.Counts(right = right, wrong = wrong)
    }
}
