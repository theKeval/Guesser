package io.keval.apps.guesser.domain.usecase

import io.keval.apps.guesser.domain.repository.SettingsRepository
import io.keval.apps.guesser.domain.settings.AudioSettings
import kotlinx.coroutines.flow.Flow

class ObserveAudioSettingsUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AudioSettings> = repository.audioSettings
}
