package io.keval.apps.guesser.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import io.keval.apps.guesser.domain.repository.TutorialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tutorialPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "guesser_preferences",
)

class PreferencesTutorialRepository(context: Context) : TutorialRepository {
    private val preferences = context.applicationContext.tutorialPreferences

    override val hasCompletedFirstRun: Flow<Boolean> =
        preferences.data.map { it[HAS_COMPLETED_FIRST_RUN] ?: false }

    override suspend fun markFirstRunComplete() {
        preferences.edit { it[HAS_COMPLETED_FIRST_RUN] = true }
    }

    private companion object {
        val HAS_COMPLETED_FIRST_RUN = booleanPreferencesKey("has_completed_first_run_tutorial")
    }
}
