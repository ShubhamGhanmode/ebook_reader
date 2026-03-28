package com.shubhamghanmode.inkfold.data.book

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

class LocalLibraryFileStore(
    private val libraryRootDir: File,
    private val importCacheDir: File
) {
    private val booksDir: File = File(libraryRootDir, "books")
    private val coversDir: File = File(libraryRootDir, "covers")

    init {
        booksDir.mkdirs()
        coversDir.mkdirs()
        importCacheDir.mkdirs()
    }

    fun createTempImportFile(displayName: String?): File {
        val extension = displayName
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotBlank() }
            ?: "epub"
        return File.createTempFile("inkfold-import-", ".$extension", importCacheDir)
    }

    fun writeStreamToTempFile(inputStream: InputStream, displayName: String?): File {
        val tempFile = createTempImportFile(displayName)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
    }

    fun computeContentHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) {
                    break
                }
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }

    fun moveIntoLibrary(tempFile: File, contentHash: String): File {
        val finalFile = File(booksDir, "$contentHash.epub")
        tempFile.copyTo(target = finalFile, overwrite = true)
        tempFile.delete()
        return finalFile
    }

    fun writeCover(contentHash: String, coverBytes: ByteArray?): String? {
        if (coverBytes == null) {
            return null
        }

        val coverFile = File(coversDir, "$contentHash.png")
        coverFile.outputStream().use { outputStream ->
            outputStream.write(coverBytes)
        }
        return coverFile.absolutePath
    }

    fun deleteBookFiles(book: BookEntity) {
        File(book.bookPath).delete()
        book.coverPath?.let { coverPath ->
            File(coverPath).delete()
        }
    }
}
