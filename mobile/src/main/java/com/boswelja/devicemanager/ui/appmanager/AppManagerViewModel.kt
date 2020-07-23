/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.common.appmanager.AppPackageInfoList
import com.boswelja.devicemanager.common.appmanager.References
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber
import java.io.InvalidClassException

class AppManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val messageClient = Wearable.getMessageClient(application)

    private val messageListener = MessageClient.OnMessageReceivedListener {
        Timber.i("Received a message")
        when (it.path) {
            References.GET_ALL_PACKAGES -> {
                Timber.i("Updating app list")
                try {
                    val appPackageInfoList = AppPackageInfoList.fromByteArray(it.data)
                    val allApps = separateAppListToSections(appPackageInfoList)
                    _allAppsList.postValue(allApps)
                } catch (e: InvalidClassException) {
                    // createSnackBar(getString(R.string.app_manager_version_mismatch))
                }
            }
            References.PACKAGE_ADDED -> {
                val appPackageInfo = AppPackageInfo.fromByteArray(it.data)
                _appAdded.postValue(appPackageInfo)
            }
            References.PACKAGE_UPDATED -> {
                val appPackageInfo = AppPackageInfo.fromByteArray(it.data)
                _appUpdated.postValue(appPackageInfo)
            }
            References.PACKAGE_REMOVED -> {
                val appPackageName = String(it.data, Charsets.UTF_8)
            }
        }
    }

    private val _allAppsList = MutableLiveData<ArrayList<Pair<String, ArrayList<AppPackageInfo>>>?>(null)
    val allAppsList: LiveData<ArrayList<Pair<String, ArrayList<AppPackageInfo>>>?>
        get() = _allAppsList

    private val _appAdded = MutableLiveData<AppPackageInfo?>(null)
    val appAdded: LiveData<AppPackageInfo?>
        get() = _appAdded

    private val _appUpdated = MutableLiveData<AppPackageInfo?>(null)
    val appUpdated: LiveData<AppPackageInfo?>
        get() = _appUpdated

    private val _appRemoved = MutableLiveData<String?>(null)
    val appRemoved: LiveData<String?>
        get() = _appRemoved

    private var isServiceRunning: Boolean = false

    var canStopAppManagerService: Boolean = true
    var watchId: String? = null

    init {
        messageClient.addListener(messageListener)
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(messageListener)
    }

    /**
     * Start the App Manager service on the connected watch.
     */
    fun startAppManagerService() {
        Timber.i("startAppManagerService() called")
        if (!isServiceRunning) {
            Timber.i("Trying to start App Manager service")
            isServiceRunning = true
            messageClient.sendMessage(watchId!!, References.START_SERVICE, null)
        }
    }

    /**
     * Stop the App Manager service on the connected watch.
     */
    fun tryStopAppManagerService() {
        Timber.i("stopAppManagerService() called")
        if (isServiceRunning && canStopAppManagerService) {
            Timber.i("Trying to stop App Manager service")
            isServiceRunning = false
            messageClient.sendMessage(watchId!!, References.STOP_SERVICE, null)
        }
    }

    fun addMessageListener(messageListener: MessageClient.OnMessageReceivedListener) {
        messageClient.addListener(messageListener)
    }

    fun removeMessageListener(messageListener: MessageClient.OnMessageReceivedListener) {
        messageClient.removeListener(messageListener)
    }

    /**
     * Request uninstalling an app from the connected watch.
     * @param appPackageInfo The [AppPackageInfo] to request an uninstall for.
     */
    fun sendUninstallRequestMessage(appPackageInfo: AppPackageInfo) {
        messageClient.sendMessage(
            watchId!!, References.REQUEST_UNINSTALL_PACKAGE,
            appPackageInfo.packageName.toByteArray(Charsets.UTF_8)
        )
    }

    /**
     * Request opening an app's launch activity on the connected watch.
     * @param appPackageInfo The [AppPackageInfo] to open the launch activity for.
     */
    fun sendOpenRequestMessage(appPackageInfo: AppPackageInfo) {
        messageClient.sendMessage(
            watchId!!, References.REQUEST_OPEN_PACKAGE,
            appPackageInfo.packageName.toByteArray(Charsets.UTF_8)
        )
    }

    fun onAppAddHandled() = _appAdded.postValue(null)
    fun onAppUpdateHandled() = _appUpdated.postValue(null)
    fun onAppRemoveHandled() = _appRemoved.postValue(null)

    /**
     * Converts an [AppPackageInfoList] into an [ArrayList] that can be used by [AppsAdapter].
     * @param appPackageInfoList The [AppPackageInfoList] to convert.
     * @return The newly created [ArrayList].
     */
    private fun separateAppListToSections(appPackageInfoList: AppPackageInfoList):
        ArrayList<Pair<String, ArrayList<AppPackageInfo>>> {
            val data = ArrayList<Pair<String, ArrayList<AppPackageInfo>>>()
            val userApps = ArrayList<AppPackageInfo>()
            userApps.addAll(appPackageInfoList.filterNot { it.isSystemApp })
            Pair(getApplication<Application>().getString(R.string.app_manager_section_user_apps), userApps).also {
                data.add(it)
            }
            val systemApps = ArrayList<AppPackageInfo>()
            systemApps.addAll(appPackageInfoList.filter { it.isSystemApp })
            Pair(getApplication<Application>().getString(R.string.app_manager_section_system_apps), systemApps).also {
                data.add(it)
            }
            return data
        }
}
