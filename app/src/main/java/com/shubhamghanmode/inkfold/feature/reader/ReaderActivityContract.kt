package com.shubhamghanmode.inkfold.feature.reader

import android.content.Context
import android.content.Intent

object ReaderActivityContract {
    private const val EXTRA_BOOK_ID = "com.shubhamghanmode.inkfold.BOOK_ID"

    fun createIntent(context: Context, bookId: Long): Intent =
        Intent(context, ReaderActivity::class.java).apply {
            putExtra(EXTRA_BOOK_ID, bookId)
        }

    fun parseBookId(intent: Intent): Long =
        intent.getLongExtra(EXTRA_BOOK_ID, 0L)
}
