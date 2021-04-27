package com.boswelja.smartwatchextensions.capability

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

/**
 * A class for handling adding and removing local capabilities based on what permissions the watch
 * has.
 */
class CapabilityUpdater(
    private val context: Context,
    private val capabilityClient: CapabilityClient
) {
    constructor(context: Context) : this(
        context,
        Wearable.getCapabilityClient(context)
    )

    /**
     * Update all capabilities.
     */
    fun updateCapabilities() {
        Timber.d("Updating capabilities")
        updateSendDnD()
        updateReceiveDnD()
        updateSendBattery()
        updateManageApps()
    }

    /**
     * Update [Capability.SEND_DND].
     */
    internal fun updateSendDnD() {
        // We can always read DnD state
        capabilityClient.addLocalCapability(Capability.SEND_DND.name)
    }

    /**
     * Update [Capability.RECEIVE_DND].
     */
    internal fun updateReceiveDnD() {
        // Either the watch is capable of granting ACCESS_NOTIFICATION_POLICY (via older SDKs), or
        // it's already granted.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || hasNotiPolicyAccess()) {
            capabilityClient.addLocalCapability(Capability.RECEIVE_DND.name)
        } else {
            capabilityClient.removeLocalCapability(Capability.RECEIVE_DND.name)
        }
    }

    /**
     * Update [Capability.SYNC_BATTERY].
     */
    internal fun updateSendBattery() {
        // We can always get battery stats
        capabilityClient.addLocalCapability(Capability.SYNC_BATTERY.name)
    }

    /**
     * Update [Capability.MANAGE_APPS].
     */
    internal fun updateManageApps() {
        // QUERY_ALL_APPS should be granted automatically upon app install, so we only check if it's
        // been granted.
        if (canQueryAllPackages()) {
            capabilityClient.addLocalCapability(Capability.MANAGE_APPS.name)
        } else {
            capabilityClient.removeLocalCapability(Capability.MANAGE_APPS.name)
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
