package com.shubhamghanmode.inkfold.data.book

import java.io.File
import java.time.Clock

class ImportCopiedBookUseCase(
    private val fileStore: LocalLibraryFileStore,
    private val publicationInspector: PublicationInspector,
    private val bookStore: BookMutationStore,
    private val clock: Clock
) {
    suspend fun import(tempFile: File, originalFileName: String?): Result<Long> {
        val importedAt = clock.instant().toEpochMilli()
        val contentHash = fileStore.computeContentHash(tempFile)

        val existing = bookStore.findByContentHash(contentHash)
        if (existing != null) {
            tempFile.delete()
            bookStore.updateLastOpenedAt(existing.id, importedAt)
            return Result.success(existing.id)
        }

        val publicationInfo = publicationInspector.inspectEpub(tempFile).getOrElse { error ->
            tempFile.delete()
            return Result.failure(error)
        }

        val managedBookFile = runCatching {
            fileStore.moveIntoLibrary(tempFile, contentHash)
        }.getOrElse { error ->
            tempFile.delete()
            return Result.failure(error)
        }

        val coverPath = runCatching {
            fileStore.writeCover(contentHash, publicationInfo.coverBytes)
        }.getOrElse { error ->
            managedBookFile.delete()
            return Result.failure(error)
        }

        val book = BookEntity(
            contentHash = contentHash,
            title = resolveTitle(
                explicitTitle = publicationInfo.title,
                originalFileName = originalFileName,
                contentHash = contentHash
            ),
            author = publicationInfo.author?.takeIf { it.isNotBlank() },
            identifier = publicationInfo.identifier?.takeIf { it.isNotBlank() } ?: contentHash,
            mediaType = publicationInfo.mediaType,
            bookPath = managedBookFile.absolutePath,
            coverPath = coverPath,
            progressionJson = null,
            progressionPercent = null,
            importedAt = importedAt,
            lastOpenedAt = importedAt
        )

        return runCatching {
            bookStore.insert(book)
        }.onFailure {
            managedBookFile.delete()
            coverPath?.let(::File)?.delete()
        }
    }

    internal fun resolveTitle(
        explicitTitle: String?,
        originalFileName: String?,
        contentHash: String
    ): String =
        explicitTitle?.takeIf { it.isNotBlank() }
            ?: originalFileName
                ?.substringBeforeLast('.')
                ?.takeIf { it.isNotBlank() }
            ?: contentHash
}
