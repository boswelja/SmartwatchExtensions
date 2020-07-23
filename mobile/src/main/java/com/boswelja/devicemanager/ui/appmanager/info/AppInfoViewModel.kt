/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager.info

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import java.text.SimpleDateFormat
import java.util.Locale

class AppInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val dateFormatter = SimpleDateFormat("EE, dd MMM yyyy, h:mm aa", Locale.getDefault())

    val appInfo = MutableLiveData<AppPackageInfo>()

    val appName = Transformations.map(appInfo) { it.packageLabel }

    val canOpen = Transformations.map(appInfo) { it.hasLaunchActivity }
    val canUninstall = Transformations.map(appInfo) {
        (it.packageName != BuildConfig.APPLICATION_ID) && !it.isSystemApp
    }

    val shouldShowInstallTime = Transformations.map(appInfo) { !it.isSystemApp }
    val shouldShowLastUpdateTime = Transformations.map(appInfo) {
        it.installTime == it.lastUpdateTime && !it.isSystemApp
    }
    val installTime = Transformations.map(appInfo) {
        dateFormatter.format(it.installTime)
    }
    val lastUpdateTime = Transformations.map(appInfo) {
        dateFormatter.format(it.lastUpdateTime)
    }

    val versionText = Transformations.map(appInfo) {
        if (it.versionName.isNullOrBlank()) {
            it.versionCode.toString()
        } else {
            it.versionName
        }
    }

    val appIcon = Transformations.map(appInfo) {
        it.packageIcon.bitmap
    }
}
