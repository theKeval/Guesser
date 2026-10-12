package io.keval.apps.numbcrunch.domain.usecase

import io.keval.apps.numbcrunch.domain.repository.SettingsRepository

class SetMusicEnabledUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setMusicEnabled(enabled)
}
