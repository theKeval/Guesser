package io.keval.apps.guesser.domain.repository

import kotlinx.coroutines.flow.Flow

interface TutorialRepository {
    val hasCompletedFirstRun: Flow<Boolean>

    suspend fun markFirstRunComplete()
}
