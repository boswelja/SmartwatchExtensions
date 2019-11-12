package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "watches")
data class Watch(
        @PrimaryKey val id: String,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "has_app") val hasApp: Boolean = false,
        @Ignore val intPrefs: HashMap<String, Int>,
        @Ignore val boolPrefs: HashMap<String, Boolean>) {

    constructor(id: String, name: String, hasApp: Boolean) : this(id, name, hasApp, HashMap(), HashMap())
}
