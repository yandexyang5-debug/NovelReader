package com.novelreader

import android.app.Application
import com.novelreader.data.local.NovelDatabase

class NovelReaderApplication : Application() {
    val database by lazy { NovelDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
