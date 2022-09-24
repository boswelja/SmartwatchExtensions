package com.boswelja.smartwatchextensions.capability

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.appmanager.ManageAppsCapability
import com.boswelja.smartwatchextensions.batterysync.SyncBatteryStatus
import com.boswelja.smartwatchextensions.dndsync.SendDnDCapability
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient

/**
 * A class for handling adding and removing local capabilities based on what permissions the watch
 * has.
 */
class CapabilityUpdater(
    private val context: Context,
    private val capabilityClient: DiscoveryClient
) {

    /**
     * Update all capabilities.
     */
    suspend fun updateCapabilities() {
        updateSendDnD()
        updateSendBattery()
        updateManageApps()
    }

    /**
     * Update [SendDnDCapability].
     */
    internal suspend fun updateSendDnD() {
        // We can always read DnD state
        capabilityClient.addLocalCapability(SendDnDCapability)
    }

    /**
     * Update [SyncBatteryStatus].
     */
    internal suspend fun updateSendBattery() {
        // We can always get battery stats
        capabilityClient.addLocalCapability(SyncBatteryStatus)
    }

    /**
     * Update [ManageAppsCapability].
     */
    internal suspend fun updateManageApps() {
        // QUERY_ALL_APPS should be granted automatically upon app install, so we only check if it's
        // been granted.
        if (canQueryAllPackages()) {
            capabilityClient.addLocalCapability(ManageAppsCapability)
        } else {
            capabilityClient.removeLocalCapability(ManageAppsCapability)
        }
    }

    internal fun canQueryAllPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.checkSelfPermission(Manifest.permission.QUERY_ALL_PACKAGES) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
