package io.keval.apps.numbcrunch.domain.usecase

import io.keval.apps.numbcrunch.domain.repository.TutorialRepository
import kotlinx.coroutines.flow.Flow

class ObserveFirstRunTutorialUseCase(
    private val repository: TutorialRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.hasCompletedFirstRun
}
