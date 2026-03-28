package com.shubhamghanmode.inkfold

import android.app.Application

class InkFoldApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}
