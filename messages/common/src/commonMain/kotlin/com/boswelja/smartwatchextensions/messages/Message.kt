package com.boswelja.smartwatchextensions.messages

import kotlinx.serialization.Serializable

/**
 * Defines a message to display to the user.
 * @param icon The message icon.
 * @param title The message title. This should give a summary of the message's purpose.
 * @param text The message text. This should explain the message's purpose in more details.
 * @param action The message action.
 * @param timestamp The message timestamp, measured in milliseconds since UNIX epoch.
 */
@Serializable
data class Message(
    val icon: Icon,
    val title: String,
    val text: String,
    val action: Action,
    val timestamp: Long
) {
    /**
     * Defines possible icon types for a message
     */
    enum class Icon {
        ERROR,
        UPDATE,
        HELP
    }

    /**
     * Defines possible actions for a message.
     */
    enum class Action {
        NONE,

        /**
         * An Action to launch notification settings.
         */
        NOTIFICATION_SETTINGS,

        /**
         * An action to launch the app changelog.
         */
        CHANGELOG,

        /**
         * An action to install an app update.
         */
        INSTALL_UPDATE
    }
}
