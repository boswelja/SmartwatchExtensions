package com.boswelja.devicemanager.common.connection

/**
 * Definitions for different watch capabilities
 */
enum class Capability(val id: Short) {

    /**
     * Whether a node can manage it's own apps.
     */
    MANAGE_APPS(0x0001),

    /**
     * Whether a node can read it's battery state.
     */
    SYNC_BATTERY(0x0002),

    /**
     * Whether a node can read it's own DnD state.
     */
    SEND_DND(0x0004),

    /**
     * Whether a node can update it's own DnD state.
     */
    RECEIVE_DND(0x0008)
}
