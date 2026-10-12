package io.keval.apps.numbcrunch.domain.usecase

import io.keval.apps.numbcrunch.domain.repository.GameSessionRepository

class SaveFriendSecretUseCase(
    private val gameSessionRepository: GameSessionRepository,
) {
    operator fun invoke(secret: String?) {
        gameSessionRepository.saveFriendSecret(secret)
    }
}
