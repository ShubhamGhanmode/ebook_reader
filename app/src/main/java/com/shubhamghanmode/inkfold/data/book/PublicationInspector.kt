package com.shubhamghanmode.inkfold.data.book

import java.io.File

data class PublicationInfo(
    val mediaType: String,
    val title: String?,
    val author: String?,
    val identifier: String?,
    val coverBytes: ByteArray?
)

interface PublicationInspector {
    suspend fun inspectEpub(file: File): Result<PublicationInfo>
}
