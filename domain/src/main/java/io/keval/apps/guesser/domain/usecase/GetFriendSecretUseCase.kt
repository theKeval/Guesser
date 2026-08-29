package io.keval.apps.guesser.domain.usecase

import io.keval.apps.guesser.domain.repository.GameSessionRepository

class GetFriendSecretUseCase(
    private val gameSessionRepository: GameSessionRepository,
) {
    operator fun invoke(): String? = gameSessionRepository.getFriendSecret()
}
