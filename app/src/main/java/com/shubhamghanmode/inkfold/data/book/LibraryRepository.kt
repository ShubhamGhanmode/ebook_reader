package com.shubhamghanmode.inkfold.data.book

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator

class LibraryRepository(
    private val bookDao: BookDao,
    private val uriImportSourceCopier: UriImportSourceCopier,
    private val importCopiedBookUseCase: ImportCopiedBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val clock: Clock
) {
    fun observeContinueReading(): Flow<BookEntity?> = bookDao.observeContinueReading()

    fun observeRecentImports(): Flow<List<BookEntity>> = bookDao.observeRecentImports()

    fun observeAllBooks(): Flow<List<BookEntity>> = bookDao.observeAllBooks()

    fun observeBookCount(): Flow<Int> = bookDao.observeBookCount()

    suspend fun getBook(bookId: Long): BookEntity? = bookDao.getById(bookId)

    suspend fun importFromUri(uri: Uri): Result<Long> {
        val copiedSource = uriImportSourceCopier.copy(uri).getOrElse { error ->
            return Result.failure(error)
        }

        return importCopiedBookUseCase.import(
            tempFile = copiedSource.tempFile,
            originalFileName = copiedSource.displayName
        )
    }

    suspend fun deleteBook(bookId: Long): Result<Unit> {
        val book = bookDao.getById(bookId)
            ?: return Result.failure(IllegalArgumentException("The selected book no longer exists."))

        return deleteBookUseCase.delete(book)
    }

    suspend fun markBookOpened(bookId: Long) {
        bookDao.updateLastOpenedAt(
            bookId = bookId,
            timestamp = clock.instant().toEpochMilli()
        )
    }

    suspend fun saveProgression(bookId: Long, locator: Locator) {
        bookDao.updateProgression(
            bookId = bookId,
            progressionJson = locator.toJSON().toString(),
            progressionPercent = ProgressionNormalizer.fromLocator(locator),
            lastOpenedAt = clock.instant().toEpochMilli()
        )
    }

    fun parseLocator(locatorJson: String?): Locator? =
        locatorJson
            ?.takeIf { it.isNotBlank() }
            ?.let { Locator.fromJSON(JSONObject(it)) }
}
