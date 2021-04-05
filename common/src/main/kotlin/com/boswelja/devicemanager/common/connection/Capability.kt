package com.boswelja.devicemanager.common.connection

import androidx.annotation.StringRes
import com.boswelja.devicemanager.common.R

/**
 * Definitions for different watch capabilities
 */
enum class Capability(
    val id: Short,
    @StringRes val label: Int
) {

    /**
     * Whether a node can manage it's own apps.
     */
    MANAGE_APPS(0x0001, R.string.capability_manage_apps),

    /**
     * Whether a node can read it's battery state.
     */
    SYNC_BATTERY(0x0002, R.string.capability_sync_battery),

    /**
     * Whether a node can read it's own DnD state.
     */
    SEND_DND(0x0004, R.string.capability_send_dnd),

    /**
     * Whether a node can update it's own DnD state.
     */
    RECEIVE_DND(0x0008, R.string.capability_receive_dnd)
}
