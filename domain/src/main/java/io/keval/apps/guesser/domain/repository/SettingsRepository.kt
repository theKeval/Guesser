package io.keval.apps.guesser.domain.repository

import io.keval.apps.guesser.domain.settings.AudioSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val audioSettings: Flow<AudioSettings>

    suspend fun setMusicEnabled(enabled: Boolean)

    suspend fun setSoundEffectsEnabled(enabled: Boolean)
}
