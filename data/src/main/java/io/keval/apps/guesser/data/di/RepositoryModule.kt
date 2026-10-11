package io.keval.apps.guesser.data.di

import android.content.Context
import io.keval.apps.guesser.data.repository.DefaultAppInfoRepository
import io.keval.apps.guesser.data.repository.InMemoryGameSessionRepository
import io.keval.apps.guesser.data.repository.PreferencesSettingsRepository
import io.keval.apps.guesser.data.repository.PreferencesTutorialRepository
import io.keval.apps.guesser.domain.repository.AppInfoRepository
import io.keval.apps.guesser.domain.repository.GameSessionRepository
import io.keval.apps.guesser.domain.repository.SettingsRepository
import io.keval.apps.guesser.domain.repository.TutorialRepository

object RepositoryModule {
    fun createAppInfoRepository(): AppInfoRepository {
        return DefaultAppInfoRepository()
    }

    fun createGameSessionRepository(): GameSessionRepository {
        return InMemoryGameSessionRepository()
    }

    fun createTutorialRepository(context: Context): TutorialRepository {
        return PreferencesTutorialRepository(context)
    }

    fun createSettingsRepository(context: Context): SettingsRepository {
        return PreferencesSettingsRepository(context)
    }
}
