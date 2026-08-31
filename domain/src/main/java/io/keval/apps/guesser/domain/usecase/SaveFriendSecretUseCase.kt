package io.keval.apps.guesser.domain.usecase

import io.keval.apps.guesser.domain.repository.GameSessionRepository

class SaveFriendSecretUseCase(
    private val gameSessionRepository: GameSessionRepository,
) {
    operator fun invoke(secret: String?) {
        gameSessionRepository.saveFriendSecret(secret)
    }
}
