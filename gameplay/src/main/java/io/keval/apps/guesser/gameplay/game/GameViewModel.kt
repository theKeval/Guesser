package io.keval.apps.guesser.gameplay.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.keval.apps.guesser.domain.game.CODE_LENGTH
import io.keval.apps.guesser.domain.game.GuessRules
import io.keval.apps.guesser.gameplay.home.PlayerMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
    private val savedStateHandle: SavedStateHandle,
    friendSecret: String?,
    private val generateSecret: () -> String = { GuessRules.generateSecret() },
) : ViewModel() {

    private val playerMode: PlayerMode =
        if (friendSecret != null && GuessRules.isValidCode(friendSecret)) PlayerMode.DOUBLE else PlayerMode.SINGLE

    private var secret: String = savedStateHandle.get<String>(KEY_SECRET)
        ?: (if (playerMode == PlayerMode.DOUBLE) friendSecret!! else generateSecret())
            .also { savedStateHandle[KEY_SECRET] = it }

    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onDigit(digit: Char) {
        val state = _uiState.value
        if (!digit.isDigit() || !state.isDigitEnabled(digit)) return
        updateInput(state.currentInput + digit)
    }

    fun onDelete() {
        val state = _uiState.value
        if (!state.canDelete) return
        updateInput(state.currentInput.dropLast(1))
    }

    fun onEnter() {
        val state = _uiState.value
        if (!state.canEnter || !GuessRules.isValidCode(state.currentInput)) return

        val evaluation = GuessRules.evaluate(secret, state.currentInput)
        val guesses = state.guesses + evaluation
        savedStateHandle[KEY_GUESSES] = ArrayList(guesses.map { it.guess })
        savedStateHandle[KEY_INPUT] = ""
        savedStateHandle[KEY_WIN_DIALOG] = evaluation.isWin

        _uiState.value = state.copy(
            guesses = guesses,
            currentInput = "",
            isWon = evaluation.isWin,
            revealedSecret = secret.takeIf { evaluation.isWin },
            showWinDialog = evaluation.isWin,
            lastRevealedIndex = guesses.lastIndex,
        )
    }

    /** Returns true when the screen may leave immediately without confirmation. */
    fun onBackRequested(): Boolean {
        val state = _uiState.value
        if (state.isWon) return true
        _uiState.value = state.copy(showExitConfirmation = true)
        return false
    }

    fun onDismissExitConfirmation() {
        _uiState.value = _uiState.value.copy(showExitConfirmation = false)
    }

    fun onDismissWinDialog() {
        savedStateHandle[KEY_WIN_DIALOG] = false
        _uiState.value = _uiState.value.copy(showWinDialog = false)
    }

    /** Starts a fresh single-player round. Double-player rounds need a new friend secret from Home. */
    fun onPlayAgain() {
        if (playerMode != PlayerMode.SINGLE) return
        secret = generateSecret()
        savedStateHandle[KEY_SECRET] = secret
        savedStateHandle[KEY_GUESSES] = ArrayList<String>()
        savedStateHandle[KEY_INPUT] = ""
        savedStateHandle[KEY_WIN_DIALOG] = false
        _uiState.value = GameUiState(playerMode = playerMode)
    }

    private fun updateInput(input: String) {
        savedStateHandle[KEY_INPUT] = input
        _uiState.value = _uiState.value.copy(currentInput = input, lastRevealedIndex = null)
    }

    private fun restoreState(): GameUiState {
        val guesses = savedStateHandle.get<ArrayList<String>>(KEY_GUESSES)
            .orEmpty()
            .filter(GuessRules::isValidCode)
            .map { GuessRules.evaluate(secret, it) }
        val isWon = guesses.lastOrNull()?.isWin == true
        val input = savedStateHandle.get<String>(KEY_INPUT)
            .orEmpty()
            .takeIf { !isWon && it.length <= CODE_LENGTH && it.all(Char::isDigit) && it.toSet().size == it.length }
            .orEmpty()
        return GameUiState(
            playerMode = playerMode,
            guesses = guesses,
            currentInput = input,
            isWon = isWon,
            revealedSecret = secret.takeIf { isWon },
            showWinDialog = isWon && savedStateHandle.get<Boolean>(KEY_WIN_DIALOG) == true,
        )
    }

    companion object {
        private const val KEY_SECRET = "game_secret"
        private const val KEY_GUESSES = "game_guesses"
        private const val KEY_INPUT = "game_input"
        private const val KEY_WIN_DIALOG = "game_win_dialog"

        fun factory(friendSecret: String?): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras,
                ): T {
                    require(modelClass.isAssignableFrom(GameViewModel::class.java)) {
                        "Unknown ViewModel class: ${modelClass.name}"
                    }
                    return GameViewModel(extras.createSavedStateHandle(), friendSecret) as T
                }
            }
        }
    }
}
