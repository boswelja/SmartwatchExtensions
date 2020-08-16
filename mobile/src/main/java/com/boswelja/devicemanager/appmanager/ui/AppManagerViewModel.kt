/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.ui.adapter.Item
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
                    _allAppsList.postValue(appPackageInfoList)
                } catch (e: InvalidClassException) {
                    Timber.w("App list version mismatch")
                    // createSnackBar(getString(R.string.app_manager_version_mismatch))
                }
            }
            References.PACKAGE_ADDED -> {
                Timber.i("Adding app to list")
                val appPackageInfo = AppPackageInfo.fromByteArray(it.data)
                _allAppsList.value?.let { appList ->
                    appList.add(appPackageInfo)
                    _allAppsList.postValue(appList)
                }
            }
            References.PACKAGE_UPDATED -> {
                Timber.i("Updating app in list")
                val appPackageInfo = AppPackageInfo.fromByteArray(it.data)
                _allAppsList.value?.let { allApps ->
                    allApps.removeAll { app -> app.packageName == appPackageInfo.packageName }
                    allApps.add(appPackageInfo)
                    _allAppsList.postValue(allApps)
                }
            }
            References.PACKAGE_REMOVED -> {
                Timber.i("Removing app from list")
                val appPackageName = String(it.data, Charsets.UTF_8)
                val appList = _allAppsList.value
                appList?.let { list ->
                    list.removeAll { app -> app.packageName == appPackageName }
                    _allAppsList.postValue(list)
                }
            }
        }
    }

    private val _allAppsList = MutableLiveData<AppPackageInfoList?>(null)
    val allAppsList: LiveData<AppPackageInfoList?>
        get() = _allAppsList

    val adapterList: LiveData<List<Item>?> = Transformations.map(allAppsList) {
        if (it != null)
            separateAppListToSections(it)
        else
            null
    }

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

    fun getAppDetails(appInfo: Item.App): AppPackageInfo? {
        return _allAppsList.value?.first { it.packageName == appInfo.packageName }
    }

    /**
     * Converts an [AppPackageInfoList] into an [ArrayList] that can be used by [AppsAdapter].
     * @param appPackageInfoList The [AppPackageInfoList] to convert.
     * @return The newly created [ArrayList].
     */
    private fun separateAppListToSections(appPackageInfoList: AppPackageInfoList): List<Item> {
        val context = getApplication<Application>()
        val result = ArrayList<Item>()
        val userApps = appPackageInfoList.filterNot { it.isSystemApp }.sortedBy { it.packageLabel }
        result.add(Item.Header(context.getString(R.string.app_manager_section_user_apps), 0.toString()))
        result.addAll(
            userApps.map {
                Item.App(it.packageIcon.bitmap, it.packageName, it.packageLabel, it.versionName ?: it.versionCode.toString())
            }
        )

        val systemApps = appPackageInfoList.filter { it.isSystemApp }.sortedBy { it.packageLabel }
        result.add(Item.Header(context.getString(R.string.app_manager_section_system_apps), 1.toString()))
        result.addAll(
            systemApps.map {
                Item.App(it.packageIcon.bitmap, it.packageName, it.packageLabel, it.versionName ?: it.versionCode.toString())
            }
        )

        return result
    }
}
