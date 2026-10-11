package io.keval.apps.guesser.gameplay.game

import io.keval.apps.guesser.domain.game.DigitFeedback
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class DigitTileBrushTest {

    @Test
    fun `disabled color feedback keeps every tile gray on both sides of the flip`() {
        val grayBrush = digitTileBrush(
            TileState.Revealed('3', DigitFeedback.ABSENT),
            showFront = false,
            colorFeedbackEnabled = true,
        )
        val states = listOf(TileState.Empty, TileState.Pending('2')) +
            DigitFeedback.entries.map { TileState.Revealed('5', it) }

        states.forEach { state ->
            listOf(true, false).forEach { showFront ->
                assertSame(grayBrush, digitTileBrush(state, showFront, colorFeedbackEnabled = false))
            }
        }
    }

    @Test
    fun `enabled color feedback preserves neutral fronts and distinct revealed colors`() {
        val neutralBrush = digitTileBrush(TileState.Empty, showFront = true, colorFeedbackEnabled = true)
        assertSame(
            neutralBrush,
            digitTileBrush(TileState.Pending('2'), showFront = true, colorFeedbackEnabled = true),
        )
        val revealedBrushes = DigitFeedback.entries.map { feedback ->
            val state = TileState.Revealed('5', feedback)
            assertSame(neutralBrush, digitTileBrush(state, showFront = true, colorFeedbackEnabled = true))
            digitTileBrush(state, showFront = false, colorFeedbackEnabled = true).also {
                assertNotSame(neutralBrush, it)
            }
        }
        assertNotSame(revealedBrushes[0], revealedBrushes[1])
        assertNotSame(revealedBrushes[0], revealedBrushes[2])
        assertNotSame(revealedBrushes[1], revealedBrushes[2])
    }
}
