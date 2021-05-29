package com.boswelja.smartwatchextensions.appmanager

/**
 * An enum of App Manager states.
 */
enum class State {

    /**
     * Disconnected from the selected watch.
     */
    STOPPED,

    /**
     * Connecting to the selected watch.
     */
    CONNECTING,

    /**
     * Getting apps from the selected watch.
     */
    LOADING_APPS,

    /**
     * Received all apps from the watch, and is ready to perform actions.
     */
    READY,

    /**
     * Indicates there was an issue with App Manager.
     */
    ERROR
}
