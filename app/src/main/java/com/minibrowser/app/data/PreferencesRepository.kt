package com.minibrowser.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.minibrowser.app.engine.SearchEngineConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {

    private val engineKey = stringPreferencesKey("search_engine_id")

    val selectedEngineId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[engineKey] ?: SearchEngineConfig.defaultEngineId
    }

    suspend fun setSearchEngine(engineId: String) {
        context.dataStore.edit { prefs ->
            prefs[engineKey] = engineId
        }
    }
}
