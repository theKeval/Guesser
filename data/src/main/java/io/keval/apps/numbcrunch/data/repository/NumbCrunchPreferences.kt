package io.keval.apps.numbcrunch.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// One DataStore per file: every preferences repository must share this delegate.
internal val Context.numbCrunchPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "numbcrunch_preferences",
)
