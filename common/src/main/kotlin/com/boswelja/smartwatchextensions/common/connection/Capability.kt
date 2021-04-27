package com.boswelja.smartwatchextensions.common.connection

import androidx.annotation.StringRes
import com.boswelja.smartwatchextensions.common.R

/**
 * Definitions for different watch capabilities
 */
enum class Capability(
    @StringRes val label: Int
) {

    /**
     * Whether a node can manage it's own apps.
     */
    MANAGE_APPS(R.string.capability_manage_apps),

    /**
     * Whether a node can read it's battery state.
     */
    SYNC_BATTERY(R.string.capability_sync_battery),

    /**
     * Whether a node can read it's own DnD state.
     */
    SEND_DND(R.string.capability_send_dnd),

    /**
     * Whether a node can update it's own DnD state.
     */
    RECEIVE_DND(R.string.capability_receive_dnd)
}
