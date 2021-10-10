package com.boswelja.smartwatchextensions.watchmanager

/**
 * Definitions for different watch capabilities
 */
enum class Capability {

    /**
     * Whether a node can manage it's own apps.
     */
    MANAGE_APPS,

    /**
     * Whether a node can read it's battery state.
     */
    SYNC_BATTERY,

    /**
     * Whether a node can read it's own DnD state.
     */
    SEND_DND,

    /**
     * Whether a node can update it's own DnD state.
     */
    RECEIVE_DND
}
