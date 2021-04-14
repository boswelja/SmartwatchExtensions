package com.boswelja.smartwatchextensions.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.boswelja.smartwatchextensions.appmanager.AppManager
import com.boswelja.smartwatchextensions.appmanager.State
import com.boswelja.smartwatchextensions.common.appmanager.App
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import java.text.SimpleDateFormat
import java.util.Locale

class AppManagerViewModel internal constructor(
    application: Application,
    private val appManager: AppManager,
    val watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        AppManager(application),
        WatchManager.getInstance(application)
    )

    private val dateFormatter = SimpleDateFormat("EE, dd MMM yyyy, h:mm aa", Locale.getDefault())

    val state: LiveData<State>
        get() = appManager.state

    val userApps: LiveData<List<App>>
        get() = appManager.userApps

    val systemApps: LiveData<List<App>>
        get() = appManager.systemApps

    val progress: LiveData<Int>
        get() = appManager.progress

    /**
     * Format a given date in milliseconds to the correct formet for display.
     * @param dateMillis The date in milliseconds to convert.
     * @return The formatted date string.
     */
    fun formatDate(dateMillis: Long): String = dateFormatter.format(dateMillis)

    fun startAppManagerService() = appManager.startAppManagerService()

    fun sendOpenRequest(app: App) = appManager.sendOpenRequestMessage(app)

    fun sendUninstallRequest(app: App) = appManager.sendUninstallRequestMessage(app)

    override fun onCleared() {
        super.onCleared()
        appManager.destroy()
    }
}
