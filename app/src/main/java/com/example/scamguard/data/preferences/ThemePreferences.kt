package com.example.scamguard.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "scamguard_settings")

class ThemePreferences(context: Context) {

    private val dataStore = context.themeDataStore

    private val darkModeKey = booleanPreferencesKey("dark_mode")

    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[darkModeKey] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[darkModeKey] = enabled
        }
    }
}

