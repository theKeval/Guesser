package io.keval.apps.guesser.domain.usecase

import io.keval.apps.guesser.domain.repository.AppInfoRepository

class GetWelcomeMessageUseCase(
    private val appInfoRepository: AppInfoRepository,
) {
    operator fun invoke(): String = appInfoRepository.getWelcomeMessage()
}
