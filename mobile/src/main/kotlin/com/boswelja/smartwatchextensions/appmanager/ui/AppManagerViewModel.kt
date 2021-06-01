package com.boswelja.smartwatchextensions.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_OPEN_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Status
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

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

    val allApps = watchManager.selectedWatch.switchMap { watch ->
        watch?.let {
            appDatabase.apps().allForWatch(watch.id).asLiveData(Dispatchers.IO)
        } ?: liveData { emit(emptyList<App>()) }
    }

    val watchStatus = watchManager.selectedWatch.switchMap { watch ->
        watch?.let {
            watchManager.getStatusFor(watch)?.asLiveData(Dispatchers.IO)
        } ?: liveData { emit(Status.ERROR) }
    }

    val userApps = allApps.map { apps ->
        apps.filter { !it.isSystemApp }
    }

    val systemApps = allApps.map { apps ->
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
        return watchManager.sendMessage(selectedWatch.value!!, REQUEST_OPEN_PACKAGE, data)
    }

    suspend fun sendUninstallRequest(app: App): Boolean {
        val data = app.packageName.toByteArray(Charsets.UTF_8)
        return watchManager.sendMessage(selectedWatch.value!!, REQUEST_UNINSTALL_PACKAGE, data)
    }
}
