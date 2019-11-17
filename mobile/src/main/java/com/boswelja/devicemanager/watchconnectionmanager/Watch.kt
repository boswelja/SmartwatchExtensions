package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.wearable.Node

@Entity(tableName = "watches")
data class Watch(
        @PrimaryKey val id: String,
        @ColumnInfo(name = "name") val name: String,
        @Ignore val intPrefs: HashMap<String, Int>,
        @Ignore val boolPrefs: HashMap<String, Boolean>) {

    constructor (id: String, name: String) : this(id, name, HashMap(), HashMap())

    constructor(node: Node) : this(node.id, node.displayName)
}
