package com.shubhamghanmode.inkfold.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

@Composable
fun InkFoldTheme(
    themePreset: AppThemePreset = AppThemePreset.CLASSIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = themePreset.colorScheme(darkTheme)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity() ?: return@SideEffect
            activity.syncInkFoldWindow(colorScheme = colorScheme, darkTheme = darkTheme, view = view)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

@Suppress("DEPRECATION")
private fun Activity.syncInkFoldWindow(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
    view: android.view.View
) {
    val backgroundColor = colorScheme.background.toArgb()
    val window = window

    window.setBackgroundDrawable(ColorDrawable(backgroundColor))
    window.navigationBarColor = backgroundColor
    WindowCompat.getInsetsController(window, view).apply {
        isAppearanceLightStatusBars = !darkTheme
        isAppearanceLightNavigationBars = !darkTheme
    }
}
