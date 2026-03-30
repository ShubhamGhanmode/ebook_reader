package com.shubhamghanmode.inkfold.ui.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class AppThemeSettings(
    val themePreset: AppThemePreset = AppThemePreset.IRON
)

class AppThemePreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    val settings: Flow<AppThemeSettings> = dataStore.data
        .map { preferences ->
            AppThemeSettings(
                themePreset = AppThemePreset.fromStorageKey(preferences[THEME_PRESET_KEY])
            )
        }
        .distinctUntilChanged()

    suspend fun setThemePreset(themePreset: AppThemePreset) {
        dataStore.edit { preferences ->
            preferences[THEME_PRESET_KEY] = themePreset.storageKey
        }
    }

    private companion object {
        val THEME_PRESET_KEY = stringPreferencesKey("app_theme_preset")
    }
}
