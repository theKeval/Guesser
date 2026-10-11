package io.keval.apps.guesser.domain.usecase

import io.keval.apps.guesser.domain.repository.TutorialRepository

class CompleteFirstRunTutorialUseCase(
    private val repository: TutorialRepository,
) {
    suspend operator fun invoke() = repository.markFirstRunComplete()
}
