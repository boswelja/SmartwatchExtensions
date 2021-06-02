package com.boswelja.smartwatchextensions.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_OPEN_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Status
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest

@ExperimentalCoroutinesApi
class AppManagerViewModel internal constructor(
    application: Application,
    private val appDatabase: WatchAppDatabase,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchAppDatabase.getInstance(application),
        WatchManager.getInstance(application)
    )

    private val dateFormatter = SimpleDateFormat("EE, dd MMM yyyy, h:mm aa", Locale.getDefault())

    val selectedWatch = watchManager.selectedWatch
    val registeredWatches = watchManager.registeredWatches

    val allApps = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            appDatabase.apps().allForWatch(watch.id)
        } ?: flow { emit(emptyList<App>()) }
    }

    val watchStatus = watchManager.selectedWatch.flatMapLatest { watch ->
        watch?.let {
            watchManager.getStatusFor(watch)
        } ?: flow { emit(Status.ERROR) }
    }

    val userApps = allApps.mapLatest { apps ->
        apps.filter { !it.isSystemApp }
    }

    val systemApps = allApps.mapLatest { apps ->
        apps.filter { it.isSystemApp }
    }

    /**
     * Format a given date in milliseconds to the correct formet for display.
     * @param dateMillis The date in milliseconds to convert.
     * @return The formatted date string.
     */
    fun formatDate(dateMillis: Long): String = dateFormatter.format(dateMillis)

    fun selectWatchById(watchId: UUID) = watchManager.selectWatchById(watchId)

    suspend fun sendOpenRequest(app: App): Boolean {
        val data = app.packageName.toByteArray(Charsets.UTF_8)
        val watch = selectedWatch.first()!!
        return watchManager.sendMessage(watch, REQUEST_OPEN_PACKAGE, data)
    }

    suspend fun sendUninstallRequest(app: App): Boolean {
        val data = app.packageName.toByteArray(Charsets.UTF_8)
        val watch = selectedWatch.first()!!
        return watchManager.sendMessage(watch, REQUEST_UNINSTALL_PACKAGE, data)
    }
}
