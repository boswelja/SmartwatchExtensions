/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui.info

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.appmanager.Messages
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import java.text.SimpleDateFormat
import java.util.Locale

class AppInfoViewModel internal constructor(
    application: Application,
    private val messageClient: MessageClient
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) :
        this(application, Wearable.getMessageClient(application))

    private val dateFormatter = SimpleDateFormat("EE, dd MMM yyyy, h:mm aa", Locale.getDefault())

    private val _finishActivity = MutableLiveData(false)
    val finishActivity: LiveData<Boolean>
        get() = _finishActivity

    var watchId: String? = null

    val appInfo = MutableLiveData<App>()

    val appName = Transformations.map(appInfo) { it.packageLabel }

    val canOpen = Transformations.map(appInfo) { it.hasLaunchActivity }
    val canUninstall =
        Transformations.map(appInfo) {
            (it.packageName != BuildConfig.APPLICATION_ID) && !it.isSystemApp
        }

    val shouldShowInstallTime = Transformations.map(appInfo) { !it.isSystemApp }
    val shouldShowLastUpdateTime =
        Transformations.map(appInfo) { it.installTime != it.lastUpdateTime }
    val installTime = Transformations.map(appInfo) { dateFormatter.format(it.installTime) }
    val lastUpdateTime = Transformations.map(appInfo) { dateFormatter.format(it.lastUpdateTime) }

    val versionText =
        Transformations.map(appInfo) {
            if (it.versionName.isNullOrBlank()) {
                it.versionCode.toString()
            } else {
                it.versionName
            }
        }

    val appIcon = Transformations.map(appInfo) { it.packageIcon?.bitmap }

    /** Request uninstalling an app from the connected watch. */
    fun sendUninstallRequestMessage() {
        messageClient.sendMessage(
            watchId!!,
            Messages.REQUEST_UNINSTALL_PACKAGE,
            appInfo.value!!.packageName.toByteArray(Charsets.UTF_8)
        )
        _finishActivity.postValue(true)
    }

    /** Request opening an app's launch activity on the connected watch. */
    fun sendOpenRequestMessage() {
        messageClient.sendMessage(
            watchId!!,
            Messages.REQUEST_OPEN_PACKAGE,
            appInfo.value!!.packageName.toByteArray(Charsets.UTF_8)
        )
    }
}
