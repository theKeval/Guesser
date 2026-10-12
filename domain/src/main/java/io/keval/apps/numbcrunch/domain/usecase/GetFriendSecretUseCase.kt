package io.keval.apps.numbcrunch.domain.usecase

import io.keval.apps.numbcrunch.domain.repository.GameSessionRepository

class GetFriendSecretUseCase(
    private val gameSessionRepository: GameSessionRepository,
) {
    operator fun invoke(): String? = gameSessionRepository.getFriendSecret()
}
