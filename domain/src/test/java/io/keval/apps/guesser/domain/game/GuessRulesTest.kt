package io.keval.apps.guesser.domain.game

import io.keval.apps.guesser.domain.game.DigitFeedback.ABSENT
import io.keval.apps.guesser.domain.game.DigitFeedback.CLOSE
import io.keval.apps.guesser.domain.game.DigitFeedback.EXACT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GuessRulesTest {

    @Test
    fun `mixed exact and close digits use the V1 remark`() {
        val result = GuessRules.evaluate(secret = "245", guess = "325")

        assertEquals(listOf(ABSENT, CLOSE, EXACT), result.digits)
        assertEquals("1R, 1W", result.remark)
        assertFalse(result.isWin)
    }

    @Test
    fun `remarks cover only right, only wrong, smyle, and winner`() {
        assertEquals("2R", GuessRules.evaluate("245", "249").remark)
        assertEquals("3W", GuessRules.evaluate("245", "524").remark)
        assertEquals(GuessRules.SMYLE_REMARK, GuessRules.evaluate("245", "013").remark)

        val win = GuessRules.evaluate("045", "045")
        assertEquals(GuessRules.WINNER_REMARK, win.remark)
        assertTrue(win.isWin)
    }

    @Test
    fun `codes require three unique digits and allow leading zero`() {
        assertTrue(GuessRules.isValidCode("012"))
        assertFalse(GuessRules.isValidCode("112"))
        assertFalse(GuessRules.isValidCode("12"))
        assertFalse(GuessRules.isValidCode("1a2"))
        assertFalse(GuessRules.isValidCode("\u0661\u0662\u0663"))
    }

    @Test
    fun `generated secrets are valid codes`() {
        val random = Random(7)
        repeat(50) { assertTrue(GuessRules.isValidCode(GuessRules.generateSecret(random))) }
    }
}
