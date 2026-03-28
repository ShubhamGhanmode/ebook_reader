package com.shubhamghanmode.inkfold.data.book

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProgressionPersistenceTest {
    @Test
    fun locatorJsonRoundTripsAndKeepsTotalProgression() {
        val locator = Locator(
            href = Url("chapter-1.xhtml")!!,
            mediaType = MediaType("application/xhtml+xml")!!,
            title = "Chapter 1",
            locations = Locator.Locations(
                progression = 0.2,
                position = 12,
                totalProgression = 0.65
            )
        )

        val restored = Locator.fromJSON(JSONObject(locator.toJSON().toString()))

        assertNotNull(restored)
        assertEquals("chapter-1.xhtml", restored!!.href.toString())
        assertEquals(12, restored.locations.position)
        assertEquals(0.65f, ProgressionNormalizer.fromLocator(restored) ?: -1f, 0.0001f)
    }

    @Test
    fun normalizerFallsBackToResourceProgressionWhenTotalProgressionIsMissing() {
        val locator = Locator(
            href = Url("chapter-2.xhtml")!!,
            mediaType = MediaType("application/xhtml+xml")!!,
            locations = Locator.Locations(
                progression = 0.42,
                totalProgression = null
            )
        )

        assertEquals(0.42f, ProgressionNormalizer.fromLocator(locator) ?: -1f, 0.0001f)
    }
}
