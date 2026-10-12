package io.keval.apps.numbcrunch.domain.repository

import kotlinx.coroutines.flow.Flow

interface TutorialRepository {
    val hasCompletedFirstRun: Flow<Boolean>

    suspend fun markFirstRunComplete()
}
