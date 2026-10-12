package io.keval.apps.numbcrunch.gameplay.game

import io.keval.apps.numbcrunch.domain.game.GuessRules
import org.junit.Assert.assertEquals
import org.junit.Test

class RemarkMeaningTest {

    private fun meaning(guess: String) = GuessRules.evaluate(secret = "245", guess = guess).remarkMeaning()

    @Test
    fun `each remark maps to its meaning`() {
        assertEquals(RemarkMeaning.Winner, meaning("245"))
        assertEquals(RemarkMeaning.Smyle, meaning("361"))
        assertEquals(RemarkMeaning.Counts(right = 1, wrong = 1), meaning("325"))
        assertEquals(RemarkMeaning.Counts(right = 2, wrong = 0), meaning("247"))
        assertEquals(RemarkMeaning.Counts(right = 0, wrong = 3), meaning("452"))
    }
}
