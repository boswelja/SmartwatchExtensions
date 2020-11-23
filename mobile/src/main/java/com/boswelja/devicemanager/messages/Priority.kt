package com.boswelja.devicemanager.messages

/**
 * Defines a [Message] priority.
 * [HIGH] will show the message in-app as well as push a notification.
 * [LOW] will only show the message in-app.
 */
enum class Priority {
    HIGH,
    LOW
}
