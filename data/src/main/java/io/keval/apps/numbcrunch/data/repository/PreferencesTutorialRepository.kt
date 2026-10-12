package io.keval.apps.numbcrunch.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import io.keval.apps.numbcrunch.domain.repository.TutorialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesTutorialRepository(context: Context) : TutorialRepository {
    private val preferences = context.applicationContext.numbCrunchPreferences

    override val hasCompletedFirstRun: Flow<Boolean> =
        preferences.data.map { it[HAS_COMPLETED_FIRST_RUN] ?: false }

    override suspend fun markFirstRunComplete() {
        preferences.edit { it[HAS_COMPLETED_FIRST_RUN] = true }
    }

    private companion object {
        val HAS_COMPLETED_FIRST_RUN = booleanPreferencesKey("has_completed_first_run_tutorial")
    }
}
