package io.keval.apps.guesser.domain.usecase

import io.keval.apps.guesser.domain.repository.SettingsRepository

class SetSoundEffectsEnabledUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setSoundEffectsEnabled(enabled)
}
