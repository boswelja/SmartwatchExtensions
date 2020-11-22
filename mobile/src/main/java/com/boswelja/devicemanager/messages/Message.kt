package com.boswelja.devicemanager.messages

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @DrawableRes val iconRes: Int,
    val title: String,
    val text: String,
    val buttonLabel: String = "",
    val action: Action? = null,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    /** Indicates whether the message has an action. */
    @Ignore
    val hasAction: Boolean = action != null

    /** Possible actions for a given [Message] action button. */
    enum class Action {
        LAUNCH_NOTIFICATION_SETTINGS,
        LAUNCH_PLAY_STORE,
        LAUNCH_CHANGELOG
    }
}
