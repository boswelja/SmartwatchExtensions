package com.boswelja.devicemanager.watchmanager.item

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.connection.Capability
import kotlin.experimental.and

/**
 * A representation of a watch that can be stored in a Room database.
 */
@Entity(tableName = "watches")
data class Watch(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    val platform: String,
    @Ignore var capabilities: Short,
    @Ignore var status: Status
) {
    constructor(id: String, name: String, platform: String) : this(
        id, name, platform, 0, Status.UNKNOWN
    )

    override fun equals(other: Any?): Boolean {
        if (other !is Watch) return super.equals(other)
        return other.id == id &&
            other.name == name &&
            other.platform == platform
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + platform.hashCode()
        return result
    }

    /**
     * Check whether this [Watch] instance has a specified capability.
     * @param capability The [Capability] to check for.
     * @return true if this watch is capable, false otherwise.
     */
    fun hasCapability(capability: Capability): Boolean {
        val id = capability.id
        return (capabilities and id) == id
    }

    enum class Status(
        @StringRes val stringRes: Int,
        val imageVector: ImageVector
    ) {
        UNKNOWN(R.string.watch_status_unknown, Icons.Outlined.ErrorOutline),
        ERROR(R.string.watch_status_error, Icons.Outlined.ErrorOutline),
        MISSING_APP(R.string.watch_status_missing_app, Icons.Outlined.ErrorOutline),
        NOT_REGISTERED(R.string.watch_status_not_registered, Icons.Outlined.ErrorOutline),
        DISCONNECTED(R.string.watch_status_disconnected, Icons.Outlined.CloudOff),
        CONNECTED(R.string.watch_status_connected, Icons.Outlined.CheckCircleOutline)
    }
}
