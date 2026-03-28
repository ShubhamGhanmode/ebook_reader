package com.shubhamghanmode.inkfold

import android.content.Context
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser

class ReadiumServices(context: Context) {
    val httpClient = DefaultHttpClient()

    val assetRetriever = AssetRetriever(
        contentResolver = context.contentResolver,
        httpClient = httpClient
    )

    val publicationOpener = PublicationOpener(
        publicationParser = DefaultPublicationParser(
            context = context,
            assetRetriever = assetRetriever,
            httpClient = httpClient,
            pdfFactory = null
        )
    )
}
