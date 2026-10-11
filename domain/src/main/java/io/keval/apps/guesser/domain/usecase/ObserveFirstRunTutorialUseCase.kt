package io.keval.apps.guesser.domain.usecase

import io.keval.apps.guesser.domain.repository.TutorialRepository
import kotlinx.coroutines.flow.Flow

class ObserveFirstRunTutorialUseCase(
    private val repository: TutorialRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.hasCompletedFirstRun
}
