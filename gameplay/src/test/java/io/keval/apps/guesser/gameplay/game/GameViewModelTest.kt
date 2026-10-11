package io.keval.apps.guesser.gameplay.game

import androidx.lifecycle.SavedStateHandle
import io.keval.apps.guesser.domain.game.DigitFeedback
import io.keval.apps.guesser.gameplay.home.PlayerMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {

    private fun viewModel(
        handle: SavedStateHandle = SavedStateHandle(),
        friendSecret: String? = null,
        secrets: MutableList<String> = mutableListOf("573", "012"),
    ) = GameViewModel(handle, friendSecret) { secrets.removeAt(0) }

    private fun GameViewModel.type(code: String) = code.forEach(::onDigit)

    @Test
    fun `digits stay pending until enter, then are evaluated with the V1 remark`() {
        val vm = viewModel()
        vm.type("527")

        assertEquals("527", vm.uiState.value.currentInput)
        assertTrue(vm.uiState.value.guesses.isEmpty())

        vm.onEnter()

        val guess = vm.uiState.value.guesses.single()
        assertEquals(listOf(DigitFeedback.EXACT, DigitFeedback.ABSENT, DigitFeedback.CLOSE), guess.digits)
        assertEquals("1R, 1W", guess.remark)
        assertEquals("", vm.uiState.value.currentInput)
        assertEquals(0, vm.uiState.value.lastRevealedIndex)
    }

    @Test
    fun `duplicate digits and a fourth digit are ignored and enter needs three digits`() {
        val vm = viewModel()
        vm.type("55")
        assertEquals("5", vm.uiState.value.currentInput)

        vm.onEnter()
        assertTrue(vm.uiState.value.guesses.isEmpty())

        vm.type("7890")
        assertEquals("578", vm.uiState.value.currentInput)

        vm.onDelete()
        assertEquals("57", vm.uiState.value.currentInput)
    }

    @Test
    fun `winning locks input, reveals the secret, and allows leaving without confirmation`() {
        val vm = viewModel()
        vm.type("573")
        vm.onEnter()

        val state = vm.uiState.value
        assertTrue(state.isWon)
        assertTrue(state.showWinDialog)
        assertEquals("573", state.revealedSecret)
        assertEquals("Winner", state.guesses.last().remark)

        vm.onDigit('1')
        assertEquals("", vm.uiState.value.currentInput)
        assertTrue(vm.onBackRequested())
    }

    @Test
    fun `back during a round asks for confirmation`() {
        val vm = viewModel()
        assertFalse(vm.onBackRequested())
        assertTrue(vm.uiState.value.showExitConfirmation)

        vm.onDismissExitConfirmation()
        assertFalse(vm.uiState.value.showExitConfirmation)
    }

    @Test
    fun `state is restored from the saved state handle`() {
        val handle = SavedStateHandle()
        viewModel(handle).apply {
            type("481")
            onEnter()
            type("21")
        }

        val restored = viewModel(handle, secrets = mutableListOf("999")).uiState.value
        assertEquals(listOf("481"), restored.guesses.map { it.guess })
        assertEquals("21", restored.currentInput)
        assertNull(restored.lastRevealedIndex)
    }

    @Test
    fun `double player mode is restored even when the in-memory friend secret is gone`() {
        val handle = SavedStateHandle()
        viewModel(handle, friendSecret = "908").apply {
            type("901")
            onEnter()
        }

        val restored = viewModel(handle, friendSecret = null).uiState.value
        assertEquals(PlayerMode.DOUBLE, restored.playerMode)
        assertEquals("2R", restored.guesses.single().remark)
    }

    @Test
    fun `valid friend secret starts double player and play again is single player only`() {
        val vm = viewModel(friendSecret = "908")
        assertEquals(PlayerMode.DOUBLE, vm.uiState.value.playerMode)
        vm.type("908")
        vm.onEnter()
        vm.onPlayAgain()
        assertTrue(vm.uiState.value.isWon)

        val single = viewModel()
        single.type("573")
        single.onEnter()
        single.onPlayAgain()
        assertFalse(single.uiState.value.isWon)
        assertTrue(single.uiState.value.guesses.isEmpty())
        single.type("012")
        single.onEnter()
        assertTrue(single.uiState.value.isWon)
    }
}
