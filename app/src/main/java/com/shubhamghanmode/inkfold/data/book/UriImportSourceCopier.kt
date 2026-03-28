package com.shubhamghanmode.inkfold.data.book

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

data class CopiedImportSource(
    val tempFile: File,
    val displayName: String?
)

class UriImportSourceCopier(
    private val contentResolver: ContentResolver,
    private val fileStore: LocalLibraryFileStore
) {
    suspend fun copy(uri: Uri): Result<CopiedImportSource> =
        withContext(Dispatchers.IO) {
            var tempFile: File? = null

            runCatching {
                val displayName = resolveDisplayName(uri)
                val inputStream = openInputStream(uri)
                    ?: throw IllegalArgumentException("InkFold could not read the selected file.")

                inputStream.use { stream ->
                    tempFile = fileStore.writeStreamToTempFile(stream, displayName)
                }

                CopiedImportSource(
                    tempFile = checkNotNull(tempFile),
                    displayName = displayName
                )
            }.onFailure {
                tempFile?.delete()
            }
        }

    private fun openInputStream(uri: Uri) =
        when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> contentResolver.openInputStream(uri)
            ContentResolver.SCHEME_FILE -> {
                val path = uri.path ?: return null
                FileInputStream(File(path))
            }
            else -> null
        }

    private fun resolveDisplayName(uri: Uri): String? =
        when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> queryDisplayName(uri)
            ContentResolver.SCHEME_FILE -> uri.path?.let(::File)?.name
            else -> null
        }

    private fun queryDisplayName(uri: Uri): String? =
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                cursor.readSingleString(OpenableColumns.DISPLAY_NAME)
            }

    private fun Cursor.readSingleString(columnName: String): String? {
        val columnIndex = getColumnIndex(columnName)
        if (columnIndex == -1 || !moveToFirst()) {
            return null
        }
        return getString(columnIndex)
    }
}
