package com.shubhamghanmode.inkfold

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shubhamghanmode.inkfold.feature.importer.ImportActivity
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EpubIntentResolutionTest {
    @Test
    fun epubViewIntentResolvesInkFoldImporter() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addCategory(Intent.CATEGORY_BROWSABLE)
            setDataAndType(
                Uri.parse("content://com.example.provider/books/sample.epub"),
                "application/epub+zip"
            )
        }

        val matches = context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        assertTrue(
            matches.any { resolveInfo ->
                resolveInfo.activityInfo.packageName == context.packageName &&
                    resolveInfo.activityInfo.name == ImportActivity::class.java.name
            }
        )
    }
}
