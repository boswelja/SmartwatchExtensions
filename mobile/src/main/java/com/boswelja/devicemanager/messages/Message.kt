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
        LAUNCH_CHANGELOG(R.string.message_action_changelog),
        INSTALL_UPDATE(R.string.message_action_install_update)
    }

    /** Possible icons for a given [Message] **/
    enum class Icon(@DrawableRes val iconRes: Int) {
        UPDATE(R.drawable.message_ic_update),
        HELP(R.drawable.message_ic_help),
        ERROR(R.drawable.message_ic_error)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Message) return false

        if (icon != other.icon) return false
        if (title != other.title) return false
        if (text != other.text) return false
        if (action != other.action) return false
        if (id != other.id) return false
        if (deleted != other.deleted) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = icon.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + (action?.hashCode() ?: 0)
        result = 31 * result + id.hashCode()
        result = 31 * result + deleted.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
