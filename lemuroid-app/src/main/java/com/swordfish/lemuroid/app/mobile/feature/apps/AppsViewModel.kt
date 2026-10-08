package com.swordfish.lemuroid.app.mobile.feature.apps

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

class AppsViewModel(context: Context) : ViewModel() {
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppsViewModel(context.applicationContext) as T
        }
    }

    val apps: StateFlow<List<LauncherApp>?> =
        flow {
            emit(queryApps(context.applicationContext))
        }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private fun queryApps(context: Context): List<LauncherApp> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager
            .queryIntentActivities(intent, 0)
            .asSequence()
            .mapNotNull { it.activityInfo?.applicationInfo }
            .filter { !isSameAppFamily(it.packageName, context.packageName) }
            .distinctBy { it.packageName }
            .map { appInfo ->
                LauncherApp(
                    packageName = appInfo.packageName,
                    label = appInfo.loadLabel(packageManager).toString(),
                    icon = appInfo.loadIcon(packageManager),
                )
            }
            .sortedWith(compareBy { it.label.lowercase(Locale.getDefault()) })
            .toList()
    }

    // Debug builds append an applicationId suffix, so multiple variants of the
    // same app can be installed side by side. Treat them as a single app.
    private fun isSameAppFamily(
        a: String,
        b: String,
    ): Boolean = a == b || a.startsWith("$b.") || b.startsWith("$a.")
}
