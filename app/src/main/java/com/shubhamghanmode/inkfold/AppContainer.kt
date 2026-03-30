package com.shubhamghanmode.inkfold

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.shubhamghanmode.inkfold.data.book.BookDao
import com.shubhamghanmode.inkfold.data.book.DeleteBookUseCase
import com.shubhamghanmode.inkfold.data.book.ImportCopiedBookUseCase
import com.shubhamghanmode.inkfold.data.book.InkFoldDatabase
import com.shubhamghanmode.inkfold.data.book.LibraryRepository
import com.shubhamghanmode.inkfold.data.book.LocalLibraryFileStore
import com.shubhamghanmode.inkfold.data.book.ReadiumPublicationInspector
import com.shubhamghanmode.inkfold.data.book.RoomBookMutationStore
import com.shubhamghanmode.inkfold.data.book.UriImportSourceCopier
import com.shubhamghanmode.inkfold.feature.reader.ReaderRepository
import com.shubhamghanmode.inkfold.ui.theme.AppThemePreferencesRepository
import java.io.File
import java.time.Clock

private val Context.readerPreferencesDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "reader-preferences")

private val Context.appThemePreferencesDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "app-theme-preferences")

class AppContainer(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val clock: Clock = Clock.systemUTC()

    val database: InkFoldDatabase =
        Room
            .databaseBuilder(
                appContext,
                InkFoldDatabase::class.java,
                "inkfold.db",
            ).build()

    val bookDao: BookDao = database.bookDao()
    val readiumServices = ReadiumServices(appContext)
    val appThemePreferencesRepository =
        AppThemePreferencesRepository(
            dataStore = appContext.appThemePreferencesDataStore,
        )

    private val fileStore =
        LocalLibraryFileStore(
            libraryRootDir = File(appContext.filesDir, "library"),
            importCacheDir = File(appContext.cacheDir, "inkfold-imports"),
        )
    private val bookMutationStore = RoomBookMutationStore(bookDao)
    private val publicationInspector = ReadiumPublicationInspector(readiumServices)
    private val uriImportSourceCopier =
        UriImportSourceCopier(
            contentResolver = appContext.contentResolver,
            fileStore = fileStore,
        )
    private val importCopiedBookUseCase =
        ImportCopiedBookUseCase(
            fileStore = fileStore,
            publicationInspector = publicationInspector,
            bookStore = bookMutationStore,
            clock = clock,
        )
    private val deleteBookUseCase =
        DeleteBookUseCase(
            fileStore = fileStore,
            bookStore = bookMutationStore,
        )

    val libraryRepository =
        LibraryRepository(
            bookDao = bookDao,
            uriImportSourceCopier = uriImportSourceCopier,
            importCopiedBookUseCase = importCopiedBookUseCase,
            deleteBookUseCase = deleteBookUseCase,
            clock = clock,
        )

    val readerRepository =
        ReaderRepository(
            context = appContext,
            libraryRepository = libraryRepository,
            readiumServices = readiumServices,
            preferencesDataStore = appContext.readerPreferencesDataStore,
        )
}
