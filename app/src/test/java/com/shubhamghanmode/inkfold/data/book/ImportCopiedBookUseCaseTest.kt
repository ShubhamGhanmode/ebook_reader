package com.shubhamghanmode.inkfold.data.book

import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ImportCopiedBookUseCaseTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val fixedClock: Clock =
        Clock.fixed(Instant.parse("2026-03-27T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun importStoresManagedBookMetadataAndCover() = runTest {
        val fileStore = newFileStore()
        val tempFile = newTempEpub("odyssey.epub", "epub-content")
        val capturedBooks = mutableListOf<BookEntity>()
        val bookStore = FakeBookMutationStore(
            onInsert = {
                capturedBooks += it
                41L
            }
        )
        val publicationInspector = FakePublicationInspector(
            PublicationInfo(
                mediaType = "application/epub+zip",
                title = "The Odyssey",
                author = "Homer",
                identifier = "urn:test:odyssey",
                coverBytes = byteArrayOf(1, 2, 3, 4)
            )
        )

        val result = ImportCopiedBookUseCase(
            fileStore = fileStore,
            publicationInspector = publicationInspector,
            bookStore = bookStore,
            clock = fixedClock
        ).import(tempFile, originalFileName = "odyssey.epub")

        assertTrue(result.isSuccess)
        assertEquals(41L, result.getOrThrow())
        assertFalse(tempFile.exists())
        assertEquals(1, publicationInspector.inspectedFiles.size)
        assertEquals(1, capturedBooks.size)

        val stored = capturedBooks.single()
        assertEquals("The Odyssey", stored.title)
        assertEquals("Homer", stored.author)
        assertEquals("urn:test:odyssey", stored.identifier)
        assertEquals("application/epub+zip", stored.mediaType)
        assertTrue(File(stored.bookPath).exists())
        assertNotNull(stored.coverPath)
        assertTrue(File(stored.coverPath!!).exists())
        assertNull(stored.progressionJson)
        assertNull(stored.progressionPercent)
        assertEquals(fixedClock.instant().toEpochMilli(), stored.importedAt)
        assertEquals(fixedClock.instant().toEpochMilli(), stored.lastOpenedAt)
    }

    @Test
    fun duplicateImportReusesExistingBookAndSkipsInspection() = runTest {
        val fileStore = newFileStore()
        val tempFile = newTempEpub("duplicate.epub", "same-content")
        val existing = BookEntity(
            id = 7L,
            contentHash = fileStore.computeContentHash(tempFile),
            title = "Existing",
            author = "Author",
            identifier = "existing-id",
            mediaType = "application/epub+zip",
            bookPath = "existing.epub",
            coverPath = null,
            progressionJson = null,
            progressionPercent = null,
            importedAt = 1L,
            lastOpenedAt = 1L
        )
        val bookStore = FakeBookMutationStore(existingByHash = existing)
        val publicationInspector = FakePublicationInspector(
            PublicationInfo(
                mediaType = "application/epub+zip",
                title = "Should not open",
                author = "No one",
                identifier = "unused",
                coverBytes = null
            )
        )

        val result = ImportCopiedBookUseCase(
            fileStore = fileStore,
            publicationInspector = publicationInspector,
            bookStore = bookStore,
            clock = fixedClock
        ).import(tempFile, originalFileName = "duplicate.epub")

        assertTrue(result.isSuccess)
        assertEquals(7L, result.getOrThrow())
        assertFalse(tempFile.exists())
        assertTrue(publicationInspector.inspectedFiles.isEmpty())
        assertEquals(listOf(7L to fixedClock.instant().toEpochMilli()), bookStore.updatedLastOpenedAt)
        assertTrue(bookStore.insertedBooks.isEmpty())
    }

    @Test
    fun missingTitleFallsBackToFilenameAndBlankAuthorBecomesNull() = runTest {
        val fileStore = newFileStore()
        val tempFile = newTempEpub("fallback-title.epub", "fallback-content")
        val bookStore = FakeBookMutationStore()
        val publicationInspector = FakePublicationInspector(
            PublicationInfo(
                mediaType = "application/epub+zip",
                title = " ",
                author = " ",
                identifier = "",
                coverBytes = null
            )
        )

        val result = ImportCopiedBookUseCase(
            fileStore = fileStore,
            publicationInspector = publicationInspector,
            bookStore = bookStore,
            clock = fixedClock
        ).import(tempFile, originalFileName = "fallback-title.epub")

        assertTrue(result.isSuccess)

        val stored = bookStore.insertedBooks.single()
        assertEquals("fallback-title", stored.title)
        assertNull(stored.author)
        assertEquals(stored.contentHash, stored.identifier)
        assertNull(stored.coverPath)
    }

    private fun newFileStore(): LocalLibraryFileStore =
        LocalLibraryFileStore(
            libraryRootDir = temporaryFolder.newFolder("library"),
            importCacheDir = temporaryFolder.newFolder("imports")
        )

    private fun newTempEpub(name: String, content: String): File =
        temporaryFolder.newFile(name).apply {
            writeText(content)
        }
}

internal class FakePublicationInspector(
    private val info: PublicationInfo
) : PublicationInspector {
    val inspectedFiles = mutableListOf<File>()

    override suspend fun inspectEpub(file: File): Result<PublicationInfo> {
        inspectedFiles += file
        return Result.success(info)
    }
}

internal class FakeBookMutationStore(
    existingByHash: BookEntity? = null,
    private val onInsert: (BookEntity) -> Long = { 1L }
) : BookMutationStore {
    private var storedExisting: BookEntity? = existingByHash
    val insertedBooks = mutableListOf<BookEntity>()
    val updatedLastOpenedAt = mutableListOf<Pair<Long, Long>>()
    val deletedIds = mutableListOf<Long>()

    override suspend fun findByContentHash(contentHash: String): BookEntity? =
        storedExisting?.takeIf { it.contentHash == contentHash }

    override suspend fun insert(book: BookEntity): Long {
        insertedBooks += book
        val insertedId = onInsert(book)
        storedExisting = book.copy(id = insertedId)
        return insertedId
    }

    override suspend fun updateLastOpenedAt(bookId: Long, timestamp: Long) {
        updatedLastOpenedAt += bookId to timestamp
    }

    override suspend fun delete(bookId: Long) {
        deletedIds += bookId
    }
}
