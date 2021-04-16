@file:Suppress("DEPRECATION")

package com.boswelja.devicemanager.bootorupdate.updater

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.appStateStore
import com.boswelja.devicemanager.phonelocking.PhoneLockingAccessibilityService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class Updater(private val context: Context) {

    private var lastAppVersion: Int = BuildConfig.VERSION_CODE

    suspend fun checkNeedsUpdate(): Boolean {
        lastAppVersion = context.appStateStore.data.map { it.lastAppVersion }.first()
        context.appStateStore.updateData {
            it.copy(lastAppVersion = BuildConfig.VERSION_CODE)
        }
        return lastAppVersion > 0 && lastAppVersion < BuildConfig.VERSION_CODE
    }

    /**
     * Update the app's working environment.
     * @return The [Result] of the update
     */
    fun doUpdate(): Result {
        var updateStatus = Result.NOT_NEEDED
        if (lastAppVersion < 2028500000) {
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, PhoneLockingAccessibilityService::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            updateStatus = Result.COMPLETED
        }
        return updateStatus
    }
}
