package com.swordfish.lemuroid.app.shared.startup

import android.content.Context
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.app.shared.library.LibraryIndexScheduler
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import timber.log.Timber

class MainProcessInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Timber.i("Requested initialization of main process tasks")
        SaveSyncWork.enqueueAutoWork(context, 0)
        LibraryIndexScheduler.scheduleCoreUpdate(context)
        scheduleLibraryScanAfterUpgrade(context)
    }

    private fun scheduleLibraryScanAfterUpgrade(context: Context) {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        if (preferences.getInt(LAST_SCAN_VERSION, -1) == BuildConfig.VERSION_CODE) return

        LibraryIndexScheduler.scheduleLibrarySync(context)
        preferences.edit().putInt(LAST_SCAN_VERSION, BuildConfig.VERSION_CODE).apply()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java, DebugInitializer::class.java)
    }

    companion object {
        private const val PREFERENCES = "opengba_startup"
        private const val LAST_SCAN_VERSION = "last_scan_version"
    }
}
