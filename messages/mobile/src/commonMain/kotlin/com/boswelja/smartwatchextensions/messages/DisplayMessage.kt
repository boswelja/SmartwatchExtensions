package com.boswelja.smartwatchextensions.messages

/**
 * A data class containing all the data needed to display a Message.
 * @param id The message ID.
 * @param sourceUid The device UID that sent this message, or null for the local device.
 * @param icon The [Message.Icon] to display.
 * @param title The message title.
 * @param text The message text.
 * @param action The [Message.Action] to display.
 * @param timestamp The timestamp in milliseconds the message was posted at.
 */
data class DisplayMessage(
    val id: Long,
    val sourceUid: String?,
    val icon: Message.Icon,
    val title: String,
    val text: String,
    val action: Message.Action,
    val timestamp: Long
)
