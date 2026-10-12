package io.keval.apps.numbcrunch.domain.usecase

import io.keval.apps.numbcrunch.domain.repository.SettingsRepository
import io.keval.apps.numbcrunch.domain.settings.AudioSettings
import kotlinx.coroutines.flow.Flow

class ObserveAudioSettingsUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AudioSettings> = repository.audioSettings
}
