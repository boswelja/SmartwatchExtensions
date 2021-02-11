package com.boswelja.devicemanager.common.connection

/**
 * Definitions for different watch capabilities
 */
object Capability {

    /**
     * Whether a node can manage it's own apps.
     */
    const val MANAGE_APPS = "manage_apps"

    /**
     * Whether a node can read it's battery state.
     */
    const val SYNC_BATTERY = "sync_battery"

    /**
     * Whether a node can read it's own DnD state.
     */
    const val SEND_DND = "send_dnd"

    /**
     * Whether a node can update it's own DnD state.
     */
    const val RECEIVE_DND = "receive_dnd"
}
