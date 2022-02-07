package com.boswelja.smartwatchextensions.capability

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.appmanager.MANAGE_APPS_CAPABILITY
import com.boswelja.smartwatchextensions.batterysync.SYNC_BATTERY_CAPABILITY
import com.boswelja.smartwatchextensions.dndsync.RECEIVE_DND_CAPABILITY
import com.boswelja.smartwatchextensions.dndsync.SEND_DND_CAPABILITY
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
        updateReceiveDnD()
        updateSendBattery()
        updateManageApps()
    }

    /**
     * Update [SEND_DND_CAPABILITY].
     */
    internal suspend fun updateSendDnD() {
        // We can always read DnD state
        capabilityClient.addLocalCapability(SEND_DND_CAPABILITY)
    }

    /**
     * Update [RECEIVE_DND_CAPABILITY].
     */
    internal suspend fun updateReceiveDnD() {
        // Either the watch is capable of granting ACCESS_NOTIFICATION_POLICY (via older SDKs), or
        // it's already granted.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || hasNotiPolicyAccess()) {
            capabilityClient.addLocalCapability(RECEIVE_DND_CAPABILITY)
        } else {
            capabilityClient.removeLocalCapability(RECEIVE_DND_CAPABILITY)
        }
    }

    /**
     * Update [SYNC_BATTERY_CAPABILITY].
     */
    internal suspend fun updateSendBattery() {
        // We can always get battery stats
        capabilityClient.addLocalCapability(SYNC_BATTERY_CAPABILITY)
    }

    /**
     * Update [MANAGE_APPS_CAPABILITY].
     */
    internal suspend fun updateManageApps() {
        // QUERY_ALL_APPS should be granted automatically upon app install, so we only check if it's
        // been granted.
        if (canQueryAllPackages()) {
            capabilityClient.addLocalCapability(MANAGE_APPS_CAPABILITY)
        } else {
            capabilityClient.removeLocalCapability(MANAGE_APPS_CAPABILITY)
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

    internal fun hasNotiPolicyAccess(): Boolean {
        return context.getSystemService<NotificationManager>()
            ?.isNotificationPolicyAccessGranted == true
    }
}
