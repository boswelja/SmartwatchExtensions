package com.boswelja.smartwatchextensions.core.watches.status

/**
 * Defines possible connection modes for a device.
 */
enum class ConnectionMode {

    /**
     * Indicates the device is disconnected. A disconnected device may or may not be nearby, and
     * cannot receive data.
     */
    Disconnected,

    /**
     * Indicates the device is connected via the internet. An internet-connected device may or may
     * not be nearby, and can receive data.
     */
    Internet,

    /**
     * Indicates the device is connected via bluetooth. A bluetooth-connected device is considered
     * nearby, and can receive data.
     */
    Bluetooth
}
