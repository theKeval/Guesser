package io.keval.apps.guesser.domain.usecase

import io.keval.apps.guesser.domain.repository.TutorialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialUseCaseTest {
    @Test
    fun `completion is observed and persisted through repository contract`() = runBlocking {
        val repository = FakeTutorialRepository()
        val observe = ObserveFirstRunTutorialUseCase(repository)
        val complete = CompleteFirstRunTutorialUseCase(repository)

        assertFalse(observe().first())
        complete()
        assertTrue(observe().first())
    }

    private class FakeTutorialRepository : TutorialRepository {
        private val completed = MutableStateFlow(false)
        override val hasCompletedFirstRun: Flow<Boolean> = completed

        override suspend fun markFirstRunComplete() {
            completed.value = true
        }
    }
}
