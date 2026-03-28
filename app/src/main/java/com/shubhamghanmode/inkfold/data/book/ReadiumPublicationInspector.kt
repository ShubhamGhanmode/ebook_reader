package com.shubhamghanmode.inkfold.data.book

import android.graphics.Bitmap
import com.shubhamghanmode.inkfold.ReadiumServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.allAreHtml
import org.readium.r2.shared.publication.services.cover
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.format.Specification

class ReadiumPublicationInspector(
    private val readiumServices: ReadiumServices
) : PublicationInspector {
    override suspend fun inspectEpub(file: File): Result<PublicationInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val format = readiumServices.assetRetriever
                    .sniffFormat(file)
                    .getOrElse {
                        throw IllegalArgumentException("InkFold could not recognize that file as an EPUB.")
                    }

                if (!format.conformsTo(Specification.Epub)) {
                    throw IllegalArgumentException("InkFold currently supports EPUB files only.")
                }

                val asset = readiumServices.assetRetriever
                    .retrieve(file, format.mediaType)
                    .getOrElse {
                        throw IllegalArgumentException("InkFold could not read that EPUB.")
                    }

                val publication = readiumServices.publicationOpener
                    .open(asset, allowUserInteraction = false)
                    .getOrElse {
                        throw IllegalArgumentException("InkFold could not open that EPUB.")
                    }

                try {
                    if (!publication.conformsTo(Publication.Profile.EPUB) && !publication.readingOrder.allAreHtml) {
                        throw IllegalArgumentException("InkFold currently supports EPUB files only.")
                    }

                    val title: String? = publication.metadata.title?.takeIf(String::isNotBlank)
                    val author: String? = publication.metadata.authors
                        .firstOrNull()
                        ?.name
                        ?.takeIf(String::isNotBlank)
                    val identifier: String? = publication.metadata.identifier?.takeIf(String::isNotBlank)
                    val coverBytes: ByteArray? = publication.cover()?.toPngBytes()

                    PublicationInfo(
                        mediaType = format.mediaType.toString(),
                        title = title,
                        author = author,
                        identifier = identifier,
                        coverBytes = coverBytes
                    )
                } finally {
                    publication.close()
                }
            }
        }

    private fun Bitmap.toPngBytes(): ByteArray {
        val byteStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, byteStream)
        return byteStream.toByteArray()
    }
}
