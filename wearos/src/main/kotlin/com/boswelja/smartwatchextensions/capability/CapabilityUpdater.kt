package com.boswelja.smartwatchextensions.capability

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.boswelja.smartwatchextensions.appmanager.ManageAppsCapability
import com.boswelja.smartwatchextensions.batterysync.SyncBatteryStatus
import com.boswelja.smartwatchextensions.dndsync.SendDnDCapability
import com.google.android.gms.wearable.CapabilityClient

/**
 * A class for handling adding and removing local capabilities based on what permissions the watch
 * has.
 */
class CapabilityUpdater(
    private val context: Context,
    private val capabilityClient: CapabilityClient
) {

    /**
     * Update all capabilities.
     */
    fun updateCapabilities() {
        updateSendDnD()
        updateSendBattery()
        updateManageApps()
    }

    /**
     * Update [SendDnDCapability].
     */
    internal fun updateSendDnD() {
        // We can always read DnD state
        capabilityClient.addLocalCapability(SendDnDCapability)
    }

    /**
     * Update [SyncBatteryStatus].
     */
    internal fun updateSendBattery() {
        // We can always get battery stats
        capabilityClient.addLocalCapability(SyncBatteryStatus)
    }

    /**
     * Update [ManageAppsCapability].
     */
    internal fun updateManageApps() {
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
