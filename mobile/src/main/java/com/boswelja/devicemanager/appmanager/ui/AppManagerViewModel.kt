package com.boswelja.devicemanager.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.boswelja.devicemanager.appmanager.AppManager
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.appmanager.App

class AppManagerViewModel internal constructor(
    application: Application,
    private val appManager: AppManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(application, AppManager(application))

    val state: LiveData<State>
        get() = appManager.state

    val apps: LiveData<List<App>>
        get() = appManager.apps

    val progress: LiveData<Int>
        get() = appManager.progress

    var canStopAppManagerService: Boolean
        get() = appManager.canStopAppManagerService
        set(value) { appManager.canStopAppManagerService = value }

    override fun onCleared() {
        super.onCleared()
        appManager.destroy()
    }

    fun startAppManagerService() = appManager.startAppManagerService()
}
