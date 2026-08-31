package io.keval.apps.guesser.data.di

import io.keval.apps.guesser.data.repository.DefaultAppInfoRepository
import io.keval.apps.guesser.data.repository.InMemoryGameSessionRepository
import io.keval.apps.guesser.domain.repository.AppInfoRepository
import io.keval.apps.guesser.domain.repository.GameSessionRepository

object RepositoryModule {
    fun createAppInfoRepository(): AppInfoRepository {
        return DefaultAppInfoRepository()
    }

    fun createGameSessionRepository(): GameSessionRepository {
        return InMemoryGameSessionRepository()
    }
}
