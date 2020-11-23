package com.boswelja.devicemanager.messages

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.boswelja.devicemanager.R

@Entity(tableName = "messages")
data class Message(
    val icon: Icon,
    val title: String,
    val text: String,
    val action: Action? = null,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {

    /** Possible actions for a given [Message] action button. */
    enum class Action(@StringRes val labelRes: Int) {
        LAUNCH_NOTIFICATION_SETTINGS(R.string.message_action_noti_settings),
        LAUNCH_PLAY_STORE(R.string.message_action_play_store),
        LAUNCH_CHANGELOG(R.string.message_action_changelog),
        INSTALL_UPDATE(R.string.message_action_install_update)
    }

    /** Possible icons for a given [Message] **/
    enum class Icon(@DrawableRes val iconRes: Int) {
        UPDATE(R.drawable.noti_ic_update),
        HELP(R.drawable.noti_ic_help),
        ERROR(R.drawable.noti_ic_error)
    }
}
