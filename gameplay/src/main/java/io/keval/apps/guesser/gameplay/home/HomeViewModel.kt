package io.keval.apps.guesser.gameplay.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import io.keval.apps.guesser.domain.usecase.GetWelcomeMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(
    getWelcomeMessageUseCase: GetWelcomeMessageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        getWelcomeMessageUseCase()
    }

    fun onPlayerModeSelected(playerMode: PlayerMode) {
        _uiState.value = _uiState.value.copy(
            playerMode = playerMode,
            friendSecretNumber = if (playerMode == PlayerMode.SINGLE) "" else _uiState.value.friendSecretNumber,
            secretValidationMessage = null,
        )
    }

    fun onFriendSecretNumberChanged(value: String) {
        val sanitized = value.filter(Char::isDigit).take(3)
        _uiState.value = _uiState.value.copy(
            friendSecretNumber = sanitized,
            secretValidationMessage = buildValidationMessage(sanitized),
        )
    }

    fun canStartGame(): Boolean {
        val currentState = _uiState.value
        if (currentState.playerMode == PlayerMode.SINGLE) {
            _uiState.value = currentState.copy(secretValidationMessage = null)
            return true
        }

        val validationMessage = when {
            currentState.friendSecretNumber.length < 3 -> "Enter exactly 3 digits"
            currentState.friendSecretNumber.toSet().size != currentState.friendSecretNumber.length -> "Digits must be unique"
            else -> null
        }
        _uiState.value = currentState.copy(secretValidationMessage = validationMessage)
        return validationMessage == null
    }

    private fun buildValidationMessage(secret: String): String? {
        return when {
            secret.isEmpty() -> null
            secret.toSet().size != secret.length -> "Digits must be unique"
            secret.length < 3 -> "Enter exactly 3 digits"
            else -> null
        }
    }

    companion object {
        fun factory(
            getWelcomeMessageUseCase: GetWelcomeMessageUseCase,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras,
                ): T {
                    require(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                        "Unknown ViewModel class: ${modelClass.name}"
                    }
                    return HomeViewModel(getWelcomeMessageUseCase) as T
                }
            }
        }
    }
}
