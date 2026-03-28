package com.shubhamghanmode.inkfold.data.book

import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DeleteBookUseCaseTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun deleteRemovesManagedFilesAndDatabaseRow() = runTest {
        val libraryRoot = temporaryFolder.newFolder("library")
        val importRoot = temporaryFolder.newFolder("imports")
        val fileStore = LocalLibraryFileStore(libraryRoot, importRoot)
        val bookStore = FakeBookMutationStore()
        val bookFile = File(libraryRoot, "books/book.epub").apply {
            parentFile?.mkdirs()
            writeText("book")
        }
        val coverFile = File(libraryRoot, "covers/book.png").apply {
            parentFile?.mkdirs()
            writeBytes(byteArrayOf(1, 2, 3))
        }
        val book = BookEntity(
            id = 99L,
            contentHash = "hash",
            title = "Delete Me",
            author = null,
            identifier = "hash",
            mediaType = "application/epub+zip",
            bookPath = bookFile.absolutePath,
            coverPath = coverFile.absolutePath,
            progressionJson = null,
            progressionPercent = null,
            importedAt = 1L,
            lastOpenedAt = 1L
        )

        val result = DeleteBookUseCase(
            fileStore = fileStore,
            bookStore = bookStore
        ).delete(book)

        assertEquals(Result.success(Unit), result)
        assertFalse(bookFile.exists())
        assertFalse(coverFile.exists())
        assertEquals(listOf(99L), bookStore.deletedIds)
    }
}
