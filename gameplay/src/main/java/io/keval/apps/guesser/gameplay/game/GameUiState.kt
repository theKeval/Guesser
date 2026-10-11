package io.keval.apps.guesser.gameplay.game

import io.keval.apps.guesser.domain.game.CODE_LENGTH
import io.keval.apps.guesser.domain.game.GuessEvaluation
import io.keval.apps.guesser.gameplay.home.PlayerMode

data class GameUiState(
    val playerMode: PlayerMode = PlayerMode.SINGLE,
    val guesses: List<GuessEvaluation> = emptyList(),
    val currentInput: String = "",
    val isWon: Boolean = false,
    /** The player quit the round and the secret was revealed to them. */
    val isGivenUp: Boolean = false,
    /** Revealed only once the round is won or given up. */
    val revealedSecret: String? = null,
    val showExitConfirmation: Boolean = false,
    val showQuitConfirmation: Boolean = false,
    val showWinDialog: Boolean = false,
    /** Index of the guess evaluated during this session, used to animate its reveal once. */
    val lastRevealedIndex: Int? = null,
) {
    val isRoundOver: Boolean get() = isWon || isGivenUp
    val canEnter: Boolean get() = !isRoundOver && currentInput.length == CODE_LENGTH
    val canDelete: Boolean get() = !isRoundOver && currentInput.isNotEmpty()

    fun isDigitEnabled(digit: Char): Boolean {
        return !isRoundOver && currentInput.length < CODE_LENGTH && digit !in currentInput
    }
}
