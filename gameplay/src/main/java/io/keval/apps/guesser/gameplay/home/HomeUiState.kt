package io.keval.apps.guesser.gameplay.home

enum class PlayerMode {
    SINGLE,
    DOUBLE,
}

data class HomeUiState(
    val playerMode: PlayerMode = PlayerMode.SINGLE,
    val friendSecretNumber: String = "",
    val secretValidationMessage: String? = null,
)
