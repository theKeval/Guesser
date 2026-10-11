package io.keval.apps.guesser.gameplay.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.keval.apps.guesser.domain.game.CODE_LENGTH
import io.keval.apps.guesser.domain.game.GuessRules
import io.keval.apps.guesser.domain.game.isCodeDigit
import io.keval.apps.guesser.gameplay.home.PlayerMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
    private val savedStateHandle: SavedStateHandle,
    friendSecret: String?,
    private val generateSecret: () -> String = { GuessRules.generateSecret() },
) : ViewModel() {

    private val savedSecret: String? = savedStateHandle.get<String>(KEY_SECRET)
        ?.takeIf(GuessRules::isValidCode)

    // The friend secret lives only in process memory, so a restored round must use its saved mode.
    private val playerMode: PlayerMode =
        savedStateHandle.get<String>(KEY_MODE)
            ?.takeIf { savedSecret != null }
            ?.let { saved -> PlayerMode.entries.firstOrNull { it.name == saved } }
            ?: if (friendSecret != null && GuessRules.isValidCode(friendSecret)) PlayerMode.DOUBLE else PlayerMode.SINGLE

    private var secret: String = savedSecret
        ?: (if (playerMode == PlayerMode.DOUBLE) friendSecret!! else generateSecret())

    init {
        savedStateHandle[KEY_SECRET] = secret
        savedStateHandle[KEY_MODE] = playerMode.name
    }

    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onDigit(digit: Char) {
        val state = _uiState.value
        if (!isCodeDigit(digit) || !state.isDigitEnabled(digit)) return
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
        if (state.isRoundOver) return true
        _uiState.value = state.copy(showExitConfirmation = true)
        return false
    }

    fun onDismissExitConfirmation() {
        _uiState.value = _uiState.value.copy(showExitConfirmation = false)
    }

    /** Returns true when the round is already over, so the screen may leave right away. */
    fun onQuitRequested(): Boolean {
        val state = _uiState.value
        if (state.isRoundOver) return true
        _uiState.value = state.copy(showQuitConfirmation = true)
        return false
    }

    fun onDismissQuitConfirmation() {
        _uiState.value = _uiState.value.copy(showQuitConfirmation = false)
    }

    /** Ends the round and reveals the secret. */
    fun onConfirmQuit() {
        val state = _uiState.value
        if (state.isRoundOver) return
        savedStateHandle[KEY_GIVEN_UP] = true
        savedStateHandle[KEY_INPUT] = ""
        _uiState.value = state.copy(
            isGivenUp = true,
            revealedSecret = secret,
            currentInput = "",
            showQuitConfirmation = false,
            lastRevealedIndex = null,
        )
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
        savedStateHandle[KEY_GIVEN_UP] = false
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
        val isGivenUp = !isWon && savedStateHandle.get<Boolean>(KEY_GIVEN_UP) == true
        val input = savedStateHandle.get<String>(KEY_INPUT)
            .orEmpty()
            .takeIf {
                !isWon && !isGivenUp && it.length <= CODE_LENGTH && it.all(::isCodeDigit) && it.toSet().size == it.length
            }
            .orEmpty()
        return GameUiState(
            playerMode = playerMode,
            guesses = guesses,
            currentInput = input,
            isWon = isWon,
            isGivenUp = isGivenUp,
            revealedSecret = secret.takeIf { isWon || isGivenUp },
            showWinDialog = isWon && savedStateHandle.get<Boolean>(KEY_WIN_DIALOG) == true,
        )
    }

    companion object {
        private const val KEY_SECRET = "game_secret"
        private const val KEY_MODE = "game_mode"
        private const val KEY_GUESSES = "game_guesses"
        private const val KEY_INPUT = "game_input"
        private const val KEY_WIN_DIALOG = "game_win_dialog"
        private const val KEY_GIVEN_UP = "game_given_up"

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
