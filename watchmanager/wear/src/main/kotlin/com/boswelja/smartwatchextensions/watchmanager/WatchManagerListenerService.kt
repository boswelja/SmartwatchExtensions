package com.boswelja.smartwatchextensions.watchmanager

import android.app.ActivityManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import com.boswelja.smartwatchextensions.common.RequestAppVersion
import com.boswelja.smartwatchextensions.common.RequestResetApp
import com.boswelja.smartwatchextensions.common.Version
import com.boswelja.smartwatchextensions.common.VersionSerializer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

class WatchManagerListenerService : WearableListenerService() {

    private val messageClient = Wearable.getMessageClient(this)

    override fun onMessageReceived(message: MessageEvent) {
        when (message.path) {
            RequestResetApp -> tryResetApp()
            RequestAppVersion -> sendVersionInfoTo(message.sourceNodeId)
        }
    }

    private fun tryResetApp() {

        val activityManager = getSystemService<ActivityManager>()!!
        activityManager.clearApplicationUserData()
    }

    private fun sendVersionInfoTo(targetId: String) {
        val packageInfo = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(
                packageName,
                0
            )
        } else {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        }

        val version = Version(PackageInfoCompat.getLongVersionCode(packageInfo), packageInfo.versionName!!)
        messageClient.sendMessage(
            targetId,
            RequestAppVersion,
            VersionSerializer.serialize(version)
        )
    }
}
