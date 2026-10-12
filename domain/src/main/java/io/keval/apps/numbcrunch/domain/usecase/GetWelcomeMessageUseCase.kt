package io.keval.apps.numbcrunch.domain.usecase

import io.keval.apps.numbcrunch.domain.repository.AppInfoRepository

class GetWelcomeMessageUseCase(
    private val appInfoRepository: AppInfoRepository,
) {
    operator fun invoke(): String = appInfoRepository.getWelcomeMessage()
}
