package io.keval.apps.numbcrunch.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import io.keval.apps.numbcrunch.domain.repository.SettingsRepository
import io.keval.apps.numbcrunch.domain.settings.AudioSettings
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PreferencesSettingsRepository(context: Context) : SettingsRepository {
    private val preferences = context.applicationContext.guesserPreferences

    override val audioSettings: Flow<AudioSettings> = preferences.data
        // Unreadable preferences fall back to the defaults rather than silencing the app.
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map {
            AudioSettings(
                musicEnabled = it[MUSIC_ENABLED] ?: true,
                soundEffectsEnabled = it[SOUND_EFFECTS_ENABLED] ?: true,
            )
        }

    override suspend fun setMusicEnabled(enabled: Boolean) {
        preferences.edit { it[MUSIC_ENABLED] = enabled }
    }

    override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        preferences.edit { it[SOUND_EFFECTS_ENABLED] = enabled }
    }

    private companion object {
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
    }
}
