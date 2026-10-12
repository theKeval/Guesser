package io.keval.apps.guesser.gameplay.home

import io.keval.apps.guesser.domain.game.CODE_LENGTH
import io.keval.apps.guesser.domain.game.isCodeDigit

enum class PlayerMode {
    SINGLE,
    DOUBLE,
}

data class HomeUiState(
    val playerMode: PlayerMode = PlayerMode.SINGLE,
    val friendSecretNumber: String = "",
    val secretValidationMessage: String? = null,
)

/** True when [digit] can be added: the secret is not full and the digit is not used yet. */
internal fun canAddSecretDigit(secret: String, digit: Char): Boolean =
    isCodeDigit(digit) && secret.length < CODE_LENGTH && digit !in secret
