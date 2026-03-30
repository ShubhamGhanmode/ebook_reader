package com.shubhamghanmode.inkfold.feature.reader.tts

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesSerializer
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
class ReaderTtsPreferencesManager(
    private val dataStore: DataStore<Preferences>
) {
    private val serializer = AndroidTtsPreferencesSerializer()

    val preferences: Flow<AndroidTtsPreferences> = dataStore.data
        .map { preferences ->
            preferences[PREFERENCES_KEY]
                ?.let(::deserialize)
                ?: AndroidTtsPreferences()
        }

    suspend fun currentPreferences(): AndroidTtsPreferences =
        preferences.first()

    suspend fun setPreferences(preferences: AndroidTtsPreferences) {
        dataStore.edit { stored ->
            stored[PREFERENCES_KEY] = serializer.serialize(preferences)
        }
    }

    suspend fun reset() {
        dataStore.edit { stored ->
            stored.remove(PREFERENCES_KEY)
        }
    }

    private fun deserialize(serializedPreferences: String): AndroidTtsPreferences =
        runCatching {
            serializer.deserialize(serializedPreferences)
        }.getOrElse {
            AndroidTtsPreferences()
        }

    private companion object {
        val PREFERENCES_KEY = stringPreferencesKey("reader_tts_preferences")
    }
}
