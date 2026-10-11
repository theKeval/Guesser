package io.keval.apps.guesser.domain.usecase

import io.keval.apps.guesser.domain.repository.SettingsRepository
import io.keval.apps.guesser.domain.settings.AudioSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioSettingsUseCaseTest {
    @Test
    fun `music and sound effects default on and are toggled independently`() = runBlocking {
        val repository = FakeSettingsRepository()
        val observe = ObserveAudioSettingsUseCase(repository)

        assertEquals(AudioSettings(musicEnabled = true, soundEffectsEnabled = true), observe().first())

        SetMusicEnabledUseCase(repository)(false)
        assertEquals(AudioSettings(musicEnabled = false, soundEffectsEnabled = true), observe().first())

        SetSoundEffectsEnabledUseCase(repository)(false)
        SetMusicEnabledUseCase(repository)(true)
        assertEquals(AudioSettings(musicEnabled = true, soundEffectsEnabled = false), observe().first())
    }

    private class FakeSettingsRepository : SettingsRepository {
        private val settings = MutableStateFlow(AudioSettings())
        override val audioSettings: Flow<AudioSettings> = settings

        override suspend fun setMusicEnabled(enabled: Boolean) {
            settings.value = settings.value.copy(musicEnabled = enabled)
        }

        override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
            settings.value = settings.value.copy(soundEffectsEnabled = enabled)
        }
    }
}
