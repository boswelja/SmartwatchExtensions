/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.item

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.boswelja.devicemanager.watchmanager.communication.WatchStatus
import com.google.android.gms.wearable.Node

@Entity(tableName = "watches")
data class Watch(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @Ignore var status: WatchStatus
) {
    constructor(id: String, name: String) : this(
        id, name, WatchStatus.UNKNOWN
    )

    constructor(node: Node, status: WatchStatus) : this(node.id, node.displayName, status)

    constructor(node: Node) : this(node.id, node.displayName, WatchStatus.UNKNOWN)
}
