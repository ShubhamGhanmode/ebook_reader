package com.shubhamghanmode.inkfold.feature.reader.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.epub.EpubPreferencesSerializer
import org.readium.r2.navigator.epub.EpubPublicationPreferencesFilter
import org.readium.r2.navigator.epub.EpubSharedPreferencesFilter
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
class ReaderPreferencesManager(
    private val bookId: Long,
    private val dataStore: DataStore<Preferences>,
    val defaultSharedPreferences: EpubPreferences
) {
    private val serializer = EpubPreferencesSerializer()

    val sharedPreferences: Flow<EpubPreferences> = dataStore.data
        .map { preferences ->
            preferences[sharedKey()]
                ?.let(::deserializePreferences)
                ?: defaultSharedPreferences
        }

    val bookPreferences: Flow<EpubPreferences> = dataStore.data
        .map { preferences ->
            preferences[bookKey(bookId)]
                ?.let(::deserializePreferences)
                ?: EpubPreferences()
        }

    val preferences: Flow<EpubPreferences> = combine(
        sharedPreferences,
        bookPreferences
    ) { sharedPreferences, bookPreferences ->
        sharedPreferences + bookPreferences
    }

    suspend fun currentSharedPreferences(): EpubPreferences = sharedPreferences.first()

    suspend fun currentBookPreferences(): EpubPreferences = bookPreferences.first()

    suspend fun setPreferences(
        scope: ReaderAppearanceScope,
        preferences: EpubPreferences
    ) {
        dataStore.edit { storedPreferences ->
            storedPreferences[key(scope)] = serializer.serialize(
                when (scope) {
                    ReaderAppearanceScope.SharedDefaults ->
                        EpubSharedPreferencesFilter.filter(preferences)

                    is ReaderAppearanceScope.Book ->
                        EpubPublicationPreferencesFilter.filter(preferences)
                }
            )
        }
    }

    suspend fun reset(scope: ReaderAppearanceScope) {
        dataStore.edit { preferences ->
            preferences.remove(key(scope))
        }
    }

    private fun deserializePreferences(serializedPreferences: String): EpubPreferences =
        runCatching {
            serializer.deserialize(serializedPreferences)
        }.getOrElse {
            EpubPreferences()
        }

    private fun key(scope: ReaderAppearanceScope): Preferences.Key<String> =
        when (scope) {
            ReaderAppearanceScope.SharedDefaults -> sharedKey()
            is ReaderAppearanceScope.Book -> bookKey(scope.bookId)
        }

    private fun sharedKey(): Preferences.Key<String> = key(EpubPreferences::class)

    private fun bookKey(bookId: Long): Preferences.Key<String> =
        stringPreferencesKey("book-$bookId")

    private fun <T : Any> key(klass: KClass<T>): Preferences.Key<String> =
        stringPreferencesKey("class-${klass.simpleName}")
}
