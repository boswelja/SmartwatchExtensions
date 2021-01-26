/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.item

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.boswelja.devicemanager.R
import com.google.android.gms.wearable.Node

@Entity(tableName = "watches")
data class Watch(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    val platform: Platform,
    @Ignore var status: Status
) {
    constructor(id: String, name: String, platform: Platform) : this(
        id, name, platform, Status.UNKNOWN
    )

    constructor(node: Node, status: Status) : this(
        node.id,
        node.displayName,
        Platform.WEAR_OS,
        status
    )

    constructor(node: Node) : this(node.id, node.displayName, Platform.WEAR_OS, Status.UNKNOWN)

    enum class Status(
        @StringRes val stringRes: Int,
        @DrawableRes val iconRes: Int
    ) {
        UNKNOWN(R.string.watch_status_unknown, R.drawable.ic_error),
        ERROR(R.string.watch_status_error, R.drawable.ic_error),
        MISSING_APP(R.string.watch_status_missing_app, R.drawable.ic_error),
        NOT_REGISTERED(R.string.watch_status_not_registered, R.drawable.ic_error),
        DISCONNECTED(R.string.watch_status_disconnected, R.drawable.ic_disconnected),
        CONNECTED(R.string.watch_status_connected, R.drawable.ic_connected)
    }

    enum class Platform {
        WEAR_OS
    }
}
