package io.keval.apps.numbcrunch.domain.usecase

import io.keval.apps.numbcrunch.domain.repository.TutorialRepository

class CompleteFirstRunTutorialUseCase(
    private val repository: TutorialRepository,
) {
    suspend operator fun invoke() = repository.markFirstRunComplete()
}
