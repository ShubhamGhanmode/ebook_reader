package com.shubhamghanmode.inkfold.feature.reader

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shubhamghanmode.inkfold.ReadiumServices
import com.shubhamghanmode.inkfold.data.book.LibraryRepository
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderPreferencesManager
import org.readium.r2.navigator.preferences.Theme
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.allAreHtml
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
class ReaderRepository(
    private val context: Context,
    private val libraryRepository: LibraryRepository,
    private val readiumServices: ReadiumServices,
    private val preferencesDataStore: DataStore<Preferences>
) {
    private val sessions = ConcurrentHashMap<Long, ReaderSession>()
    private val sessionMutex = Mutex()

    suspend fun prepare(bookId: Long): Result<ReaderSession> = sessionMutex.withLock {
        sessions[bookId]?.let { return Result.success(it) }

        val book = libraryRepository.getBook(bookId)
            ?: return Result.failure(IllegalArgumentException("That book could not be found."))

        val asset = readiumServices.assetRetriever
            .retrieve(File(book.bookPath))
            .fold(
                onSuccess = { it },
                onFailure = {
                    return Result.failure(IllegalArgumentException("InkFold could not read that EPUB."))
                }
            )

        val publication = readiumServices.publicationOpener
            .open(asset, allowUserInteraction = false)
            .fold(
                onSuccess = { it },
                onFailure = {
                    return Result.failure(IllegalArgumentException("InkFold could not open that EPUB."))
                }
            )

        if (!publication.conformsTo(Publication.Profile.EPUB) && !publication.readingOrder.allAreHtml) {
            publication.close()
            return Result.failure(IllegalArgumentException("InkFold currently supports EPUB files only."))
        }

        val preferencesManager = ReaderPreferencesManager(
            bookId = bookId,
            dataStore = preferencesDataStore,
            defaultSharedPreferences = EpubPreferences(theme = defaultReaderTheme())
        )
        val initialSharedPreferences = preferencesManager.currentSharedPreferences()
        val initialBookPreferences = preferencesManager.currentBookPreferences()

        val session = ReaderSession(
            bookId = bookId,
            title = book.title,
            publication = publication,
            navigatorFactory = EpubNavigatorFactory(publication),
            preferencesManager = preferencesManager,
            initialLocator = libraryRepository.parseLocator(book.progressionJson),
            initialSharedPreferences = initialSharedPreferences,
            initialBookPreferences = initialBookPreferences,
            initialPreferences = initialSharedPreferences + initialBookPreferences
        )

        sessions[bookId] = session
        Result.success(session)
    }

    fun get(bookId: Long): ReaderSession? = sessions[bookId]

    fun close(bookId: Long) {
        sessions.remove(bookId)?.publication?.close()
    }

    private fun defaultReaderTheme(): Theme =
        when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> Theme.DARK
            else -> Theme.LIGHT
        }
}
