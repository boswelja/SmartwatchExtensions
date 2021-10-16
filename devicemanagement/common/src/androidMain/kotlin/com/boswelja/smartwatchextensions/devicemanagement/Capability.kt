package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.smartwatchextensions.watchmanager.common.R

/**
 * Get the label string resource associated with this capability.
 */
fun Capability.labelRes(): Int {
    return when (this) {
        Capability.MANAGE_APPS -> R.string.capability_manage_apps
        Capability.SYNC_BATTERY -> R.string.capability_sync_battery
        Capability.SEND_DND -> R.string.capability_send_dnd
        Capability.RECEIVE_DND -> R.string.capability_receive_dnd
    }
}
