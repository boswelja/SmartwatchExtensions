/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.wearable.Node
import kotlin.random.Random

@Entity(tableName = "watches")
data class Watch(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "battery_sync_job_id") val batterySyncJobId: Int,
    @Ignore val hasApp: Boolean,
    @Ignore val connected: Boolean,
    @Ignore val intPrefs: HashMap<String, Int>,
    @Ignore val boolPrefs: HashMap<String, Boolean>
) {
    constructor (id: String, name: String, batterySyncJobId: Int, hasApp: Boolean, connected: Boolean) : this(id, name, batterySyncJobId, hasApp, connected, HashMap(), HashMap())

    constructor (id: String, name: String, batterySyncJobId: Int, hasApp: Boolean) : this(id, name, batterySyncJobId, hasApp, HashMap(), HashMap())

    constructor (id: String, name: String, batterySyncJobId: Int) : this(id, name, batterySyncJobId, false, HashMap(), HashMap())

    constructor(node: Node, hasApp: Boolean, connected: Boolean) : this(node.id, node.displayName, Random.nextInt(100000, 999999), hasApp)

    constructor(node: Node, hasApp: Boolean) : this(node.id, node.displayName, Random.nextInt(100000, 999999), hasApp)

    constructor(node: Node) : this(node.id, node.displayName, Random.nextInt(100000, 999999))
}
