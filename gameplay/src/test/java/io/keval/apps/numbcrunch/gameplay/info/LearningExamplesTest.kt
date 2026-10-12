package io.keval.apps.numbcrunch.gameplay.info

import io.keval.apps.numbcrunch.domain.game.DigitFeedback
import io.keval.apps.numbcrunch.domain.game.GuessRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningExamplesTest {
    @Test
    fun `clue book uses the real scoring engine`() {
        assertEquals(listOf("1R, 1W", "1R, 2W", "SMYLE", "Winner"), LearningExamples.evaluations.map { it.remark })
        assertEquals(
            listOf(DigitFeedback.ABSENT, DigitFeedback.CLOSE, DigitFeedback.EXACT),
            LearningExamples.evaluations.first().digits,
        )
        assertTrue(LearningExamples.evaluations.last().isWin)
    }

    @Test
    fun `tutorial deduction holds for every possible secret matching the first row`() {
        val possibleSecrets = (0..999).map { it.toString().padStart(3, '0') }
            .filter(GuessRules::isValidCode)
            .filter { GuessRules.evaluate(it, "325").digits == LearningExamples.evaluations.first().digits }
        assertTrue(possibleSecrets.isNotEmpty())
        assertTrue(possibleSecrets.all { it[0] == '2' && it[2] == '5' && '3' !in it })
        assertTrue(possibleSecrets.map { it[1] }.distinct().size > 1)
    }
}
